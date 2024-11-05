package com.coveo.service.impl;

import com.coveo.constants.SearchprovidercoveosearchservicesConstants;
import com.coveo.searchservices.data.CoveoSearchSnSearchProviderConfiguration;
import com.coveo.pushapiclient.exceptions.NoOpenFileContainerException;
import com.coveo.pushapiclient.exceptions.NoOpenStreamException;
import com.coveo.stream.service.CoveoStreamServiceStrategy;
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
import de.hybris.platform.searchservices.spi.data.SnExportConfiguration;
import de.hybris.platform.searchservices.spi.service.impl.AbstractSnSearchProvider;
import de.hybris.platform.searchservices.suggest.data.SnSuggestQuery;
import de.hybris.platform.searchservices.suggest.data.SnSuggestResult;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CoveoSearchSnSearchProvider extends AbstractSnSearchProvider<CoveoSearchSnSearchProviderConfiguration> implements InitializingBean {

    private static final Logger LOG = Logger.getLogger(CoveoSearchSnSearchProvider.class);

    private final Map<String, CoveoStreamServiceStrategy> streamServiceStrategyMap = new HashMap<>();

    private ConfigurationService configurationService;

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    @Override
    public void exportConfiguration(SnExportConfiguration exportConfiguration, List<Locale> locales) throws SnException {
        //there is no need to export any configuration to Coveo at this stage
        //a placeholder to export synonyms dictionaries
        LOG.warn("Export configuration method is not implemented");
    }

    @Override
    public SnIndex createIndex(SnContext context) throws SnException {
        //a placeholder to create the source on Coveo Org
        //for now we will consider that the source is already created
        SnIndex snIndex = new SnIndex();
        snIndex.setId(context.getIndexType().getId());
        snIndex.setIndexTypeId(context.getIndexType().getId());
        snIndex.setActive(true);
        return snIndex;
    }


    @Override
    public void deleteIndex(SnContext context, String indexId) throws SnException {
        LOG.warn("Delete index method is not implemented");
    }

    @Override
    public SnIndexerOperation createIndexerOperation(SnContext context, SnIndexerOperationType indexerOperationType, int totalItems) throws SnException {
        if(LOG.isDebugEnabled()) LOG.debug(String.format("Starting to create the indexer operation for the index type %s and operation type %s", context.getIndexType().getId(), indexerOperationType.getCode()));
        SnIndexerOperation indexerOperation = createSnIndexerOperation(context, indexerOperationType);

        String composedType = context.getIndexType().getItemComposedType();
        String[] availabilityTypes = configurationService.getConfiguration().getString(SearchprovidercoveosearchservicesConstants.SUPPORTED_AVAILABILITY_TYPES_CODE).split(",");

        if(LOG.isDebugEnabled()) LOG.debug(String.format("Availability types are %s and composed type is %s", Arrays.toString(availabilityTypes), composedType));
        if (availabilityTypes != null && Arrays.asList(availabilityTypes).contains(composedType)) {
            if (indexerOperationType == SnIndexerOperationType.FULL) {
                streamServiceStrategyMap.put(indexerOperation.getIndexId(), (CoveoStreamServiceStrategy) context.getAttributes().get(SearchprovidercoveosearchservicesConstants.COVEO_AVAILABILITY_REBUILD_STREAM_SERVICES_KEY));
            } else {
                streamServiceStrategyMap.put(indexerOperation.getIndexId(), (CoveoStreamServiceStrategy) context.getAttributes().get(SearchprovidercoveosearchservicesConstants.COVEO_AVAILABILITY_UPDATE_STREAM_SERVICES_KEY));
            }
        } else {
            if (indexerOperationType == SnIndexerOperationType.FULL) {
                streamServiceStrategyMap.put(indexerOperation.getIndexId(), (CoveoStreamServiceStrategy) context.getAttributes().get(SearchprovidercoveosearchservicesConstants.COVEO_PRODUCT_REBUILD_STREAM_SERVICES_KEY));
            } else {
                streamServiceStrategyMap.put(indexerOperation.getIndexId(), (CoveoStreamServiceStrategy) context.getAttributes().get(SearchprovidercoveosearchservicesConstants.COVEO_PRODUCT_UPDATE_STREAM_SERVICES_KEY));
            }
        }

        if (streamServiceStrategyMap.isEmpty()) {
            LOG.error("No stream service found for the index operation");
            throw new SnException("error creating client service");
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("Using index operation type of " + indexerOperationType.getCode());
            LOG.trace("createIndexerOperation :");
            LOG.trace("Index Context ID: " + context.getIndexConfiguration().getId());
            LOG.trace("Index ID: " + indexerOperation.getIndexId());
            LOG.trace("Index Type ID: " + indexerOperation.getIndexTypeId());
        }
        return indexerOperation;
    }

    private static SnIndexerOperation createSnIndexerOperation(SnContext context, SnIndexerOperationType indexerOperationType) {
        SnIndexerOperation indexerOperation = new SnIndexerOperation();
        // Here we are using the combination of the index type id and the operation type code to create a unique index id
        // This will then be used to identify the stream service to use for the operation within a map
        indexerOperation.setId(context.getIndexType().getId() + indexerOperationType.getCode());
        // We need to set this the same as the ID because this is the value passed into the commit method
        indexerOperation.setIndexId(indexerOperation.getId());
        indexerOperation.setIndexTypeId(context.getIndexType().getId());
        indexerOperation.setOperationType(indexerOperationType);
        indexerOperation.setStatus(SnIndexerOperationStatus.RUNNING);
        return indexerOperation;
    }

    @Override
    public SnIndexerOperation updateIndexerOperationStatus(SnContext context, String indexerOperationId, SnIndexerOperationStatus status, String errorMessage) throws SnException {
        LOG.warn("Update indexer operation method is not implemented");
        return null;
    }


    @Override
    public void completeIndexerOperation(SnContext context, String indexerOperationId) throws SnException {
        LOG.info(String.format("The indexer operation %s has been completed", indexerOperationId));
    }

    @Override
    public void abortIndexerOperation(SnContext context, String indexerOperationId, String message) throws SnException {
        //TODO what should happen if the client abort the indexation before it ends ?
        closeService(context, indexerOperationId);
    }

    @Override
    public void failIndexerOperation(SnContext context, String indexerOperationId, String message) throws SnException {
        LOG.warn(String.format("The indexer operation %s failed with message: %s", indexerOperationId, message));
    }

    @Override
    public SnDocumentBatchResponse executeDocumentBatch(SnContext context, String indexId, SnDocumentBatchRequest documentBatchRequest, String indexerOperationId) throws SnException {
        List<SnDocumentBatchOperationRequest> requests = documentBatchRequest.getRequests();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Document batch with size " + requests.size());
            LOG.debug("Have indexerOperationId " + indexerOperationId);
        }
        CoveoStreamServiceStrategy streamServiceStrategy = streamServiceStrategyMap.get(indexerOperationId);
        List<SnDocumentBatchOperationResponse> responses = streamServiceStrategy.pushDocuments(requests);
        SnDocumentBatchResponse documentBatchResponse = new SnDocumentBatchResponse();
        documentBatchResponse.setResponses(responses);

        if (LOG.isTraceEnabled()) {
            LOG.trace("executeDocumentBatch :");
            LOG.trace("Index Context ID: " + context.getIndexConfiguration().getId());
            LOG.trace("Index ID: " + indexId);
            LOG.trace("Index Operation ID: " + indexerOperationId);
        }
        return documentBatchResponse;
    }

    @Override
    public void commit(SnContext context, String indexId) throws SnException {
        closeService(context, indexId);
    }

    @Override
    public SnSearchResult search(SnContext context, String indexId, SnSearchQuery searchQuery) throws SnException {
        //since we are using coveo just in the frontend , this method is never used , it is called by search api from occ
        //and probably from backoffice
        LOG.error("Search method is not implemented");
        return null;
    }

    @Override
    public SnSuggestResult suggest(SnContext context, String indexId, SnSuggestQuery suggestQuery) throws SnException {
        //since we are using coveo just in the frontend , this method is never used , it is called for autocomplete via occ
        LOG.error("Suggest method is not implemented");
        return null;
    }

    private void closeService(SnContext context, String indexId) throws SnException {
        if (LOG.isDebugEnabled()) LOG.debug("Closing Service");
        try {
            CoveoStreamServiceStrategy streamServiceStrategy = streamServiceStrategyMap.get(indexId);
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
