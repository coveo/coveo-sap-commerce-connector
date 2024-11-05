package com.coveo.indexer.service.impl;

import de.hybris.platform.commerceservices.search.searchservices.provider.impl.ProductUrlSnIndexerValueProvider;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.searchservices.indexer.SnIndexerException;
import de.hybris.platform.searchservices.indexer.service.SnIndexerContext;
import de.hybris.platform.searchservices.indexer.service.SnIndexerFieldWrapper;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.util.Map;

/**
 * This class provides the ability to generate a clickable URL for your product when pushing to index.
 * It is based on the SAP Commerce convention of having a property configured of the form
 * <pre>{@code
 * website.{siteId}.https=https://yourcommercesite.com/
 * }</pre>
 * An example of how this is configured is as follows
 * <pre>{@code
 *     <alias name="coveoSimpleClickableProductUriSnIndexerValueProvider" alias="coveoSimpleClickableProductUriSnIndexerValueProvider" />
 *     <bean id="coveoSimpleClickableProductUriSnIndexerValueProvider"
 *           class="com.coveo.indexer.service.impl.CoveoSimpleClickableProductUriSnIndexerValueProvider"
 *           parent="productUrlSnIndexerValueProvider">
 *         <property name="configurationService" ref="configurationService"/>
 *     </bean>
 * }</pre>
 * And in your IMPEX file
 * <pre>{@code
 * INSERT_UPDATE SnField; indexType(id)[unique = true]; id[unique = true] ; name                ; fieldType(code); valueProvider                                        ; valueProviderParameters[map-delimiter = |]
 *                      ; $coveoProductIndexType      ; coveoClickableUri ; Coveo Clickable URI ; STRING         ; coveoSimpleClickableProductUriSnIndexerValueProvider ; siteId->electronics
 *}</pre>
 * This is an optional SnField and is applicable to product only.
 * Note that we have used the valueProviderParameters to set the siteId.
 */
public class CoveoSimpleClickableProductUriSnIndexerValueProvider extends ProductUrlSnIndexerValueProvider {

    private static final Logger LOG = Logger.getLogger(CoveoSimpleClickableProductUriSnIndexerValueProvider.class);

    private static final String SITE_ID_PARAM = "siteId";

    private ConfigurationService configurationService;

    @Override
    protected Object getFieldValue(final SnIndexerContext indexerContext, final SnIndexerFieldWrapper fieldWrapper,
                                   final ProductModel source, final String productUrl) throws SnIndexerException
    {
        Map<String, String> parameters = fieldWrapper.getValueProviderParameters();
        if (!parameters.containsKey(SITE_ID_PARAM)) {
            LOG.error("Missing required Value Provider Parameter : " + SITE_ID_PARAM);
            return super.getFieldValue(indexerContext, fieldWrapper, source, productUrl);
        }
        String storeId = parameters.get(SITE_ID_PARAM);
        String propertyName = "website." + storeId + ".https";
        String baseUri = configurationService.getConfiguration().getString(propertyName, StringUtils.EMPTY);
        String clickableUri = baseUri + super.getFieldValue(indexerContext, fieldWrapper, source, productUrl);
        if (LOG.isDebugEnabled()) LOG.debug("Product : " + source.getCode() + "; Clickable URI: " + clickableUri);
        return clickableUri;
    }

    @Required
    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }
}
