package com.coveo.stream.service.impl;

import com.coveo.pushapiclient.CatalogSource;
import com.coveo.pushapiclient.DocumentBuilder;
import com.coveo.pushapiclient.StreamService;
import com.coveo.pushapiclient.exceptions.NoOpenStreamException;
import com.coveo.searchservices.data.CoveoSource;
import com.coveo.stream.service.CoveoAbstractStreamService;

import java.io.IOException;

import static com.coveo.constants.SearchprovidercoveosearchservicesConstants.COSAP_CONNECTOR_USER_AGENT;

public class CoveoRebuildStreamService extends CoveoAbstractStreamService<StreamService> {

    StreamService rebuildStreamService;

    public CoveoRebuildStreamService(CoveoSource coveoSource, String[] userAgents) {
        super(coveoSource);
        rebuildStreamService = createStreamService(createCatalogSource(coveoSource), userAgents);
    }

    @Override
    protected StreamService createStreamService(CatalogSource catalogSource, String[] userAgents) {
        return new StreamService(catalogSource, userAgents);
    }

    @Override
    public void pushDocument(DocumentBuilder document) throws IOException, InterruptedException {
        rebuildStreamService.add(document);
    }

    @Override
    public void closeStream() throws NoOpenStreamException, IOException, InterruptedException {
        rebuildStreamService.close();
    }
}
