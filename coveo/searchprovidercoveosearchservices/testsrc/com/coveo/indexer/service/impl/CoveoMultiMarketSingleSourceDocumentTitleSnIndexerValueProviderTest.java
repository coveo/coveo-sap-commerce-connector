package com.coveo.indexer.service.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.searchservices.admin.data.SnField;
import de.hybris.platform.searchservices.core.SnException;
import de.hybris.platform.searchservices.core.service.SnExpressionEvaluator;
import de.hybris.platform.searchservices.core.service.SnQualifier;
import de.hybris.platform.searchservices.indexer.SnIndexerException;
import de.hybris.platform.searchservices.indexer.service.SnIndexerFieldWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.coveo.indexer.service.impl.CoveoMultiMarketSingleSourceDocumentTitleSnIndexerValueProvider.DEFAULT_LANGUAGE_PARAM;
import static de.hybris.platform.searchservices.indexer.service.impl.ModelAttributeSnIndexerValueProvider.EXPRESSION_PARAM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@UnitTest
class CoveoMultiMarketSingleSourceDocumentTitleSnIndexerValueProviderTest
{
    @Mock
    ProductModel productModelMock;

    @Mock
    SnQualifier zhQualifierMock;
    @Mock
    SnQualifier enQualifierMock;

    @Mock
    SnField fieldMock;

    @Mock
    SnIndexerFieldWrapper fieldWrapper;

    @Mock
    SnExpressionEvaluator snExpressionEvaluator;

    @InjectMocks
    CoveoMultiMarketSingleSourceDocumentTitleSnIndexerValueProvider coveoMultiMarketSingleSourceDocumentTitleSnIndexerValueProvider;

    @BeforeEach
    void setUp() throws SnException
    {
        lenient().when(zhQualifierMock.getAs(Locale.class)).thenReturn(Locale.forLanguageTag("zh"));
        lenient().when(enQualifierMock.getAs(Locale.class)).thenReturn(Locale.forLanguageTag("en"));
        lenient().when(fieldWrapper.getQualifiers()).thenReturn(List.of(zhQualifierMock, enQualifierMock));

        lenient().when(snExpressionEvaluator.evaluate(productModelMock, "name", List.of(Locale.forLanguageTag("zh"), Locale.forLanguageTag("en")))).thenReturn(Map.of(
                Locale.forLanguageTag("zh"), "Chinese Name",
                Locale.forLanguageTag("en"), "English Name"
        ));
        lenient().when(snExpressionEvaluator.evaluate(productModelMock, "name")).thenReturn("Non-localized Name");

        when(fieldMock.getId()).thenReturn("name");
        when(fieldWrapper.getField()).thenReturn(fieldMock);
    }

    @ParameterizedTest
    @CsvSource({
        "zh,Chinese Name",
        "en,English Name"
    })
    void getFieldValueWhenCorrectLanguageConfigured(String languageCode, String expectedValue) throws SnIndexerException {
        Map<String, String> parameters = Map.of(DEFAULT_LANGUAGE_PARAM, languageCode, EXPRESSION_PARAM, "name");
        when(fieldWrapper.getValueProviderParameters()).thenReturn(parameters);
        when(fieldWrapper.isLocalized()).thenReturn(true);

        Object value = coveoMultiMarketSingleSourceDocumentTitleSnIndexerValueProvider.getFieldValue(null, fieldWrapper, productModelMock, null);
        assertInstanceOf(Map.class, value, "Returned value should be a Map");
        Map<?, ?> valueMap = (Map<?, ?>) value;
        assertEquals(1, valueMap.size(), "Map should contain exactly one entry");
        assertTrue(valueMap.containsKey(Locale.forLanguageTag(languageCode)), "Map should contain the locale key");
        assertEquals(expectedValue, valueMap.get(Locale.forLanguageTag(languageCode)), "Map value for locale should match expected");
    }

    @Test
    void getFieldValueThrowsExceptionWhenIncorrectLanguageConfigured() {
        Map<String, String> parameters = Map.of(DEFAULT_LANGUAGE_PARAM, "de", EXPRESSION_PARAM, "name");
        when(fieldWrapper.getValueProviderParameters()).thenReturn(parameters);
        when(fieldWrapper.isLocalized()).thenReturn(true);

        SnIndexerException exception = assertThrows(
            SnIndexerException.class,
            () -> coveoMultiMarketSingleSourceDocumentTitleSnIndexerValueProvider.getFieldValue(null, fieldWrapper, productModelMock, null)
        );
        assertTrue(exception.getMessage().contains("No localised value for language de"), "Exception message should mention missing language");
    }

    @Test
    void getFieldValueReturnsAllWhenFieldNotLocalised() throws SnIndexerException
    {
        Map<String, String> parameters = Map.of(DEFAULT_LANGUAGE_PARAM, "de", EXPRESSION_PARAM, "name");
        when(fieldWrapper.getValueProviderParameters()).thenReturn(parameters);
        when(fieldWrapper.isLocalized()).thenReturn(false);

        Object value = coveoMultiMarketSingleSourceDocumentTitleSnIndexerValueProvider.getFieldValue(null, fieldWrapper, productModelMock, null);
        assertInstanceOf(String.class, value, "Returned value should be a String");
        assertEquals("Non-localized Name", value, "Returned value should match expected");
    }
}
