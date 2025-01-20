package com.coveo.stream.service;

import com.coveo.pushapiclient.CatalogSource;
import com.coveo.pushapiclient.DocumentBuilder;
import com.coveo.pushapiclient.exceptions.NoOpenFileContainerException;
import com.coveo.pushapiclient.exceptions.NoOpenStreamException;
import com.coveo.searchservices.data.CoveoSource;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

@UnitTest
public class CoveoAbstractStreamServiceTest {

    private static final String SOURCE_SECRET = "sourceSecret";
    private static final String SOURCE_URL = "https://api.cloud.coveo.com/push/v1/organizations/organizationId/sources/sourceId/";

    CoveoSource coveoSource = new CoveoSource();

    @Before
    public void setUp() {
        coveoSource.setDestinationSecret(SOURCE_SECRET);
        coveoSource.setDestinationTargetUrl(SOURCE_URL);
    }

    @Test
    public void testCreateCatalogSource() {
        TestCoveoAbstractStreamService testCoveoAbstractStreamService = new TestCoveoAbstractStreamService();
        CatalogSource catalogSource = testCoveoAbstractStreamService.createCatalogSource(coveoSource);
        assertEquals(SOURCE_SECRET, catalogSource.getApiKey());
    }

    @Test
    public void testGetCoveoSource() {
        TestCoveoAbstractStreamService testCoveoAbstractStreamService = new TestCoveoAbstractStreamService();
        testCoveoAbstractStreamService.init(coveoSource, new String[]{});
        assertEquals(coveoSource, testCoveoAbstractStreamService.getCoveoSource());
    }

    private static class TestCoveoAbstractStreamService extends CoveoAbstractStreamService<Object> {
        public TestCoveoAbstractStreamService() {
            super();
        }

        @Override
        protected Object createStreamService(CatalogSource catalogSource, String[] userAgents) {
            return null;
        }

        @Override
        protected void init(CoveoSource coveoSource, String[] userAgents) {
            this.coveoSource = coveoSource;
        }
        
        @Override
        public void pushDocument(DocumentBuilder document) throws IOException, InterruptedException {

        }

        @Override
        public void closeStream() throws NoOpenStreamException, IOException, InterruptedException, NoOpenFileContainerException {

        }
    }
}