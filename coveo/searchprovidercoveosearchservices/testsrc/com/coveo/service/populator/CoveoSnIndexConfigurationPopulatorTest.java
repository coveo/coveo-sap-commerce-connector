package com.coveo.service.populator;

import com.coveo.model.CoveoSnIndexConfigurationModel;
import com.coveo.searchservices.admin.data.CoveoSnCountry;
import com.coveo.searchservices.admin.data.CoveoSnIndexConfiguration;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.searchservices.model.SnIndexConfigurationModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@UnitTest
public class CoveoSnIndexConfigurationPopulatorTest {

    private static final String COUNTRY_CA = "CA";
    private static final String COUNTRY_US = "US";

    CoveoSnCountry coveoSnCountryCa = new CoveoSnCountry();
    CoveoSnCountry coveoSnCountryUs = new CoveoSnCountry();

    @Mock
    CoveoSnIndexConfigurationModel coveoSnIndexConfigurationModel;

    @Mock
    Converter<CountryModel, CoveoSnCountry> countryConverter;

    @InjectMocks
    CoveoSnIndexConfigurationPopulator coveoSnIndexConfigurationPopulator = new CoveoSnIndexConfigurationPopulator();

    @Before
    public void setUp() throws Exception {
        coveoSnCountryCa.setId(COUNTRY_CA);
        coveoSnCountryUs.setId(COUNTRY_US);
        List<CoveoSnCountry> coveoSnCountries = new ArrayList<>();
        coveoSnCountries.add(coveoSnCountryCa);
        coveoSnCountries.add(coveoSnCountryUs);
        when(countryConverter.convertAll(any())).thenReturn(coveoSnCountries);
    }

    @Test
    public void testPopulate() {
        CoveoSnIndexConfiguration coveoSnIndexConfiguration = new CoveoSnIndexConfiguration();
        coveoSnIndexConfigurationPopulator.populate(coveoSnIndexConfigurationModel, coveoSnIndexConfiguration);
        assertEquals(2, coveoSnIndexConfiguration.getCountries().size());
        assertEquals(COUNTRY_CA, coveoSnIndexConfiguration.getCountries().get(0).getId());
        assertEquals(COUNTRY_US, coveoSnIndexConfiguration.getCountries().get(1).getId());
    }
}