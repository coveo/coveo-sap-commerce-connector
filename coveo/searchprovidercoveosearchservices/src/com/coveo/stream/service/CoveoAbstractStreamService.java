package com.coveo.stream.service;

import com.coveo.pushapiclient.CatalogSource;
import com.coveo.searchservices.data.CoveoSource;
import org.apache.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;

public abstract class CoveoAbstractStreamService<S> implements CoveoStreamService {
    private static final Logger LOG = Logger.getLogger(CoveoAbstractStreamService.class);

    protected CoveoSource coveoSource;

    protected CatalogSource createCatalogSource(CoveoSource coveoSource) {
        try {
            return new CatalogSource(coveoSource.getDestinationSecret(), new URL(coveoSource.getDestinationTargetUrl()));
        } catch (MalformedURLException e) {
            LOG.error(String.format("url: %s is malformed", coveoSource.getDestinationTargetUrl()), e);
            return null;
        }
    }

    protected abstract S createStreamService(CatalogSource catalogSource, String[] userAgents);

    protected abstract void init(CoveoSource coveoSource, String[] userAgents);

    public CoveoSource getCoveoSource() {
        return coveoSource;
    }

}
