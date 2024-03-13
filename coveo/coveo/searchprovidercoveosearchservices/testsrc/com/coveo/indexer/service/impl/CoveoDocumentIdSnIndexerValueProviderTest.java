package com.coveo.indexer.service.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.searchservices.admin.data.SnField;
import de.hybris.platform.searchservices.core.service.SnExpressionEvaluator;
import de.hybris.platform.searchservices.indexer.SnIndexerException;
import de.hybris.platform.searchservices.indexer.service.SnIndexerFieldWrapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@UnitTest
public class CoveoDocumentIdSnIndexerValueProviderTest {

    private static final String PREFIX_PARAM = "prefix";
    private static final String PREFIX_PARAM_VALUE = "document";
    private static final String URI_ELEMENT = "://";
    private static final String CODE = "code";

    @Mock
    SnIndexerFieldWrapper fieldWrapper;

    @Mock
    SnExpressionEvaluator snExpressionEvaluator;

    @InjectMocks
    CoveoDocumentIdSnIndexerValueProvider coveoDocumentIdSnIndexerValueProvider;

    @Before
    public void setUp() throws Exception {
        when(fieldWrapper.getField()).thenReturn(new SnField());
        when(snExpressionEvaluator.evaluate(any(), any())).thenReturn(CODE);
    }

    @Test
    public void getFieldValue() throws SnIndexerException {
        Map<String, String> parameters = Map.of(PREFIX_PARAM, PREFIX_PARAM_VALUE);
        when(fieldWrapper.getValueProviderParameters()).thenReturn(parameters);
        Object value = coveoDocumentIdSnIndexerValueProvider.getFieldValue(null, fieldWrapper, null, null);
        assertEquals(PREFIX_PARAM_VALUE + URI_ELEMENT + CODE, value);
    }

    @Test
    public void getFieldValue_NoPrefix() throws SnIndexerException {
        Object value = coveoDocumentIdSnIndexerValueProvider.getFieldValue(null, fieldWrapper, null, null);
        assertEquals(URI_ELEMENT + CODE, value);
    }
}