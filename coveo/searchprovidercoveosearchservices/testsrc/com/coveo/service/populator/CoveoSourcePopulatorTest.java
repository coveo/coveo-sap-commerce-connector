package com.coveo.service.populator;

import com.coveo.enums.CoveoCatalogSourceObjectType;
import com.coveo.model.CoveoSourceModel;
import com.coveo.searchservices.admin.data.CoveoSnCountry;
import com.coveo.searchservices.data.CoveoCatalogObjectType;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.apiregistryservices.model.ConsumedDestinationModel;
import de.hybris.platform.apiregistryservices.model.ConsumedOAuthCredentialModel;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.searchservices.admin.data.SnCurrency;
import de.hybris.platform.searchservices.admin.data.SnLanguage;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@UnitTest
public class CoveoSourcePopulatorTest {

    private static final String SECRET = "4321";
    private static final String URL = "http://www.example.com";
    private static final String ID = "1234";
    private static final String LANGUAGE = "en";
    private static final String COUNTRY = "CA";
    private static final String CURRENCY = "USD";

    SnLanguage snLanguage = new SnLanguage();
    CoveoSnCountry coveoSnCountry = new CoveoSnCountry();
    SnCurrency snCurrency = new SnCurrency();

    CoveoSourceModel coveoSourceModel;

    @Mock
    Converter<LanguageModel, SnLanguage> languageConverter;
    @Mock
    Converter<CountryModel, CoveoSnCountry> countryConverter;
    @Mock
    Converter<CurrencyModel, SnCurrency> currencyConverter;

    @InjectMocks
    CoveoSourcePopulator coveoSourcePopulator = new CoveoSourcePopulator();

    @BeforeEach
    void setUp() {
        snLanguage.setId(LANGUAGE);
        when(languageConverter.convert(any())).thenReturn(snLanguage);
        coveoSnCountry.setId(COUNTRY);
        when(countryConverter.convert(any())).thenReturn(coveoSnCountry);
        snCurrency.setId(CURRENCY);
        when(currencyConverter.convert(any())).thenReturn(snCurrency);

        coveoSourceModel = new CoveoSourceModel();
        coveoSourceModel.setLanguage(new LanguageModel());
        coveoSourceModel.setCurrency(new CurrencyModel());
        coveoSourceModel.setCountry(new CountryModel());
        ConsumedDestinationModel consumedDestinationModel = new ConsumedDestinationModel();
        ConsumedOAuthCredentialModel consumedOAuthCredentialModel = new ConsumedOAuthCredentialModel();
        consumedOAuthCredentialModel.setClientSecret(SECRET);
        consumedDestinationModel.setCredential(consumedOAuthCredentialModel);
        consumedDestinationModel.setId(ID);
        consumedDestinationModel.setUrl(URL);
        coveoSourceModel.setConsumedDestination(consumedDestinationModel);
        coveoSourceModel.setObjectType(CoveoCatalogSourceObjectType.AVAILABILITY);
    }

    @Test
    void testPopulate() {
        com.coveo.searchservices.data.CoveoSource coveoSource = new com.coveo.searchservices.data.CoveoSource();
        coveoSourcePopulator.populate(coveoSourceModel, coveoSource);
        assertEquals(LANGUAGE, coveoSource.getLanguage().getId());
        assertEquals(COUNTRY, coveoSource.getCountry().getId());
        assertEquals(CURRENCY, coveoSource.getCurrency().getId());
        assertEquals(ID, coveoSource.getDestinationId());
        assertEquals(SECRET, coveoSource.getDestinationSecret());
        assertEquals(URL, coveoSource.getDestinationTargetUrl());
        assertEquals(CoveoCatalogObjectType.AVAILABILITY, coveoSource.getObjectType());
    }
}