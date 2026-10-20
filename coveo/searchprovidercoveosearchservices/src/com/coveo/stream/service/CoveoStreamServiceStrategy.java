package com.coveo.stream.service;

import com.coveo.pushapiclient.exceptions.NoOpenFileContainerException;
import com.coveo.pushapiclient.exceptions.NoOpenStreamException;
import de.hybris.platform.searchservices.document.data.SnDocumentBatchOperationRequest;
import de.hybris.platform.searchservices.document.data.SnDocumentBatchOperationResponse;

import java.io.IOException;
import java.util.List;

public interface CoveoStreamServiceStrategy {

    List<SnDocumentBatchOperationResponse> pushDocuments(List<SnDocumentBatchOperationRequest> documents);

    void closeServices() throws NoOpenStreamException, IOException, InterruptedException, NoOpenFileContainerException;
}
