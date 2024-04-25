package com.coveo.service.impl;

import com.coveo.constants.SearchprovidercoveosearchservicesConstants;
import com.coveo.searchservices.admin.data.CoveoSnCountry;
import com.coveo.searchservices.admin.data.CoveoSnIndexConfiguration;
import com.coveo.searchservices.data.CoveoCatalogObjectType;
import com.coveo.searchservices.data.CoveoSearchSnSearchProviderConfiguration;
import com.coveo.searchservices.data.CoveoSource;
import com.coveo.stream.service.CoveoStreamServiceStrategy;
import com.coveo.stream.service.impl.CoveoAvailabilityStreamServiceStrategy;
import com.coveo.stream.service.impl.CoveoProductStreamServiceStrategy;
import com.coveo.stream.service.impl.CoveoRebuildStreamService;
import com.coveo.stream.service.impl.CoveoUpdateStreamService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.searchservices.admin.data.SnCurrency;
import de.hybris.platform.searchservices.admin.data.SnIndexType;
import de.hybris.platform.searchservices.admin.data.SnLanguage;
import de.hybris.platform.searchservices.indexer.service.SnIndexerRequest;
import de.hybris.platform.searchservices.indexer.service.impl.DefaultSnIndexerContext;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.commons.configuration.Configuration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.coveo.constants.SearchprovidercoveosearchservicesConstants.COSAP_CONNECTOR_USER_AGENT;
import static com.coveo.constants.SearchprovidercoveosearchservicesConstants.COSAP_CONNECTOR_USER_AGENT_PROPERTY;
import static com.coveo.constants.SearchprovidercoveosearchservicesConstants.SUPPORTED_AVAILABILITY_TYPES_CODE;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@UnitTest
public class CoveoSnIndexerContextFactoryTest {

    private static final String SUPPORTED_AVAILABILITY_TYPES_CODE_VALUES = "Warehouse,Store,WarehouseStore";
    private static final String PRODUCT_COMPOSED_TYPE = "Product";
    private static final String WAREHOUSE_COMPOSED_TYPE = "Warehouse";
    private static final String USER_AGENT_HEADER = "TestAgent/v3";

    DefaultSnIndexerContext context;
    SnIndexType indexType;

    @Mock
    private ConfigurationService configurationService;
    @Mock
    private Configuration configuration;

    @InjectMocks
    CoveoSnIndexerContextFactory coveoSnIndexerContextFactory = new CoveoSnIndexerContextFactory();

    @Before
    public void setUp() {
        when(configurationService.getConfiguration()).thenReturn(configuration);
        when(configuration.getString(COSAP_CONNECTOR_USER_AGENT_PROPERTY, COSAP_CONNECTOR_USER_AGENT)).thenReturn(USER_AGENT_HEADER);
        when(configuration.getString(SUPPORTED_AVAILABILITY_TYPES_CODE)).thenReturn(SUPPORTED_AVAILABILITY_TYPES_CODE_VALUES);
        context = new DefaultSnIndexerContext();
        CoveoSnIndexConfiguration indexConfiguration = new CoveoSnIndexConfiguration();
        CoveoSearchSnSearchProviderConfiguration coveoSearchSnSearchProviderConfiguration = getCoveoSearchSnSearchProviderConfiguration();
        indexConfiguration.setSearchProviderConfiguration(coveoSearchSnSearchProviderConfiguration);
        indexConfiguration.setCountries(new ArrayList<>(Collections.singletonList(mock(CoveoSnCountry.class))));
        indexConfiguration.setCurrencies(new ArrayList<>(Collections.singletonList(mock(SnCurrency.class))));
        indexConfiguration.setLanguages(new ArrayList<>(Collections.singletonList(mock(SnLanguage.class))));
        context.setIndexConfiguration(indexConfiguration);
        indexType = new SnIndexType();
        context.setIndexType(indexType);
    }

    private static CoveoSearchSnSearchProviderConfiguration getCoveoSearchSnSearchProviderConfiguration() {
        CoveoSearchSnSearchProviderConfiguration coveoSearchSnSearchProviderConfiguration = new CoveoSearchSnSearchProviderConfiguration();
        List<CoveoSource> coveoSources = new ArrayList<>();
        CoveoSource coveoProductSource = new CoveoSource();
        coveoProductSource.setDestinationId("destinationId");
        coveoProductSource.setDestinationTargetUrl("https://api.cloud.coveo.com/push/v1/organizations/organizationId/sources/sourceId/");
        coveoProductSource.setDestinationSecret("destinationSecret");
        coveoProductSource.setObjectType(CoveoCatalogObjectType.PRODUCTANDVARIANT);
        CoveoSource coveoAvailabilitySource = new CoveoSource();
        coveoAvailabilitySource.setDestinationId("destinationId");
        coveoAvailabilitySource.setDestinationTargetUrl("https://api.cloud.coveo.com/push/v1/organizations/organizationId/sources/sourceId/");
        coveoAvailabilitySource.setDestinationSecret("destinationSecret");
        coveoAvailabilitySource.setObjectType(CoveoCatalogObjectType.AVAILABILITY);
        coveoSources.add(coveoAvailabilitySource);
        coveoSearchSnSearchProviderConfiguration.setSources(coveoSources);
        return coveoSearchSnSearchProviderConfiguration;
    }

