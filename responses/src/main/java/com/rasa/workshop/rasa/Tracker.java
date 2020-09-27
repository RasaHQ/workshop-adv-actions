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

import com.rasa.workshop.common.Utils;
import com.rasa.workshop.rasa.event.Event;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class Tracker {

  private final JsonObject mJson;

  private IncomingMessage mLatestMessage;

  private ActiveForm mActiveForm;

  public Tracker(JsonObject pJson) {
    this.mJson = pJson;
  }

  public String conversationId() {
    return mJson.getString("conversation_id");
  }

  public String senderId() {
    return mJson.getString("sender_id");
  }

  public String latestInputChannel() {
    return mJson.getString("latest_input_channel");
  }

  public String latestActionName() {
    return mJson.getString("latest_action_name");
  }

  private String followupAction() {
    return mJson.getString("followup_action");
  }

  public Boolean paused() {
    return mJson.getBoolean("paused");
  }

  public JsonObject slots() {
    return mJson.getJsonObject("slots", null);
  }

  public JsonArray events() {
    return mJson.getJsonArray("events", Utils.EMPTY_JSON_ARRAY);
  }

  public IncomingMessage latestMessage() {
    if (mLatestMessage == null) {
      mLatestMessage = new IncomingMessage(mJson.getJsonObject("latest_message", Utils.EMPTY_JSON));
    }

    return mLatestMessage;
  }

  public ActiveForm activeForm() {
    if (mActiveForm == null) {
      mActiveForm = new ActiveForm(mJson.getJsonObject("active_form", Utils.EMPTY_JSON));
    }

    return mActiveForm;
  }

  public boolean hasActiveForm() {
    return activeForm().hasName();
  }

  public Object slotValue(String pSlotName) {
    return slots().getValue(pSlotName, null);
  }

  public Object entityValue(String pEntityName) {
    List<Entity> entities = latestMessage().entities();
    for (Entity entity : entities) {
      if (pEntityName.equals(entity.entity())) {
        return entity.value();
      }
    }

    return null;
  }

  public JsonObject metadata() {
    JsonArray events = events();
    List<Event> userEvents = new ArrayList<>();

    for (int e = 0; e < events.size(); e++) {
      Event event = new Event(events.getJsonObject(e));
      if ("user".equals(event.eventName())) {
        userEvents.add(event);
      }
    }

    if (userEvents.isEmpty()) return Utils.EMPTY_JSON;
    return userEvents.get(userEvents.size() - 1).toJson().getJsonObject("metadata", Utils.EMPTY_JSON);
  }

  public JsonObject toJson() {
    return mJson;
  }
}
