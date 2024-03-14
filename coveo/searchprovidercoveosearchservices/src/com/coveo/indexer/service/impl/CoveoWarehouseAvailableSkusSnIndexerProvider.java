package com.coveo.indexer.service.impl;

import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.searchservices.indexer.SnIndexerException;
import de.hybris.platform.searchservices.indexer.service.SnIndexerContext;
import de.hybris.platform.searchservices.indexer.service.SnIndexerFieldWrapper;
import de.hybris.platform.searchservices.indexer.service.impl.AbstractSnIndexerValueProvider;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CoveoWarehouseAvailableSkusSnIndexerProvider extends AbstractSnIndexerValueProvider<WarehouseModel, Void>  {

    protected static final Set<Class<?>> SUPPORTED_QUALIFIER_CLASSES = Set.of();

    @Override
    protected Object getFieldValue(SnIndexerContext indexerContext, SnIndexerFieldWrapper fieldWrapper, WarehouseModel source, Void data) throws SnIndexerException {
        List<?> productCodes = source.getStockLevels().stream().map(StockLevelModel::getProductCode).collect(Collectors.toList());
        return productCodes.toArray();
    }

    @Override
    public Set<Class<?>> getSupportedQualifierClasses() throws SnIndexerException {
        return SUPPORTED_QUALIFIER_CLASSES;
    }
}
