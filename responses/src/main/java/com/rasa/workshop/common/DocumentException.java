package com.rasa.workshop.common;

public class DocumentException
    extends Exception {

  public DocumentException(String pMsg) {
    super(pMsg);
  }

  public DocumentException(String pMsg, Throwable pCause) {
    super(pMsg, pCause);
  }
}