    @After
    public void tearDown() {
        context = null;
    }

    @Test
    public void testPopulateIndexerContextForProduct() {
        context.getIndexType().setItemComposedType(PRODUCT_COMPOSED_TYPE);
        coveoSnIndexerContextFactory.populateIndexerContext(context, mock(SnIndexerRequest.class));
        CoveoProductStreamServiceStrategy<CoveoUpdateStreamService> updateStreamService = (CoveoProductStreamServiceStrategy<CoveoUpdateStreamService>) context.getAttributes().get(SearchprovidercoveosearchservicesConstants.COVEO_PRODUCT_UPDATE_STREAM_SERVICES_KEY);
        CoveoProductStreamServiceStrategy<CoveoRebuildStreamService> rebuildStreamService = (CoveoProductStreamServiceStrategy<CoveoRebuildStreamService>) context.getAttributes().get(SearchprovidercoveosearchservicesConstants.COVEO_PRODUCT_REBUILD_STREAM_SERVICES_KEY);
        CoveoAvailabilityStreamServiceStrategy<CoveoRebuildStreamService> availabilityStreamServiceStrategy = (CoveoAvailabilityStreamServiceStrategy<CoveoRebuildStreamService>) context.getAttributes().get(SearchprovidercoveosearchservicesConstants.COVEO_AVAILABILITY_REBUILD_STREAM_SERVICES_KEY);
        CoveoAvailabilityStreamServiceStrategy<CoveoUpdateStreamService> availabilityUpdateStreamServiceStrategy = (CoveoAvailabilityStreamServiceStrategy<CoveoUpdateStreamService>) context.getAttributes().get(SearchprovidercoveosearchservicesConstants.COVEO_AVAILABILITY_UPDATE_STREAM_SERVICES_KEY);
        assertNotNull(updateStreamService);
        assertNotNull(rebuildStreamService);
        assertNull(availabilityStreamServiceStrategy);
        assertNull(availabilityUpdateStreamServiceStrategy);
    }

    @Test
    public void testPopulateIndexerContextForWarehouse() {
        context.getIndexType().setItemComposedType(WAREHOUSE_COMPOSED_TYPE);
        coveoSnIndexerContextFactory.populateIndexerContext(context, mock(SnIndexerRequest.class));
        CoveoProductStreamServiceStrategy<CoveoUpdateStreamService> updateStreamService = (CoveoProductStreamServiceStrategy<CoveoUpdateStreamService>) context.getAttributes().get(SearchprovidercoveosearchservicesConstants.COVEO_PRODUCT_UPDATE_STREAM_SERVICES_KEY);
        CoveoProductStreamServiceStrategy<CoveoRebuildStreamService> rebuildStreamService = (CoveoProductStreamServiceStrategy<CoveoRebuildStreamService>) context.getAttributes().get(SearchprovidercoveosearchservicesConstants.COVEO_PRODUCT_REBUILD_STREAM_SERVICES_KEY);
        CoveoAvailabilityStreamServiceStrategy<CoveoRebuildStreamService> availabilityStreamServiceStrategy = (CoveoAvailabilityStreamServiceStrategy<CoveoRebuildStreamService>) context.getAttributes().get(SearchprovidercoveosearchservicesConstants.COVEO_AVAILABILITY_REBUILD_STREAM_SERVICES_KEY);
        CoveoAvailabilityStreamServiceStrategy<CoveoUpdateStreamService> availabilityUpdateStreamServiceStrategy = (CoveoAvailabilityStreamServiceStrategy<CoveoUpdateStreamService>) context.getAttributes().get(SearchprovidercoveosearchservicesConstants.COVEO_AVAILABILITY_UPDATE_STREAM_SERVICES_KEY);
        assertNull(updateStreamService);
        assertNull(rebuildStreamService);
        assertNotNull(availabilityStreamServiceStrategy);
        assertNotNull(availabilityUpdateStreamServiceStrategy);
    }
}