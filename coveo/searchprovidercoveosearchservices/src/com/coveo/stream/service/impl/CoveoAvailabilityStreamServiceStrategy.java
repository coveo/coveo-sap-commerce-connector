package com.coveo.stream.service.impl;

import com.coveo.pushapiclient.DocumentBuilder;
import com.coveo.pushapiclient.exceptions.NoOpenFileContainerException;
import com.coveo.pushapiclient.exceptions.NoOpenStreamException;
import com.coveo.searchservices.data.CoveoCatalogObjectType;
import com.coveo.stream.service.CoveoStreamService;
import com.coveo.stream.service.CoveoStreamServiceStrategy;
import com.coveo.stream.service.utils.CoveoFieldValueResolverUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.hybris.platform.searchservices.document.data.SnDocument;
import de.hybris.platform.searchservices.document.data.SnDocumentBatchOperationRequest;
import de.hybris.platform.searchservices.document.data.SnDocumentBatchOperationResponse;
import de.hybris.platform.searchservices.enums.SnDocumentOperationStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.coveo.constants.SearchprovidercoveosearchservicesConstants.COVEO_DOCUMENT_ID_INDEX_ATTRIBUTE;
import static com.coveo.constants.SearchprovidercoveosearchservicesConstants.COVEO_URI_TYPE_INDEX_ATTRIBUTE;

public class CoveoAvailabilityStreamServiceStrategy<T extends CoveoStreamService> implements CoveoStreamServiceStrategy {

    private static final Logger LOG = Logger.getLogger(CoveoAvailabilityStreamServiceStrategy.class);

    T availabilityStreamService;

    public CoveoAvailabilityStreamServiceStrategy(List<T> streamServices) {
        for (T streamService : streamServices) {
            if (streamService.getCoveoSource().getObjectType().equals(CoveoCatalogObjectType.AVAILABILITY)) {
                availabilityStreamService = streamService;
                // There should only ever be 1 availability stream service, hence why we break here
                break;
            }
        }
        if (availabilityStreamService == null) {
            throw new IllegalArgumentException("No availability stream service found");
        }
    }

    @Override
    public List<SnDocumentBatchOperationResponse> pushDocuments(List<SnDocumentBatchOperationRequest> documents) {
        List<SnDocumentBatchOperationResponse> responses = new ArrayList<>();
        documents.forEach(request -> {
            responses.add(streamDocument(request));
        });
        return responses;
    }

    private SnDocumentBatchOperationResponse streamDocument(SnDocumentBatchOperationRequest request) {
        if (LOG.isDebugEnabled()) {
            JsonObject jsonDocument = (new Gson()).toJsonTree(request.getDocument()).getAsJsonObject();
            LOG.debug("Adding SnDocument " + jsonDocument.toString());
        }
        SnDocumentBatchOperationResponse documentBatchOperationResponse = new SnDocumentBatchOperationResponse();
        documentBatchOperationResponse.setId(request.getDocument().getId());

        if (LOG.isDebugEnabled()) LOG.debug("Adding Availability Document " + request.getDocument());
        synchronized (availabilityStreamService) {
            DocumentBuilder coveoDocument = createCoveoDocument(request.getDocument());
            documentBatchOperationResponse.setStatus(SnDocumentOperationStatus.UPDATED);
            if (coveoDocument != null) {
                try {
                    availabilityStreamService.pushDocument(coveoDocument);
                } catch (IOException | InterruptedException exception) {
                    LOG.error("failed to index " + request.getDocument().getId(), exception);
                }
            }
        }

        return documentBatchOperationResponse;
    }

    private DocumentBuilder createCoveoDocument(SnDocument document) {
        Map<String, Object> documentFields = document.getFields();

        String documentId = (String) CoveoFieldValueResolverUtils.resolveFieldValue(COVEO_DOCUMENT_ID_INDEX_ATTRIBUTE, documentFields);
        // If the value is still blank at this point we are unable to build the document
        if (StringUtils.isBlank(documentId)) {
            LOG.warn("SnDocument with id " + document.getId() + " does not have a " + COVEO_DOCUMENT_ID_INDEX_ATTRIBUTE + " field, will not push this document");
            return null;
        }

        String documentName = (String) CoveoFieldValueResolverUtils.resolveFieldValue("name", documentFields);
        // If the value is still blank at this point we are unable to build the document
        if (StringUtils.isBlank(documentName)) {
            LOG.warn("SnDocument with id " + document.getId() + " does not have a name field, will not push this document");
            return null;
        }

        DocumentBuilder documentBuilder = new DocumentBuilder(documentId, documentName).withMetadata(document.getFields());

        String coveoClickableUri = (String) CoveoFieldValueResolverUtils.resolveFieldValue(COVEO_URI_TYPE_INDEX_ATTRIBUTE, documentFields);
        if (!StringUtils.isBlank(coveoClickableUri)) {
            documentBuilder.withClickableUri(coveoClickableUri);
        }

        if (LOG.isDebugEnabled()) {
            JsonObject jsonDocument = (new Gson()).toJsonTree(documentBuilder.getDocument()).getAsJsonObject();
            LOG.debug("Coveo Document " + jsonDocument.toString());
        }
        return documentBuilder;
    }

    @Override
    public void closeServices() throws NoOpenStreamException, IOException, InterruptedException, NoOpenFileContainerException {
        availabilityStreamService.closeStream();
    }
}
