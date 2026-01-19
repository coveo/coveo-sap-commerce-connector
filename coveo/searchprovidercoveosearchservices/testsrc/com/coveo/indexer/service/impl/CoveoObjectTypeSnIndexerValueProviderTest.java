package com.coveo.indexer.service.impl;

import com.coveo.constants.SearchprovidercoveosearchservicesConstants;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.searchservices.indexer.SnIndexerException;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.variants.model.VariantProductModel;
import org.apache.commons.configuration2.Configuration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@UnitTest
public class CoveoObjectTypeSnIndexerValueProviderTest {

    @Mock
    ProductModel coveoMockProduct;

    @Mock
    ConfigurationService configurationService;

    @Mock
    Configuration configuration;

    @InjectMocks
    CoveoObjectTypeSnIndexerValueProvider coveoObjectTypeSnIndexerValueProvider = new CoveoObjectTypeSnIndexerValueProvider();

    @Test
    void testGetFieldValue_ForProduct() throws SnIndexerException {
        when(configurationService.getConfiguration()).thenReturn(configuration);
        when(configuration.getString(SearchprovidercoveosearchservicesConstants.SUPPORTED_PRODUCT_TYPES_CODE)).thenReturn("Product,CoveoMockProduct");
        assertEquals(CoveoObjectTypeSnIndexerValueProvider.PRODUCT_OBJECT_TYPE, coveoObjectTypeSnIndexerValueProvider.getFieldValue(null, null, new ProductModel(), null));
    }

    @Test
    void testGetFieldValue_ForCoveoMockProduct() throws SnIndexerException {
        when(configurationService.getConfiguration()).thenReturn(configuration);

        when(configuration.getString(SearchprovidercoveosearchservicesConstants.SUPPORTED_PRODUCT_TYPES_CODE)).thenReturn("Product,CoveoMockProduct");
        when(coveoMockProduct.getItemtype()).thenReturn("CoveoMockProduct");
        assertEquals(CoveoObjectTypeSnIndexerValueProvider.PRODUCT_OBJECT_TYPE, coveoObjectTypeSnIndexerValueProvider.getFieldValue(null, null, coveoMockProduct, null));
    }

    @Test
    void testGetFieldValue_ForVariantProduct() throws SnIndexerException {
        when(configurationService.getConfiguration()).thenReturn(configuration);
        when(configuration.getString(SearchprovidercoveosearchservicesConstants.SUPPORTED_PRODUCT_TYPES_CODE)).thenReturn("Product,CoveoMockProduct");
        when(configuration.getString(SearchprovidercoveosearchservicesConstants.SUPPORTED_VARIANT_TYPES_CODE)).thenReturn("VariantProduct");
        assertEquals(CoveoObjectTypeSnIndexerValueProvider.PRODUCT_VARIANT_TYPE, coveoObjectTypeSnIndexerValueProvider.getFieldValue(null, null, new VariantProductModel(), null));
    }

    @Test
    void testGetFieldValue_ForWarehouse() throws SnIndexerException {
        when(configurationService.getConfiguration()).thenReturn(configuration);
        when(configuration.getString(SearchprovidercoveosearchservicesConstants.SUPPORTED_PRODUCT_TYPES_CODE)).thenReturn("Product,CoveoMockProduct");
        when(configuration.getString(SearchprovidercoveosearchservicesConstants.SUPPORTED_VARIANT_TYPES_CODE)).thenReturn("VariantProduct");
        when(configuration.getString(SearchprovidercoveosearchservicesConstants.SUPPORTED_AVAILABILITY_TYPES_CODE)).thenReturn("Warehouse");
        assertEquals(CoveoObjectTypeSnIndexerValueProvider.PRODUCT_AVAILABILITY_TYPE, coveoObjectTypeSnIndexerValueProvider.getFieldValue(null, null, new WarehouseModel(), null));
    }

    @Test
    void testGetFieldValue_ForUnknownItem() throws SnIndexerException {
        when(configurationService.getConfiguration()).thenReturn(configuration);
        when(configuration.getString(SearchprovidercoveosearchservicesConstants.SUPPORTED_PRODUCT_TYPES_CODE)).thenReturn("Product,CoveoMockProduct");
        when(configuration.getString(SearchprovidercoveosearchservicesConstants.SUPPORTED_VARIANT_TYPES_CODE)).thenReturn("VariantProduct");
        when(configuration.getString(SearchprovidercoveosearchservicesConstants.SUPPORTED_AVAILABILITY_TYPES_CODE)).thenReturn("Warehouse");
        assertNull(coveoObjectTypeSnIndexerValueProvider.getFieldValue(null, null, new CategoryModel(), null));
    }
}