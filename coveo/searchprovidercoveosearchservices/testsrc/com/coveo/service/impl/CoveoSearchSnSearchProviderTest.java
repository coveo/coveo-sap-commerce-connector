package com.coveo.service.impl;

import com.coveo.constants.SearchprovidercoveosearchservicesConstants;
import com.coveo.stream.service.impl.CoveoAvailabilityStreamServiceStrategy;
import com.coveo.stream.service.impl.CoveoProductStreamServiceStrategy;
import com.coveo.stream.service.impl.CoveoRebuildStreamService;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@UnitTest
class CoveoSearchSnSearchProviderTest {

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

    void setUpTestContext(Boolean singalSourceEnabled) {
        when(configurationService.getConfiguration()).thenReturn(configuration);
        when(configuration.getString(SearchprovidercoveosearchservicesConstants.SUPPORTED_AVAILABILITY_TYPES_CODE)).thenReturn(
                SUPPORTED_AVAILABILITY_TYPES_CODE);

        when(snIndexType.getId()).thenReturn(INDEX_TYPE_ID);
        when(snContext.getIndexType()).thenReturn(snIndexType);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(SearchprovidercoveosearchservicesConstants.COVEO_PRODUCT_REBUILD_STREAM_SERVICES_KEY,
                       coveoProductRebuildStreamServiceStrategy);
        attributes.put(SearchprovidercoveosearchservicesConstants.COVEO_PRODUCT_UPDATE_STREAM_SERVICES_KEY,
                       coveoProductUpdateStreamServiceStrategy);
        attributes.put(SearchprovidercoveosearchservicesConstants.COVEO_AVAILABILITY_REBUILD_STREAM_SERVICES_KEY,
                       coveoAvailabilityRebuildStreamServiceStrategy);
        attributes.put(SearchprovidercoveosearchservicesConstants.COVEO_AVAILABILITY_UPDATE_STREAM_SERVICES_KEY,
                       coveoAvailabilityUpdateStreamServiceStrategy);
        attributes.put(SearchprovidercoveosearchservicesConstants.COVEO_SINGLE_SOURCE_ENABLED_KEY,
                       singalSourceEnabled);
        when(snContext.getAttributes()).thenReturn(attributes);
    }

    private SnContext createContext(CoveoProductStreamServiceStrategy<?> rebuildStrategy,
                                    CoveoProductStreamServiceStrategy<?> updateStrategy,
                                    String composedType) {
        SnContext context = mock(SnContext.class);
        SnIndexType indexType = mock(SnIndexType.class);
        when(indexType.getId()).thenReturn(INDEX_TYPE_ID);
        when(indexType.getItemComposedType()).thenReturn(composedType);
        when(context.getIndexType()).thenReturn(indexType);

        when(configurationService.getConfiguration()).thenReturn(configuration);
        when(configuration.getString(SearchprovidercoveosearchservicesConstants.SUPPORTED_AVAILABILITY_TYPES_CODE)).thenReturn(
                SUPPORTED_AVAILABILITY_TYPES_CODE);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(SearchprovidercoveosearchservicesConstants.COVEO_PRODUCT_REBUILD_STREAM_SERVICES_KEY, rebuildStrategy);
        attributes.put(SearchprovidercoveosearchservicesConstants.COVEO_PRODUCT_UPDATE_STREAM_SERVICES_KEY, updateStrategy);
        attributes.put(SearchprovidercoveosearchservicesConstants.COVEO_SINGLE_SOURCE_ENABLED_KEY, Boolean.FALSE);
        when(context.getAttributes()).thenReturn(attributes);
        return context;
    }

    @Test
    void testCreateIndex() throws SnException {
        when(snIndexType.getId()).thenReturn(INDEX_TYPE_ID);
        when(snContext.getIndexType()).thenReturn(snIndexType);
        final SnIndex index = coveoSearchSnSearchProvider.createIndex(snContext);
        assertNotNull(index);
        assertTrue(index.getActive());
        assertEquals(INDEX_TYPE_ID, index.getIndexTypeId());
        assertEquals(INDEX_TYPE_ID, index.getId());
    }

