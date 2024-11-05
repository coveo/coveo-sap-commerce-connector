/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.coveo.service.impl;

import com.coveo.searchservices.data.CoveoSearchSnSearchProviderConfiguration;
import com.coveo.model.CoveoSearchSnSearchProviderConfigurationModel;
import de.hybris.platform.searchservices.spi.service.SnSearchProviderConfigurationLoadStrategy;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;


/**
 * Load Strategy for the coveo search provider configuration.
 */
public class CoveoSearchSnSearchProviderConfigurationLoadStrategy implements
		SnSearchProviderConfigurationLoadStrategy<CoveoSearchSnSearchProviderConfigurationModel, CoveoSearchSnSearchProviderConfiguration>
{
	private final static Logger LOG = Logger.getLogger(CoveoSearchSnSearchProviderConfigurationLoadStrategy.class);
	private Converter<CoveoSearchSnSearchProviderConfigurationModel, CoveoSearchSnSearchProviderConfiguration> coveoSearchSnSearchProviderConfigurationConverter;

	@Override
	public CoveoSearchSnSearchProviderConfiguration load(final CoveoSearchSnSearchProviderConfigurationModel searchProviderConfiguration)
	{
		CoveoSearchSnSearchProviderConfiguration converted = coveoSearchSnSearchProviderConfigurationConverter.convert(searchProviderConfiguration);
		if (LOG.isDebugEnabled()) {
			if (converted != null) {
				LOG.debug("Loaded search provider configuration: " + converted.getId());
			} else {
				LOG.debug("No search provider configuration has been loaded");
			}
		}
		return converted;
	}


	@Required
	public void setCoveoSearchSnSearchProviderConfigurationConverter(Converter<CoveoSearchSnSearchProviderConfigurationModel, CoveoSearchSnSearchProviderConfiguration> coveoSearchSnSearchProviderConfigurationConverter) {
		this.coveoSearchSnSearchProviderConfigurationConverter = coveoSearchSnSearchProviderConfigurationConverter;
	}
}
