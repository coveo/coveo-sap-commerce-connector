package com.coveo.stream.service.utils;

import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Currency;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
@UnitTest
public class CoveoFieldValueResolverUtilsTest {

    private static final String FIELD_NAME = "fieldName";
    private static final String FIELD_URL = "fieldUrl";
    private static final String FIELD_CODE = "fieldCode";
    private static final String FIELD_LOCALISED = "fieldLocalised";
    private static final String FIELD_PRICE= "fieldPrice";

    private static final String FIELD_NONE = "fieldNone";
    private static final String FIELD_NAME_VALUE = "Name";
    private static final String FIELD_URL_VALUE = "Url";
    private static final String FIELD_CODE_VALUE = "Code";
    private static final String FIELD_NONE_VALUE = null;

    private static final Locale LOCALE_ENGLISH = Locale.ENGLISH;
    private static final Locale LOCALE_FRENCH = Locale.FRENCH;
    private static final String LOCALE_ENGLISH_VALUE = "english";
    private static final String LOCALE_FRENCH_VALUE = "french";

    private static final String CURRENCY_USD = "USD";
    private static final Double CURRENCY_USD_VALUE = 100.00;
    private static final String CURRENCY_EUR = "EUR";
    private static final Double CURRENCY_EUR_VALUE = 90.00;

    private final Map<String, Object> documentFields = new HashMap<>();

    private final Map<Locale, String> localisedValues = new LinkedHashMap<>();

    private final Map<String, Double> currencyValues = new LinkedHashMap<>();

    @Before
    public void setUp() {
        localisedValues.put(LOCALE_ENGLISH, LOCALE_ENGLISH_VALUE);
        localisedValues.put(LOCALE_FRENCH, LOCALE_FRENCH_VALUE);
        currencyValues.put(CURRENCY_USD, CURRENCY_USD_VALUE);
        currencyValues.put(CURRENCY_EUR, CURRENCY_EUR_VALUE);
        documentFields.put(FIELD_NAME, FIELD_NAME_VALUE);
        documentFields.put(FIELD_URL, FIELD_URL_VALUE);
        documentFields.put(FIELD_CODE, FIELD_CODE_VALUE);
        documentFields.put(FIELD_NONE, FIELD_NONE_VALUE);
        documentFields.put(FIELD_LOCALISED, localisedValues);
        documentFields.put(FIELD_PRICE, currencyValues);
    }

    @Test
    public void testResolveFieldValue() {
        String value = CoveoFieldValueResolverUtils.resolveFieldValue(FIELD_LOCALISED, documentFields);
        assertEquals(LOCALE_ENGLISH_VALUE, value);
    }

    @Test
    public void testTestResolveFieldValue() {
        String englishValue = CoveoFieldValueResolverUtils.resolveFieldValue(FIELD_LOCALISED, documentFields, LOCALE_ENGLISH, null);
        assertEquals(LOCALE_ENGLISH_VALUE, englishValue);
        String frenchValue = CoveoFieldValueResolverUtils.resolveFieldValue(FIELD_LOCALISED, documentFields, Locale.FRENCH, null);
        assertEquals(LOCALE_FRENCH_VALUE, frenchValue);
        String nameValue = CoveoFieldValueResolverUtils.resolveFieldValue(FIELD_NAME, documentFields, null, null);
        assertEquals(FIELD_NAME_VALUE, nameValue);
        String noValue = CoveoFieldValueResolverUtils.resolveFieldValue(FIELD_NONE, documentFields, null, null);
        assertEquals("", noValue);
        String usdValue = CoveoFieldValueResolverUtils.resolveFieldValue(FIELD_PRICE, documentFields, null, Currency.getInstance(CURRENCY_USD));
        assertEquals(CURRENCY_USD_VALUE.toString(), usdValue);
        String eurValue = CoveoFieldValueResolverUtils.resolveFieldValue(FIELD_PRICE, documentFields, null, Currency.getInstance(CURRENCY_EUR));
        assertEquals(CURRENCY_EUR_VALUE.toString(), eurValue);
    }

    @Test
    public void testTestResolveFieldValue_FieldName() {
        String englishValue = CoveoFieldValueResolverUtils.resolveFieldValue(documentFields.get(FIELD_LOCALISED), LOCALE_ENGLISH, null);
        assertEquals(LOCALE_ENGLISH_VALUE, englishValue);
        String nameValue = CoveoFieldValueResolverUtils.resolveFieldValue(documentFields.get(FIELD_NAME), null, null);
        assertEquals(FIELD_NAME_VALUE, nameValue);
        String usdValue = CoveoFieldValueResolverUtils.resolveFieldValue(documentFields.get(FIELD_PRICE), null, Currency.getInstance(CURRENCY_USD));
        assertEquals(CURRENCY_USD_VALUE.toString(), usdValue);
    }
}