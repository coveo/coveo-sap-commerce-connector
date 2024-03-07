/*
 * Copyright (c) 2021 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.coveo.service.populator;

import com.coveo.data.CoveoSearchSnSearchProviderConfiguration;
import com.coveo.model.CoveoSearchSnSearchProviderConfigurationModel;
import de.hybris.platform.converters.Populator;



/**
 * Populates {@link CoveoSearchSnSearchProviderConfiguration} from {@link CoveoSearchSnSearchProviderConfigurationModel}.
 */
public class CoveoSearchSnSearchProviderConfigurationPopulator
		implements Populator<CoveoSearchSnSearchProviderConfigurationModel, CoveoSearchSnSearchProviderConfiguration>
{
	@Override
	public void populate(final CoveoSearchSnSearchProviderConfigurationModel source,
			final CoveoSearchSnSearchProviderConfiguration target)
	{
		target.setDestinationId(source.getConsumedDestination().getId());
		target.setDestinationTargetId(source.getConsumedDestination().getDestinationTarget().getId());
	}
}
