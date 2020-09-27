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

package com.rasa.workshop.common;

import io.vertx.core.json.JsonObject;

class JsonDocument
    implements Document {

  private final String mId;
  private final String mCollectionId;
  private final JsonObject mPayload;

  JsonDocument(Builder pBuilder) {
    mId = pBuilder.mId;
    mCollectionId = pBuilder.mCollectionId;
    mPayload = pBuilder.mPayload;
  }

  @Override
  public String id() {
    return mId;
  }

  @Override
  public String collectionId() {
    return mCollectionId;
  }

  @Override
  public JsonObject payload() {
    return mPayload;
  }
}
