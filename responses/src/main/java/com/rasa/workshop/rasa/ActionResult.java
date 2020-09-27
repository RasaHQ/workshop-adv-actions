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

import com.rasa.workshop.rasa.event.Event;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

public class ActionResult {

  private final JsonObject mResponses;
  private JsonObject mJson;

  public ActionResult(JsonObject pResponses) {
    mResponses = pResponses;

    mJson = new JsonObject();
    mJson.put("responses", new JsonArray());
    mJson.put("events", new JsonArray());
  }

  public ActionResult setEvents(List<Event> pEvents) {
    JsonArray jsonArray = mJson.getJsonArray("events");
    pEvents.forEach(event -> jsonArray.add(event.toJson()));
    return this;
  }

  public ActionResult addTemplateMessage(String pTemplate) {
    JsonArray response = mResponses.getJsonArray(pTemplate);
    mJson.getJsonArray("responses").add(response.getJsonObject(0));
    return this;
  }

  public JsonObject toJson() {
    return mJson;
  }
}
