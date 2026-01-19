package com.coveo.indexer.service.impl;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.searchservices.indexer.SnIndexerException;
import de.hybris.platform.searchservices.indexer.service.SnIndexerContext;
import de.hybris.platform.searchservices.indexer.service.SnIndexerFieldWrapper;
import de.hybris.platform.searchservices.indexer.service.impl.ModelAttributeSnIndexerValueProvider;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.Set;


/**
 * This class provides the ability to generate a unique document ID for pushing a document to a Coveo source.
 * This value provider must be included with all indexes with the value documentId in your spring configuration
 * An example of how this should be configured is as follows
 * <pre>{@code
 * <alias name="coveoDocumentIdSnIndexerValueProvider" alias="coveoDocumentIdSnIndexerValueProvider" />
 * <bean id="coveoDocumentIdSnIndexerValueProvider"
 *       class="com.coveo.indexer.service.impl.CoveoDocumentIdSnIndexerValueProvider"
 *       parent="modelAttributeSnIndexerValueProvider">
 * </bean>
 * }</pre>
 * And in your IMPEX file
 * <pre>{@code
 * INSERT_UPDATE SnField; indexType(id)[unique = true]; id[unique = true]               ; name                    ; fieldType(code) ; valueProvider                                        ; valueProviderParameters[map-delimiter = |] ; localized
 *                      ; index-type-id               ; documentId                      ; Document ID             ; STRING          ; coveoProductDocumentIdSnIndexerValueProvider         ; prefix->product|expression->code           ; false
 *}</pre>
 * The prefix value in the valueProviderParameters is optional. Use the expression attribute to define which field of the model should be used for the unique id
 */
public class CoveoDocumentIdSnIndexerValueProvider extends ModelAttributeSnIndexerValueProvider {
    private static final Logger LOG = Logger.getLogger(CoveoDocumentIdSnIndexerValueProvider.class);

    private static final String PREFIX_PARAM = "prefix";
    private static final String URI_ELEMENT = "://";

    protected static final Set<Class<?>> SUPPORTED_QUALIFIER_CLASSES = Set.of();

    @Override
    protected Object getFieldValue(SnIndexerContext indexerContext, SnIndexerFieldWrapper fieldWrapper, ItemModel source, Void data) throws SnIndexerException {
        String documentIdPrefix = fieldWrapper.getValueProviderParameters().getOrDefault(PREFIX_PARAM, StringUtils.EMPTY);
        String documentId = documentIdPrefix + URI_ELEMENT + super.getFieldValue(indexerContext, fieldWrapper, source, data);
        if (LOG.isDebugEnabled()) LOG.debug("Document ID: " + documentId);
        return documentId;
    }

    @Override
    public Set<Class<?>> getSupportedQualifierClasses() throws SnIndexerException {
        return SUPPORTED_QUALIFIER_CLASSES;
    }
}
