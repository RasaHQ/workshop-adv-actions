package com.rasa.workshop.common;

import io.vertx.core.json.JsonObject;

import java.util.Collections;
import java.util.List;

public class JsonCollection
    implements Collection {

  private final String mId;
  private final JsonObject mPayload;
  private final List<Document> mDocuments;

  JsonCollection(Builder pBuilder) {
    mId = pBuilder.mId;
    mPayload = pBuilder.mPayload;
    mDocuments = pBuilder.mDocuments != null ? Collections.unmodifiableList(pBuilder.mDocuments) : List.of();
  }

  @Override
  public String id() {
    return mId;
  }

  @Override
  public String collectionId() {
    return mId;
  }

  @Override
  public JsonObject payload() {
    return mPayload;
  }

  @Override
  public List<Document> documents() {
    return mDocuments;
  }
}

