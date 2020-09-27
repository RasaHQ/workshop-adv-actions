package com.rasa.workshop.rasa;

import java.util.List;

public class SlotExtractor {

  public enum Type {
    Entity,
    Intent,
    Text,
    TriggerIntent
  }

  private Type mType;
  private String mEntity;
  private List<String> mIntents;
  private List<String> mNotIntents;
  private Object mIntentValue;

  public SlotExtractor(String pEntity) {
    this(Type.Entity);
    this.mEntity = pEntity;
  }

  public SlotExtractor(Type pType) {
    this.mType = pType;
    this.mIntents = List.of();
    this.mNotIntents = List.of();
  }

  public Type type() {
    return mType;
  }

  public String entity() {
    return mEntity;
  }

  public List<String> intents() {
    return mIntents;
  }

  public List<String> notIntents() {
    return mNotIntents;
  }

  public Object intentValue() {
    return mIntentValue;
  }
}
