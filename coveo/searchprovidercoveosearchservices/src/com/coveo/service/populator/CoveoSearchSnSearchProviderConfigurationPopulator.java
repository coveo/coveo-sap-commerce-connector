/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.coveo.service.populator;

import com.coveo.searchservices.data.CoveoSearchSnSearchProviderConfiguration;
import com.coveo.model.CoveoSearchSnSearchProviderConfigurationModel;
import com.coveo.model.CoveoSourceModel;
import com.coveo.searchservices.data.CoveoSource;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.springframework.beans.factory.annotation.Required;

/**
 * Populates {@link CoveoSearchSnSearchProviderConfiguration} from {@link CoveoSearchSnSearchProviderConfigurationModel}.
 */
public class CoveoSearchSnSearchProviderConfigurationPopulator
		implements Populator<CoveoSearchSnSearchProviderConfigurationModel, CoveoSearchSnSearchProviderConfiguration>
{
	Converter<CoveoSourceModel, CoveoSource> sourceConverter;
	@Override
	public void populate(final CoveoSearchSnSearchProviderConfigurationModel source,
			final CoveoSearchSnSearchProviderConfiguration target)
	{
		target.setSources(sourceConverter.convertAll(source.getCoveoSource()));
	}

	@Required
	public void setSourceConverter(Converter<CoveoSourceModel, CoveoSource> sourceConverter) {
		this.sourceConverter = sourceConverter;
	}
}
