package com.coveo.indexer.service.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.searchservices.indexer.SnIndexerException;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@UnitTest
public class CoveoWarehouseAvailableSkusSnIndexerProviderTest {

    private static final String PRODUCT_CODE_A = "A";
    private static final String PRODUCT_CODE_B = "B";
    @Mock
    WarehouseModel warehouseModel;
    @Mock
    StockLevelModel stockLevelModelA;
    @Mock
    StockLevelModel stockLevelModelB;

    @InjectMocks
    CoveoWarehouseAvailableSkusSnIndexerProvider coveoWarehouseAvailableSkusSnIndexerProvider;

    @Before
    public void setUp() throws Exception {
        when(warehouseModel.getStockLevels()).thenReturn(Set.of(stockLevelModelA, stockLevelModelB));
        when(stockLevelModelA.getProductCode()).thenReturn(PRODUCT_CODE_A);
        when(stockLevelModelB.getProductCode()).thenReturn(PRODUCT_CODE_B);
    }

    @Test
    public void getFieldValue() throws SnIndexerException {
        Object value = coveoWarehouseAvailableSkusSnIndexerProvider.getFieldValue(null, null, warehouseModel, null);
        assertTrue(value instanceof Object[]);
        assertEquals(2, ((Object[]) value).length);
        MatcherAssert.assertThat((Object[]) value, hasItemInArray(PRODUCT_CODE_A));
        MatcherAssert.assertThat((Object[]) value, hasItemInArray(PRODUCT_CODE_B));
    }
}