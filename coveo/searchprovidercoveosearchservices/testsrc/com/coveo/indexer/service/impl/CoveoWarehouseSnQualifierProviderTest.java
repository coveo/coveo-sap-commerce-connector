package com.coveo.indexer.service.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commerceservices.search.searchservices.strategies.SnStoreSelectionStrategy;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.searchservices.core.service.SnContext;
import de.hybris.platform.searchservices.admin.data.SnIndexType;
import de.hybris.platform.searchservices.core.service.SnQualifier;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@UnitTest
public class CoveoWarehouseSnQualifierProviderTest {

    private static final String INDEX_TYPE_ID = "indexType";

    @Mock
    BaseStoreModel storeA;
    @Mock
    BaseStoreModel storeB;

    @Mock
    WarehouseModel warehouseA;
    @Mock
    WarehouseModel warehouseB;
    @Mock
    WarehouseModel warehouseC;
    @Mock
    WarehouseModel warehouseD;

    @Mock
    SnContext snContext;
    @Mock
    SnIndexType snIndexType;

    @Mock
    BaseStoreService baseStoreService;
    @Mock
    SnStoreSelectionStrategy snStoreSelectionStrategy;

    @InjectMocks
    CoveoWarehouseSnQualifierProvider coveoWarehouseSnQualifierProvider;

    @Test
    public void getSupportedQualifierClasses() {
        Set<Class<?>> supportedQualifiers = coveoWarehouseSnQualifierProvider.getSupportedQualifierClasses();
        assertEquals(1, supportedQualifiers.size());
        assertTrue(supportedQualifiers.contains(WarehouseModel.class));
    }

    @Test
    public void getAvailableQualifiers() {
        Map<String, Object> contextMap = new HashMap<>();

        when(snContext.getAttributes()).thenReturn(contextMap);
        when(snIndexType.getId()).thenReturn(INDEX_TYPE_ID);
        when(snContext.getIndexType()).thenReturn(snIndexType);
        when(snStoreSelectionStrategy.getStores("indexType")).thenReturn(List.of(storeA, storeB));
        when(storeA.getWarehouses()).thenReturn(List.of(warehouseA, warehouseB));
        when(storeB.getWarehouses()).thenReturn(List.of(warehouseC, warehouseD));

        List<SnQualifier> qualifiers = coveoWarehouseSnQualifierProvider.getAvailableQualifiers(snContext);
        assertEquals(4, qualifiers.size());
        assertEquals(1, contextMap.size());
        List<SnQualifier> mapQualifiers = (List<SnQualifier>) contextMap.get(CoveoWarehouseSnQualifierProvider.WAREHOUSE_QUALIFIERS_KEY);
        assertEquals(4, mapQualifiers.size());

        Set<WarehouseModel> expectedWarehouses = new HashSet<>(Arrays.asList(warehouseA, warehouseB, warehouseC, warehouseD));
        Set<WarehouseModel> actualReturnedWarehouses = qualifiers.stream().map(q -> q.getAs(WarehouseModel.class)).collect(Collectors.toSet());
        Set<WarehouseModel> actualContextWarehouses = mapQualifiers.stream().map(q -> q.getAs(WarehouseModel.class)).collect(Collectors.toSet());

        assertEquals(expectedWarehouses, actualReturnedWarehouses);
        assertEquals(expectedWarehouses, actualContextWarehouses);
    }

    @Test
    public void getCurrentQualifiers() {
        when(baseStoreService.getCurrentBaseStore()).thenReturn(storeA);
        when(storeA.getWarehouses()).thenReturn(List.of(warehouseA, warehouseB));

        List<SnQualifier> qualifiers = coveoWarehouseSnQualifierProvider.getCurrentQualifiers(snContext);
        assertEquals(2, qualifiers.size());
        Set<WarehouseModel> expectedWarehouses = new HashSet<>(Arrays.asList(warehouseA, warehouseB));
        Set<WarehouseModel> actualReturnedWarehouses = qualifiers.stream().map(q -> q.getAs(WarehouseModel.class)).collect(Collectors.toSet());
        assertEquals(expectedWarehouses, actualReturnedWarehouses);
    }

    @Test
    public void testWarehouseSnQualifier() {
        when(baseStoreService.getCurrentBaseStore()).thenReturn(storeA);
        when(storeA.getWarehouses()).thenReturn(List.of(warehouseA));

        List<SnQualifier> qualifiers = coveoWarehouseSnQualifierProvider.getCurrentQualifiers(snContext);
        assertEquals(1, qualifiers.size());
        SnQualifier qualifier = qualifiers.get(0);
        assertTrue(qualifier.canGetAs(WarehouseModel.class));
        assertFalse(qualifier.canGetAs(BaseStoreModel.class));

        List<SnQualifier> additionalQualifiers  = coveoWarehouseSnQualifierProvider.getCurrentQualifiers(snContext);
        assertEquals(1, additionalQualifiers.size());
        SnQualifier additionalQualifier = additionalQualifiers.get(0);
        assertTrue(qualifier.equals(additionalQualifier));

        assertEquals(warehouseA, qualifier.getAs(WarehouseModel.class));
        assertThrows(IllegalArgumentException.class, () -> qualifier.getAs(BaseStoreModel.class));
    }
}