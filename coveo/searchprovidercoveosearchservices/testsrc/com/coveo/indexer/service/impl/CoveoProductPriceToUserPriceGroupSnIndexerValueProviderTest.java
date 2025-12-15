package com.coveo.indexer.service.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.europe1.enums.UserPriceGroup;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.searchservices.core.service.SnQualifier;
import de.hybris.platform.searchservices.core.service.SnSessionService;
import de.hybris.platform.searchservices.indexer.SnIndexerException;
import de.hybris.platform.searchservices.indexer.service.SnIndexerFieldWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@UnitTest
public class CoveoProductPriceToUserPriceGroupSnIndexerValueProviderTest {

    private static final Double JPY_PRICE = 100.00;
    private static final Double JPY_UGA_PRICE = 200.00;
    private static final Double JPY_UGB_PRICE = 300.00;
    private static final Double USD_PRICE = 10.00;
    private static final Double USD_UGA_PRICE = 20.00;
    private static final Double USD_UGB_PRICE = 30.00;
    private static final Double EUR_PRICE = 1.00;
    private static final Double EUR_UGA_PRICE = 2.00;
    private static final Double EUR_UGB_PRICE = 3.00;

    private static final String UGA_CODE = "UGA";
    private static final String UGB_CODE = "UGB";

    private static final String JPA_ISO = "JPA";
    private static final String USD_ISO = "USD";
    private static final String EUR_ISO = "EUR";

    @Mock
    ProductModel productModel;
    @Mock
    PriceRowModel priceRowModelJpy;
    @Mock
    PriceRowModel priceRowModelJpyUgA;
    @Mock
    PriceRowModel priceRowModelJpyUgB;
    @Mock
    PriceRowModel priceRowModelJpyUgC;
    @Mock
    PriceRowModel priceRowModelUsd;
    @Mock
    PriceRowModel priceRowModelUsdUgA;
    @Mock
    PriceRowModel priceRowModelUsdUgB;
    @Mock
    PriceRowModel priceRowModelUsdUgC;
    @Mock
    PriceRowModel priceRowModelEur;
    @Mock
    PriceRowModel priceRowModelEurUgA;
    @Mock
    PriceRowModel priceRowModelEurUgB;
    @Mock
    UserPriceGroup userPriceGroupA;
    @Mock
    UserPriceGroup userPriceGroupB;

    @Mock
    SnIndexerFieldWrapper fieldWrapper;
    @Mock
    SnQualifier jpaQualifier;
    @Mock
    SnQualifier usdQualifier;
    @Mock
    CurrencyModel currencyModelJpy;
    @Mock
    CurrencyModel currencyModelUsd;
    @Mock
    CurrencyModel currencyModelEur;
    @Mock
    SnSessionService snSessionService;

    @InjectMocks
    CoveoProductPriceToUserPriceGroupSnIndexerValueProvider coveoProductPriceToUserPriceGroupSnIndexerValueProvider;

