package com.coveo.indexer.service.impl;

import de.hybris.platform.searchservices.indexer.service.impl.AbstractSnIndexerValueProvider;
import de.hybris.platform.commerceservices.url.UrlResolver;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.searchservices.indexer.SnIndexerException;
import de.hybris.platform.searchservices.indexer.service.SnIndexerContext;
import de.hybris.platform.searchservices.indexer.service.SnIndexerFieldWrapper;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.util.Map;
import java.util.Set;

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
 *         <property name="urlResolver" ref="productModelUrlResolver"/>
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
public class CoveoSimpleClickableProductUriSnIndexerValueProvider extends AbstractSnIndexerValueProvider<ProductModel, Void> {

    private static final Logger LOG = Logger.getLogger(CoveoSimpleClickableProductUriSnIndexerValueProvider.class);

    protected static final Set<Class<?>> SUPPORTED_QUALIFIER_CLASSES = Set.of();
    private static final String SITE_ID_PARAM = "siteId";

    private ConfigurationService configurationService;
    private UrlResolver<ProductModel> urlResolver;

    @Override
    protected Object getFieldValue(SnIndexerContext indexerContext, SnIndexerFieldWrapper fieldWrapper, ProductModel source, Void data) throws SnIndexerException {
        Map<String, String> parameters = fieldWrapper.getValueProviderParameters();
        if (!parameters.containsKey(SITE_ID_PARAM)) {
            LOG.error("Missing required Value Provider Parameter : " + SITE_ID_PARAM);
            return urlResolver.resolve(source);
        }
        String storeId = parameters.get(SITE_ID_PARAM);
        String propertyName = "website." + storeId + ".https";
        String baseUri = configurationService.getConfiguration().getString(propertyName, StringUtils.EMPTY);
        return baseUri + urlResolver.resolve(source);
    }

    @Override
    public Set<Class<?>> getSupportedQualifierClasses() throws SnIndexerException {
        return SUPPORTED_QUALIFIER_CLASSES;
    }

    @Required
    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    @Required
    public void setUrlResolver(final UrlResolver<ProductModel> urlResolver)
    {
        this.urlResolver = urlResolver;
    }
}
