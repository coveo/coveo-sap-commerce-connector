package com.coveo.stream.service.impl;

import com.coveo.pushapiclient.DocumentBuilder;
import com.coveo.pushapiclient.exceptions.NoOpenFileContainerException;
import com.coveo.pushapiclient.exceptions.NoOpenStreamException;
import com.coveo.searchservices.admin.data.CoveoSnCountry;
import com.coveo.searchservices.data.CoveoCatalogObjectType;
import com.coveo.searchservices.data.CoveoSource;
import com.coveo.stream.service.CoveoStreamService;
import com.coveo.stream.service.CoveoStreamServiceStrategy;
import com.coveo.stream.service.utils.CoveoFieldValueResolverUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.hybris.platform.searchservices.admin.data.SnCurrency;
import de.hybris.platform.searchservices.admin.data.SnLanguage;
import de.hybris.platform.searchservices.document.data.SnDocument;
import de.hybris.platform.searchservices.document.data.SnDocumentBatchOperationRequest;
import de.hybris.platform.searchservices.document.data.SnDocumentBatchOperationResponse;
import de.hybris.platform.searchservices.enums.SnDocumentOperationStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.coveo.constants.SearchprovidercoveosearchservicesConstants.COVEO_DOCUMENT_ID_INDEX_ATTRIBUTE;
import static com.coveo.constants.SearchprovidercoveosearchservicesConstants.COVEO_URI_TYPE_INDEX_ATTRIBUTE;

public class CoveoProductStreamServiceStrategy<T extends CoveoStreamService> implements CoveoStreamServiceStrategy {

    private static final Logger LOG = Logger.getLogger(CoveoProductStreamServiceStrategy.class);

    List<SnLanguage> languages;
    List<SnCurrency> currencies;
    List<CoveoSnCountry> countries;
    List<T> streamServices;

    public CoveoProductStreamServiceStrategy(List<SnLanguage> languages, List<SnCurrency> currencies, List<CoveoSnCountry> countries, List<T> incomingStreamServices) {
        this.languages = languages;
        this.currencies = currencies;
        this.countries = countries;
        this.streamServices = new ArrayList<>();
        incomingStreamServices.forEach(streamService -> {
            CoveoSource coveoSource = streamService.getCoveoSource();
            if (coveoSource.getObjectType().equals(CoveoCatalogObjectType.PRODUCTANDVARIANT)) {
                streamServices.add(streamService);
            }
        });
    }

    @Override
    public List<SnDocumentBatchOperationResponse> pushDocuments(List<SnDocumentBatchOperationRequest> documents) {
        Map<String, SnDocumentBatchOperationResponse> responseMap = new HashMap<>();
        if (LOG.isDebugEnabled()) LOG.debug("Streaming Documents");
        for (SnLanguage language : languages) {
            for (SnCurrency currency : currencies) {
                for (CoveoSnCountry country : countries) {
                    T streamService = getStreamService(language, currency, country);
                    if (streamService != null) {
                        documents.forEach(request -> {
                            SnDocumentBatchOperationResponse documentBatchOperationResponse = new SnDocumentBatchOperationResponse();
                            documentBatchOperationResponse.setId(request.getDocument().getId());
                            documentBatchOperationResponse.setStatus(
                                    streamDocument(request, language, currency, country, streamService) ?
                                            SnDocumentOperationStatus.UPDATED : SnDocumentOperationStatus.FAILED);
                            if (!responseMap.containsKey(documentBatchOperationResponse.getId()) ||
                                    documentBatchOperationResponse.getStatus() == SnDocumentOperationStatus.FAILED) {
                                responseMap.put(documentBatchOperationResponse.getId(), documentBatchOperationResponse);
                            }
                        });
                    }
                }
            }
        }
        return new ArrayList<>(responseMap.values());
    }

