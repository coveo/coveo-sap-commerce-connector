package com.coveo.pushapiclient;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.Objects;

/**
 * @see <a href="https://docs.coveo.com/en/90">Manage Batches of Items in a Push Source</a>
 */
public class BatchUpdate {

  private final List<DocumentBuilder> addOrUpdate;

  public BatchUpdate(List<DocumentBuilder> addOrUpdate) {
    this.addOrUpdate = addOrUpdate;
  }

  public BatchUpdateRecord marshal() {
    return new BatchUpdateRecord(
        this.addOrUpdate.stream()
            .map(DocumentBuilder::marshalJsonObject)
            .toArray(JsonObject[]::new));
  }

  public List<DocumentBuilder> getAddOrUpdate() {
    return addOrUpdate;
  }

  @Override
  public String toString() {
    return "BatchUpdate[" + "addOrUpdate=" + addOrUpdate + ']';
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    BatchUpdate that = (BatchUpdate) obj;
    return addOrUpdate.equals(that.addOrUpdate);
  }

  @Override
  public int hashCode() {
    return Objects.hash(addOrUpdate);
  }
}
