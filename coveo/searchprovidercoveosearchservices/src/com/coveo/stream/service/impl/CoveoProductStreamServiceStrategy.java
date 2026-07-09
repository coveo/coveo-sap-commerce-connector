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
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.searchservices.admin.data.SnCurrency;
import de.hybris.platform.searchservices.admin.data.SnLanguage;
import de.hybris.platform.searchservices.core.SnRuntimeException;
import de.hybris.platform.searchservices.document.data.SnDocument;
import de.hybris.platform.searchservices.document.data.SnDocumentBatchOperationRequest;
import de.hybris.platform.searchservices.document.data.SnDocumentBatchOperationResponse;
import de.hybris.platform.searchservices.enums.SnDocumentOperationStatus;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static com.coveo.constants.SearchprovidercoveosearchservicesConstants.COVEO_DOCUMENT_ID_INDEX_ATTRIBUTE;
import static com.coveo.constants.SearchprovidercoveosearchservicesConstants.COVEO_PRODUCT_STREAM_LOG_INTERVAL_PERCENTAGE;
import static com.coveo.constants.SearchprovidercoveosearchservicesConstants.COVEO_PRODUCT_STREAM_LOG_INTERVAL_PERCENTAGE_DEFAULT;
import static com.coveo.constants.SearchprovidercoveosearchservicesConstants.COVEO_URI_TYPE_INDEX_ATTRIBUTE;

public class CoveoProductStreamServiceStrategy<T extends CoveoStreamService> implements CoveoStreamServiceStrategy {
    private static final String FAILED_TO_INDEX = "Failed to index ";
    private static final String SNDOCUMENT_WITH_ID = "SnDocument with id ";

    private static final Logger LOG = Logger.getLogger(CoveoProductStreamServiceStrategy.class);

    List<SnLanguage> languages;
    List<SnCurrency> currencies;
    List<CoveoSnCountry> countries;
    List<T> streamServices;

    private final ConfigurationService configurationService;
    private final CommonI18NService commonI18NService;

    public CoveoProductStreamServiceStrategy(List<SnLanguage> languages, List<SnCurrency> currencies,
                                             List<CoveoSnCountry> countries, List<T> incomingStreamServices,
                                             ConfigurationService configurationService, CommonI18NService commonI18NService, Boolean singleSourceEnabled) {
        this.languages = languages;
        this.currencies = currencies;
        this.countries = countries;
        this.streamServices = new ArrayList<>();
        incomingStreamServices.forEach(streamService -> {
            CoveoSource coveoSource = streamService.getCoveoSource();
            if (coveoSource.getObjectType().equals(CoveoCatalogObjectType.PRODUCTANDVARIANT)) {
                if (LOG.isDebugEnabled()) LOG.debug("Adding stream service based on source " + coveoSource.getId());
                streamServices.add(streamService);
            }
        });
        if (Boolean.TRUE.equals(singleSourceEnabled) && streamServices.size() > 1) {
            throw new SnRuntimeException("Single source mode is enabled but multiple sources have been configured");
        }
        this.configurationService = configurationService;
        this.commonI18NService = commonI18NService;
    }

