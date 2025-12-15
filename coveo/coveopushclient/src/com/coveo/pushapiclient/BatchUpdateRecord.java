package com.coveo.pushapiclient;

import com.google.gson.JsonObject;

import java.util.Arrays;

/**
 * @see <a href="https://docs.coveo.com/en/75/#batchdocumentbody">BatchDocumentBody</a>
 */
public class BatchUpdateRecord {

  private final JsonObject[] addOrUpdate;

  public BatchUpdateRecord(JsonObject[] addOrUpdate) {
    this.addOrUpdate = addOrUpdate;
  }

  public JsonObject[] getAddOrUpdate() {
    return addOrUpdate;
  }

  @Override
  public String toString() {
    return "BatchUpdateRecord["
        + "addOrUpdate="
        + Arrays.toString(addOrUpdate)
        + ']';
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    BatchUpdateRecord that = (BatchUpdateRecord) obj;
    return Arrays.equals(addOrUpdate, that.addOrUpdate);
  }

  @Override
  public int hashCode() {
    return 31 * Arrays.hashCode(addOrUpdate);
  }
}
