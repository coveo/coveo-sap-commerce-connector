package com.coveo.stream.service.impl;

import com.coveo.constants.SearchprovidercoveosearchservicesConstants;
import com.coveo.pushapiclient.exceptions.NoOpenFileContainerException;
import com.coveo.pushapiclient.exceptions.NoOpenStreamException;
import com.coveo.searchservices.admin.data.CoveoSnCountry;
import com.coveo.searchservices.data.CoveoCatalogObjectType;
import com.coveo.searchservices.data.CoveoSource;
import com.coveo.stream.service.CoveoAbstractStreamService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.searchservices.admin.data.SnCurrency;
import de.hybris.platform.searchservices.admin.data.SnField;
import de.hybris.platform.searchservices.admin.data.SnLanguage;
import de.hybris.platform.searchservices.document.data.SnDocument;
import de.hybris.platform.searchservices.document.data.SnDocumentBatchOperationRequest;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.commons.configuration.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@UnitTest
public class CoveoAvailabilityStreamServiceStrategyTest {
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
    CoveoSource coveoSourceUS;
    @Mock
    CoveoSource coveoSourceFR;
    @Mock
    CoveoSource coveoSourceDE;
    @Mock
    CoveoSource coveoSourceAvailability;

    @Mock
    CoveoAbstractStreamService<Object> coveoAbstractStreamServiceUS;
    @Mock
    CoveoAbstractStreamService<Object> coveoAbstractStreamServiceFR;
    @Mock
    CoveoAbstractStreamService<Object> coveoAbstractStreamServiceDE;
    @Mock
    CoveoAbstractStreamService<Object> coveoAbstractStreamServiceAvailability;
    @Mock
    ConfigurationService configurationService;
    @Mock
    private Configuration configuration;

    CoveoAvailabilityStreamServiceStrategy<CoveoAbstractStreamService<Object>> coveoAvailabilityStreamServiceStrategy;

    @BeforeEach
    public void setUp() {
        when(coveoSourceUS.getObjectType()).thenReturn(CoveoCatalogObjectType.PRODUCTANDVARIANT);
        when(coveoSourceFR.getObjectType()).thenReturn(CoveoCatalogObjectType.PRODUCTANDVARIANT);
        when(coveoSourceDE.getObjectType()).thenReturn(CoveoCatalogObjectType.PRODUCTANDVARIANT);
        when(coveoSourceAvailability.getObjectType()).thenReturn(CoveoCatalogObjectType.AVAILABILITY);

        when(coveoAbstractStreamServiceUS.getCoveoSource()).thenReturn(coveoSourceUS);
        when(coveoAbstractStreamServiceFR.getCoveoSource()).thenReturn(coveoSourceFR);
        when(coveoAbstractStreamServiceDE.getCoveoSource()).thenReturn(coveoSourceDE);
        when(coveoAbstractStreamServiceAvailability.getCoveoSource()).thenReturn(coveoSourceAvailability);

        List<SnLanguage> languages = new ArrayList<>();
        List<SnCurrency> currencies = new ArrayList<>();
        List<CoveoSnCountry> countries = new ArrayList<>();
        List<CoveoAbstractStreamService<Object>> streamServices = new ArrayList<>();

        languages.add(snLanguageEn);
        languages.add(snLanguageFr);
        languages.add(snLanguageDe);
        currencies.add(snCurrencyUsd);
        currencies.add(snCurrencyEur);
        countries.add(coveoSnCountryUs);
        countries.add(coveoSnCountryFr);
        countries.add(coveoSnCountryDe);
        streamServices.add(coveoAbstractStreamServiceUS);
        streamServices.add(coveoAbstractStreamServiceFR);
        streamServices.add(coveoAbstractStreamServiceDE);
        streamServices.add(coveoAbstractStreamServiceAvailability);

        coveoAvailabilityStreamServiceStrategy = new CoveoAvailabilityStreamServiceStrategy<>(streamServices, configurationService);
    }

    @Test
    public void testPushDocuments_MissingOneName() throws IOException, InterruptedException {
        when(configurationService.getConfiguration()).thenReturn(configuration);
        when(configuration.getInt(SearchprovidercoveosearchservicesConstants.COVEO_PRODUCT_STREAM_LOG_INTERVAL_PERCENTAGE)).thenReturn(0);

        List<SnDocumentBatchOperationRequest> documents = new ArrayList<>();
        SnDocumentBatchOperationRequest documentA = new SnDocumentBatchOperationRequest();
        documentA.setDocument(createDocumentFields("nameA", "codeA"));
        SnDocumentBatchOperationRequest documentB = new SnDocumentBatchOperationRequest();
        documentB.setDocument(createDocumentFields("", "codeB"));
        documents.add(documentA);
        documents.add(documentB);
        coveoAvailabilityStreamServiceStrategy.pushDocuments(documents);
        verify(coveoAbstractStreamServiceUS, times(0)).pushDocument(any());
        verify(coveoAbstractStreamServiceFR, times(0)).pushDocument(any());
        verify(coveoAbstractStreamServiceDE, times(0)).pushDocument(any());
        verify(coveoAbstractStreamServiceAvailability, times(1)).pushDocument(any());
    }

