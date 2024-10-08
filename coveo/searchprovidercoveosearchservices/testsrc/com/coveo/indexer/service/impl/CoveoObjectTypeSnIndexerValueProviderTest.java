package com.coveo.indexer.service.impl;

import com.coveo.constants.SearchprovidercoveosearchservicesConstants;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.searchservices.indexer.SnIndexerException;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.variants.model.VariantProductModel;
import org.apache.commons.configuration.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
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

    @Before
    public void setUp() {
        when(configurationService.getConfiguration()).thenReturn(configuration);

        when(configuration.getString(SearchprovidercoveosearchservicesConstants.SUPPORTED_PRODUCT_TYPES_CODE)).thenReturn("Product,CoveoMockProduct");
        when(configuration.getString(SearchprovidercoveosearchservicesConstants.SUPPORTED_VARIANT_TYPES_CODE)).thenReturn("VariantProduct");
        when(configuration.getString(SearchprovidercoveosearchservicesConstants.SUPPORTED_AVAILABILITY_TYPES_CODE)).thenReturn("Warehouse");
    }

    @Test
    public void testGetFieldValue_ForProduct() throws SnIndexerException {
        assertEquals(CoveoObjectTypeSnIndexerValueProvider.PRODUCT_OBJECT_TYPE, coveoObjectTypeSnIndexerValueProvider.getFieldValue(null, null, new ProductModel(), null));
    }

    @Test
    public void testGetFieldValue_ForCoveoMockProduct() throws SnIndexerException {
        when(coveoMockProduct.getItemtype()).thenReturn("CoveoMockProduct");
        assertEquals(CoveoObjectTypeSnIndexerValueProvider.PRODUCT_OBJECT_TYPE, coveoObjectTypeSnIndexerValueProvider.getFieldValue(null, null, coveoMockProduct, null));
    }

    @Test
    public void testGetFieldValue_ForVariantProduct() throws SnIndexerException {
        assertEquals(CoveoObjectTypeSnIndexerValueProvider.PRODUCT_VARIANT_TYPE, coveoObjectTypeSnIndexerValueProvider.getFieldValue(null, null, new VariantProductModel(), null));
    }

    @Test
    public void testGetFieldValue_ForWarehouse() throws SnIndexerException {
        assertEquals(CoveoObjectTypeSnIndexerValueProvider.PRODUCT_AVAILABILITY_TYPE, coveoObjectTypeSnIndexerValueProvider.getFieldValue(null, null, new WarehouseModel(), null));
    }

    @Test
    public void testGetFieldValue_ForUnknownItem() throws SnIndexerException {
        assertNull(coveoObjectTypeSnIndexerValueProvider.getFieldValue(null, null, new CategoryModel(), null));
    }
}