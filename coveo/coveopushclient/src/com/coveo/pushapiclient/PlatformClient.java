package com.coveo.pushapiclient;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Stream;

/** PlatformClient handles network requests to the Coveo platform */
public class PlatformClient {
  private final String apiKey;
  private final String organizationId;
  private final ApiCore api;
  private final PlatformUrl platformUrl;
  private String[] userAgents;

  /**
   * Construct a PlatformClient
   *
   * @param apiKey An apiKey capable of pushing documents and managing sources in a Coveo
   *     organization.
   * @see <a href="https://docs.coveo.com/en/1718">Manage API Keys</a>
   * @param organizationId The Coveo Organization identifier.
   * @param platformUrl The PlatformUrl.
   * @param api The configuration options for exponential backoff.
   */
  public PlatformClient(
      String apiKey, String organizationId, PlatformUrl platformUrl, ApiCore api) {
    this.apiKey = apiKey;
    this.organizationId = organizationId;
    this.api = api;
    this.platformUrl = platformUrl;
  }

  /**
   * Create a new source
   *
   * @param name The name of the source to create
   * @param sourceType The type of the source to create
   * @param sourceVisibility The security option that should be applied to the content of the
   *     source.
   * @see <a href="https://docs.coveo.com/en/1779">Content Security</a>
   * @return
   * @throws IOException
   * @throws InterruptedException
   */
  public HttpResponse<String> createSource(
      String name, final SourceType sourceType, SourceVisibility sourceVisibility)
      throws IOException, InterruptedException {
    String[] headers =
        this.getHeaders(this.getAuthorizationHeader(), this.getContentTypeApplicationJSONHeader());

    String json =
        this.toJSON(
            new HashMap<>() {
              {
                put("sourceType", sourceType.toString());
                put("pushEnabled", sourceType.isPushEnabled());
                put("streamEnabled", sourceType.isStreamEnabled());
                put("name", name);
                put("sourceVisibility", sourceVisibility);
              }
            });

    URI uri = URI.create(this.getBaseSourceURL());

    return this.api.post(uri, headers, HttpRequest.BodyPublishers.ofString(json));
  }

  public HttpResponse<String> openStream(String sourceId) throws IOException, InterruptedException {
    String[] headers =
        this.getHeaders(this.getAuthorizationHeader(), this.getContentTypeApplicationJSONHeader());

    URI uri =
        URI.create(this.getBasePushURL() + String.format("/sources/%s/stream/open", sourceId));

    return this.api.post(uri, headers);
  }

  public HttpResponse<String> closeStream(String sourceId, String streamId)
      throws IOException, InterruptedException {
    String[] headers =
        this.getHeaders(this.getAuthorizationHeader(), this.getContentTypeApplicationJSONHeader());
    URI uri =
        URI.create(
            this.getBasePushURL()
                + String.format("/sources/%s/stream/%s/close", sourceId, streamId));

    return this.api.post(uri, headers);
  }

  public HttpResponse<String> requireStreamChunk(String sourceId, String streamId)
      throws IOException, InterruptedException {
    String[] headers =
        this.getHeaders(this.getAuthorizationHeader(), this.getContentTypeApplicationJSONHeader());

    URI uri =
        URI.create(
            this.getBasePushURL()
                + String.format("/sources/%s/stream/%s/chunk", sourceId, streamId));

    return this.api.post(uri, headers);
  }

  /**
   * Create a file container.
   *
   * @see <a href="https://docs.coveo.com/en/43">Creating a File Container</a>
   * @return
   * @throws IOException
   * @throws InterruptedException
   */
  public HttpResponse<String> createFileContainer() throws IOException, InterruptedException {
    String[] headers =
        this.getHeaders(this.getAuthorizationHeader(), this.getContentTypeApplicationJSONHeader());

    URI uri = URI.create(this.getBasePushURL() + "/files");

    return this.api.post(uri, headers);
  }

  /**
   * Upload content update into a file container.
   *
   * @see <a
   *     href="https://docs.coveo.com/en/90/index-content/manage-batches-of-items-in-a-push-source#step-2-upload-the-content-update-into-the-file-container">Upload
   *     the Content Update into the File Container</a>
   * @param fileContainer
   * @param batchUpdateJson
   * @return
   * @throws IOException
   * @throws InterruptedException
   */
  public HttpResponse<String> uploadContentToFileContainer(
      FileContainer fileContainer, String batchUpdateJson)
      throws IOException, InterruptedException {
    String[] headers =
        fileContainer.requiredHeaders.entrySet().stream()
            .flatMap(entry -> Stream.of(entry.getKey(), entry.getValue()))
            .toArray(String[]::new);

    URI uri = URI.create(fileContainer.uploadUri);

    return this.api.put(uri, headers, HttpRequest.BodyPublishers.ofString(batchUpdateJson));
  }

  /**
   * Push a file container into a stream source. See [Push the File Container into a Stream
   * Source](https://docs.coveo.com/en/l62e0540/coveo-for-commerce/how-to-update-your-catalog#step-3-send-the-file-container-to-update-your-catalog).
   *
   * @param sourceId
   * @param fileContainer
   * @return
   * @throws IOException
   * @throws InterruptedException
   */
  public HttpResponse<String> pushFileContainerContentToStreamSource(
      String sourceId, FileContainer fileContainer) throws IOException, InterruptedException {
    String[] headers =
        this.getHeaders(this.getAuthorizationHeader(), this.getContentTypeApplicationJSONHeader());
    URI uri =
        URI.create(
            this.getBasePushURL()
                + String.format(
                    "/sources/%s/stream/update?fileId=%s", sourceId, fileContainer.fileId));

    return this.api.put(uri, headers, HttpRequest.BodyPublishers.ofString(""));
  }

  private String getBaseSourceURL() {
    return String.format("%s/sources", this.getBasePlatformURL());
  }

  private String getBasePlatformURL() {
    return String.format(
        "%s/rest/organizations/%s", this.platformUrl.getPlatformUrl(), this.organizationId);
  }

  private String getBasePushURL() {
    return String.format(
        "%s/push/v1/organizations/%s", this.platformUrl.getApiUrl(), this.organizationId);
  }

  private String[] getHeaders(String[]... headers) {
    String[] out = new String[] {};
    for (String[] header : headers) {
      out = Stream.concat(Arrays.stream(out), Arrays.stream(header)).toArray(String[]::new);
    }
    return out;
  }

  private String[] getAuthorizationHeader() {
    return new String[] {"Authorization", String.format("Bearer %s", this.apiKey)};
  }

  private String[] getContentTypeApplicationJSONHeader() {
    StringBuilder userAgentValue = new StringBuilder();

    if (userAgents != null && userAgents.length > 0) {
      userAgentValue.append(String.join(" ", userAgents));
    }

    return new String[] {
      "Content-Type",
      "application/json",
      "Accept",
      "application/json",
      "User-Agent",
      userAgentValue.toString()
    };
  }

  private String toJSON(HashMap<String, Object> hashMap) {
    return new Gson().toJson(hashMap, new TypeToken<HashMap<String, Object>>() {}.getType());
  }

  public void setUserAgents(String[] userAgents) {
    if (!validUserAgents(userAgents)) {
      throw new IllegalArgumentException("Invalid user agents");
    }
    this.userAgents = userAgents;
  }

  protected boolean validUserAgents(String[] userAgents) {
    String pattern = "^.+/v(\\d+(\\.\\d+){0,2})$";
    return Arrays.stream(userAgents).allMatch(agent -> agent.matches(pattern));
  }
}
