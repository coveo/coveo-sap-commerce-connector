package com.coveo.pushapiclient;

public enum SourceType implements SourceTypeInterface {
  CATALOG {
    public String toString() {
      return "CATALOG";
    }

    @Override
    public boolean isPushEnabled() {
      return true;
    }

    @Override
    public boolean isStreamEnabled() {
      return true;
    }
  },
}

interface SourceTypeInterface {

  String toString();

  boolean isPushEnabled();

  boolean isStreamEnabled();
}
