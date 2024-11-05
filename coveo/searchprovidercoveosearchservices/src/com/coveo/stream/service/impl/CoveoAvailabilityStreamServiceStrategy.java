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
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.coveo.constants.SearchprovidercoveosearchservicesConstants.COVEO_DOCUMENT_ID_INDEX_ATTRIBUTE;
import static com.coveo.constants.SearchprovidercoveosearchservicesConstants.COVEO_PRODUCT_STREAM_LOG_INTERVAL_PERCENTAGE;
import static com.coveo.constants.SearchprovidercoveosearchservicesConstants.COVEO_PRODUCT_STREAM_LOG_INTERVAL_PERCENTAGE_DEFAULT;
import static com.coveo.constants.SearchprovidercoveosearchservicesConstants.COVEO_URI_TYPE_INDEX_ATTRIBUTE;

public class CoveoAvailabilityStreamServiceStrategy<T extends CoveoStreamService> implements CoveoStreamServiceStrategy {

    private static final Logger LOG = Logger.getLogger(CoveoAvailabilityStreamServiceStrategy.class);

    T availabilityStreamService;

    private ConfigurationService configurationService;

    public CoveoAvailabilityStreamServiceStrategy(List<T> streamServices, ConfigurationService configurationService) {
        for (T streamService : streamServices) {
            if (streamService.getCoveoSource().getObjectType().equals(CoveoCatalogObjectType.AVAILABILITY)) {
                if (LOG.isDebugEnabled()) LOG.debug("Setting availability stream service based on source " + streamService.getCoveoSource().getId());
                availabilityStreamService = streamService;
                // There should only ever be 1 availability stream service, hence why we break here
                break;
            }
        }
        if (availabilityStreamService == null) {
            throw new IllegalArgumentException("No availability stream service found");
        }
        this.configurationService = configurationService;
    }

    @Override
    public List<SnDocumentBatchOperationResponse> pushDocuments(List<SnDocumentBatchOperationRequest> documents) {
        List<SnDocumentBatchOperationResponse> responses = new ArrayList<>();
        int totalDocumentsCount = documents.size();
        int logIntervalPercentage = configurationService.getConfiguration().getInt(COVEO_PRODUCT_STREAM_LOG_INTERVAL_PERCENTAGE);
        if (logIntervalPercentage < 0 || logIntervalPercentage > 100) {
            LOG.warn("Log interval percentage is out of range (0-100%). Using default of 20%.");
            logIntervalPercentage = COVEO_PRODUCT_STREAM_LOG_INTERVAL_PERCENTAGE_DEFAULT;
        }
        int logInterval = (int) Math.ceil(totalDocumentsCount * (logIntervalPercentage / 100.0));
        LOG.info(String.format("Streaming %s documents for source %s", totalDocumentsCount, availabilityStreamService.getCoveoSource().getId()));
        for (int documentIndex = 1; documentIndex <= totalDocumentsCount; documentIndex++) {
            responses.add(streamDocument(documents.get(documentIndex - 1)));
            if (logInterval != 0 && documentIndex % logInterval == 0) {
                LOG.info(String.format("Processed %s of %s documents", documentIndex, totalDocumentsCount));
            }
        }
        LOG.info(String.format("Finished streaming %s documents", responses.size()));
        return responses;
    }

    private SnDocumentBatchOperationResponse streamDocument(SnDocumentBatchOperationRequest request) {
        SnDocumentBatchOperationResponse documentBatchOperationResponse = new SnDocumentBatchOperationResponse();
        documentBatchOperationResponse.setId(request.getDocument().getId());
        synchronized (availabilityStreamService) {
            DocumentBuilder coveoDocument = createCoveoDocument(request.getDocument());
            if (coveoDocument != null) {
                try {
                    if (LOG.isDebugEnabled()) {
                        JsonObject jsonDocument = (new Gson()).toJsonTree(coveoDocument.getDocument()).getAsJsonObject();
                        LOG.debug("Pushing document: " + jsonDocument.toString());
                    }
                    availabilityStreamService.pushDocument(coveoDocument);
                    documentBatchOperationResponse.setStatus(SnDocumentOperationStatus.UPDATED);
                } catch (IOException | InterruptedException exception) {
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
        return documentBuilder;
    }

    @Override
    public void closeServices() throws NoOpenStreamException, IOException, InterruptedException, NoOpenFileContainerException {
        availabilityStreamService.closeStream();
    }
}