    @Test
    void testExecuteDocumentBatch_AvailabilityFullIndexOperation() throws Exception {
        setUpTestContext(Boolean.FALSE);
        when(snIndexType.getItemComposedType()).thenReturn("Warehouse");
        List<SnDocumentBatchOperationResponse> responses = new ArrayList<>();
        when(coveoAvailabilityRebuildStreamServiceStrategy.pushDocuments(anyList(), eq(Boolean.FALSE))).thenReturn(responses);
        SnIndexerOperation operation = coveoSearchSnSearchProvider.createIndexerOperation(snContext, SnIndexerOperationType.FULL, DOCS_TO_INDEX);
        SnDocumentBatchRequest request = new SnDocumentBatchRequest();
        request.setRequests(new ArrayList<>());
        coveoSearchSnSearchProvider.executeDocumentBatch(snContext, operation.getIndexId(), request, operation.getId());
        verify(coveoAvailabilityRebuildStreamServiceStrategy, times(1)).pushDocuments(Collections.emptyList(), Boolean.FALSE);
        verify(coveoAvailabilityUpdateStreamServiceStrategy, times(0)).pushDocuments(Collections.emptyList(), Boolean.FALSE);
    }

    @Test
    void testExecuteDocumentBatch_AvailabilityIncrementalIndexOperation() throws Exception {
        setUpTestContext(Boolean.TRUE);
        when(snIndexType.getItemComposedType()).thenReturn("Warehouse");
        List<SnDocumentBatchOperationResponse> responses = new ArrayList<>();
        when(coveoAvailabilityUpdateStreamServiceStrategy.pushDocuments(anyList(), eq(Boolean.TRUE))).thenReturn(responses);
        SnIndexerOperation operation = coveoSearchSnSearchProvider.createIndexerOperation(snContext, SnIndexerOperationType.INCREMENTAL, DOCS_TO_INDEX);
        SnDocumentBatchRequest request = new SnDocumentBatchRequest();
        request.setRequests(new ArrayList<>());
        coveoSearchSnSearchProvider.executeDocumentBatch(snContext, operation.getIndexId(), request, operation.getId());
        verify(coveoAvailabilityRebuildStreamServiceStrategy, times(0)).pushDocuments(Collections.emptyList(), Boolean.TRUE);
        verify(coveoAvailabilityUpdateStreamServiceStrategy, times(1)).pushDocuments(Collections.emptyList(), Boolean.TRUE);
    }

    @Test
    void testCommit_AvailabilityFullIndexOperation() throws Exception {
        setUpTestContext(Boolean.FALSE);
        when(snIndexType.getItemComposedType()).thenReturn("Warehouse");
        SnIndexerOperation operation = coveoSearchSnSearchProvider.createIndexerOperation(snContext, SnIndexerOperationType.FULL, DOCS_TO_INDEX);
        coveoSearchSnSearchProvider.commit(snContext, operation.getIndexId());
        verify(coveoAvailabilityRebuildStreamServiceStrategy, times(1)).closeServices();
        verify(coveoAvailabilityUpdateStreamServiceStrategy, times(0)).closeServices();
    }

    @Test
    void testCommit_AvailabilityIncrementalIndexOperation() throws Exception {
        setUpTestContext(Boolean.FALSE);
        when(snIndexType.getItemComposedType()).thenReturn("Warehouse");
        SnIndexerOperation operation = coveoSearchSnSearchProvider.createIndexerOperation(snContext, SnIndexerOperationType.INCREMENTAL, DOCS_TO_INDEX);
        coveoSearchSnSearchProvider.commit(snContext, operation.getIndexId());
        verify(coveoAvailabilityRebuildStreamServiceStrategy, times(0)).closeServices();
        verify(coveoAvailabilityUpdateStreamServiceStrategy, times(1)).closeServices();
    }

