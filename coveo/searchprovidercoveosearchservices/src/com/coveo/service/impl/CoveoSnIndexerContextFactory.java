package com.coveo.service.impl;

import com.coveo.constants.SearchprovidercoveosearchservicesConstants;

import com.coveo.searchservices.admin.data.CoveoSnCountry;
import com.coveo.searchservices.admin.data.CoveoSnIndexConfiguration;
import com.coveo.searchservices.data.CoveoSearchSnSearchProviderConfiguration;
import com.coveo.stream.service.impl.CoveoAvailabilityStreamServiceStrategy;
import com.coveo.stream.service.impl.CoveoProductStreamServiceStrategy;
import com.coveo.stream.service.impl.CoveoRebuildStreamService;
import com.coveo.stream.service.impl.CoveoUpdateStreamService;
import de.hybris.platform.searchservices.admin.data.SnCurrency;
import de.hybris.platform.searchservices.admin.data.SnLanguage;
import de.hybris.platform.searchservices.core.service.SnContext;
import de.hybris.platform.searchservices.indexer.service.SnIndexerRequest;
import de.hybris.platform.searchservices.indexer.service.impl.DefaultSnIndexerContext;
import de.hybris.platform.searchservices.indexer.service.impl.DefaultSnIndexerContextFactory;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CoveoSnIndexerContextFactory extends DefaultSnIndexerContextFactory
{
    private static final Logger LOG = Logger.getLogger(CoveoSnIndexerContextFactory.class);

    private ConfigurationService configurationService;

    protected void populateIndexerContext(final DefaultSnIndexerContext context, final SnIndexerRequest indexerRequest) {
        super.populateIndexerContext(context,indexerRequest);
        CoveoSearchSnSearchProviderConfiguration coveoSearchProviderConfiguration = (CoveoSearchSnSearchProviderConfiguration) context.getIndexConfiguration().getSearchProviderConfiguration();
        List<CoveoUpdateStreamService> updateStreamServices = new ArrayList<>();
        List<CoveoRebuildStreamService> rebuildStreamServices = new ArrayList<>();
        if (LOG.isDebugEnabled()) LOG.debug("Number of sources configured is: " + coveoSearchProviderConfiguration.getSources().size());
        coveoSearchProviderConfiguration.getSources().forEach(source -> {
            updateStreamServices.add(new CoveoUpdateStreamService(source));
            rebuildStreamServices.add(new CoveoRebuildStreamService(source));
        });

        String[] availabilityTypes = configurationService.getConfiguration().getString(SearchprovidercoveosearchservicesConstants.SUPPORTED_AVAILABILITY_TYPES_CODE).split(",");
        if (availabilityTypes != null && Arrays.asList(availabilityTypes).contains(context.getIndexType().getItemComposedType())) {
            context.getAttributes().put(SearchprovidercoveosearchservicesConstants.COVEO_AVAILABILITY_REBUILD_STREAM_SERVICES_KEY,new CoveoAvailabilityStreamServiceStrategy<>(rebuildStreamServices));
            context.getAttributes().put(SearchprovidercoveosearchservicesConstants.COVEO_AVAILABILITY_UPDATE_STREAM_SERVICES_KEY,new CoveoAvailabilityStreamServiceStrategy<>(updateStreamServices));
        } else {
            List<SnLanguage> languages = getLanguages(context);
            List<SnCurrency> currencies = getCurrencies(context);
            List<CoveoSnCountry> countries = getCountries(context);

            context.getAttributes().put(SearchprovidercoveosearchservicesConstants.COVEO_PRODUCT_REBUILD_STREAM_SERVICES_KEY,new CoveoProductStreamServiceStrategy<>(languages, currencies, countries, rebuildStreamServices));
            context.getAttributes().put(SearchprovidercoveosearchservicesConstants.COVEO_PRODUCT_UPDATE_STREAM_SERVICES_KEY,new CoveoProductStreamServiceStrategy<>(languages, currencies, countries, updateStreamServices));
        }
    }

    private List<SnLanguage> getLanguages(SnContext context) {
        List<SnLanguage> languages = context.getIndexConfiguration().getLanguages();
        if (CollectionUtils.isEmpty(languages)) {
            LOG.warn("No language is specified in index configuration");
        }
        return languages;
    }

    private List<SnCurrency> getCurrencies(SnContext context) {
        List<SnCurrency> currencies = context.getIndexConfiguration().getCurrencies();
        if (CollectionUtils.isEmpty(currencies)) {
            LOG.warn("No language is specified in index configuration");
        }
        return currencies;
    }

    private List<CoveoSnCountry> getCountries(SnContext context) {
        List<CoveoSnCountry> countries = ((CoveoSnIndexConfiguration) context.getIndexConfiguration()).getCountries();
        if (CollectionUtils.isEmpty(countries)) {
            LOG.warn("No country is specified in index configuration");
        }
        return countries;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }
}
