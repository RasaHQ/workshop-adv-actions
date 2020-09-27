package com.rasa.workshop.common;

public class DocumentExistsException
    extends Exception {

  public DocumentExistsException(String pMsg) {
    super(pMsg);
  }

  public DocumentExistsException(String pMsg, Throwable pCause) {
    super(pMsg, pCause);
  }
}

