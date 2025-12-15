package com.coveo.indexer.service.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commerceservices.url.UrlResolver;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.searchservices.indexer.SnIndexerException;
import de.hybris.platform.searchservices.indexer.service.SnIndexerFieldWrapper;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.configuration.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@UnitTest
public class CoveoSimpleClickableProductUriSnIndexerValueProviderTest {

    private static final String SITE_ID_PARAM = "siteId";
    private static final String STORE_ID = "electronics";
    private static final String WRONG_STORE_ID = "wrongStoreId";
    private static final String BASE_URL = "https://yourcommercesite.com/";
    private static final String PRODUCT_URL = "productUrl";

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private Configuration configuration;

    @Mock
    private UrlResolver<ProductModel> urlResolver;

    @Mock
    SnIndexerFieldWrapper fieldWrapper;

    @InjectMocks
    private CoveoSimpleClickableProductUriSnIndexerValueProvider coveoSimpleClickableProductUriSnIndexerValueProvider;

    @BeforeEach
    void setUp() {
        when(urlResolver.resolve(any())).thenReturn(PRODUCT_URL);
    }

    @Test
    void getFieldValue() throws SnIndexerException {
        when(configurationService.getConfiguration()).thenReturn(configuration);
        when(configuration.getString("website." + STORE_ID + ".https", "")).thenReturn(BASE_URL);
        Map<String, String> parameters = Map.of(SITE_ID_PARAM, STORE_ID);
        when(fieldWrapper.getValueProviderParameters()).thenReturn(parameters);
        Object value = coveoSimpleClickableProductUriSnIndexerValueProvider.getFieldValue(null, fieldWrapper, null, null);
        assertEquals(BASE_URL + PRODUCT_URL, value);
    }

    @Test
    void getFieldValue_ForIncorrectStore() throws SnIndexerException {
        when(configurationService.getConfiguration()).thenReturn(configuration);
        when(configuration.getString("website." + WRONG_STORE_ID + ".https", "")).thenReturn("");
        Map<String, String> parameters = Map.of(SITE_ID_PARAM, WRONG_STORE_ID);
        when(fieldWrapper.getValueProviderParameters()).thenReturn(parameters);
        Object value = coveoSimpleClickableProductUriSnIndexerValueProvider.getFieldValue(null, fieldWrapper, null, null);
        assertEquals(PRODUCT_URL, value);
    }

    @Test
    void getFieldValue_ForInvalidValueParameter() throws SnIndexerException {
        Map<String, String> parameters = MapUtils.EMPTY_MAP;
        when(fieldWrapper.getValueProviderParameters()).thenReturn(parameters);
        Object value = coveoSimpleClickableProductUriSnIndexerValueProvider.getFieldValue(null, fieldWrapper, null, null);
        assertEquals(PRODUCT_URL, value);
    }
}