package com.coveo.indexer.service.impl;

import com.coveo.constants.SearchprovidercoveosearchservicesConstants;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.searchservices.indexer.SnIndexerException;
import de.hybris.platform.searchservices.indexer.service.SnIndexerContext;
import de.hybris.platform.searchservices.indexer.service.SnIndexerFieldWrapper;
import de.hybris.platform.searchservices.indexer.service.impl.AbstractSnIndexerValueProvider;
import de.hybris.platform.servicelayer.config.ConfigurationService;

import java.util.Arrays;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

public class CoveoObjectTypeSnIndexerValueProvider extends AbstractSnIndexerValueProvider<ItemModel, Void> {

    public static final String PRODUCT_OBJECT_TYPE = "product";
    public static final String PRODUCT_VARIANT_TYPE = "variant";
    public static final String PRODUCT_AVAILABILITY_TYPE = "availability";
    private static final Logger LOG = Logger.getLogger(CoveoObjectTypeSnIndexerValueProvider.class);

    protected static final Set<Class<?>> SUPPORTED_QUALIFIER_CLASSES = Set.of();

    private ConfigurationService configurationService;

    @Override
    public Set<Class<?>> getSupportedQualifierClasses() throws SnIndexerException
    {
        return SUPPORTED_QUALIFIER_CLASSES;
    }

    @Override
    protected Object getFieldValue(SnIndexerContext indexerContext, SnIndexerFieldWrapper fieldWrapper, ItemModel source, Void data) throws SnIndexerException {
        if (Arrays.asList(configurationService.getConfiguration().getString(SearchprovidercoveosearchservicesConstants.SUPPORTED_PRODUCT_TYPES_CODE).split(",")).contains(source.getItemtype())) {
            return PRODUCT_OBJECT_TYPE;
        } else if (Arrays.asList(configurationService.getConfiguration().getString(SearchprovidercoveosearchservicesConstants.SUPPORTED_VARIANT_TYPES_CODE).split(",")).contains(source.getItemtype())) {
            return PRODUCT_VARIANT_TYPE;
        } else if (Arrays.asList(configurationService.getConfiguration().getString(SearchprovidercoveosearchservicesConstants.SUPPORTED_AVAILABILITY_TYPES_CODE).split(",")).contains(source.getItemtype())) {
            return PRODUCT_AVAILABILITY_TYPE;
        } else {
            LOG.warn("Item type not supported: " + source.getItemtype());
        }
        return null;
    }

    @Required
    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }
}
