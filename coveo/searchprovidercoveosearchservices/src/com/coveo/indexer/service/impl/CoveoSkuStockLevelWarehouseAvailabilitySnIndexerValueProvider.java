package com.coveo.indexer.service.impl;

import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.searchservices.indexer.SnIndexerException;
import de.hybris.platform.searchservices.indexer.service.SnIndexerContext;
import de.hybris.platform.searchservices.indexer.service.SnIndexerFieldWrapper;
import de.hybris.platform.searchservices.indexer.service.impl.AbstractSnIndexerValueProvider;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CoveoSkuStockLevelWarehouseAvailabilitySnIndexerValueProvider extends AbstractSnIndexerValueProvider<WarehouseModel, Void> {

    private static final Logger LOG = Logger.getLogger(CoveoSkuStockLevelWarehouseAvailabilitySnIndexerValueProvider.class);
    private static final int LOW_STOCK_LEVEL_THRESHOLD = 0;

    protected static final Set<Class<?>> SUPPORTED_QUALIFIER_CLASSES = Set.of();

    private ConfigurationService configurationService;

    @Override
    protected Object getFieldValue(SnIndexerContext indexerContext, SnIndexerFieldWrapper fieldWrapper, WarehouseModel source, Void data) throws SnIndexerException {
        int threshold = configurationService.getConfiguration().getInt("coveo.availability.lowstock.threshold",LOW_STOCK_LEVEL_THRESHOLD);
        List<String> productCodes = new ArrayList<>();
        for (StockLevelModel stockLevel : source.getStockLevels()) {
            if (stockLevel.getAvailable() > threshold) {
                productCodes.add(stockLevel.getProductCode());
            }
        }
        if (LOG.isDebugEnabled()) LOG.debug("Warehouse : " + source.getCode() + "; Available SKUs : " + productCodes);
        return productCodes.toArray();
    }

    @Override
    public Set<Class<?>> getSupportedQualifierClasses() throws SnIndexerException {
        return SUPPORTED_QUALIFIER_CLASSES;
    }

    @Required
    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }
}
