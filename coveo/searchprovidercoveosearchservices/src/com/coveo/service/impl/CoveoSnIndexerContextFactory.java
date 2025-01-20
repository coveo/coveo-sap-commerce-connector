package com.coveo.service.impl;

import com.coveo.constants.SearchprovidercoveosearchservicesConstants;

import com.coveo.pushapiclient.UpdateStreamService;
import com.coveo.searchservices.admin.data.CoveoSnCountry;
import com.coveo.searchservices.admin.data.CoveoSnIndexConfiguration;
import com.coveo.searchservices.data.CoveoSearchSnSearchProviderConfiguration;
import com.coveo.stream.service.impl.CoveoAvailabilityStreamServiceStrategy;
import com.coveo.stream.service.impl.CoveoProductStreamServiceStrategy;
import com.coveo.stream.service.impl.CoveoRebuildStreamService;
import com.coveo.stream.service.impl.CoveoUpdateStreamService;
import de.hybris.platform.core.Registry;
import de.hybris.platform.searchservices.admin.data.SnCurrency;
import de.hybris.platform.searchservices.admin.data.SnLanguage;
import de.hybris.platform.searchservices.core.service.SnContext;
import de.hybris.platform.searchservices.indexer.service.SnIndexerRequest;
import de.hybris.platform.searchservices.indexer.service.impl.DefaultSnIndexerContext;
import de.hybris.platform.searchservices.indexer.service.impl.DefaultSnIndexerContextFactory;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import org.apache.log4j.Logger;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.coveo.constants.SearchprovidercoveosearchservicesConstants.COSAP_CONNECTOR_USER_AGENT;
import static com.coveo.constants.SearchprovidercoveosearchservicesConstants.COSAP_CONNECTOR_USER_AGENT_PROPERTY;
import static com.coveo.constants.SearchprovidercoveosearchservicesConstants.SUPPORTED_AVAILABILITY_TYPES_CODE;

public class CoveoSnIndexerContextFactory extends DefaultSnIndexerContextFactory {
    private static final Logger LOG = Logger.getLogger(CoveoSnIndexerContextFactory.class);

    private ConfigurationService configurationService;
    private CommonI18NService commonI18NService;

    protected void populateIndexerContext(final DefaultSnIndexerContext context, final SnIndexerRequest indexerRequest) {
        if (LOG.isDebugEnabled()) LOG.debug("Populating indexer context for Coveo search provider");
        super.populateIndexerContext(context,indexerRequest);
        CoveoSearchSnSearchProviderConfiguration coveoSearchProviderConfiguration = (CoveoSearchSnSearchProviderConfiguration) context.getIndexConfiguration().getSearchProviderConfiguration();
        String userAgent = configurationService.getConfiguration().getString(COSAP_CONNECTOR_USER_AGENT_PROPERTY, COSAP_CONNECTOR_USER_AGENT);
        List<CoveoUpdateStreamService> updateStreamServices = new ArrayList<>();
        List<CoveoRebuildStreamService> rebuildStreamServices = new ArrayList<>();
        coveoSearchProviderConfiguration.getSources().forEach(source -> {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Populating Source with name: " + source.getName());
                LOG.trace("Source target URL: " + source.getDestinationTargetUrl());
            }
            CoveoUpdateStreamService coveoUpdateStreamService = Registry.getApplicationContext().getBean(CoveoUpdateStreamService.class);
            coveoUpdateStreamService.init(source, new String[]{userAgent});
            updateStreamServices.add(coveoUpdateStreamService);

            CoveoRebuildStreamService coveoRebuildStreamService = Registry.getApplicationContext().getBean(CoveoRebuildStreamService.class);
            coveoRebuildStreamService.init(source, new String[]{userAgent});
            rebuildStreamServices.add(coveoRebuildStreamService);
        });

        String[] availabilityTypes = configurationService.getConfiguration().getString(SUPPORTED_AVAILABILITY_TYPES_CODE).split(",");
        String composedType = context.getIndexType().getItemComposedType();
        if(LOG.isDebugEnabled()) LOG.debug(String.format("Availability types are %s and composed type is %s", Arrays.toString(availabilityTypes), composedType));
        if (availabilityTypes != null && Arrays.asList(availabilityTypes).contains(composedType)) {
            context.getAttributes().put(SearchprovidercoveosearchservicesConstants.COVEO_AVAILABILITY_REBUILD_STREAM_SERVICES_KEY,new CoveoAvailabilityStreamServiceStrategy<>(rebuildStreamServices, configurationService));
            context.getAttributes().put(SearchprovidercoveosearchservicesConstants.COVEO_AVAILABILITY_UPDATE_STREAM_SERVICES_KEY,new CoveoAvailabilityStreamServiceStrategy<>(updateStreamServices, configurationService));
        } else {
            List<SnLanguage> languages = getLanguages(context);
            List<SnCurrency> currencies = getCurrencies(context);
            List<CoveoSnCountry> countries = getCountries(context);

            if (LOG.isTraceEnabled()) {
                LOG.trace("Languages: " + Arrays.toString(languages.stream().map(SnLanguage::getId).toArray()));
                LOG.trace("Currencies: " + Arrays.toString(currencies.stream().map(SnCurrency::getId).toArray()));
                LOG.trace("Countries:" + Arrays.toString(countries.stream().map(CoveoSnCountry::getId).toArray()));
            }

            context.getAttributes().put(SearchprovidercoveosearchservicesConstants.COVEO_PRODUCT_REBUILD_STREAM_SERVICES_KEY,
                    new CoveoProductStreamServiceStrategy<>(languages, currencies, countries, rebuildStreamServices, configurationService, commonI18NService));
            context.getAttributes().put(SearchprovidercoveosearchservicesConstants.COVEO_PRODUCT_UPDATE_STREAM_SERVICES_KEY,
                    new CoveoProductStreamServiceStrategy<>(languages, currencies, countries, updateStreamServices, configurationService, commonI18NService));
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
            LOG.warn("No currency is specified in index configuration");
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

    public void setCommonI18NService(CommonI18NService commonI18NService) {
        this.commonI18NService = commonI18NService;
    }
}
