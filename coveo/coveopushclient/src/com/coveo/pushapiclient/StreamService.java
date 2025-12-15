package com.coveo.pushapiclient;

import com.coveo.pushapiclient.exceptions.NoOpenStreamException;
import com.google.gson.Gson;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.http.HttpResponse;

import static com.coveo.pushapiclient.BackoffOptionsBuilder.DEFAULT_MAX_RETRIES;
import static com.coveo.pushapiclient.BackoffOptionsBuilder.DEFAULT_RETRY_AFTER;

public class StreamService {
  private static final Logger LOG = Logger.getLogger(StreamService.class);
  private StreamEnabledSource source;
  private PlatformClient platformClient;
  private StreamServiceInternal service;
  private String streamId;
  private DocumentUploadQueue queue;

  private final ConfigurationService configurationService;

  public StreamService(ConfigurationService configurationService) {
    super();
    this.configurationService = configurationService;
  }

  public void init(StreamEnabledSource source, String[] userAgents) {
    String apiKey = source.getApiKey();
    String organizationId = source.getOrganizationId();
    PlatformUrl platformUrl = source.getPlatformUrl();
    UploadStrategy uploader = this.getUploadStrategy();
    Logger logger = LogManager.getLogger(StreamService.class);

    BackoffOptions backoffOptions =
            new BackoffOptionsBuilder()
                    .withMaxRetries(configurationService.getConfiguration().getInt("coveopushclient.maxretries", DEFAULT_MAX_RETRIES))
                    .withRetryAfter(configurationService.getConfiguration().getInt("coveopushclient.retryafter.milliseconds", DEFAULT_RETRY_AFTER))
                    .build();

    if (LOG.isDebugEnabled()) {
      LOG.debug("Initializing UpdateStreamService with backoff options: " + backoffOptions);
    }

    this.source = source;
    this.queue = new DocumentUploadQueue(uploader);
    ApiCore api = new ApiCore(backoffOptions);
    this.platformClient = new PlatformClient(apiKey, organizationId, platformUrl, api);
    platformClient.setUserAgents(userAgents);
    this.service = new StreamServiceInternal(this.source, this.queue, this.platformClient, logger);
  }

  /**
   * Adds documents to the previously specified source. This function will open a stream before
   * uploading documents into it.
   *
   * <p>If called several times, the service will automatically batch documents and create new
   * stream chunks whenever the data payload exceeds the <a
   * href="https://docs.coveo.com/en/lb4a0344#stream-api-limits">batch size limit</a> set for the
   * Stream API.
   *
   * <p>Once there are no more documents to add, it is important to call the {@link
   * StreamService#close} function in order to send any buffered documents and close the open
   * stream. Otherwise, changes will not be reflected in the index.
   *
   * <p>
   *
   * <pre>{@code
   * //...
   * StreamService service = new StreamService(source));
   * for (DocumentBuilder document : fictionalDocumentList) {
   *     service.add(document);
   * }
   * service.close(document);
   * }</pre>
   *
   * <p>For more code samples, @see `samples/StreamDocuments.java`
   *
   * @param document The documentBuilder to add to your source
   * @throws InterruptedException
   * @throws IOException
   */
  public void add(DocumentBuilder document) throws IOException, InterruptedException {
      try {
          this.streamId = this.service.add(document);
      }  catch (Exception e) {
        LOG.error("Error adding document", e);
        throw e;
      }
  }

  /**
   * Sends any buffered documents and <a
   * href="https://docs.coveo.com/en/lb4a0344#step-3-close-the-stream">closes the stream</a>.
   *
   * <p>Upon invoking this method, any indexed items not added through this {@link StreamService}
   * instance will be removed. All documents added from the initialization of the service until the
   * invocation of the {@link StreamService#close} function will completely replace the previous
   * content of the source.
   *
   * <p>When you upload a catalog into a source, it will replace the previous content of the source
   * completely. Expect a 15-minute delay for the removal of the old items from the index.
   *
   * @return
   * @throws IOException
   * @throws InterruptedException
   * @throws NoOpenStreamException
   */
  public HttpResponse<String> close()
      throws IOException, InterruptedException, NoOpenStreamException {
      try {
          return this.service.close();
      } catch (Exception e) {
        LOG.error("Error closing stream", e);
        throw e;
      }
  }

  private UploadStrategy getUploadStrategy() {
    return (batchUpdate) -> {
      String sourceId = this.getSourceId();
      HttpResponse<String> resFileContainer =
          this.platformClient.requireStreamChunk(sourceId, this.streamId);
      FileContainer fileContainer =
          new Gson().fromJson(resFileContainer.body(), FileContainer.class);
      String batchUpdateJson = new Gson().toJson(batchUpdate.marshal());
      return this.platformClient.uploadContentToFileContainer(fileContainer, batchUpdateJson);
    };
  }

  private String getSourceId() {
    return this.source.getId();
  }
}
