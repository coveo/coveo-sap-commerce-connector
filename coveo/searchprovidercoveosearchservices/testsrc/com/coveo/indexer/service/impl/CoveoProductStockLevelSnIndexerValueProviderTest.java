package com.coveo.indexer.service.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.searchservices.core.service.SnQualifier;
import de.hybris.platform.searchservices.core.service.SnSessionService;
import de.hybris.platform.searchservices.indexer.SnIndexerException;
import de.hybris.platform.searchservices.indexer.service.SnIndexerFieldWrapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@UnitTest
public class CoveoProductStockLevelSnIndexerValueProviderTest {

    private static final String WAREHOUSE_A = "warehouseA";
    private static final String WAREHOUSE_B = "warehouseB";
    private static final String WAREHOUSE_C = "warehouseC";
    private static final String WAREHOUSE_D = "warehouseD";
    @Mock
    ProductModel productModel;
    @Mock
    StockLevelModel stockLevelA;
    @Mock
    StockLevelModel stockLevelB;
    @Mock
    StockLevelModel stockLevelD;
    @Mock
    WarehouseModel warehouseA;
    @Mock
    WarehouseModel warehouseB;
    @Mock
    WarehouseModel warehouseC;
    @Mock
    WarehouseModel warehouseD;
    @Mock
    SnIndexerFieldWrapper fieldWrapper;
    @Mock
    SnQualifier warehouesAQualifier;
    @Mock
    SnQualifier warehouesBQualifier;
    @Mock
    SnQualifier warehouesCQualifier;

    @Mock
    SnSessionService snSessionService;

    @InjectMocks
    CoveoProductStockLevelSnIndexerValueProvider coveoProductStockLevelSnIndexerValueProvider;

    public void setUpLoadData() {
        Set<StockLevelModel> stockLevels = new HashSet<>();
        stockLevels.add(stockLevelA);
        stockLevels.add(stockLevelB);
        stockLevels.add(stockLevelD);
        when(productModel.getStockLevels()).thenReturn(stockLevels);
        when(stockLevelA.getWarehouse()).thenReturn(warehouseA);
        when(stockLevelB.getWarehouse()).thenReturn(warehouseB);
        when(stockLevelD.getWarehouse()).thenReturn(warehouseD);
        when(warehouseA.getCode()).thenReturn(WAREHOUSE_A);
        when(warehouseB.getCode()).thenReturn(WAREHOUSE_B);
        when(warehouseC.getCode()).thenReturn(WAREHOUSE_C);
        when(warehouseD.getCode()).thenReturn(WAREHOUSE_D);
        when(stockLevelA.getAvailable()).thenReturn(10);
        when(stockLevelB.getAvailable()).thenReturn(20);
        when(stockLevelD.getAvailable()).thenReturn(40);

        List<SnQualifier> warehouseQualifiers = List.of(warehouesAQualifier, warehouesBQualifier, warehouesCQualifier);
        when(fieldWrapper.getQualifiers()).thenReturn(warehouseQualifiers);
        when(warehouesAQualifier.getId()).thenReturn(WAREHOUSE_A);
        when(warehouesAQualifier.getAs(WarehouseModel.class)).thenReturn(warehouseA);
        when(warehouesBQualifier.getId()).thenReturn(WAREHOUSE_B);
        when(warehouesBQualifier.getAs(WarehouseModel.class)).thenReturn(warehouseB);
        when(warehouesCQualifier.getId()).thenReturn(WAREHOUSE_C);
        when(warehouesCQualifier.getAs(WarehouseModel.class)).thenReturn(warehouseC);
    }

    @Test
    public void getFieldValue() throws SnIndexerException {
        setUpLoadData();
        when(fieldWrapper.isQualified()).thenReturn(true);
        Map<String, Integer> qualifiedStockLevelsData = coveoProductStockLevelSnIndexerValueProvider.loadData(null, List.of(fieldWrapper), productModel);
        Map<String, Integer> qualifiedStockLevels = (Map<String, Integer>) coveoProductStockLevelSnIndexerValueProvider.getFieldValue(null, fieldWrapper, productModel, qualifiedStockLevelsData);
        assertEquals(3, qualifiedStockLevels.size());
        assertEquals(10, qualifiedStockLevels.get(WAREHOUSE_A).intValue());
        assertEquals(20, qualifiedStockLevels.get(WAREHOUSE_B).intValue());
        assertEquals(0, qualifiedStockLevels.get(WAREHOUSE_C).intValue());

        when(fieldWrapper.isQualified()).thenReturn(false);
        Map<String, Integer> unqualifiedStockLevelsData = coveoProductStockLevelSnIndexerValueProvider.loadData(null, List.of(fieldWrapper), productModel);
        Map<String, Integer> unqualifiedStockLevels = (Map<String, Integer>) coveoProductStockLevelSnIndexerValueProvider.getFieldValue(null, fieldWrapper, productModel, unqualifiedStockLevelsData);
        assertEquals(3, unqualifiedStockLevels.size());
        assertEquals(10, unqualifiedStockLevels.get(WAREHOUSE_A).intValue());
        assertEquals(20, unqualifiedStockLevels.get(WAREHOUSE_B).intValue());
        assertEquals(40, unqualifiedStockLevels.get(WAREHOUSE_D).intValue());
    }

    @Test
    public void loadData() throws SnIndexerException {
        setUpLoadData();
        when(fieldWrapper.isQualified()).thenReturn(true);
        Map<String, Integer> qualifiedStockLevels = coveoProductStockLevelSnIndexerValueProvider.loadData(null, List.of(fieldWrapper), productModel);
        assertEquals(2, qualifiedStockLevels.size());
        assertEquals(10, qualifiedStockLevels.get(WAREHOUSE_A).intValue());
        assertEquals(20, qualifiedStockLevels.get(WAREHOUSE_B).intValue());

        when(fieldWrapper.isQualified()).thenReturn(false);
        Map<String, Integer> unqualifiedStockLevels = coveoProductStockLevelSnIndexerValueProvider.loadData(null, List.of(fieldWrapper), productModel);
        assertEquals(3, unqualifiedStockLevels.size());
        assertEquals(10, unqualifiedStockLevels.get(WAREHOUSE_A).intValue());
        assertEquals(20, unqualifiedStockLevels.get(WAREHOUSE_B).intValue());
        assertEquals(40, unqualifiedStockLevels.get(WAREHOUSE_D).intValue());
    }

    @Test
    public void getSupportedQualifierClasses() throws SnIndexerException {
        Set<Class<?>> supportedQualifiers = coveoProductStockLevelSnIndexerValueProvider.getSupportedQualifierClasses();
        assertEquals(1, supportedQualifiers.size());
        assertTrue(supportedQualifiers.contains(WarehouseModel.class));
    }
}