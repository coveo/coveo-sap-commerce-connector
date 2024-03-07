package com.coveo.service.impl;

import com.coveo.constants.SearchprovidercoveosearchservicesConstants;
import com.coveo.data.CoveoSearchSnSearchProviderConfiguration;
import com.coveo.pushapiclient.CatalogSource;
import com.coveo.pushapiclient.StreamService;
import de.hybris.platform.apiregistryservices.model.ConsumedDestinationModel;
import de.hybris.platform.apiregistryservices.model.ConsumedOAuthCredentialModel;
import de.hybris.platform.apiregistryservices.services.DestinationService;
import de.hybris.platform.searchservices.admin.data.SnIndexConfiguration;
import de.hybris.platform.searchservices.indexer.service.SnIndexerRequest;
import de.hybris.platform.searchservices.indexer.service.impl.DefaultSnIndexerContext;
import de.hybris.platform.searchservices.indexer.service.impl.DefaultSnIndexerContextFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import javax.annotation.Resource;
import java.net.MalformedURLException;
import java.net.URL;

public class CoveoSnIndexerContextFactory extends DefaultSnIndexerContextFactory
{
    private static final Logger LOG = Logger.getLogger(CoveoSnIndexerContextFactory.class);
    @Resource
    private DestinationService<ConsumedDestinationModel> destinationService;

    protected void populateIndexerContext(final DefaultSnIndexerContext context, final SnIndexerRequest indexerRequest)
    {
        super.populateIndexerContext(context,indexerRequest);
        CatalogSource catalogSource = instantiateCatalogSource(context.getIndexConfiguration());
        if(catalogSource == null){
            return;
        }
        StreamService streamService = new StreamService(catalogSource);
        context.getAttributes().put(SearchprovidercoveosearchservicesConstants.COVEO_STREAM_SERVICE_KEY,streamService);
    }

    private CatalogSource instantiateCatalogSource(final SnIndexConfiguration indexConfiguration) {
        CoveoSearchSnSearchProviderConfiguration coveoSearchProviderConfiguration = (CoveoSearchSnSearchProviderConfiguration) indexConfiguration.getSearchProviderConfiguration();

        final ConsumedDestinationModel destination = destinationService.getDestinationByIdAndByDestinationTargetId(
                coveoSearchProviderConfiguration.getDestinationId(), coveoSearchProviderConfiguration.getDestinationTargetId());

        try {
            URL url = new URL(destination.getUrl());

            String apiKey = ((ConsumedOAuthCredentialModel)destination.getCredential()).getClientSecret();

            return  new CatalogSource(apiKey,url);
        } catch (MalformedURLException e) {
            LOG.error(String.format("url: %s is malformed", destination.getUrl()),e);
            return null;
        }
    }
}
