package com.coveo.indexer.service.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.searchservices.indexer.SnIndexerException;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.commons.configuration.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@UnitTest
public class CoveoSkuStockLevelWarehouseAvailabilitySnIndexerValueProviderTest {

    private static final String HIGH_STOCK_SKU = "highStockSku";

    @InjectMocks
    private CoveoSkuStockLevelWarehouseAvailabilitySnIndexerValueProvider coveoSkuStockLevelWarehouseAvailabilitySnIndexerValueProvider;

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private Configuration configuration;

    @Mock
    private WarehouseModel warehouseModel;

    @Mock
    private StockLevelModel noStockLevelModel;
    @Mock
    private StockLevelModel lowStockLevelModel;
    @Mock
    private StockLevelModel highStockLevelModel;

    @BeforeEach
    void setUp() {
        when(configurationService.getConfiguration()).thenReturn(configuration);
        when(configuration.getInt("coveo.availability.lowstock.threshold", 0)).thenReturn(2);
        when(warehouseModel.getStockLevels()).thenReturn(Set.of(noStockLevelModel, lowStockLevelModel, highStockLevelModel));
        when(noStockLevelModel.getAvailable()).thenReturn(0);
        when(lowStockLevelModel.getAvailable()).thenReturn(1);
        when(highStockLevelModel.getAvailable()).thenReturn(3);
        when(highStockLevelModel.getProductCode()).thenReturn(HIGH_STOCK_SKU);
    }

    @Test
    void getFieldValue() throws SnIndexerException {
        Object[] value = (Object[]) coveoSkuStockLevelWarehouseAvailabilitySnIndexerValueProvider.getFieldValue(null,
                null,
                warehouseModel, null);
        assertEquals(1, value.length, "Expected only high stock SKU");
        assertEquals(HIGH_STOCK_SKU, value[0], "Expected high stock SKU");
    }
}