    private boolean streamDocument(SnDocumentBatchOperationRequest request, SnLanguage language, SnCurrency currency, CoveoSnCountry country, T streamService) {
        boolean success = true;
        if (LOG.isDebugEnabled()) {
            JsonObject jsonDocument = (new Gson()).toJsonTree(request.getDocument()).getAsJsonObject();
            LOG.debug("Adding SnDocument " + jsonDocument.toString());
        }
        synchronized (streamService) {
            DocumentBuilder coveoDocument = createCoveoDocument(request.getDocument(), language.getId(), currency.getId());
            if (coveoDocument != null) {
                try {
                    streamService.pushDocument(coveoDocument);
                } catch (IOException | InterruptedException exception) {
                    success = false;
                    LOG.error("failed to index " + request.getDocument().getId(), exception);
                }
            } else {
                LOG.error("failed to index " + request.getDocument().getId());
                success = false;
            }
        }

        return success;
    }

    private DocumentBuilder createCoveoDocument(SnDocument document, String languageIsoCode, String currencyIsoCode) {
        Locale locale = new Locale(languageIsoCode);
        Currency currency = Currency.getInstance(currencyIsoCode);
        Map<String, Object> documentFields = document.getFields();
        String documentId = CoveoFieldValueResolverUtils.resolveFieldValue(COVEO_DOCUMENT_ID_INDEX_ATTRIBUTE, documentFields, locale, currency);
        // If the value is still blank at this point we are unable to build the document
        if (StringUtils.isBlank(documentId)) {
            LOG.warn("SnDocument with id " + document.getId() + " does not have a " + COVEO_DOCUMENT_ID_INDEX_ATTRIBUTE + " field, will not push this document");
            return null;
        }

        String documentName = CoveoFieldValueResolverUtils.resolveFieldValue("name", documentFields, locale, currency);
        // If the value is still blank at this point we are unable to build the document
        if (StringUtils.isBlank(documentName)) {
            LOG.warn("SnDocument with id " + document.getId() + " does not have a name field, will not push this document");
            return null;
        }

        DocumentBuilder documentBuilder = new DocumentBuilder(documentId, documentName);
        Map<String, Object> fields = document.getFields();
        for (Map.Entry<String, Object> field : fields.entrySet()) {
            documentBuilder.withMetadataValue(field.getKey(), CoveoFieldValueResolverUtils.resolveFieldValue(field.getValue(), locale, currency));
        }

        String coveoClickableUri = CoveoFieldValueResolverUtils.resolveFieldValue(COVEO_URI_TYPE_INDEX_ATTRIBUTE, documentFields, locale, currency);

        if (!StringUtils.isBlank(coveoClickableUri)) {
            documentBuilder.withClickableUri(coveoClickableUri);
        }

        if (LOG.isDebugEnabled()) {
            JsonObject jsonDocument = (new Gson()).toJsonTree(documentBuilder.getDocument()).getAsJsonObject();
            LOG.debug("Coveo Document " + jsonDocument.toString());
        }
        return documentBuilder;
    }

    private static boolean isNameLocalised(Map<String, Object> documentFields, Locale locale) {
        return documentFields.get("name") instanceof HashMap<?, ?> && ((HashMap<Locale, String>) documentFields.get("name")).containsKey(locale);
    }

    private T getStreamService(SnLanguage language, SnCurrency currency, CoveoSnCountry country) {
        T streamService = null;
        for (T service : streamServices) {
            CoveoSource coveoSource = service.getCoveoSource();
            if (coveoSource.getLanguage() == null || coveoSource.getCurrency() == null || coveoSource.getCountry() == null) {
                // this will be the availability source which is a special case, so it's safe to continue
                continue;
            }
            if (coveoSource.getLanguage().getId().equals(language.getId())
                    && coveoSource.getCurrency().getId().equals(currency.getId())
                    && coveoSource.getCountry().getId().equals(country.getId())) {
                streamService = service;
                // there should only ever be 1 stream service for a given language, currency and country, hence why we break here
                break;
            }
        }
        return streamService;
    }

    @Override
    public void closeServices() throws NoOpenStreamException, IOException, InterruptedException, NoOpenFileContainerException {
        for (T streamService : streamServices) {
            streamService.closeStream();
        }
    }
}
