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

public class ActiveForm {

  private final JsonObject mJson;

  private IncomingMessage mMessage;

  public ActiveForm(JsonObject pJson) {
    this.mJson = pJson;
  }

  public String name() {
    return mJson.getString("name", "");
  }

  public Boolean validate() {
    return mJson.getBoolean("validate", true);
  }

  public Boolean rejected() {
    return mJson.getBoolean("rejected");
  }

  public IncomingMessage message() {
    if (mMessage == null) {
      mMessage = new IncomingMessage(mJson.getJsonObject("trigger_message"));
    }

    return mMessage;
  }

  public boolean hasName() {
    return name().length() > 0;
  }

  public JsonObject toJson() {
    return mJson;
  }
}

