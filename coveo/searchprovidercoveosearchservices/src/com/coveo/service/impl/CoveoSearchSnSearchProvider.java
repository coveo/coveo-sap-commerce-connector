package com.coveo.service.impl;

import com.coveo.constants.SearchprovidercoveosearchservicesConstants;
import com.coveo.searchservices.data.CoveoSearchSnSearchProviderConfiguration;
import com.coveo.pushapiclient.exceptions.NoOpenFileContainerException;
import com.coveo.pushapiclient.exceptions.NoOpenStreamException;
import com.coveo.stream.service.CoveoStreamServiceStrategy;
import com.coveo.stream.service.impl.CoveoRebuildStreamService;
import com.coveo.stream.service.impl.CoveoProductStreamServiceStrategy;
import com.coveo.stream.service.impl.CoveoUpdateStreamService;
import de.hybris.platform.searchservices.core.SnException;
import de.hybris.platform.searchservices.core.service.SnContext;
import de.hybris.platform.searchservices.document.data.SnDocumentBatchOperationRequest;
import de.hybris.platform.searchservices.document.data.SnDocumentBatchOperationResponse;
import de.hybris.platform.searchservices.document.data.SnDocumentBatchRequest;
import de.hybris.platform.searchservices.document.data.SnDocumentBatchResponse;
import de.hybris.platform.searchservices.enums.SnIndexerOperationStatus;
import de.hybris.platform.searchservices.enums.SnIndexerOperationType;
import de.hybris.platform.searchservices.index.data.SnIndex;
import de.hybris.platform.searchservices.indexer.data.SnIndexerOperation;
import de.hybris.platform.searchservices.search.data.SnSearchQuery;
import de.hybris.platform.searchservices.search.data.SnSearchResult;
import de.hybris.platform.searchservices.spi.service.impl.AbstractSnSearchProvider;
import de.hybris.platform.searchservices.suggest.data.SnSuggestQuery;
import de.hybris.platform.searchservices.suggest.data.SnSuggestResult;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class CoveoSearchSnSearchProvider extends AbstractSnSearchProvider<CoveoSearchSnSearchProviderConfiguration> implements InitializingBean {

    private static final Logger LOG = Logger.getLogger(CoveoSearchSnSearchProvider.class);

    private CoveoStreamServiceStrategy streamServiceStrategy;

    private ConfigurationService configurationService;

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    @Override
    public void exportConfiguration(SnContext exportConfiguration) throws SnException {
        //there is no need to export any configuration to Coveo at this stage
        //a placeholder to export synonyms dictionaries
    }

    @Override
    public void createIndex(SnContext context, String indexId) throws SnException {
        //a placeholder to create the source on Coveo Org
        //for now we will consider that the source is already created
        SnIndex snIndex = new SnIndex();
        snIndex.setId(context.getIndexType().getId());
        snIndex.setIndexTypeId(context.getIndexType().getId());
        snIndex.setActive(true);
    }


    @Override
    public void deleteIndex(SnContext context, String indexId) throws SnException {
    }

    @Override
    public SnIndexerOperation createIndexerOperation(SnContext context, SnIndexerOperationType indexerOperationType, int totalItems) throws SnException {
        SnIndexerOperation indexerOperation = new SnIndexerOperation();
        indexerOperation.setIndexId(context.getIndexType().getId());
        indexerOperation.setIndexTypeId(context.getIndexType().getId());
        indexerOperation.setOperationType(indexerOperationType);
        indexerOperation.setStatus(SnIndexerOperationStatus.RUNNING);

        String composedType = context.getIndexType().getItemComposedType();
        String[] availabilityTypes = configurationService.getConfiguration().getString(SearchprovidercoveosearchservicesConstants.SUPPORTED_AVAILABILITY_TYPES_CODE).split(",");
        if (availabilityTypes != null && Arrays.asList(availabilityTypes).contains(composedType)) {
            if (indexerOperationType == SnIndexerOperationType.FULL) {
                streamServiceStrategy = (CoveoStreamServiceStrategy) context.getAttributes().get(SearchprovidercoveosearchservicesConstants.COVEO_AVAILABILITY_REBUILD_STREAM_SERVICES_KEY);
            } else {
                streamServiceStrategy = (CoveoStreamServiceStrategy) context.getAttributes().get(SearchprovidercoveosearchservicesConstants.COVEO_AVAILABILITY_UPDATE_STREAM_SERVICES_KEY);
            }
        } else {
            if (indexerOperationType == SnIndexerOperationType.FULL) {
                streamServiceStrategy = (CoveoProductStreamServiceStrategy<CoveoRebuildStreamService>) context.getAttributes().get(SearchprovidercoveosearchservicesConstants.COVEO_PRODUCT_REBUILD_STREAM_SERVICES_KEY);
            } else {
                streamServiceStrategy = (CoveoProductStreamServiceStrategy<CoveoUpdateStreamService>) context.getAttributes().get(SearchprovidercoveosearchservicesConstants.COVEO_PRODUCT_UPDATE_STREAM_SERVICES_KEY);
            }
        }

        if (streamServiceStrategy == null) {
            throw new SnException("error creating client service");
        }
        LOG.info("Using index operation type of " + indexerOperationType.getCode());
        return indexerOperation;
    }

    @Override
    public SnIndexerOperation updateIndexerOperationStatus(SnContext context, String indexerOperationId, SnIndexerOperationStatus status, String errorMessage) throws SnException {
        return null;
    }

    @Override
    public SnDocumentBatchResponse executeDocumentBatch(SnContext context, String indexId, SnDocumentBatchRequest documentBatchRequest, String indexerOperationId) throws SnException {
        List<SnDocumentBatchOperationRequest> requests = documentBatchRequest.getRequests();
        if (LOG.isDebugEnabled()) LOG.debug("Document batch with size " + requests.size());
        List<SnDocumentBatchOperationResponse> responses = streamServiceStrategy.pushDocuments(requests);
        SnDocumentBatchResponse documentBatchResponse = new SnDocumentBatchResponse();
        documentBatchResponse.setResponses(responses);
        return documentBatchResponse;
    }

    @Override
    public void commit(SnContext context, String indexId) throws SnException {
        closeService(context);
    }

    @Override
    public SnSearchResult search(SnContext context, String indexId, SnSearchQuery searchQuery) throws SnException {
        //since we are using coveo just in the frontend , this method is never used , it is called by search api from occ
        //and probably from backoffice
        return null;
    }

    @Override
    public SnSuggestResult suggest(SnContext context, String indexId, SnSuggestQuery suggestQuery) throws SnException {
        //since we are using coveo just in the frontend , this method is never used , it is called for autocomplete via occ
        return null;
    }

    private void closeService(SnContext context) throws SnException {
        if (LOG.isDebugEnabled()) LOG.debug("Closing Service");
        try {
            streamServiceStrategy.closeServices();
        } catch (IOException | InterruptedException| NoOpenStreamException | NoOpenFileContainerException exception) {
            LOG.error("There was an issue closing one of the streams. We will continue to close the remaining streams", exception);
        }
    }

    @Required
    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }
}