    @Override
    public List<SnDocumentBatchOperationResponse> pushDocuments(List<SnDocumentBatchOperationRequest> documents,
                                                                Boolean singleSourceEnabled) {
        Map<String, SnDocumentBatchOperationResponse> responseMap = new HashMap<>();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Streaming Documents");
        }
        int logIntervalPercentage = configurationService.getConfiguration().getInt(COVEO_PRODUCT_STREAM_LOG_INTERVAL_PERCENTAGE, COVEO_PRODUCT_STREAM_LOG_INTERVAL_PERCENTAGE_DEFAULT);
        if (logIntervalPercentage < 0 || logIntervalPercentage > 100) {
            LOG.warn("Log interval percentage is out of range (0-100%). Using default of " + COVEO_PRODUCT_STREAM_LOG_INTERVAL_PERCENTAGE_DEFAULT + "%");
            logIntervalPercentage = COVEO_PRODUCT_STREAM_LOG_INTERVAL_PERCENTAGE_DEFAULT;
        }
        for (T streamService : streamServices) {
            CoveoSource source = streamService.getCoveoSource();
            if (Boolean.FALSE.equals(singleSourceEnabled) && !isSourceConfiguredForJob(source)) {
                continue;
            }
            int totalDocumentsCount = documents.size();
            int logInterval = (int) Math.ceil(totalDocumentsCount * (logIntervalPercentage / 100.0));
            LOG.info(String.format("Streaming %s documents for source %s", totalDocumentsCount, source.getId()));
            processDocumentsForSource(documents, responseMap, streamService, source, singleSourceEnabled, logInterval, totalDocumentsCount);
        }
        LOG.info(String.format("Finished streaming %s documents", responseMap.size()));
        return new ArrayList<>(responseMap.values());

    }

    private void processDocumentsForSource(List<SnDocumentBatchOperationRequest> documents,
                                           Map<String, SnDocumentBatchOperationResponse> responseMap,
                                           T streamService, CoveoSource source, Boolean singleSourceEnabled,
                                           int logInterval, int totalDocumentsCount) {
        for (int documentIndex = 1; documentIndex <= totalDocumentsCount; documentIndex++) {
            SnDocumentBatchOperationRequest request = documents.get(documentIndex - 1);
            SnDocumentBatchOperationResponse documentBatchOperationResponse = new SnDocumentBatchOperationResponse();
            documentBatchOperationResponse.setId(request.getDocument().getId());
            streamDocument(request, source.getLanguage(), source.getCurrency(), source.getCountry(), streamService, singleSourceEnabled);
            documentBatchOperationResponse.setStatus(SnDocumentOperationStatus.UPDATED);
            responseMap.putIfAbsent(documentBatchOperationResponse.getId(), documentBatchOperationResponse);
            if (logInterval != 0 && documentIndex % logInterval == 0) {
                LOG.info(String.format("Processed %s of %s documents", documentIndex, totalDocumentsCount));
            }
        }
    }

    private boolean isSourceConfiguredForJob(CoveoSource source) {
        return languages.contains(source.getLanguage()) && currencies.contains(source.getCurrency()) && countries.contains(source.getCountry());
    }

    private void streamDocument(SnDocumentBatchOperationRequest request, SnLanguage language, SnCurrency currency,
                            CoveoSnCountry country, T streamService, Boolean singleSourceEnabled) {

        if (Boolean.FALSE.equals(singleSourceEnabled) && !isApplicableForCountry(request, language, currency,
                                                                                 country)) {
            return;
        }

        // Use a dedicated lock object for synchronization instead of the method parameter
        Object lock = streamService.getClass();
        Gson gson = new Gson();
        synchronized (lock) {
            DocumentBuilder coveoDocument = createCoveoDocument(request.getDocument(), language, currency, singleSourceEnabled);
            if (coveoDocument != null) {
                try {
                    if (LOG.isDebugEnabled()) {
                        JsonObject jsonDocument = gson.toJsonTree(coveoDocument.getDocument()).getAsJsonObject();
                        LOG.debug("Pushing document: " + jsonDocument.toString());
                    }
                    streamService.pushDocument(coveoDocument);
                } catch (IOException exception) {
                    LOG.error(FAILED_TO_INDEX + request.getDocument().getId(), exception);
                } catch (InterruptedException exception) {
                    LOG.error(FAILED_TO_INDEX + request.getDocument().getId(), exception);
                    Thread.currentThread().interrupt();
                }
            } else {
                LOG.error(FAILED_TO_INDEX + request.getDocument().getId());
            }
        }
    }

    protected boolean isApplicableForCountry(SnDocumentBatchOperationRequest request, SnLanguage language,
                                                    SnCurrency currency, CoveoSnCountry country) {
        Object authorizedCountries = CoveoFieldValueResolverUtils.resolveFieldValue(
                "coveoAuthorizedCountries",
                request.getDocument().getFields(),
                Locale.forLanguageTag(language.getId()),
                Currency.getInstance(currency.getId())
        );

        // skip as default behavior is to send to all sources
        if (authorizedCountries == null) {
            return true;
        }

        if (!(authorizedCountries instanceof Collection<?> countriesToCheck)) {
            LOG.warn("Document " + request.getDocument().getId() + " has an invalid coveoAuthorizedCountries field. This must be a collection of CountryModel objects.");
            return true;
        }
        if (countriesToCheck.isEmpty()) {
            return true;
        }
        for (Object countryToCheck : countriesToCheck) {
            if (countryToCheck instanceof CountryModel countryModel) {
                if (countryModel.getIsocode().equalsIgnoreCase(country.getId())) {
                    return true;
                }
            } else {
                LOG.warn(String.format("Document %s has an invalid country object %s in the coveoAuthorizedCountries field. This must be a CountryModel object.",
                        request.getDocument().getId(), countryToCheck));
            }
        }

        LOG.debug("Document " + request.getDocument().getId() + " is not authorized for country " + country.getId());
        return false;
    }

    private DocumentBuilder createCoveoDocument(SnDocument document, SnLanguage snLanguage, SnCurrency snCurrency,
                                                Boolean singleSourceEnabled) {

        Locale locale = (snLanguage != null) ? commonI18NService.getLocaleForIsoCode(snLanguage.getId()) : null;
        Currency currency = (snCurrency != null) ? Currency.getInstance(snCurrency.getId()) : null;

        Map<String, Object> documentFields = document.getFields();
        String documentId = (String) CoveoFieldValueResolverUtils.resolveFieldValue(COVEO_DOCUMENT_ID_INDEX_ATTRIBUTE, documentFields, locale, currency);
        // If the value is still blank at this point we are unable to build the document
        if (StringUtils.isBlank(documentId)) {
            LOG.warn(SNDOCUMENT_WITH_ID + document.getId() + " does not have a " + COVEO_DOCUMENT_ID_INDEX_ATTRIBUTE + " field, will not push this document");
            return null;
        }

        Object nameFieldValue = CoveoFieldValueResolverUtils.resolveFieldValue("name", documentFields, locale, currency);
        String documentName;
        // In the case where singleSourceEnabled is true, this will be a map.
        // If the coveoMultiMarketSingleSourceDocumentTitleSnIndexerValueProvider
        // has been configured correctly, this map will contain a single entry.
        // The default is to take the first localised value and use that for the title.
        if (nameFieldValue instanceof Map<?, ?> map && !map.isEmpty()) {
            Object firstValue = map.values().iterator().next();
            documentName = firstValue != null ? firstValue.toString() : null;
        } else {
            documentName = nameFieldValue != null ? nameFieldValue.toString() : null;
        }

        // If the value is still blank at this point we are unable to build the document
        if (StringUtils.isBlank(documentName)) {
            LOG.warn(SNDOCUMENT_WITH_ID + document.getId() + " does not have a name field, will not push this document");
            return null;
        }

        Map<String, Object> values = getDocumentValues(document,
                                                       singleSourceEnabled,
                                                       locale,
                                                       currency,
                                                       documentId,
                                                       documentFields);
        DocumentBuilder documentBuilder = new DocumentBuilder(documentId, documentName).withMetadata(values);

        String coveoClickableUri = (String) CoveoFieldValueResolverUtils.resolveFieldValue(COVEO_URI_TYPE_INDEX_ATTRIBUTE, documentFields, locale, currency);
        if (!StringUtils.isBlank(coveoClickableUri)) {
            documentBuilder.withClickableUri(coveoClickableUri);
        }
        return documentBuilder;
    }

    @NonNull
    private static Map<String, Object> getDocumentValues(SnDocument document,
                                                         Boolean singleSourceEnabled,
                                                         Locale locale,
                                                         Currency currency,
                                                         String documentId,
                                                         Map<String, Object> documentFields)
    {
        Map<String, Object> fields = document.getFields();
        Map<String, Object> values = new HashMap<>();
        for (Map.Entry<String, Object> field : fields.entrySet()) {
            Object fieldValue;
            if (Boolean.TRUE.equals(singleSourceEnabled)) {
                fieldValue = field.getValue();
            } else {
                fieldValue = CoveoFieldValueResolverUtils.resolveFieldValue(field.getValue(), locale, currency);
            }
            if (fieldValue != null && !Objects.equals(fieldValue, "")) {
                values.put(field.getKey(), fieldValue);
            } else if (LOG.isDebugEnabled()) {
                LOG.debug("Field " + field.getKey() + " is empty or null, will not push this field for document " + documentId);
            }
        }
        addEcProductIdToValues(document, documentFields, locale, currency, values);
        return values;
    }

    protected static void addEcProductIdToValues(SnDocument document, Map<String, Object> documentFields, Locale locale, Currency currency, Map<String, Object> values) {
        String documentCode = (String) CoveoFieldValueResolverUtils.resolveFieldValue("code", documentFields, locale, currency);
        if (StringUtils.isNotBlank(documentCode)) {
            values.put("ec_product_id", documentCode);
        } else {
            LOG.warn(SNDOCUMENT_WITH_ID + document.getId() + " does not have a code field, will not push ec_product_id field");
        }
    }

    @Override
    public void closeServices() throws NoOpenStreamException, IOException, InterruptedException, NoOpenFileContainerException {
        if (LOG.isDebugEnabled()) LOG.debug("Closing stream services");
        for (T streamService : streamServices) {
            streamService.closeStream();
        }
    }
}
