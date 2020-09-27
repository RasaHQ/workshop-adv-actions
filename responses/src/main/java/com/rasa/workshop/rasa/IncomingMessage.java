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
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class IncomingMessage {

  private final JsonObject mJson;

  private List<Entity> mEntities;
  private Intent mIntent;

  public IncomingMessage(JsonObject pJson) {
    this.mJson = pJson;
  }

  public String text() {
    return mJson.getString("text");
  }

  public List<Entity> entities() {
    if (mEntities == null) {
      mEntities = new ArrayList<>();
      mJson.getJsonArray("entities", Utils.EMPTY_JSON_ARRAY).forEach(o -> mEntities.add(new Entity((JsonObject) o)));
      mEntities = Collections.unmodifiableList(mEntities);
    }

    return mEntities;
  }

  public Intent intent() {
    if (mIntent == null) {
      mIntent = new Intent(mJson.getJsonObject("intent", Utils.EMPTY_JSON));
    }

    return mIntent;
  }
}
