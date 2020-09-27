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

package com.rasa.workshop.service;

import com.rasa.workshop.common.Collection;
import com.rasa.workshop.common.Document;
import com.rasa.workshop.common.DocumentException;
import com.rasa.workshop.common.DocumentExistsException;
import com.rasa.workshop.common.DocumentNotFoundException;
import com.rasa.workshop.db.DB;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.stream.Collectors;

public class ResponsesService {

  public static final String RESPONSES_COLLECTION_ID = "responses";
  public static final String DEFAULT_BOT_ID = "workshop";

  private final DB mDB;

  public ResponsesService(DB pDB)
    throws DocumentExistsException, DocumentException {
    this.mDB = Objects.requireNonNull(pDB, "DB should be provided");

    if (!mDB.hasCollection(RESPONSES_COLLECTION_ID)) {
      mDB.createCollection(Collection
        .newBuilder()
        .withId(RESPONSES_COLLECTION_ID)
        .build());
    }

    if (!mDB.hasDocument(RESPONSES_COLLECTION_ID, DEFAULT_BOT_ID)) {
      mDB.createDocument(Document
        .newBuilder()
        .underCollection(RESPONSES_COLLECTION_ID)
        .withId(DEFAULT_BOT_ID)
        .withPayload(rawResponsesJson())
        .build());
    }
  }

  public Document getResponse(String pBotId)
    throws DocumentNotFoundException, DocumentException {
    return mDB.getDocument(RESPONSES_COLLECTION_ID, pBotId);
  }

  public Document getBotBuilderResponse()
    throws DocumentNotFoundException, DocumentException {
    return mDB.getDocument(RESPONSES_COLLECTION_ID, DEFAULT_BOT_ID);
  }

  public JsonObject generateResponse(String pBotId, JsonObject pPayload)
    throws DocumentNotFoundException, DocumentException {
    // TODO: Get tracker and use slots for variable replacement.
    String template = pPayload.getString("template");
    var doc = mDB.getDocument(RESPONSES_COLLECTION_ID, pBotId);
    JsonArray response = doc.payload().getJsonArray(template);
    return response.getJsonObject(0);
  }

  private static JsonObject rawResponsesJson() {
    String rawJson = new BufferedReader(
      new InputStreamReader(
        ResponsesService.class.getResourceAsStream("workshop-responses.json")
      )
    ).lines().collect(Collectors.joining("\n"));

    return new JsonObject(rawJson);
  }
}

