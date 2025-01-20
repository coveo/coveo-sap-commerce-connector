package com.coveo.stream.service.impl;

import com.coveo.pushapiclient.CatalogSource;
import com.coveo.pushapiclient.DocumentBuilder;
import com.coveo.pushapiclient.UpdateStreamService;
import com.coveo.pushapiclient.exceptions.NoOpenFileContainerException;
import com.coveo.searchservices.data.CoveoSource;
import com.coveo.stream.service.CoveoAbstractStreamService;
import de.hybris.platform.core.Registry;

import java.io.IOException;

public class CoveoUpdateStreamService extends CoveoAbstractStreamService<UpdateStreamService> {

    UpdateStreamService updateStreamService;

    @Override
    public void init(CoveoSource coveoSource, String[] userAgents) {
        this.coveoSource = coveoSource;
        updateStreamService = createStreamService(createCatalogSource(coveoSource), userAgents);
    }

    @Override
    protected UpdateStreamService createStreamService(CatalogSource catalogSource, String[] userAgents) {
        final UpdateStreamService updateStreamService = Registry.getApplicationContext().getBean(UpdateStreamService.class);
        updateStreamService.init(catalogSource, userAgents);
        return updateStreamService;
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
