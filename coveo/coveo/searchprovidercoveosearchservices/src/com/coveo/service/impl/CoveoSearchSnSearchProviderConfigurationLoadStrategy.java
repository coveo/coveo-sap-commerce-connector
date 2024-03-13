/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.coveo.service.impl;

import com.coveo.searchservices.data.CoveoSearchSnSearchProviderConfiguration;
import com.coveo.model.CoveoSearchSnSearchProviderConfigurationModel;
import de.hybris.platform.searchservices.spi.service.SnSearchProviderConfigurationLoadStrategy;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.springframework.beans.factory.annotation.Required;


/**
 * Load Strategy for the coveo search provider configuration.
 */
public class CoveoSearchSnSearchProviderConfigurationLoadStrategy implements
		SnSearchProviderConfigurationLoadStrategy<CoveoSearchSnSearchProviderConfigurationModel, CoveoSearchSnSearchProviderConfiguration>
{
	private Converter<CoveoSearchSnSearchProviderConfigurationModel, CoveoSearchSnSearchProviderConfiguration> coveoSearchSnSearchProviderConfigurationConverter;

	@Override
	public CoveoSearchSnSearchProviderConfiguration load(final CoveoSearchSnSearchProviderConfigurationModel searchProviderConfiguration)
	{
		return coveoSearchSnSearchProviderConfigurationConverter.convert(searchProviderConfiguration);
	}


	@Required
	public void setCoveoSearchSnSearchProviderConfigurationConverter(Converter<CoveoSearchSnSearchProviderConfigurationModel, CoveoSearchSnSearchProviderConfiguration> coveoSearchSnSearchProviderConfigurationConverter) {
		this.coveoSearchSnSearchProviderConfigurationConverter = coveoSearchSnSearchProviderConfigurationConverter;
	}
}
