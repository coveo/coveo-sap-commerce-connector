package com.coveo.service.impl;

import com.coveo.constants.SearchprovidercoveosearchservicesConstants;
import com.coveo.pushapiclient.exceptions.NoOpenFileContainerException;
import com.coveo.pushapiclient.exceptions.NoOpenStreamException;
import com.coveo.stream.service.impl.CoveoAvailabilityStreamServiceStrategy;
import com.coveo.stream.service.impl.CoveoRebuildStreamService;
import com.coveo.stream.service.impl.CoveoProductStreamServiceStrategy;
import com.coveo.stream.service.impl.CoveoUpdateStreamService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.searchservices.admin.data.SnIndexConfiguration;
import de.hybris.platform.searchservices.admin.data.SnIndexType;
import de.hybris.platform.searchservices.core.SnException;
import de.hybris.platform.searchservices.core.service.SnContext;
import de.hybris.platform.searchservices.document.data.SnDocumentBatchOperationResponse;
import de.hybris.platform.searchservices.document.data.SnDocumentBatchRequest;
import de.hybris.platform.searchservices.enums.SnIndexerOperationStatus;
import de.hybris.platform.searchservices.enums.SnIndexerOperationType;
import de.hybris.platform.searchservices.index.data.SnIndex;
import de.hybris.platform.searchservices.indexer.data.SnIndexerOperation;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.commons.configuration.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@UnitTest
public class CoveoSearchSnSearchProviderTest {

    private static final String INDEX_TYPE_ID = "indexTypeId";
    private static final int DOCS_TO_INDEX = 2;
    private static final String SUPPORTED_AVAILABILITY_TYPES_CODE = "Warehouse,Store,WarehouseStore";

    @Mock
    private SnContext snContext;
    @Mock
    private SnIndexType snIndexType;
    @Mock
    private SnIndexConfiguration snIndexConfiguration;
    @Mock
    private CoveoProductStreamServiceStrategy<CoveoUpdateStreamService> coveoProductUpdateStreamServiceStrategy;
    @Mock
    private CoveoProductStreamServiceStrategy<CoveoRebuildStreamService> coveoProductRebuildStreamServiceStrategy;
    @Mock
    private CoveoAvailabilityStreamServiceStrategy<CoveoUpdateStreamService> coveoAvailabilityUpdateStreamServiceStrategy;
    @Mock
    private CoveoAvailabilityStreamServiceStrategy<CoveoRebuildStreamService> coveoAvailabilityRebuildStreamServiceStrategy;
    @Mock
    private ConfigurationService configurationService;
    @Mock
    private Configuration configuration;

    @InjectMocks
    private final CoveoSearchSnSearchProvider coveoSearchSnSearchProvider = new CoveoSearchSnSearchProvider();

    @Before
    public void setUp() throws Exception {
        when(configurationService.getConfiguration()).thenReturn(configuration);
        when(configuration.getString(SearchprovidercoveosearchservicesConstants.SUPPORTED_AVAILABILITY_TYPES_CODE)).thenReturn(SUPPORTED_AVAILABILITY_TYPES_CODE);

        when(snIndexType.getId()).thenReturn(INDEX_TYPE_ID);
        when(snContext.getIndexType()).thenReturn(snIndexType);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(SearchprovidercoveosearchservicesConstants.COVEO_PRODUCT_REBUILD_STREAM_SERVICES_KEY, coveoProductRebuildStreamServiceStrategy);
        attributes.put(SearchprovidercoveosearchservicesConstants.COVEO_PRODUCT_UPDATE_STREAM_SERVICES_KEY, coveoProductUpdateStreamServiceStrategy);
        attributes.put(SearchprovidercoveosearchservicesConstants.COVEO_AVAILABILITY_REBUILD_STREAM_SERVICES_KEY, coveoAvailabilityRebuildStreamServiceStrategy);
        attributes.put(SearchprovidercoveosearchservicesConstants.COVEO_AVAILABILITY_UPDATE_STREAM_SERVICES_KEY, coveoAvailabilityUpdateStreamServiceStrategy);
        when(snContext.getAttributes()).thenReturn(attributes);
        when(snContext.getIndexConfiguration()).thenReturn(snIndexConfiguration);
    }

