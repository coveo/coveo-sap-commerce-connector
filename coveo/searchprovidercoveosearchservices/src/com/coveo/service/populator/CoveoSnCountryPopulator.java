package com.coveo.service.populator;


import com.coveo.searchservices.admin.data.CoveoSnCountry;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.i18n.I18NService;
import org.springframework.beans.factory.annotation.Required;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CoveoSnCountryPopulator implements Populator<CountryModel, CoveoSnCountry> {
    private I18NService i18NService;

    @Override
    public void populate(CountryModel countryModel, CoveoSnCountry coveoSnCountry) throws ConversionException {
        coveoSnCountry.setId(countryModel.getIsocode());
        coveoSnCountry.setName(buildLocalizedName(countryModel));
    }

    protected Map<Locale, String> buildLocalizedName(final CountryModel source)
    {
        final Set<Locale> supportedLocales = i18NService.getSupportedLocales();
        final Map<Locale, String> target = new LinkedHashMap<>();

        for (final Locale locale : supportedLocales)
        {
            target.put(locale, source.getName(locale));
        }

        return target;
    }

    @Required
    public void setI18NService(final I18NService i18nService)
    {
        i18NService = i18nService;
    }
}
