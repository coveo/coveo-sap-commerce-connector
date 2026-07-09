package com.coveo.stream.service.impl;

import com.coveo.constants.SearchprovidercoveosearchservicesConstants;
import com.coveo.indexer.service.impl.CoveoObjectTypeSnIndexerValueProvider;
import com.coveo.pushapiclient.DocumentBuilder;
import com.coveo.searchservices.admin.data.CoveoSnCountry;
import com.coveo.searchservices.data.CoveoCatalogObjectType;
import com.coveo.searchservices.data.CoveoSource;
import com.coveo.stream.service.CoveoAbstractStreamService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.searchservices.admin.data.SnCurrency;
import de.hybris.platform.searchservices.admin.data.SnField;
import de.hybris.platform.searchservices.admin.data.SnLanguage;
import de.hybris.platform.searchservices.core.SnRuntimeException;
import de.hybris.platform.searchservices.document.data.SnDocument;
import de.hybris.platform.searchservices.document.data.SnDocumentBatchOperationRequest;
import de.hybris.platform.searchservices.document.data.SnDocumentBatchOperationResponse;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import org.apache.commons.configuration.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@UnitTest
class CoveoProductStreamServiceStrategySingleSourceTest {

    private static final String LANG_EN = "en";
    private static final String LANG_FR = "fr";
    private static final String LANG_DE = "de";

    @Mock
    SnLanguage snLanguageEn;
    @Mock
    SnLanguage snLanguageFr;
    @Mock
    SnLanguage snLanguageDe;
    @Mock
    SnCurrency snCurrencyUsd;
    @Mock
    SnCurrency snCurrencyEur;
    @Mock
    CoveoSnCountry coveoSnCountryUs;
    @Mock
    CoveoSnCountry coveoSnCountryFr;
    @Mock
    CoveoSnCountry coveoSnCountryDe;

    @Mock
    CoveoSource firstSource;
    @Mock
    CoveoSource secondSource;
    @Mock
    CoveoAbstractStreamService<Object> firstAbstractStreamService;
    @Mock
    CoveoAbstractStreamService<Object> secondAbstractStreamService;
    @Mock
    ConfigurationService configurationService;
    @Mock
    CommonI18NService commonI18NService;
    @Mock
    private Configuration configuration;

    ArgumentCaptor<DocumentBuilder> captor = ArgumentCaptor.forClass(DocumentBuilder.class);

    CoveoProductStreamServiceStrategy<CoveoAbstractStreamService<Object>> coveoProductStreamServiceStrategy;

    @BeforeEach
    void setUp() {
        when(firstSource.getObjectType()).thenReturn(CoveoCatalogObjectType.PRODUCTANDVARIANT);
        when(firstAbstractStreamService.getCoveoSource()).thenReturn(firstSource);

        List<CoveoAbstractStreamService<Object>> streamServices = new ArrayList<>();

        List<SnLanguage> languages = new ArrayList<>();
        List<SnCurrency> currencies = new ArrayList<>();
        List<CoveoSnCountry> countries = new ArrayList<>();
        languages.add(snLanguageEn);
        languages.add(snLanguageFr);
        languages.add(snLanguageDe);
        currencies.add(snCurrencyUsd);
        currencies.add(snCurrencyEur);
        countries.add(coveoSnCountryUs);
        countries.add(coveoSnCountryFr);
        countries.add(coveoSnCountryDe);

        streamServices.add(firstAbstractStreamService);

        coveoProductStreamServiceStrategy = new CoveoProductStreamServiceStrategy<>(languages, currencies, countries, streamServices, configurationService, commonI18NService, Boolean.FALSE);
    }

    @Test
    void ifSingleSourceConfigured_WhenMultipleSourcesConfigured_ThrowsSnRuntimeException()
    {
        when(secondSource.getObjectType()).thenReturn(CoveoCatalogObjectType.PRODUCTANDVARIANT);
        when(secondAbstractStreamService.getCoveoSource()).thenReturn(secondSource);

        List<CoveoAbstractStreamService<Object>> streamServices = new ArrayList<>();
        streamServices.add(firstAbstractStreamService);
        streamServices.add(secondAbstractStreamService);
        Exception exception = assertThrows(
                SnRuntimeException.class,
                () -> createStrategyWithSingleSourceEnabled(streamServices)
        );
        assertTrue(exception.getMessage().contains("Single source mode is enabled but multiple sources have been configured"));
    }