    @Test
    public void testCreateIndex() throws SnException {
        final SnIndex index = coveoSearchSnSearchProvider.createIndex(snContext);
        assertNotNull(index);
        assertTrue(index.getActive());
        assertEquals(INDEX_TYPE_ID, index.getIndexTypeId());
        assertEquals(INDEX_TYPE_ID, index.getId());
    }

    @Test
    public void testExecuteDocumentBatch_AvailabilityFullIndexOperation() throws SnException {
        when(snIndexType.getItemComposedType()).thenReturn("Warehouse");
        List<SnDocumentBatchOperationResponse> responses = new ArrayList<>();
        when(coveoAvailabilityRebuildStreamServiceStrategy.pushDocuments(anyList())).thenReturn(responses);
        coveoSearchSnSearchProvider.createIndexerOperation(snContext, SnIndexerOperationType.FULL, DOCS_TO_INDEX);
        SnDocumentBatchRequest request = new SnDocumentBatchRequest();
        request.setRequests(new ArrayList<>());
        coveoSearchSnSearchProvider.executeDocumentBatch(snContext, INDEX_TYPE_ID + SnIndexerOperationType.FULL, request, INDEX_TYPE_ID + SnIndexerOperationType.FULL);
        verify(coveoAvailabilityRebuildStreamServiceStrategy, times(1)).pushDocuments(Collections.emptyList());
        verify(coveoAvailabilityUpdateStreamServiceStrategy, times(0)).pushDocuments(Collections.emptyList());
    }

    @Test
    public void testExecuteDocumentBatch_AvailabilityIncrementalIndexOperation() throws SnException {
        when(snIndexType.getItemComposedType()).thenReturn("Warehouse");
        List<SnDocumentBatchOperationResponse> responses = new ArrayList<>();
        when(coveoAvailabilityUpdateStreamServiceStrategy.pushDocuments(anyList())).thenReturn(responses);
        coveoSearchSnSearchProvider.createIndexerOperation(snContext, SnIndexerOperationType.INCREMENTAL, DOCS_TO_INDEX);
        SnDocumentBatchRequest request = new SnDocumentBatchRequest();
        request.setRequests(new ArrayList<>());
        coveoSearchSnSearchProvider.executeDocumentBatch(snContext, INDEX_TYPE_ID + SnIndexerOperationType.INCREMENTAL, request, INDEX_TYPE_ID + SnIndexerOperationType.INCREMENTAL);
        verify(coveoAvailabilityRebuildStreamServiceStrategy, times(0)).pushDocuments(Collections.emptyList());
        verify(coveoAvailabilityUpdateStreamServiceStrategy, times(1)).pushDocuments(Collections.emptyList());
    }

    @Test
    public void testCommit_AvailabilityFullIndexOperation() throws SnException, NoOpenStreamException, IOException, NoOpenFileContainerException, InterruptedException {
        when(snIndexType.getItemComposedType()).thenReturn("Warehouse");
        coveoSearchSnSearchProvider.createIndexerOperation(snContext, SnIndexerOperationType.FULL, DOCS_TO_INDEX);
        coveoSearchSnSearchProvider.commit(snContext, INDEX_TYPE_ID + SnIndexerOperationType.FULL);
        verify(coveoAvailabilityRebuildStreamServiceStrategy, times(1)).closeServices();
        verify(coveoAvailabilityUpdateStreamServiceStrategy, times(0)).closeServices();
    }

    @Test
    public void testCommit_AvailabilityIncrementalIndexOperation() throws SnException, NoOpenStreamException, IOException, NoOpenFileContainerException, InterruptedException {
        when(snIndexType.getItemComposedType()).thenReturn("Warehouse");
        coveoSearchSnSearchProvider.createIndexerOperation(snContext, SnIndexerOperationType.INCREMENTAL, DOCS_TO_INDEX);
        coveoSearchSnSearchProvider.commit(snContext, INDEX_TYPE_ID + SnIndexerOperationType.INCREMENTAL);
        verify(coveoAvailabilityRebuildStreamServiceStrategy, times(0)).closeServices();
        verify(coveoAvailabilityUpdateStreamServiceStrategy, times(1)).closeServices();
    }

