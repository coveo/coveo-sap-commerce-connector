package com.coveo.service.populator;

import com.coveo.model.CoveoSnIndexConfigurationModel;
import com.coveo.searchservices.admin.data.CoveoSnCountry;
import com.coveo.searchservices.admin.data.CoveoSnIndexConfiguration;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.springframework.beans.factory.annotation.Autowired;

public class CoveoSnIndexConfigurationPopulator implements Populator<CoveoSnIndexConfigurationModel, CoveoSnIndexConfiguration> {

    private Converter<CountryModel, CoveoSnCountry> coveoSnCountryConverter;

    @Override
    public void populate(CoveoSnIndexConfigurationModel coveoSnIndexConfigurationModel, CoveoSnIndexConfiguration coveoSnIndexConfiguration) throws ConversionException {
        coveoSnIndexConfiguration.setCountries(coveoSnCountryConverter.convertAll(coveoSnIndexConfigurationModel.getCountries()));
    }

    @Autowired
    public void setCoveoSnCountryConverter(Converter<CountryModel, CoveoSnCountry> coveoSnCountryConverter) {
        this.coveoSnCountryConverter = coveoSnCountryConverter;
    }
}