    @Test
    void testCreateIndexerOperation() throws Exception {
        setUpTestContext(Boolean.FALSE);
        when(snIndexType.getItemComposedType()).thenReturn("Product");
        final SnIndexerOperation operation = coveoSearchSnSearchProvider.createIndexerOperation(snContext, SnIndexerOperationType.FULL, DOCS_TO_INDEX);
        assertEquals(INDEX_TYPE_ID, operation.getIndexTypeId());
        assertTrue(operation.getId().startsWith(INDEX_TYPE_ID + SnIndexerOperationType.FULL),
                "Operation ID should start with indexTypeId + operationType");
        assertEquals(operation.getId(), operation.getIndexId(),
                "indexId should equal id so commit() can locate the correct stream");
        assertEquals(SnIndexerOperationType.FULL, operation.getOperationType());
        assertEquals(SnIndexerOperationStatus.RUNNING, operation.getStatus());
    }

    @Test
    void testExecuteDocumentBatch_ProductFullIndexOperation() throws Exception {
        setUpTestContext(Boolean.FALSE);
        when(snIndexType.getItemComposedType()).thenReturn("Product");
        List<SnDocumentBatchOperationResponse> responses = new ArrayList<>();
        when(coveoProductRebuildStreamServiceStrategy.pushDocuments(anyList(), eq(Boolean.FALSE))).thenReturn(responses);
        SnIndexerOperation operation = coveoSearchSnSearchProvider.createIndexerOperation(snContext, SnIndexerOperationType.FULL, DOCS_TO_INDEX);
        SnDocumentBatchRequest request = new SnDocumentBatchRequest();
        request.setRequests(new ArrayList<>());
        coveoSearchSnSearchProvider.executeDocumentBatch(snContext, operation.getIndexId(), request, operation.getId());
        verify(coveoProductRebuildStreamServiceStrategy, times(1)).pushDocuments(Collections.emptyList(), Boolean.FALSE);
        verify(coveoProductUpdateStreamServiceStrategy, times(0)).pushDocuments(Collections.emptyList(), Boolean.FALSE);
    }

    @Test
    void testExecuteDocumentBatch_ProductIncrementalIndexOperation() throws Exception {
        setUpTestContext(Boolean.FALSE);
        when(snIndexType.getItemComposedType()).thenReturn("Product");
        List<SnDocumentBatchOperationResponse> responses = new ArrayList<>();
        when(coveoProductUpdateStreamServiceStrategy.pushDocuments(anyList(), eq(Boolean.FALSE))).thenReturn(responses);
        SnIndexerOperation operation = coveoSearchSnSearchProvider.createIndexerOperation(snContext, SnIndexerOperationType.INCREMENTAL, DOCS_TO_INDEX);
        SnDocumentBatchRequest request = new SnDocumentBatchRequest();
        request.setRequests(new ArrayList<>());
        coveoSearchSnSearchProvider.executeDocumentBatch(snContext, operation.getIndexId(), request, operation.getId());
        verify(coveoProductRebuildStreamServiceStrategy, times(0)).pushDocuments(Collections.emptyList(), Boolean.FALSE);
        verify(coveoProductUpdateStreamServiceStrategy, times(1)).pushDocuments(Collections.emptyList(), Boolean.FALSE);
    }

    @Test
    void testCommit_ProductFullIndexOperation() throws Exception {
        setUpTestContext(Boolean.FALSE);
        when(snIndexType.getItemComposedType()).thenReturn("Product");
        SnIndexerOperation operation = coveoSearchSnSearchProvider.createIndexerOperation(snContext, SnIndexerOperationType.FULL, DOCS_TO_INDEX);
        coveoSearchSnSearchProvider.commit(snContext, operation.getIndexId());
        verify(coveoProductRebuildStreamServiceStrategy, times(1)).closeServices();
        verify(coveoProductUpdateStreamServiceStrategy, times(0)).closeServices();
    }