    @Test
    public void testCreateIndexerOperation() throws SnException {
        when(snIndexType.getItemComposedType()).thenReturn("Product");
        final SnIndexerOperation operation = coveoSearchSnSearchProvider.createIndexerOperation(snContext, SnIndexerOperationType.FULL, DOCS_TO_INDEX);
        assertEquals(INDEX_TYPE_ID, operation.getIndexTypeId());
        assertEquals(INDEX_TYPE_ID + SnIndexerOperationType.FULL, operation.getIndexId());
        assertEquals(INDEX_TYPE_ID + SnIndexerOperationType.FULL, operation.getId());
        assertEquals(SnIndexerOperationType.FULL, operation.getOperationType());
        assertEquals(SnIndexerOperationStatus.RUNNING, operation.getStatus());
    }

    @Test
    public void testExecuteDocumentBatch_ProductFullIndexOperation() throws SnException {
        when(snIndexType.getItemComposedType()).thenReturn("Product");
        List<SnDocumentBatchOperationResponse> responses = new ArrayList<>();
        when(coveoProductRebuildStreamServiceStrategy.pushDocuments(anyList())).thenReturn(responses);
        coveoSearchSnSearchProvider.createIndexerOperation(snContext, SnIndexerOperationType.FULL, DOCS_TO_INDEX);
        SnDocumentBatchRequest request = new SnDocumentBatchRequest();
        request.setRequests(new ArrayList<>());
        coveoSearchSnSearchProvider.executeDocumentBatch(snContext, INDEX_TYPE_ID + SnIndexerOperationType.FULL, request, INDEX_TYPE_ID + SnIndexerOperationType.FULL);
        verify(coveoProductRebuildStreamServiceStrategy, times(1)).pushDocuments(Collections.emptyList());
        verify(coveoProductUpdateStreamServiceStrategy, times(0)).pushDocuments(Collections.emptyList());
    }

    @Test
    public void testExecuteDocumentBatch_ProductIncrementalIndexOperation() throws SnException {
        when(snIndexType.getItemComposedType()).thenReturn("Product");
        List<SnDocumentBatchOperationResponse> responses = new ArrayList<>();
        when(coveoProductUpdateStreamServiceStrategy.pushDocuments(anyList())).thenReturn(responses);
        coveoSearchSnSearchProvider.createIndexerOperation(snContext, SnIndexerOperationType.INCREMENTAL, DOCS_TO_INDEX);
        SnDocumentBatchRequest request = new SnDocumentBatchRequest();
        request.setRequests(new ArrayList<>());
        coveoSearchSnSearchProvider.executeDocumentBatch(snContext, INDEX_TYPE_ID + SnIndexerOperationType.INCREMENTAL, request, INDEX_TYPE_ID + SnIndexerOperationType.INCREMENTAL);
        verify(coveoProductRebuildStreamServiceStrategy, times(0)).pushDocuments(Collections.emptyList());
        verify(coveoProductUpdateStreamServiceStrategy, times(1)).pushDocuments(Collections.emptyList());
    }

    @Test
    public void testCommit_ProductFullIndexOperation() throws SnException, NoOpenStreamException, IOException, NoOpenFileContainerException, InterruptedException {
        when(snIndexType.getItemComposedType()).thenReturn("Product");
        coveoSearchSnSearchProvider.createIndexerOperation(snContext, SnIndexerOperationType.FULL, DOCS_TO_INDEX);
        coveoSearchSnSearchProvider.commit(snContext, INDEX_TYPE_ID + SnIndexerOperationType.FULL);
        verify(coveoProductRebuildStreamServiceStrategy, times(1)).closeServices();
        verify(coveoProductUpdateStreamServiceStrategy, times(0)).closeServices();
    }

    @Test
    public void testCommit_ProductIncrementalIndexOperation() throws SnException, NoOpenStreamException, IOException, NoOpenFileContainerException, InterruptedException {
        when(snIndexType.getItemComposedType()).thenReturn("Product");
        coveoSearchSnSearchProvider.createIndexerOperation(snContext, SnIndexerOperationType.INCREMENTAL, DOCS_TO_INDEX);
        coveoSearchSnSearchProvider.commit(snContext, INDEX_TYPE_ID + SnIndexerOperationType.INCREMENTAL);
        verify(coveoProductRebuildStreamServiceStrategy, times(0)).closeServices();
        verify(coveoProductUpdateStreamServiceStrategy, times(1)).closeServices();
    }
}