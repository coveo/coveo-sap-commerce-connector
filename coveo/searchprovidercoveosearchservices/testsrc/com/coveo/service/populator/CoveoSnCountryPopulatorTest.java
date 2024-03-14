package com.coveo.service.populator;

import com.coveo.searchservices.admin.data.CoveoSnCountry;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.servicelayer.i18n.I18NService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@UnitTest
public class CoveoSnCountryPopulatorTest {

    CountryModel countryModel;
    @Mock
    I18NService i18NService;

    @InjectMocks
    CoveoSnCountryPopulator coveoSnCountryPopulator = new CoveoSnCountryPopulator();

    @Before
    public void setUp() {
        Set<Locale> locals = new HashSet<Locale>();
        locals.add(Locale.ENGLISH);
        locals.add(Locale.FRENCH);
        when(i18NService.getSupportedLocales()).thenReturn(locals);

        countryModel = new CountryModel();
        countryModel.setIsocode("CA");
        countryModel.setName("Canada", Locale.ENGLISH);
        countryModel.setName("Le Canada", Locale.FRENCH);
    }

    @Test
    public void testPopulate() {
        CoveoSnCountry coveoSnCountry = new CoveoSnCountry();
        coveoSnCountryPopulator.populate(countryModel, coveoSnCountry);
        assertEquals("CA", coveoSnCountry.getId());
        assertEquals("Canada", coveoSnCountry.getName().get(Locale.ENGLISH));
        assertEquals("Le Canada", coveoSnCountry.getName().get(Locale.FRENCH));
    }
}