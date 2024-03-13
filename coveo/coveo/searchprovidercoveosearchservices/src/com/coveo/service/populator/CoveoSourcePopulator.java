package com.coveo.service.populator;

import com.coveo.enums.CoveoCatalogSourceObjectType;
import com.coveo.model.CoveoSourceModel;
import com.coveo.searchservices.data.CoveoCatalogObjectType;
import com.coveo.searchservices.data.CoveoSource;
import com.coveo.searchservices.admin.data.CoveoSnCountry;
import de.hybris.platform.apiregistryservices.model.ConsumedOAuthCredentialModel;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.searchservices.admin.data.SnCurrency;
import de.hybris.platform.searchservices.admin.data.SnLanguage;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.springframework.beans.factory.annotation.Required;

import java.util.Optional;

public class CoveoSourcePopulator implements Populator<CoveoSourceModel, CoveoSource> {
    Converter<LanguageModel, SnLanguage> languageConverter;
    Converter<CountryModel, CoveoSnCountry> countryConverter;
    Converter<CurrencyModel, SnCurrency> currencyConverter;

    @Override
    public void populate(CoveoSourceModel coveoSourceModel, CoveoSource coveoSource) throws ConversionException {
        coveoSource.setId(coveoSourceModel.getId());
        coveoSource.setName(coveoSourceModel.getName());
        Optional.ofNullable(coveoSourceModel.getLanguage())
                .map(languageConverter::convert)
                .ifPresent(coveoSource::setLanguage);
        Optional.ofNullable(coveoSourceModel.getCountry())
                .map(countryConverter::convert)
                .ifPresent(coveoSource::setCountry);
        Optional.ofNullable(coveoSourceModel.getCurrency())
                .map(currencyConverter::convert)
                .ifPresent(coveoSource::setCurrency);
        Optional.ofNullable(coveoSourceModel.getConsumedDestination())
                .ifPresent(destination -> {
                    coveoSource.setDestinationId(destination.getId());
                    coveoSource.setDestinationTargetUrl(destination.getUrl());
                    Optional.ofNullable(destination.getCredential())
                            .filter(cred -> cred instanceof ConsumedOAuthCredentialModel)
                            .map(cred -> ((ConsumedOAuthCredentialModel) cred).getClientSecret())
                            .ifPresent(coveoSource::setDestinationSecret);
                });
        Optional.ofNullable(coveoSourceModel.getObjectType())
                .map(CoveoCatalogSourceObjectType::getCode)
                .map(CoveoCatalogObjectType::valueOf)
                .ifPresent(coveoSource::setObjectType);
    }

    @Required
    public void setLanguageConverter(Converter<LanguageModel, SnLanguage> languageConverter) {
        this.languageConverter = languageConverter;
    }

    @Required
    public void setCountryConverter(Converter<CountryModel, CoveoSnCountry> countryConverter) {
        this.countryConverter = countryConverter;
    }

    @Required
    public void setCurrencyConverter(Converter<CurrencyModel, SnCurrency> currencyConverter) {
        this.currencyConverter = currencyConverter;
    }
}