    @Test
    void testCommit_ProductIncrementalIndexOperation() throws Exception {
        setUpTestContext(Boolean.FALSE);
        when(snIndexType.getItemComposedType()).thenReturn("Product");
        SnIndexerOperation operation = coveoSearchSnSearchProvider.createIndexerOperation(snContext, SnIndexerOperationType.INCREMENTAL, DOCS_TO_INDEX);
        coveoSearchSnSearchProvider.commit(snContext, operation.getIndexId());
        verify(coveoProductRebuildStreamServiceStrategy, times(0)).closeServices();
        verify(coveoProductUpdateStreamServiceStrategy, times(1)).closeServices();
    }

    @Test
    void testConcurrentFullIndexRuns_EachRunCommitsItsOwnStream() throws Exception {
        CoveoProductStreamServiceStrategy<CoveoRebuildStreamService> run1Strategy = mock(CoveoProductStreamServiceStrategy.class);
        CoveoProductStreamServiceStrategy<CoveoRebuildStreamService> run2Strategy = mock(CoveoProductStreamServiceStrategy.class);
        CoveoProductStreamServiceStrategy<CoveoUpdateStreamService> updateStrategy = mock(CoveoProductStreamServiceStrategy.class);

        SnContext run1Context = createContext(run1Strategy, updateStrategy, "Product");
        SnContext run2Context = createContext(run2Strategy, updateStrategy, "Product");

        SnIndexerOperation run1Op = coveoSearchSnSearchProvider.createIndexerOperation(run1Context, SnIndexerOperationType.FULL, DOCS_TO_INDEX);
        SnIndexerOperation run2Op = coveoSearchSnSearchProvider.createIndexerOperation(run2Context, SnIndexerOperationType.FULL, DOCS_TO_INDEX);

        assertNotEquals(run1Op.getId(), run2Op.getId(),
                "Concurrent runs must have unique operation IDs to avoid map key collision");

        coveoSearchSnSearchProvider.commit(run1Context, run1Op.getIndexId());
        verify(run1Strategy, times(1)).closeServices();
        verify(run2Strategy, times(0)).closeServices();

        coveoSearchSnSearchProvider.commit(run2Context, run2Op.getIndexId());
        verify(run2Strategy, times(1)).closeServices();
    }

    @Test
    void testConcurrentIncrementalIndexRuns_EachRunCommitsItsOwnStream() throws Exception {
        CoveoProductStreamServiceStrategy<CoveoRebuildStreamService> rebuildStrategy = mock(CoveoProductStreamServiceStrategy.class);
        CoveoProductStreamServiceStrategy<CoveoUpdateStreamService> run1Strategy = mock(CoveoProductStreamServiceStrategy.class);
        CoveoProductStreamServiceStrategy<CoveoUpdateStreamService> run2Strategy = mock(CoveoProductStreamServiceStrategy.class);

        SnContext run1Context = createContext(rebuildStrategy, run1Strategy, "Product");
        SnContext run2Context = createContext(rebuildStrategy, run2Strategy, "Product");

        SnIndexerOperation run1Op = coveoSearchSnSearchProvider.createIndexerOperation(run1Context, SnIndexerOperationType.INCREMENTAL, DOCS_TO_INDEX);
        SnIndexerOperation run2Op = coveoSearchSnSearchProvider.createIndexerOperation(run2Context, SnIndexerOperationType.INCREMENTAL, DOCS_TO_INDEX);

        assertNotEquals(run1Op.getId(), run2Op.getId(),
                "Concurrent runs must have unique operation IDs to avoid map key collision");

        coveoSearchSnSearchProvider.commit(run1Context, run1Op.getIndexId());
        verify(run1Strategy, times(1)).closeServices();
        verify(run2Strategy, times(0)).closeServices();

        // Run 2 commits — must close only Run 2's stream
        coveoSearchSnSearchProvider.commit(run2Context, run2Op.getIndexId());
        verify(run2Strategy, times(1)).closeServices();
    }
}