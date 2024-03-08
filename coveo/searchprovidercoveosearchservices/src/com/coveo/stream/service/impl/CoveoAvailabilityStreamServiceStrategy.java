package com.coveo.stream.service.impl;

import com.coveo.pushapiclient.DocumentBuilder;
import com.coveo.pushapiclient.exceptions.NoOpenFileContainerException;
import com.coveo.pushapiclient.exceptions.NoOpenStreamException;
import com.coveo.searchservices.data.CoveoCatalogObjectType;
import com.coveo.stream.service.CoveoStreamService;
import com.coveo.stream.service.CoveoStreamServiceStrategy;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CoveoAvailabilityStreamServiceStrategy<T extends CoveoStreamService> implements CoveoStreamServiceStrategy {

    private static final Logger LOG = Logger.getLogger(CoveoAvailabilityStreamServiceStrategy.class);

    T availabilityStreamService;

    public CoveoAvailabilityStreamServiceStrategy(List<T> streamServices) {
        for(T streamService : streamServices) {
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
            if (coveoDocument != null) {
                try {
                    availabilityStreamService.pushDocument(coveoDocument);
                    documentBatchOperationResponse.setStatus(SnDocumentOperationStatus.UPDATED);
                }  catch (IOException | InterruptedException exception) {
                    documentBatchOperationResponse.setStatus(SnDocumentOperationStatus.FAILED);
                    LOG.error("failed to index " + request.getDocument().getId(), exception);
                }
            } else {
                documentBatchOperationResponse.setStatus(SnDocumentOperationStatus.FAILED);
            }
        }

        return documentBatchOperationResponse;
    }

    private DocumentBuilder createCoveoDocument(SnDocument document) {
        Map<String, Object> documentFields = document.getFields();
        String documentName = null;
        if (isNameLocalised(documentFields)) {
            documentName = ((HashMap<Locale, String>) documentFields.get("name")).entrySet().iterator().next().getValue();
        } else {
            documentName = (String) documentFields.get("name");
        }

        // If the value is still blank at this point we are unable to build the document
        if (StringUtils.isBlank(documentName)) {
            LOG.warn("SnDocument with id " + document.getId() + " does not have a name field, will not push this document");
            return null;
        }

        DocumentBuilder documentBuilder = new DocumentBuilder("https://sapcommerce/stores/" + StringUtils.deleteWhitespace(documentName), documentName)
                .withMetadata(document.getFields());
        if (LOG.isDebugEnabled()) {
            JsonObject jsonDocument = (new Gson()).toJsonTree(documentBuilder.getDocument()).getAsJsonObject();
            LOG.debug("Coveo Document " + jsonDocument.toString());
        }
        return documentBuilder;
    }

    private static boolean isNameLocalised(Map<String, Object> documentFields) {
        return documentFields.get("name") instanceof HashMap<?, ?>;
    }

    @Override
    public void closeServices() throws NoOpenStreamException, IOException, InterruptedException, NoOpenFileContainerException {
        availabilityStreamService.closeStream();
    }
}
