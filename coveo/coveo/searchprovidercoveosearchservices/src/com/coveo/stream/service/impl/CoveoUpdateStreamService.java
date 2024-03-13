package com.coveo.stream.service.impl;

import com.coveo.pushapiclient.CatalogSource;
import com.coveo.pushapiclient.DocumentBuilder;
import com.coveo.pushapiclient.UpdateStreamService;
import com.coveo.pushapiclient.exceptions.NoOpenFileContainerException;
import com.coveo.searchservices.data.CoveoSource;
import com.coveo.stream.service.CoveoAbstractStreamService;

import java.io.IOException;

public class CoveoUpdateStreamService extends CoveoAbstractStreamService<UpdateStreamService> {

    UpdateStreamService updateStreamService;

    public CoveoUpdateStreamService(CoveoSource coveoSource) {
        super(coveoSource);
        updateStreamService = createStreamService(createCatalogSource(coveoSource));
    }

    @Override
    protected UpdateStreamService createStreamService(CatalogSource catalogSource) {
        return new UpdateStreamService(catalogSource);
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