    private void setupUnqualifiedLoadData() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -10);
        Date past = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 20);
        Date future = calendar.getTime();

        List<PriceRowModel> prices = new ArrayList<>();

        when(userPriceGroupA.getCode()).thenReturn(UGA_CODE);
        when(userPriceGroupB.getCode()).thenReturn(UGB_CODE);

        when(priceRowModelJpy.getUg()).thenReturn(null);
        when(priceRowModelJpy.getPrice()).thenReturn(JPY_PRICE);
        when(priceRowModelJpy.getCurrency()).thenReturn(currencyModelJpy);
        when(priceRowModelJpy.getStartTime()).thenReturn(past);
        prices.add(priceRowModelJpy);
        when(priceRowModelJpyUgA.getUg()).thenReturn(userPriceGroupA);
        when(priceRowModelJpyUgA.getPrice()).thenReturn(JPY_UGA_PRICE);
        when(priceRowModelJpyUgA.getCurrency()).thenReturn(currencyModelJpy);
        when(priceRowModelJpyUgA.getEndTime()).thenReturn(future);
        prices.add(priceRowModelJpyUgA);
        when(priceRowModelJpyUgB.getUg()).thenReturn(userPriceGroupB);
        when(priceRowModelJpyUgB.getPrice()).thenReturn(JPY_UGB_PRICE);
        when(priceRowModelJpyUgB.getCurrency()).thenReturn(currencyModelJpy);
        when(priceRowModelJpyUgB.getStartTime()).thenReturn(past);
        when(priceRowModelJpyUgB.getEndTime()).thenReturn(future);
        prices.add(priceRowModelJpyUgB);
        // invalid date
        when(priceRowModelJpyUgC.getStartTime()).thenReturn(future);
        prices.add(priceRowModelJpyUgC);

        when(priceRowModelUsd.getUg()).thenReturn(null);
        when(priceRowModelUsd.getPrice()).thenReturn(USD_PRICE);
        when(priceRowModelUsd.getCurrency()).thenReturn(currencyModelUsd);
        prices.add(priceRowModelUsd);
        when(priceRowModelUsdUgA.getUg()).thenReturn(userPriceGroupA);
        when(priceRowModelUsdUgA.getPrice()).thenReturn(USD_UGA_PRICE);
        when(priceRowModelUsdUgA.getCurrency()).thenReturn(currencyModelUsd);
        prices.add(priceRowModelUsdUgA);
        when(priceRowModelUsdUgB.getUg()).thenReturn(userPriceGroupB);
        when(priceRowModelUsdUgB.getPrice()).thenReturn(USD_UGB_PRICE);
        when(priceRowModelUsdUgB.getCurrency()).thenReturn(currencyModelUsd);
        prices.add(priceRowModelUsdUgB);
        // invalid date
        when(priceRowModelUsdUgC.getStartTime()).thenReturn(past);
        when(priceRowModelUsdUgC.getEndTime()).thenReturn(past);
        prices.add(priceRowModelUsdUgC);

        when(priceRowModelEur.getUg()).thenReturn(null);
        when(priceRowModelEur.getPrice()).thenReturn(EUR_PRICE);
        when(priceRowModelEur.getCurrency()).thenReturn(currencyModelEur);
        prices.add(priceRowModelEur);
        when(priceRowModelEurUgA.getUg()).thenReturn(userPriceGroupA);
        when(priceRowModelEurUgA.getPrice()).thenReturn(EUR_UGA_PRICE);
        when(priceRowModelEurUgA.getCurrency()).thenReturn(currencyModelEur);
        prices.add(priceRowModelEurUgA);
        when(priceRowModelEurUgB.getUg()).thenReturn(userPriceGroupB);
        when(priceRowModelEurUgB.getPrice()).thenReturn(EUR_UGB_PRICE);
        when(priceRowModelEurUgB.getCurrency()).thenReturn(currencyModelEur);
        prices.add(priceRowModelEurUgB);

        when(productModel.getEurope1Prices()).thenReturn(prices);

        when(currencyModelJpy.getIsocode()).thenReturn(JPA_ISO);
        when(currencyModelUsd.getIsocode()).thenReturn(USD_ISO);
        when(currencyModelEur.getIsocode()).thenReturn(EUR_ISO);
    }

    private void setupQualifiedLoadData() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -10);
        Date past = calendar.getTime();
        calendar.add(Calendar.DAY_OF_MONTH, 20);
        Date future = calendar.getTime();

        List<PriceRowModel> prices = new ArrayList<>();

        when(userPriceGroupA.getCode()).thenReturn(UGA_CODE);
        when(userPriceGroupB.getCode()).thenReturn(UGB_CODE);

        when(priceRowModelJpy.getUg()).thenReturn(null);
        when(priceRowModelJpy.getPrice()).thenReturn(JPY_PRICE);
        when(priceRowModelJpy.getCurrency()).thenReturn(currencyModelJpy);
        when(priceRowModelJpy.getStartTime()).thenReturn(past);
        prices.add(priceRowModelJpy);
        when(priceRowModelJpyUgA.getUg()).thenReturn(userPriceGroupA);
        when(priceRowModelJpyUgA.getPrice()).thenReturn(JPY_UGA_PRICE);
        when(priceRowModelJpyUgA.getCurrency()).thenReturn(currencyModelJpy);
        when(priceRowModelJpyUgA.getEndTime()).thenReturn(future);
        prices.add(priceRowModelJpyUgA);
        when(priceRowModelJpyUgB.getUg()).thenReturn(userPriceGroupB);
        when(priceRowModelJpyUgB.getPrice()).thenReturn(JPY_UGB_PRICE);
        when(priceRowModelJpyUgB.getCurrency()).thenReturn(currencyModelJpy);
        when(priceRowModelJpyUgB.getStartTime()).thenReturn(past);
        when(priceRowModelJpyUgB.getEndTime()).thenReturn(future);
        prices.add(priceRowModelJpyUgB);
        // invalid date
        when(priceRowModelJpyUgC.getCurrency()).thenReturn(currencyModelJpy);
        when(priceRowModelJpyUgC.getStartTime()).thenReturn(future);
        prices.add(priceRowModelJpyUgC);

        when(priceRowModelUsd.getUg()).thenReturn(null);
        when(priceRowModelUsd.getPrice()).thenReturn(USD_PRICE);
        when(priceRowModelUsd.getCurrency()).thenReturn(currencyModelUsd);
        prices.add(priceRowModelUsd);
        when(priceRowModelUsdUgA.getUg()).thenReturn(userPriceGroupA);
        when(priceRowModelUsdUgA.getPrice()).thenReturn(USD_UGA_PRICE);
        when(priceRowModelUsdUgA.getCurrency()).thenReturn(currencyModelUsd);
        prices.add(priceRowModelUsdUgA);
        when(priceRowModelUsdUgB.getUg()).thenReturn(userPriceGroupB);
        when(priceRowModelUsdUgB.getPrice()).thenReturn(USD_UGB_PRICE);
        when(priceRowModelUsdUgB.getCurrency()).thenReturn(currencyModelUsd);
        prices.add(priceRowModelUsdUgB);
        // invalid date
        when(priceRowModelUsdUgC.getCurrency()).thenReturn(currencyModelUsd);
        when(priceRowModelUsdUgC.getStartTime()).thenReturn(past);
        when(priceRowModelUsdUgC.getEndTime()).thenReturn(past);
        prices.add(priceRowModelUsdUgC);

        when(priceRowModelEur.getCurrency()).thenReturn(currencyModelEur);
        prices.add(priceRowModelEur);
        when(priceRowModelEurUgA.getCurrency()).thenReturn(currencyModelEur);
        prices.add(priceRowModelEurUgA);
        when(priceRowModelEurUgB.getCurrency()).thenReturn(currencyModelEur);
        prices.add(priceRowModelEurUgB);

        when(productModel.getEurope1Prices()).thenReturn(prices);

        List<SnQualifier> currencyQualifiers = List.of(jpaQualifier, usdQualifier);
        when(fieldWrapper.getQualifiers()).thenReturn(currencyQualifiers);
        when(jpaQualifier.getId()).thenReturn(JPA_ISO);
        when(jpaQualifier.getAs(CurrencyModel.class)).thenReturn(currencyModelJpy);
        when(usdQualifier.getId()).thenReturn(USD_ISO);
        when(usdQualifier.getAs(CurrencyModel.class)).thenReturn(currencyModelUsd);
    }

    @Test
    void getSupportedQualifierClasses() throws SnIndexerException {
        Set<Class<?>> supportedQualifiers = coveoProductPriceToUserPriceGroupSnIndexerValueProvider.getSupportedQualifierClasses();
        assertEquals(1, supportedQualifiers.size());
        assertTrue(supportedQualifiers.contains(CurrencyModel.class));
    }

    @Test
    void getQualifiedFieldValue() throws SnIndexerException {
        setupQualifiedLoadData();
        when(fieldWrapper.isQualified()).thenReturn(true);
        CoveoProductPriceToUserPriceGroupSnIndexerValueProvider.CurrencyToUserPriceGroupAndPriceMapping data = coveoProductPriceToUserPriceGroupSnIndexerValueProvider.loadData(null, List.of(fieldWrapper), productModel);
        Object qualifiedValues = coveoProductPriceToUserPriceGroupSnIndexerValueProvider.getFieldValue(null, fieldWrapper, productModel, data);
        assertNotNull(qualifiedValues);
        assertTrue(qualifiedValues instanceof Map);
        Map<String, Map<String, Double>> qualifiedValuesMap = (Map<String, Map<String, Double>>) qualifiedValues;
        assertEquals(2, qualifiedValuesMap.size());
        assertTrue(qualifiedValuesMap.containsKey(JPA_ISO));
        assertTrue(qualifiedValuesMap.containsKey(USD_ISO));
        validateQualifiedValues(qualifiedValuesMap);
    }

    @Test
    void getUnqualifiedFieldValue() throws SnIndexerException {
        setupUnqualifiedLoadData();
        CoveoProductPriceToUserPriceGroupSnIndexerValueProvider.CurrencyToUserPriceGroupAndPriceMapping data = coveoProductPriceToUserPriceGroupSnIndexerValueProvider.loadData(null, List.of(fieldWrapper), productModel);
        Object qualifiedValues = coveoProductPriceToUserPriceGroupSnIndexerValueProvider.getFieldValue(null, fieldWrapper, productModel, data);
        assertNotNull(qualifiedValues);
        assertTrue(qualifiedValues instanceof Map);
        Map<String, Map<String, Double>> qualifiedValuesMap = (Map<String, Map<String, Double>>) qualifiedValues;
        assertEquals(3, qualifiedValuesMap.size());
        assertTrue(qualifiedValuesMap.containsKey(JPA_ISO));
        assertTrue(qualifiedValuesMap.containsKey(USD_ISO));
        assertTrue(qualifiedValuesMap.containsKey(EUR_ISO));

        validateQualifiedValues(qualifiedValuesMap);
        validateUnqualifiedValues(qualifiedValuesMap);
    }

    private static void validateUnqualifiedValues(Map<String, Map<String, Double>> qualifiedValuesMap) {
        Map<String, Double> eurPrices = qualifiedValuesMap.get(EUR_ISO);
        assertEquals(3, eurPrices.size());
        assertTrue(eurPrices.containsKey(""));
        assertEquals(EUR_PRICE, eurPrices.get(""), 0.0);
        assertTrue(eurPrices.containsKey(UGA_CODE));
        assertEquals(EUR_UGA_PRICE, eurPrices.get(UGA_CODE), 0.0);
        assertTrue(eurPrices.containsKey(UGB_CODE));
        assertEquals(EUR_UGB_PRICE, eurPrices.get(UGB_CODE), 0.0);
    }

    private static void validateQualifiedValues(Map<String, Map<String, Double>> qualifiedValuesMap) {
        Map<String, Double> jpyPrices = qualifiedValuesMap.get(JPA_ISO);
        assertEquals(3, jpyPrices.size());
        assertTrue(jpyPrices.containsKey(""));
        assertEquals(JPY_PRICE, jpyPrices.get(""), 0.0);
        assertTrue(jpyPrices.containsKey(UGA_CODE));
        assertEquals(JPY_UGA_PRICE, jpyPrices.get(UGA_CODE), 0.0);
        assertTrue(jpyPrices.containsKey(UGB_CODE));
        assertEquals(JPY_UGB_PRICE, jpyPrices.get(UGB_CODE), 0.0);

        Map<String, Double> usdPrices = qualifiedValuesMap.get(USD_ISO);
        assertEquals(3, jpyPrices.size());
        assertTrue(usdPrices.containsKey(""));
        assertEquals(USD_PRICE, usdPrices.get(""), 0.0);
        assertTrue(usdPrices.containsKey(UGA_CODE));
        assertEquals(USD_UGA_PRICE, usdPrices.get(UGA_CODE), 0.0);
        assertTrue(usdPrices.containsKey(UGB_CODE));
        assertEquals(USD_UGB_PRICE, usdPrices.get(UGB_CODE), 0.0);
    }

    @Test
    void loadQualifiedData() throws SnIndexerException {
        setupQualifiedLoadData();
        when(fieldWrapper.isQualified()).thenReturn(true);
        CoveoProductPriceToUserPriceGroupSnIndexerValueProvider.CurrencyToUserPriceGroupAndPriceMapping currencyToUserPriceGroupAndPriceMapping =
                coveoProductPriceToUserPriceGroupSnIndexerValueProvider.loadData(null, List.of(fieldWrapper), productModel);
        assertNotNull(currencyToUserPriceGroupAndPriceMapping);
        Map<String, CoveoProductPriceToUserPriceGroupSnIndexerValueProvider.UserPriceGroupToPrice> prices = currencyToUserPriceGroupAndPriceMapping.getPrices();
        assertEquals(2, prices.size());

        validateQualifiedData(prices);
    }


    @Test
    void loadUnqualifiedData() throws SnIndexerException {
        setupUnqualifiedLoadData();
        CoveoProductPriceToUserPriceGroupSnIndexerValueProvider.CurrencyToUserPriceGroupAndPriceMapping currencyToUserPriceGroupAndPriceMapping =
                coveoProductPriceToUserPriceGroupSnIndexerValueProvider.loadData(null, List.of(fieldWrapper), productModel);
        assertNotNull(currencyToUserPriceGroupAndPriceMapping);
        Map<String, CoveoProductPriceToUserPriceGroupSnIndexerValueProvider.UserPriceGroupToPrice> prices = currencyToUserPriceGroupAndPriceMapping.getPrices();

        assertEquals(3, prices.size());
        validateQualifiedData(prices);
        validateUnqualifiedData(prices);
    }

    private void validateUnqualifiedData(Map<String, CoveoProductPriceToUserPriceGroupSnIndexerValueProvider.UserPriceGroupToPrice> prices) {
        assertTrue(prices.containsKey(EUR_ISO));
        Map<String, Double> eurUnqualifiedPrices = prices.get(EUR_ISO).getUserGroupPrices();
        assertEquals(3, eurUnqualifiedPrices.size());
        assertTrue(eurUnqualifiedPrices.containsKey(""));
        assertEquals(EUR_PRICE, eurUnqualifiedPrices.get(""), 0.0);
        assertTrue(eurUnqualifiedPrices.containsKey(UGA_CODE));
        assertEquals(EUR_UGA_PRICE, eurUnqualifiedPrices.get(UGA_CODE), 0.0);
        assertTrue(eurUnqualifiedPrices.containsKey(UGB_CODE));
        assertEquals(EUR_UGB_PRICE, eurUnqualifiedPrices.get(UGB_CODE), 0.0);
    }

    private static void validateQualifiedData(Map<String, CoveoProductPriceToUserPriceGroupSnIndexerValueProvider.UserPriceGroupToPrice> prices) {
        assertTrue(prices.containsKey(USD_ISO));
        Map<String, Double> usdPrices = prices.get(USD_ISO).getUserGroupPrices();
        assertEquals(3, usdPrices.size());
        assertTrue(usdPrices.containsKey(""));
        assertEquals(USD_PRICE, usdPrices.get(""), 0.0);
        assertTrue(usdPrices.containsKey(UGA_CODE));
        assertEquals(USD_UGA_PRICE, usdPrices.get(UGA_CODE), 0.0);
        assertTrue(usdPrices.containsKey(UGB_CODE));
        assertEquals(USD_UGB_PRICE, usdPrices.get(UGB_CODE), 0.0);

        assertTrue(prices.containsKey(JPA_ISO));
        Map<String, Double> jpyPrices = prices.get(JPA_ISO).getUserGroupPrices();
        assertEquals(3, jpyPrices.size());
        assertTrue(jpyPrices.containsKey(""));
        assertEquals(JPY_PRICE, jpyPrices.get(""), 0.0);
        assertTrue(jpyPrices.containsKey(UGA_CODE));
        assertEquals(JPY_UGA_PRICE, jpyPrices.get(UGA_CODE), 0.0);
        assertTrue(jpyPrices.containsKey(UGB_CODE));
        assertEquals(JPY_UGB_PRICE, jpyPrices.get(UGB_CODE), 0.0);
    }

}