    private void createStrategyWithSingleSourceEnabled(List<CoveoAbstractStreamService<Object>> streamServices) {
        new CoveoProductStreamServiceStrategy<>(
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                streamServices,
                configurationService,
                commonI18NService,
                Boolean.TRUE
        );
    }

    @Test
    void testPushDocuments() throws IOException, InterruptedException
    {
        when(configurationService.getConfiguration()).thenReturn(configuration);
        when(configuration.getInt(SearchprovidercoveosearchservicesConstants.COVEO_PRODUCT_STREAM_LOG_INTERVAL_PERCENTAGE, SearchprovidercoveosearchservicesConstants.COVEO_PRODUCT_STREAM_LOG_INTERVAL_PERCENTAGE_DEFAULT)).thenReturn(50);

        List<SnDocumentBatchOperationRequest> documents = new ArrayList<>();
        SnDocumentBatchOperationRequest documentA = new SnDocumentBatchOperationRequest();
        documentA.setDocument(createDocumentFields("nameA", "codeA"));
        SnDocumentBatchOperationRequest documentB = new SnDocumentBatchOperationRequest();
        documentB.setDocument(createDocumentFields("nameB", "codeB"));
        SnDocumentBatchOperationRequest documentC = new SnDocumentBatchOperationRequest();
        documentC.setDocument(createDocumentFields("nameC", "codeC"));
        documents.add(documentA);
        documents.add(documentB);
        documents.add(documentC);

        List<SnDocumentBatchOperationResponse> responses = coveoProductStreamServiceStrategy.pushDocuments(documents, Boolean.TRUE);
        assertEquals(documents.size(), responses.size());
        verify(firstAbstractStreamService, atLeastOnce()).pushDocument(captor.capture());
        List<DocumentBuilder> allPushedDocs = captor.getAllValues();
        assertEquals(documents.size(), allPushedDocs.size());
        for (DocumentBuilder docBuilder : allPushedDocs) {
            Map<String, Object> metadata = docBuilder.getDocument().metadata;
            assertTrue(metadata.containsKey("name"), "Metadata should contain 'name' key");
            Object nameValue = metadata.get("name");
            assertInstanceOf(Map.class, nameValue, "The value for 'name' should be a map");
            Map<?, ?> nameMap = (Map<?, ?>) nameValue;
            assertEquals(3, nameMap.size(), "The 'name' map should have 3 entries");
            assertTrue(nameMap.containsKey(Locale.forLanguageTag(LANG_EN)), "The 'name' map should contain LANG_EN key");
            assertTrue(nameMap.containsKey(Locale.forLanguageTag(LANG_FR)), "The 'name' map should contain LANG_FR key");
            assertTrue(nameMap.containsKey(Locale.forLanguageTag(LANG_DE)), "The 'name' map should contain LANG_DE key");
        }
    }

    private SnDocument createDocumentFields(String name, String code) {
        Map<Locale, Object> localizedName = new HashMap<>();
        SnDocument snDocument = new SnDocument();
        localizedName.put(Locale.forLanguageTag(LANG_EN), name);
        localizedName.put(Locale.forLanguageTag(LANG_FR), name);
        localizedName.put(Locale.forLanguageTag(LANG_DE), name);
        SnField nameField = new SnField();
        nameField.setId("name");
        nameField.setLocalized(true);
        snDocument.setFieldValue(nameField, localizedName);
        SnField codeField = new SnField();
        codeField.setId("code");
        codeField.setLocalized(false);
        snDocument.setFieldValue(codeField, code);
        snDocument.setId(code);
        SnField objectTypeField = new SnField();
        objectTypeField.setId("objectType");
        objectTypeField.setLocalized(false);
        snDocument.setFieldValue(objectTypeField, CoveoObjectTypeSnIndexerValueProvider.PRODUCT_VARIANT_TYPE);
        SnField coveoDocumentIdField = new SnField();
        coveoDocumentIdField.setId("coveoDocumentId");
        coveoDocumentIdField.setLocalized(false);
        snDocument.setFieldValue(coveoDocumentIdField, code);
        return snDocument;
    }
}
