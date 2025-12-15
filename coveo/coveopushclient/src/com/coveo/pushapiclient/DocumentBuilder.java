package com.coveo.pushapiclient;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.codec.digest.DigestUtils;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/** Utility class to build a {@link Document} */
public class DocumentBuilder {

  public static final ArrayList<String> reservedKeynames =
      new ArrayList<>() {
        {
          add("compressedBinaryData");
          add("compressedBinaryDataFileId");
          add("parentId");
          add("fileExtension");
          add("data");
          add("permissions");
          add("documentId");
          add("orderingId");
        }
      };

  private final Document document;

  /**
   * @param uri the URI of the document.
   * @see {@link Document#uri}
   * @param title the title of the document.
   * @see {@link Document#title}
   */
  public DocumentBuilder(String uri, String title) {
    this.document = new Document();
    this.document.uri = uri;
    this.document.title = title;
  }

  public Document getDocument() {
    return this.document;
  }

  /**
   * Set the clickableURI on the document.
   *
   * @see {@link Document#clickableUri}
   * @param clickableUri
   * @return
   */
  public DocumentBuilder withClickableUri(String clickableUri) {
    this.document.clickableUri = clickableUri;
    return this;
  }

  /**
   * Set metadata on the document.
   *
   * @see {@link Document#metadata}
   * @param metadata
   * @return
   */
  public DocumentBuilder withMetadata(Map<String, Object> metadata) {
    metadata.forEach(this::setMetadataValue);
    return this;
  }

  /**
   * Marshal the document into a JSON string accepted by the push API.
   *
   * @return
   */
  public String marshal() {
    return this.marshalJsonObject().toString();
  }

  /**
   * Marshal the document into a JSON object accepted by the push API.
   *
   * @return
   */
  public JsonObject marshalJsonObject() {
    this.generatePermanentId();

    JsonObject jsonDocument = new Gson().toJsonTree(this.document).getAsJsonObject();
    this.document.metadata.forEach(
        (key, value) -> {
          jsonDocument.add(key, new Gson().toJsonTree(value));
        });
    jsonDocument.remove("metadata");
    jsonDocument.addProperty("documentId", this.document.uri);
    return jsonDocument;
  }

  private void setMetadataValue(String key, Object metadataValue) {
    this.validateReservedMetadataKeyNames(key);
    this.document.metadata.put(key, metadataValue);
  }

  private void validateReservedMetadataKeyNames(String key) {
    if (reservedKeynames.contains(key)) {
      throw new RuntimeException(
          String.format(
              "Cannot use %s as a metadata key: It is a reserved key name. See"
                  + " https://docs.coveo.com/en/78/index-content/push-api-reference#json-document-reserved-key-names",
              key));
    }
  }

  private void generatePermanentId() {
    if (this.document.permanentId == null) {
      String md5 = DigestUtils.md5Hex(this.document.uri);
      String sha1 = DigestUtils.sha1Hex(this.document.uri);
      this.document.permanentId = md5.substring(0, 30) + sha1.substring(0, 30);
    }
  }
}
