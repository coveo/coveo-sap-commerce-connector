package com.coveo.stream.service.impl;

import com.coveo.indexer.service.impl.CoveoObjectTypeSnIndexerValueProvider;
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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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

@RunWith(MockitoJUnitRunner.class)
@UnitTest
public class CoveoProductStreamServiceStrategyTest {

    private static final String LANG_EN = "en";
    private static final String LANG_FR = "fr";
    private static final String LANG_DE = "de";
    private static final String CURRENCY_USD = "USD";
    private static final String CURRENCY_EUR = "EUR";
    private static final String COUNTRY_US = "US";
    private static final String COUNTRY_FR = "FR";
    private static final String COUNTRY_DE = "DE";

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

    CoveoProductStreamServiceStrategy<CoveoAbstractStreamService<Object>> coveoProductStreamServiceStrategy;

    @Before
    public void setUp() {
        when(snLanguageEn.getId()).thenReturn(LANG_EN);
        when(snLanguageFr.getId()).thenReturn(LANG_FR);
        when(snLanguageDe.getId()).thenReturn(LANG_DE);
        when(snCurrencyUsd.getId()).thenReturn(CURRENCY_USD);
        when(snCurrencyEur.getId()).thenReturn(CURRENCY_EUR);

        when(coveoSourceUS.getLanguage()).thenReturn(snLanguageEn);
        when(coveoSourceUS.getCurrency()).thenReturn(snCurrencyUsd);
        when(coveoSourceUS.getCountry()).thenReturn(coveoSnCountryUs);
        when(coveoSourceFR.getLanguage()).thenReturn(snLanguageFr);
        when(coveoSourceFR.getCurrency()).thenReturn(snCurrencyEur);
        when(coveoSourceFR.getCountry()).thenReturn(coveoSnCountryFr);
        when(coveoSourceDE.getLanguage()).thenReturn(snLanguageDe);
        when(coveoSourceDE.getCurrency()).thenReturn(snCurrencyEur);
        when(coveoSourceDE.getCountry()).thenReturn(coveoSnCountryDe);

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

        coveoProductStreamServiceStrategy = new CoveoProductStreamServiceStrategy<>(languages, currencies, countries, streamServices);
    }

    @Test
    public void testPushDocuments() throws IOException, InterruptedException {
        List<SnDocumentBatchOperationRequest> documents = new ArrayList<>();
        SnDocumentBatchOperationRequest documentA = new SnDocumentBatchOperationRequest();
        documentA.setDocument(createDocumentFields("nameA", "codeA", CoveoObjectTypeSnIndexerValueProvider.PRODUCT_VARIANT_TYPE));
        SnDocumentBatchOperationRequest documentB = new SnDocumentBatchOperationRequest();
        documentB.setDocument(createDocumentFields("nameB", "codeB", CoveoObjectTypeSnIndexerValueProvider.PRODUCT_VARIANT_TYPE));
        documents.add(documentA);
        documents.add(documentB);
        coveoProductStreamServiceStrategy.pushDocuments(documents);
        verify(coveoAbstractStreamServiceUS, times(2)).pushDocument(any());
        verify(coveoAbstractStreamServiceFR, times(2)).pushDocument(any());
        verify(coveoAbstractStreamServiceDE, times(2)).pushDocument(any());
        verify(coveoAbstractStreamServiceAvailability, times(0)).pushDocument(any());
    }

    @Test
    public void testPushDocuments_MissingOneName() throws IOException, InterruptedException {
        List<SnDocumentBatchOperationRequest> documents = new ArrayList<>();
        SnDocumentBatchOperationRequest documentA = new SnDocumentBatchOperationRequest();
        documentA.setDocument(createDocumentFields("nameA", "codeA", CoveoObjectTypeSnIndexerValueProvider.PRODUCT_VARIANT_TYPE));
        SnDocumentBatchOperationRequest documentB = new SnDocumentBatchOperationRequest();
        documentB.setDocument(createDocumentFields("", "codeB", CoveoObjectTypeSnIndexerValueProvider.PRODUCT_OBJECT_TYPE));
        documents.add(documentA);
        documents.add(documentB);
        coveoProductStreamServiceStrategy.pushDocuments(documents);
        verify(coveoAbstractStreamServiceUS, times(1)).pushDocument(any());
        verify(coveoAbstractStreamServiceFR, times(1)).pushDocument(any());
        verify(coveoAbstractStreamServiceDE, times(1)).pushDocument(any());
        verify(coveoAbstractStreamServiceAvailability, times(0)).pushDocument(any());
    }

    @Test
    public void testPushDocuments_MissingOneCode() throws IOException, InterruptedException {
        List<SnDocumentBatchOperationRequest> documents = new ArrayList<>();
        SnDocumentBatchOperationRequest documentA = new SnDocumentBatchOperationRequest();
        documentA.setDocument(createDocumentFields("nameA", "codeA", CoveoObjectTypeSnIndexerValueProvider.PRODUCT_VARIANT_TYPE));
        SnDocumentBatchOperationRequest documentB = new SnDocumentBatchOperationRequest();
        documentB.setDocument(createDocumentFields("nameB", "", CoveoObjectTypeSnIndexerValueProvider.PRODUCT_OBJECT_TYPE));
        documents.add(documentA);
        documents.add(documentB);
        coveoProductStreamServiceStrategy.pushDocuments(documents);
        verify(coveoAbstractStreamServiceUS, times(1)).pushDocument(any());
        verify(coveoAbstractStreamServiceFR, times(1)).pushDocument(any());
        verify(coveoAbstractStreamServiceDE, times(1)).pushDocument(any());
        verify(coveoAbstractStreamServiceAvailability, times(0)).pushDocument(any());
    }

    @Test
    public void testCloseServices() throws NoOpenStreamException, IOException, NoOpenFileContainerException, InterruptedException {
        coveoProductStreamServiceStrategy.closeServices();
        verify(coveoAbstractStreamServiceUS, times(1)).closeStream();
        verify(coveoAbstractStreamServiceFR, times(1)).closeStream();
        verify(coveoAbstractStreamServiceDE, times(1)).closeStream();
        verify(coveoAbstractStreamServiceAvailability, times(0)).closeStream();
    }

    private SnDocument createDocumentFields(String name, String code, String objectType) {
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
        SnField objectTypeField = new SnField();
        objectTypeField.setId("objectType");
        objectTypeField.setLocalized(false);
        snDocument.setFieldValue(objectTypeField, objectType);
        SnField coveoDocumentIdField = new SnField();
        coveoDocumentIdField.setId("coveoDocumentId");
        coveoDocumentIdField.setLocalized(false);
        snDocument.setFieldValue(coveoDocumentIdField, code);

        return snDocument;
    }
}