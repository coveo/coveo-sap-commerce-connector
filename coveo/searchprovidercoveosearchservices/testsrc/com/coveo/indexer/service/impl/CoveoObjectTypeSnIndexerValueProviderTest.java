package com.coveo.indexer.service.impl;

import com.coveo.constants.SearchprovidercoveosearchservicesConstants;
import com.coveo.core.model.ApparelProductModel;
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
    ConfigurationService configurationService;

    @Mock
    Configuration configuration;

    @InjectMocks
    CoveoObjectTypeSnIndexerValueProvider coveoObjectTypeSnIndexerValueProvider = new CoveoObjectTypeSnIndexerValueProvider();

    @Before
    public void setUp() {
        when(configurationService.getConfiguration()).thenReturn(configuration);

        when(configuration.getStringArray(SearchprovidercoveosearchservicesConstants.SUPPORTED_PRODUCT_TYPES_CODE)).thenReturn(new String[]{"Product", "ApparelProduct"});
        when(configuration.getStringArray(SearchprovidercoveosearchservicesConstants.SUPPORTED_VARIANT_TYPES_CODE)).thenReturn(new String[]{"VariantProduct"});
        when(configuration.getStringArray(SearchprovidercoveosearchservicesConstants.SUPPORTED_AVAILABILITY_TYPES_CODE)).thenReturn(new String[]{"Warehouse"});
    }

    @Test
    public void testGetFieldValue_ForProduct() throws SnIndexerException {
        assertEquals(CoveoObjectTypeSnIndexerValueProvider.PRODUCT_OBJECT_TYPE, coveoObjectTypeSnIndexerValueProvider.getFieldValue(null, null, new ProductModel(), null));
    }

    @Test
    public void testGetFieldValue_ForApparelProduct() throws SnIndexerException {
        assertEquals(CoveoObjectTypeSnIndexerValueProvider.PRODUCT_OBJECT_TYPE, coveoObjectTypeSnIndexerValueProvider.getFieldValue(null, null, new ApparelProductModel(), null));
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