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

import java.util.ArrayList;
import java.util.List;

public interface Collection
    extends Document {

  List<Document> documents();

  static Builder newBuilder() {
    return new Builder();
  }

  class Builder {
    String mId;
    JsonObject mPayload;
    List<Document> mDocuments;

    private Builder() {
      mDocuments = new ArrayList<>();
    }

    public Builder withId(String pId) {
      mId = pId;
      return this;
    }

    public Builder withPayload(JsonObject pPayload) {
      mPayload = pPayload;
      return this;
    }

    public Builder addDocument(Document pDocument) {
      mDocuments.add(pDocument);
      return this;
    }

    public Collection build() {
      return new JsonCollection(this);
    }
  }
}
