package com.coveo.pushapiclient;

import com.coveo.pushapiclient.exceptions.NoOpenFileContainerException;
import com.google.gson.Gson;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.http.HttpResponse;

import static com.coveo.pushapiclient.BackoffOptionsBuilder.DEFAULT_MAX_RETRIES;
import static com.coveo.pushapiclient.BackoffOptionsBuilder.DEFAULT_RETRY_AFTER;

public class UpdateStreamService {

  private static final Logger LOG = Logger.getLogger(UpdateStreamService.class);

  PlatformClient platformClient;
  UpdateStreamServiceInternal updateStreamServiceInternal;

  private final ConfigurationService configurationService;

  private FileContainer fileContainer;

  public UpdateStreamService(ConfigurationService configurationService) {
    super();
    this.configurationService = configurationService;
  }

  public void init(StreamEnabledSource source, String[] userAgents) {

    BackoffOptions backoffOptions =
            new BackoffOptionsBuilder()
                    .withMaxRetries(configurationService.getConfiguration().getInt("coveopushclient.maxretries", DEFAULT_MAX_RETRIES))
                    .withRetryAfter(configurationService.getConfiguration().getInt("coveopushclient.retryafter.milliseconds", DEFAULT_RETRY_AFTER))
                    .build();

    if (LOG.isDebugEnabled()) {
        LOG.debug("Initializing UpdateStreamService with backoff options: " + backoffOptions);
    }

    this.platformClient =
            new PlatformClient(
                    source.getApiKey(), source.getOrganizationId(), source.getPlatformUrl(), backoffOptions);
    this.platformClient.setUserAgents(userAgents);
    this.updateStreamServiceInternal =
            new UpdateStreamServiceInternal(
                    source,
                    new StreamDocumentUploadQueue(this.getUploadStrategy()),
                    this.platformClient, LOG);
  }

  /**
   * Adds documents to an open file container be created or updated. If there is no file container
   * open to receive the documents, this function will open a file container before uploading
   * documents into it.
   *
   * <p>If called several times, the service will automatically batch documents and create new
   * stream chunks whenever the data payload exceeds the <a
   * href="https://docs.coveo.com/en/lb4a0344#stream-api-limits">batch size limit</a> set for the
   * Stream API.
   *
   * <p>Once there are no more documents to add, it is important to call the {@link
   * UpdateStreamService#close} function in order to send any buffered documents and push the file
   * container. Otherwise, changes will not be reflected in the index.
   *
   * <p>
   *
   * <pre>{@code
   * //...
   * UpdateStreamService service = new UpdateStreamService(source));
   * for (DocumentBuilder document : fictionalDocumentList) {
   *     service.addOrUpdate(document);
   * }
   * service.close(document);
   * }</pre>
   *
   * <p>For more code samples, @see `samples/UpdateStreamDocuments.java`
   *
   * @param document The documentBuilder to push to your file container
   * @throws InterruptedException If the creation of the file container or adding the document is
   *     interrupted.
   * @throws IOException If the creation of the file container or adding the document fails.
   */
  public void addOrUpdate(DocumentBuilder document) throws IOException, InterruptedException {
      try {
          fileContainer = updateStreamServiceInternal.addOrUpdate(document);
      } catch (Exception e) {
        LOG.error("Error adding or updating document", e);
        throw e;
      }
  }

  /**
   * Adds a document containing the specific field, and it's value to be updated. If there is no
   * file container open to receive the documents, this function will open a file container before
   * uploading the partial update details into it. More details on partial updates can be found in
   * the <a
   * href="https://docs.coveo.com/en/l62e0540/coveo-for-commerce/how-to-update-your-catalog#partial-item-updates">
   * Partial item updates</a> section.
   *
   * <p>If called several times, the service will automatically batch documents and create new
   * stream chunks whenever the data payload exceeds the <a
   * href="https://docs.coveo.com/en/lb4a0344#stream-api-limits">batch size limit</a> set for the
   * Stream API.
   *
   * <p>Once there are no more documents to add, it is important to call the {@link
   * UpdateStreamService#close} function in order to send any buffered documents and push the file
   * container. Otherwise, changes will not be reflected in the index.
   *
   * <p>
   *
   * <pre>{@code
   * //...
   * UpdateStreamService service = new UpdateStreamService(source));
   * for (PartialUpdateDocument document : fictionalDocumentList) {
   *     service.addPartialUpdate(document);
   * }
   * service.close(document);
   * }</pre>
   *
   * <p>For more code samples, @see `samples/UpdateStreamDocuments.java`
   *
   * @param document The partial update document to push to your file container
   * @throws InterruptedException If the creation of the file container or adding the document is
   *     interrupted.
   * @throws IOException If the creation of the file container or adding the document fails.
   */
  public void addPartialUpdate(PartialUpdateDocument document)
      throws IOException, InterruptedException {
      try {
          fileContainer = updateStreamServiceInternal.addPartialUpdate(document);
      } catch (Exception e) {
        LOG.error("Error adding partial document", e);
        throw e;
      }
  }

  /**
   * Adds documents to an open file container be deleted. If there is no file container open to
   * receive the documents, this function will open a file container before uploading documents into
   * it.
   *
   * <p>If called several times, the service will automatically batch documents and create new
   * stream chunks whenever the data payload exceeds the <a
   * href="https://docs.coveo.com/en/lb4a0344#stream-api-limits">batch size limit</a> set for the
   * Stream API.
   *
   * <p>Once there are no more documents to add, it is important to call the {@link
   * UpdateStreamService#close} function in order to send any buffered documents and push the file
   * container. Otherwise, changes will not be reflected in the index.
   *
   * <p>
   *
   * <pre>{@code
   * //...
   * UpdateStreamService service = new UpdateStreamService(source));
   * for (DeleteDocument document : fictionalDocumentList) {
   *     service.delete(document);
   * }
   * service.close(document);
   * }</pre>
   *
   * <p>For more code samples, @see `samples/UpdateStreamDocuments.java`
   *
   * @param document The deleteDocument to push to your file container
   * @throws InterruptedException If the creation of the file container or adding the document is
   *     interrupted.
   * @throws IOException If the creation of the file container or adding the document fails.
   */
  public void delete(DeleteDocument document) throws IOException, InterruptedException {
      try {
          fileContainer = updateStreamServiceInternal.delete(document);
      }  catch (Exception e) {
        LOG.error("Error deleting document", e);
        throw e;
      }
  }

  /**
   * Sends any buffered documents and <a
   * href="https://docs.coveo.com/en/l62e0540/how-to-update-your-catalog#step-3-send-the-file-container-to-update-your-catalog">pushes
   * the file container</a>.
   *
   * <p>Upon invoking this method, any documents added to the file container will be pushed and
   * indexed.
   *
   * @return The HttpResponse from the platform.
   * @throws IOException If the pushing file container failed.
   * @throws InterruptedException If the pushing file container is interrupted.
   * @throws NoOpenFileContainerException If there is no open file container to push.
   */
  public HttpResponse<String> close()
      throws IOException, InterruptedException, NoOpenFileContainerException {
      try {
        return updateStreamServiceInternal.close();
      }  catch (Exception e) {
        LOG.error("Error closing stream", e);
        throw e;
      }
  }

  private UploadStrategy getUploadStrategy() {
    return (streamUpdate) -> {
      String batchUpdateJson = new Gson().toJson(streamUpdate.marshal());
      return this.platformClient.uploadContentToFileContainer(fileContainer, batchUpdateJson);
    };
  }
}
