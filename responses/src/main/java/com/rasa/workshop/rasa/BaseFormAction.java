/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rasa.workshop.rasa;

import com.rasa.workshop.common.DocumentException;
import com.rasa.workshop.common.DocumentExistsException;
import com.rasa.workshop.common.DocumentNotFoundException;
import com.rasa.workshop.db.DB;
import com.rasa.workshop.rasa.event.Event;
import com.rasa.workshop.rasa.event.FormEvent;
import com.rasa.workshop.rasa.event.SlotSetEvent;
import com.rasa.workshop.service.ResponsesService;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseFormAction
  implements FormAction {

  static final String REQUESTED_SLOT = "requested_slot";

  private final String mName;
  private final Logger mLogger;

  protected final ResponsesService mService;

  public BaseFormAction(String pName, Logger pLogger, DB pDB)
    throws Exception {
    this.mName = pName;
    this.mLogger = pLogger;
    this.mService = new ResponsesService(pDB);
  }

  @Override
  public String name() {
    return mName;
  }

  @Override
  public ActionResult run(ActionRequest pRequest)
    throws ActionExecutionRejectionException, DocumentException, DocumentNotFoundException, DocumentExistsException {

    var document = mService.getBotBuilderResponse();
    ActionResult result = new ActionResult(document.payload());

    // 1) Optionally, activate the form and populate slots from tracker.
    List<Event> events = tryActivate(pRequest);

    // 2) Optionally, validate the form slots.
    events.addAll(tryValidate(pRequest));

    // 3) Get the next slot to fill, and ask the user.
    Tracker updatedTracker = updateTracker(pRequest.tracker(), events);
    SlotSetEvent nextSlotEvent = tryFillNextSlot(updatedTracker, result);
    if (nextSlotEvent != null) {
      events.add(nextSlotEvent);

    } else {
      List<Event> submitEvents = submit(updatedTracker, result);
      if (submitEvents.size() > 0) {
        events.addAll(submitEvents);
      }

      events.addAll(deactivate());
    }

    result.setEvents(events);
    return result;
  }

  private List<Event> tryActivate(ActionRequest pRequest) {
    if (pRequest.tracker().hasActiveForm() && name().equals(pRequest.tracker().activeForm().name())) {
      mLogger.debug("{0} form is already active", name());
      return new ArrayList<>();

    } else {
      List<Event> events = new ArrayList<>();
      events.add(new FormEvent(name()));

      mLogger.debug("{0} form has been activated", name());

      Map<String, Object> preFilledSlots = new HashMap<>();
      List<String> requiredSlots = requiredSlots(pRequest.tracker());

      if (requiredSlots.size() > 0) {
        requiredSlots.forEach(slotName -> {
          var slotValue = pRequest.tracker().slotValue(slotName);
          if (slotValue != null) {
            preFilledSlots.put(slotName, slotValue);
          }
        });
      }

      if (preFilledSlots.size() > 0) {
        events.addAll(validateSlots(preFilledSlots, pRequest));
      }

      return events;
    }
  }

  List<Event> tryValidate(ActionRequest pRequest)
    throws ActionExecutionRejectionException {
    Tracker tracker = pRequest.tracker();

    if (!"action_listen".equals(tracker.latestActionName())) return new ArrayList<>();
    if (!tracker.hasActiveForm() || !tracker.activeForm().validate()) return new ArrayList<>();

    Map<String, Object> slots = extractOtherSlots(pRequest);
    Object requestedSlot = tracker.slotValue(REQUESTED_SLOT);

    if (requestedSlot != null) {
      slots.putAll(extractRequestedSlot(pRequest));

      if (slots.isEmpty()) {
        throw new ActionExecutionRejectionException(
          String.format("Failed to extract slot '%s' in action '%s'", requestedSlot, name()));
      }
    }

    return validateSlots(slots, pRequest);
  }

  private List<Event> deactivate() {
    List<Event> deactivateEvents = new ArrayList<>();
    deactivateEvents.add(new FormEvent(null));
    deactivateEvents.add(new SlotSetEvent(REQUESTED_SLOT, null));
    return deactivateEvents;
  }

  private SlotSetEvent tryFillNextSlot(Tracker pTracker, ActionResult pResult) {
    List<String> requiredSlots = requiredSlots(pTracker);
    if (requiredSlots == null) return null;

    for (String slotName : requiredSlots) {
      Object slotValue = pTracker.slotValue(slotName);
      if (slotValue == null) {
        mLogger.debug("Requesting '{0}' slot", slotName);
        pResult.addTemplateMessage("utter_ask_" + slotName);
        return new SlotSetEvent(REQUESTED_SLOT, slotName);
      }
    }

    return null;
  }

  private Map<String, Object> extractOtherSlots(ActionRequest pRequest) {
    Tracker tracker = pRequest.tracker();
    List<String> requiredSlots = requiredSlots(pRequest.tracker());

    if (requiredSlots == null || requiredSlots.isEmpty()) return Map.of();

    Object requestedSlot = tracker.slotValue(REQUESTED_SLOT);
    Map<String, Object> slots = new HashMap<>();

    for (String slotName : requiredSlots) {
      if (slotName.equals(requestedSlot)) continue;

      List<SlotExtractor> extractors = slotExtractorsMap().get(slotName);
      for (SlotExtractor extractor : extractors) {
        boolean shouldFillEntitySlot = extractor.type() == SlotExtractor.Type.Entity &&
          slotName.equals(extractor.entity()) &&
          shouldExtractFromIntent(extractor, tracker);

        boolean shouldFillTriggerSlot = (!tracker.hasActiveForm() || !name().equals(tracker.activeForm().name())) &&
          extractor.type() == SlotExtractor.Type.TriggerIntent &&
          shouldExtractFromIntent(extractor, tracker);

        Object slotValue = null;
        if (shouldFillEntitySlot) {
          slotValue = tracker.entityValue(slotName);

        } else if (shouldFillTriggerSlot) {
          slotValue = extractor.intentValue();
        }

        if (slotValue != null) {
          mLogger.debug("{0} slot value extracted: {1}", slotName, slotValue);
          slots.put(slotName, slotValue);
          return slots;
        }
      }
    }

    return slots;
  }

  private Map<String, Object> extractRequestedSlot(ActionRequest pRequest) {
    Tracker tracker = pRequest.tracker();
    Object requestedSlot = tracker.slotValue(REQUESTED_SLOT);
    if (requestedSlot == null) {
      return Map.of();
    }

    String slotName = (String) requestedSlot;
    mLogger.debug("Extracting value for {0} slot", slotName);
    List<SlotExtractor> extractors = slotExtractorsMap().get(slotName);

    for (SlotExtractor extractor : extractors) {
      if (!shouldExtractFromIntent(extractor, tracker)) continue;

      Object slotValue;

      switch(extractor.type()) {
        case Entity:
          slotValue = tracker.entityValue(extractor.entity());
          break;

        case Intent:
          slotValue = extractor.intentValue();
          break;

        case Text:
          slotValue = tracker.latestMessage().text();
          break;

        default:
          throw new UnsupportedOperationException(
            String.format("Provided slot extractor type '%s' is not supported", extractor.type()));
      }

      if (slotValue != null) {
        mLogger.debug("Extracted value {0} for {1} slot", slotValue, slotName);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put(slotName, slotValue);
        return resultMap;
      }
    }

    return Map.of();
  }

  private List<Event> validateSlots(Map<String, Object> pSlots, ActionRequest pRequest) {
    List<Event> validatedEvents = new ArrayList<>();
    // TODO: validate slots.
    pSlots.forEach((key, value) -> validatedEvents.add(new SlotSetEvent(key, value)));
    return validatedEvents;
  }

  private boolean shouldExtractFromIntent(SlotExtractor pExtractor, Tracker pTracker) {
    String intent = pTracker.latestMessage().intent().name();
    boolean intentNotBlacklisted = pExtractor.intents().isEmpty() && !pExtractor.notIntents().contains(intent);
    return intentNotBlacklisted || pExtractor.intents().contains(intent);
  }

  private Tracker updateTracker(Tracker pTracker, List<Event> pEvents) {
    Tracker updatedTracker = new Tracker(pTracker.toJson().copy());
    JsonObject slots = updatedTracker.slots();
    if (slots == null) {
      slots = new JsonObject();
      updatedTracker.toJson().put("slots", slots);
    }

    for (Event event : pEvents) {
      if (event instanceof SlotSetEvent) {
        var slotSetEvent = ((SlotSetEvent) event);
        slots.put(slotSetEvent.name(), slotSetEvent.value());
      }
    }

    return updatedTracker;
  }
}
