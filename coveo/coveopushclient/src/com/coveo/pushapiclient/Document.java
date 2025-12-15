package com.coveo.pushapiclient;

import java.util.HashMap;

public class Document {
  /**
   * The metadata key-value pairs for a given document.
   *
   * <p>Each metadata in the document must be unique.
   *
   * <p>Metadata are case-insensitive (e.g., the Push API considers mykey, MyKey, myKey, MYKEY, etc.
   * as identical).
   *
   * <p>See https://docs.coveo.com/en/115 for more information.
   */
  public final HashMap<String, Object> metadata;

  /**
   * The Uniform Resource Identifier (URI) that uniquely identifies the document in a Coveo index.
   *
   * <p>Examples: - `http://www.example.com/` - `file://folder/text.txt`
   */
  public String uri;

  /** The title of the document. */
  public String title;

  /** The clickable URI associated with the document. */
  public String clickableUri;

  /**
   * The permanent identifier of a document that does not change over time.
   *
   * <p>Optional, will be derived from the document URI.
   */
  public String permanentId;

  /**
   * The textual (non-binary) content of the item.
   *
   * <p>Whenever you're pushing a compressed binary item (such as XML/HTML, PDF, Word, or binary),
   * you should use the CompressedBinaryData or CompressedBinaryDataFileId attribute instead,
   * depending on the content size.
   *
   * <p>Accepts 5 MB or less of uncompressed textual data.
   *
   * <p>See https://docs.coveo.com/en/73 for more information.
   *
   * <p>Example: `This is a simple string that will be used for searchability as well as to generate
   * excerpt and summaries for the document.`
   */
  public String data;

  public Document() {
    this.metadata = new HashMap<String, Object>();
  }
}
