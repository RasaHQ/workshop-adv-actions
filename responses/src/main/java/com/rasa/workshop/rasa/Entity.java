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

import io.vertx.core.json.JsonObject;

public class Entity {

  private final JsonObject mJson;

  public Entity(JsonObject pJson) {
    this.mJson = pJson;
  }

  public Integer start() {
    return mJson.getInteger("start", null);
  }

  public Integer end() {
    return mJson.getInteger("end", null);
  }

  public Object value() {
    return mJson.getValue("value", null);
  }

  public String entity() {
    return mJson.getString("entity", null);
  }

  public Double confidence() {
    return mJson.getDouble("confidence", null);
  }

}
