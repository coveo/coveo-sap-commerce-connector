package com.coveo.pushapiclient;

import com.google.gson.JsonObject;

import java.util.List;

public class StreamUpdate extends BatchUpdate {

  private final List<PartialUpdateDocument> partialUpdate;

  public StreamUpdate(
      List<DocumentBuilder> addOrUpdate,
      List<PartialUpdateDocument> partialUpdate) {
    super(addOrUpdate);
    this.partialUpdate = partialUpdate;
  }

  @Override
  public StreamUpdateRecord marshal() {
    return new StreamUpdateRecord(
        this.getAddOrUpdate().stream()
            .map(DocumentBuilder::marshalJsonObject)
            .toArray(JsonObject[]::new),
        this.partialUpdate.stream()
            .map(PartialUpdateDocument::marshalJsonObject)
            .toArray(JsonObject[]::new));
  }

  public List<PartialUpdateDocument> getPartialUpdate() {
    return partialUpdate;
  }

  @Override
  public String toString() {
    return "StreamUpdate["
        + "addOrUpdate="
        + getAddOrUpdate()
        + ", partialUpdate="
        + partialUpdate
        + ']';
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    StreamUpdate that = (StreamUpdate) obj;
    return getAddOrUpdate().equals(that.getAddOrUpdate())
        && partialUpdate.equals(that.partialUpdate);
  }

  @Override
  public int hashCode() {
    return super.hashCode() + partialUpdate.hashCode();
  }
}
