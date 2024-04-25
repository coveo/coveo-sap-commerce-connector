package com.coveo.stream.service.impl;

import com.coveo.pushapiclient.CatalogSource;
import com.coveo.pushapiclient.DocumentBuilder;
import com.coveo.pushapiclient.UpdateStreamService;
import com.coveo.pushapiclient.exceptions.NoOpenFileContainerException;
import com.coveo.searchservices.data.CoveoSource;
import com.coveo.stream.service.CoveoAbstractStreamService;

import java.io.IOException;

import static com.coveo.constants.SearchprovidercoveosearchservicesConstants.COSAP_CONNECTOR_USER_AGENT;

public class CoveoUpdateStreamService extends CoveoAbstractStreamService<UpdateStreamService> {

    UpdateStreamService updateStreamService;

    public CoveoUpdateStreamService(CoveoSource coveoSource, String[] userAgents) {
        super(coveoSource);
        updateStreamService = createStreamService(createCatalogSource(coveoSource), userAgents);
    }

    @Override
    protected UpdateStreamService createStreamService(CatalogSource catalogSource, String[] userAgents) {
        return new UpdateStreamService(catalogSource, userAgents);
    }

    @Override
    public void pushDocument(DocumentBuilder document) throws IOException, InterruptedException {
        updateStreamService.addOrUpdate(document);
    }

    @Override
    public void closeStream() throws IOException, InterruptedException, NoOpenFileContainerException {
        updateStreamService.close();
    }
}
