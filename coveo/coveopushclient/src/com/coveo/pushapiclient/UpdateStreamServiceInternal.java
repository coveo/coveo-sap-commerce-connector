package com.coveo.pushapiclient;

import java.io.IOException;

/** For internal use only. Made to easily test the service without having to use PowerMock */
class UpdateStreamServiceInternal {
  private final StreamDocumentUploadQueue queue;

  public UpdateStreamServiceInternal(final StreamDocumentUploadQueue queue) {
    this.queue = queue;
  }

  public void addOrUpdate(DocumentBuilder document)
      throws IOException, InterruptedException {
    queue.add(document);
  }

  public void addPartialUpdate(PartialUpdateDocument document)
      throws IOException, InterruptedException {
    queue.add(document);
  }

  public void delete(DeleteDocument document) throws IOException, InterruptedException {
    queue.add(document);
  }

  public void close()
      throws IOException, InterruptedException {
    queue.flush();
  }
}