    @Test
    public void testPushDocuments_MissingOneCode() throws IOException, InterruptedException {
        when(configurationService.getConfiguration()).thenReturn(configuration);
        when(configuration.getInt(SearchprovidercoveosearchservicesConstants.COVEO_PRODUCT_STREAM_LOG_INTERVAL_PERCENTAGE)).thenReturn(0);

        List<SnDocumentBatchOperationRequest> documents = new ArrayList<>();
        SnDocumentBatchOperationRequest documentA = new SnDocumentBatchOperationRequest();
        documentA.setDocument(createDocumentFields("nameA", "codeA"));
        SnDocumentBatchOperationRequest documentB = new SnDocumentBatchOperationRequest();
        documentB.setDocument(createDocumentFields("nameB", ""));
        documents.add(documentA);
        documents.add(documentB);
        coveoAvailabilityStreamServiceStrategy.pushDocuments(documents);
        verify(coveoAbstractStreamServiceUS, times(0)).pushDocument(any());
        verify(coveoAbstractStreamServiceFR, times(0)).pushDocument(any());
        verify(coveoAbstractStreamServiceDE, times(0)).pushDocument(any());
        verify(coveoAbstractStreamServiceAvailability, times(1)).pushDocument(any());
    }

    @Test
    public void testPushDocuments() throws IOException, InterruptedException {
        when(configurationService.getConfiguration()).thenReturn(configuration);
        when(configuration.getInt(SearchprovidercoveosearchservicesConstants.COVEO_PRODUCT_STREAM_LOG_INTERVAL_PERCENTAGE)).thenReturn(0);

        List<SnDocumentBatchOperationRequest> documents = new ArrayList<>();
        SnDocumentBatchOperationRequest documentA = new SnDocumentBatchOperationRequest();
        documentA.setDocument(createDocumentFields("nameA", "codeA"));

        SnDocumentBatchOperationRequest documentB = new SnDocumentBatchOperationRequest();
        documentB.setDocument(createDocumentFields("nameB", "codeB"));
        documents.add(documentA);
        documents.add(documentB);
        coveoAvailabilityStreamServiceStrategy.pushDocuments(documents);
        verify(coveoAbstractStreamServiceUS, times(0)).pushDocument(any());
        verify(coveoAbstractStreamServiceFR, times(0)).pushDocument(any());
        verify(coveoAbstractStreamServiceDE, times(0)).pushDocument(any());
        verify(coveoAbstractStreamServiceAvailability, times(2)).pushDocument(any());
    }

    @Test
    public void testCloseServices() throws NoOpenStreamException, IOException, NoOpenFileContainerException, InterruptedException {
        coveoAvailabilityStreamServiceStrategy.closeServices();
        verify(coveoAbstractStreamServiceUS, times(0)).closeStream();
        verify(coveoAbstractStreamServiceFR, times(0)).closeStream();
        verify(coveoAbstractStreamServiceDE, times(0)).closeStream();
        verify(coveoAbstractStreamServiceAvailability, times(1)).closeStream();
    }

    private SnDocument createDocumentFields(String name, String code) {
        Map<Locale, Object> localizedName = new HashMap<>();
        SnDocument snDocument = new SnDocument();
        localizedName.put(new Locale(LANG_EN), name);
        localizedName.put(new Locale(LANG_FR), name);
        localizedName.put(new Locale(LANG_DE), name);
        SnField nameField = new SnField();
        nameField.setId("name");
        nameField.setLocalized(true);
        snDocument.setFieldValue(nameField, localizedName);
        SnField codeField = new SnField();
        codeField.setId("code");
        codeField.setLocalized(false);
        snDocument.setFieldValue(codeField, code);
        snDocument.setId(code);
        SnField coveoDocumentIdField = new SnField();
        coveoDocumentIdField.setId("coveoDocumentId");
        coveoDocumentIdField.setLocalized(false);
        snDocument.setFieldValue(coveoDocumentIdField, code);
        snDocument.setId(code);
        return snDocument;
    }
}