package com.coveo.service.impl;


import com.coveo.constants.SearchprovidercoveosearchservicesConstants;
import com.coveo.data.CoveoSearchSnSearchProviderConfiguration;
import com.coveo.pushapiclient.DocumentBuilder;
import com.coveo.pushapiclient.StreamService;
import com.coveo.pushapiclient.exceptions.NoOpenStreamException;
import de.hybris.platform.searchservices.admin.data.SnLanguage;
import de.hybris.platform.searchservices.core.SnException;
import de.hybris.platform.searchservices.core.service.SnContext;
import de.hybris.platform.searchservices.document.data.*;
import de.hybris.platform.searchservices.enums.SnDocumentOperationStatus;
import de.hybris.platform.searchservices.enums.SnIndexerOperationStatus;
import de.hybris.platform.searchservices.enums.SnIndexerOperationType;
import de.hybris.platform.searchservices.index.data.SnIndex;
import de.hybris.platform.searchservices.indexer.data.SnIndexerOperation;
import de.hybris.platform.searchservices.search.data.SnSearchQuery;
import de.hybris.platform.searchservices.search.data.SnSearchResult;
import de.hybris.platform.searchservices.spi.service.impl.AbstractSnSearchProvider;
import de.hybris.platform.searchservices.suggest.data.SnSuggestQuery;
import de.hybris.platform.searchservices.suggest.data.SnSuggestResult;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;

public class CoveoSearchSnSearchProvider extends AbstractSnSearchProvider<CoveoSearchSnSearchProviderConfiguration> implements InitializingBean {

    private static final Logger LOG = Logger.getLogger(CoveoSearchSnSearchProvider.class);

    StreamService streamService;

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    @Override
    public void exportConfiguration(de.hybris.platform.searchservices.spi.data.SnExportConfiguration exportConfiguration, List<Locale> locales) throws SnException {
        //there is no need to export any configuration to Coveo at this stage
        //a placeholder to export synonyms dictionaries
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
    }

    @Override
    public SnIndexerOperation createIndexerOperation(SnContext context, SnIndexerOperationType indexerOperationType, int totalItems) throws SnException {
        SnIndexerOperation indexerOperation = new SnIndexerOperation();
        indexerOperation.setIndexId(context.getIndexType().getId());
        indexerOperation.setIndexTypeId(context.getIndexType().getId());
        indexerOperation.setOperationType(indexerOperationType);
        indexerOperation.setStatus(SnIndexerOperationStatus.RUNNING);
        streamService = (StreamService) context.getAttributes().get(SearchprovidercoveosearchservicesConstants.COVEO_STREAM_SERVICE_KEY);
        if (streamService == null) {
            throw new SnException("error creating stream service");
        }
        return indexerOperation;
    }

    @Override
    public SnIndexerOperation updateIndexerOperationStatus(SnContext context, String indexerOperationId, SnIndexerOperationStatus status, String errorMessage) throws SnException {
        return null;
    }


    @Override
    public void completeIndexerOperation(SnContext context, String indexerOperationId) throws SnException {
        //TODO
    }

    @Override
    public void abortIndexerOperation(SnContext context, String indexerOperationId, String message) throws SnException {
        //TODO what should happen if the client abort the indexation before it ends ?
        try {
            streamService.close();
        } catch (IOException | InterruptedException | NoOpenStreamException exception) {
            throw new SnException(exception);
        }
    }

    @Override
    public void failIndexerOperation(SnContext context, String indexerOperationId, String message) throws SnException {
    }

    @Override
    public SnDocumentBatchResponse executeDocumentBatch(SnContext context, String indexId, SnDocumentBatchRequest documentBatchRequest, String indexerOperationId) throws SnException {
        List<SnDocumentBatchOperationResponse> responses = new ArrayList<SnDocumentBatchOperationResponse>();
        List<SnDocumentBatchOperationRequest> requests = documentBatchRequest.getRequests();
        String language = getLanguage(context);
        requests.stream().forEach(request -> {
            responses.add(streamDocument(language, request));
        });

        SnDocumentBatchResponse documentBatchResponse = new SnDocumentBatchResponse();
        documentBatchResponse.setResponses(responses);
        return documentBatchResponse;
    }

    private SnDocumentBatchOperationResponse streamDocument(String language, SnDocumentBatchOperationRequest request) {
        SnDocumentBatchOperationResponse documentBatchOperationResponse = new SnDocumentBatchOperationResponse();
        synchronized (streamService) {
            try {
                streamService.add(createCoveoDocument(request.getDocument(), language));
                documentBatchOperationResponse.setId(request.getDocument().getId());
                documentBatchOperationResponse.setStatus(SnDocumentOperationStatus.CREATED);
            } catch (IOException | InterruptedException exception) {
                documentBatchOperationResponse.setId(request.getDocument().getId());
                documentBatchOperationResponse.setStatus(SnDocumentOperationStatus.FAILED);
                LOG.error("failed to index " + request.getDocument().getId(), exception);
            }
        }
        return documentBatchOperationResponse;
    }

    private String getLanguage(SnContext context) throws SnException {
        List<SnLanguage> languages = context.getIndexConfiguration().getLanguages();
        if (CollectionUtils.isEmpty(languages)) {
            throw new SnException("No Language is specified in index configuration");
        }
        return languages.get(0).getId();
    }

    @Override
    public void commit(SnContext context, String indexId) throws SnException {
        try {
            streamService.close();
        } catch (IOException | InterruptedException | NoOpenStreamException exception) {
            throw new SnException(exception);
        }
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


    protected static class ConverterContext {
        private final Set<Locale> locales;

        public ConverterContext(final List<Locale> locales) {
            this.locales = Set.copyOf(locales);
        }

        public Set<Locale> getLocales() {
            return locales;
        }
    }

    private DocumentBuilder createCoveoDocument(SnDocument document, String language) {
        Locale locale = new Locale(language);
        String name = ((HashMap<Locale, String>) document.getFields().get("name")).get(locale);
        DocumentBuilder documentBuilder = new DocumentBuilder(getUri(document), name)
                .withMetadata(document.getFields());

        return documentBuilder;
    }

    private static String getUri(SnDocument document) {
        //TODO just to make the URI valid
        return "https://sapcommerce/product/p/" + (String) document.getFields().get("code");
    }


}
