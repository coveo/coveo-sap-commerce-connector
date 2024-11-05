package com.coveo.indexer.service.impl;

import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.searchservices.indexer.SnIndexerException;
import de.hybris.platform.searchservices.indexer.service.SnIndexerContext;
import de.hybris.platform.searchservices.indexer.service.SnIndexerFieldWrapper;
import de.hybris.platform.searchservices.indexer.service.impl.AbstractSnIndexerValueProvider;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Set;

public class CoveoWarehouseAvailableSkusSnIndexerProvider extends AbstractSnIndexerValueProvider<WarehouseModel, Void>  {
    private static final Logger LOG = Logger.getLogger(CoveoWarehouseAvailableSkusSnIndexerProvider.class);

    protected static final Set<Class<?>> SUPPORTED_QUALIFIER_CLASSES = Set.of();

    @Override
    protected Object getFieldValue(SnIndexerContext indexerContext, SnIndexerFieldWrapper fieldWrapper, WarehouseModel source, Void data) throws SnIndexerException {
        List<?> productCodes = source.getStockLevels().stream().map(StockLevelModel::getProductCode).toList();
        if (LOG.isDebugEnabled()) LOG.debug("Warehouse : " + source.getCode() + "; Available SKUs : " + productCodes);
        return productCodes.toArray();
    }

    @Override
    public Set<Class<?>> getSupportedQualifierClasses() throws SnIndexerException {
        return SUPPORTED_QUALIFIER_CLASSES;
    }
}
