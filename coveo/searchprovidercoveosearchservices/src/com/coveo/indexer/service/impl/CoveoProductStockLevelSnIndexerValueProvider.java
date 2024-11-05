package com.coveo.indexer.service.impl;

import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.searchservices.core.service.SnQualifier;
import de.hybris.platform.searchservices.core.service.SnSessionService;
import de.hybris.platform.searchservices.indexer.SnIndexerException;
import de.hybris.platform.searchservices.indexer.service.SnIndexerContext;
import de.hybris.platform.searchservices.indexer.service.SnIndexerFieldWrapper;
import de.hybris.platform.searchservices.indexer.service.impl.AbstractSnIndexerValueProvider;
import org.apache.commons.collections4.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CoveoProductStockLevelSnIndexerValueProvider extends AbstractSnIndexerValueProvider<ProductModel, Map<String, Integer>> {
    private static final Logger LOG = Logger.getLogger(CoveoProductStockLevelSnIndexerValueProvider.class);
    protected static final Set<Class<?>> SUPPORTED_QUALIFIER_CLASSES = Set.of(WarehouseModel.class);

    private SnSessionService snSessionService;

    @Override
    protected Object getFieldValue(SnIndexerContext indexerContext, SnIndexerFieldWrapper fieldWrapper, ProductModel source, Map<String, Integer> data) throws SnIndexerException {
        if (MapUtils.isEmpty(data)) {
            return null;
        }
        Map<String, Integer> value = new HashMap<>();
        if (fieldWrapper.isQualified()) {
            final List<SnQualifier> qualifiers = fieldWrapper.getQualifiers();
            for (final SnQualifier qualifier : qualifiers) {
                value.put(qualifier.getId(), data.getOrDefault(qualifier.getId(), 0));
            }
        } else {
            value.putAll(data);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Product : " + source.getCode() + "; Stock Levels : " + value);
        }
        return value;
    }

    @Override
    protected Map<String, Integer> loadData(final SnIndexerContext indexerContext, final Collection<SnIndexerFieldWrapper> fieldWrappers, final ProductModel source) throws SnIndexerException {

        Map<String, Integer> stockLevels = new HashMap<>();
        for (final SnIndexerFieldWrapper fieldWrapper : fieldWrappers) {
            if (fieldWrapper.isQualified()) {
                loadQualifiedStockLevels(indexerContext, fieldWrapper, stockLevels, source);
            } else {
                stockLevels.putAll(source.getStockLevels().stream()
                        .collect(Collectors.toMap(
                                stock -> stock.getWarehouse().getCode(),
                                StockLevelModel::getAvailable
                        )));
            }

        }
        return stockLevels;
    }

    private void loadQualifiedStockLevels(SnIndexerContext indexerContext, SnIndexerFieldWrapper fieldWrapper, Map<String, Integer> data, ProductModel source) {
        try {
            snSessionService.initializeSession();
            final List<SnQualifier> qualifiers = fieldWrapper.getQualifiers();
            for (final SnQualifier qualifier : qualifiers)
            {
                data.computeIfAbsent(qualifier.getId(), key -> {
                    final WarehouseModel warehouse = qualifier.getAs(WarehouseModel.class);
                    Integer stockLevel;
                    Set<StockLevelModel> stockLevels = source.getStockLevels();
                    if (stockLevels != null) {
                        stockLevel = stockLevels.stream()
                                .filter(stock -> stock.getWarehouse().getCode().equals(warehouse.getCode()))
                                .map(StockLevelModel::getAvailable)
                                .findFirst()
                                .orElse(null);
                    } else {
                        stockLevel = null;
                    }
                    return stockLevel;
                });
            }

        } finally {
            snSessionService.destroySession();
        }
    }

    @Override
    public Set<Class<?>> getSupportedQualifierClasses() throws SnIndexerException {
        return SUPPORTED_QUALIFIER_CLASSES;
    }

    public SnSessionService getSnSessionService()
    {
        return snSessionService;
    }

    @Required
    public void setSnSessionService(final SnSessionService snSessionService)
    {
        this.snSessionService = snSessionService;
    }

}
