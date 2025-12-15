package com.coveo.indexer.service.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.ordersplitting.model.StockLevelModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.searchservices.indexer.SnIndexerException;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.hamcrest.Matchers.hasItemInArray;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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

    @BeforeEach
    void setUp() {
        when(warehouseModel.getStockLevels()).thenReturn(Set.of(stockLevelModelA, stockLevelModelB));
        when(stockLevelModelA.getProductCode()).thenReturn(PRODUCT_CODE_A);
        when(stockLevelModelB.getProductCode()).thenReturn(PRODUCT_CODE_B);
    }

    @Test
    void getFieldValue() throws SnIndexerException {
        Object value = coveoWarehouseAvailableSkusSnIndexerProvider.getFieldValue(null, null, warehouseModel, null);
        assertInstanceOf(Object[].class, value);
        assertEquals(2, ((Object[]) value).length);
        MatcherAssert.assertThat((Object[]) value, hasItemInArray(PRODUCT_CODE_A));
        MatcherAssert.assertThat((Object[]) value, hasItemInArray(PRODUCT_CODE_B));
    }
}