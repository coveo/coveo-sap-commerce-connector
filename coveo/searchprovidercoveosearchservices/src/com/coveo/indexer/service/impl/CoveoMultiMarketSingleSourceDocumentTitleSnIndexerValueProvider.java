package com.coveo.indexer.service.impl;

import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.searchservices.indexer.SnIndexerException;
import de.hybris.platform.searchservices.indexer.service.SnIndexerContext;
import de.hybris.platform.searchservices.indexer.service.SnIndexerFieldWrapper;
import de.hybris.platform.searchservices.indexer.service.impl.ModelAttributeSnIndexerValueProvider;
import de.hybris.platform.searchservices.util.ParameterUtils;

import java.util.Locale;
import java.util.Map;

public class CoveoMultiMarketSingleSourceDocumentTitleSnIndexerValueProvider
        extends ModelAttributeSnIndexerValueProvider
{
    public static final String DEFAULT_LANGUAGE_PARAM = "defaultLanguage";

    @Override
    protected Object getFieldValue(final SnIndexerContext indexerContext, final SnIndexerFieldWrapper fieldWrapper,
                                   final ItemModel source, final Void data) throws SnIndexerException
    {
        Object fieldValue = super.getFieldValue(indexerContext, fieldWrapper, source, data);
        if (fieldWrapper.isLocalized() && fieldValue instanceof Map<?, ?> map) {
            String languageCode = resolveDefaultLocal(fieldWrapper);
            Locale locale = Locale.forLanguageTag(languageCode);
            Object localisedValue = map.get(locale);
            if (localisedValue == null) {
                throw new SnIndexerException("No localised value for language " + languageCode +
                                                     ". Please ensure your index and value provider are configured correctly");
            }
            // Return a new map with the single entry
            return Map.of(locale, localisedValue);
        }
        return fieldValue;
    }

    protected String resolveDefaultLocal(final SnIndexerFieldWrapper fieldWrapper)
    {
        return ParameterUtils.getString(fieldWrapper.getValueProviderParameters(), DEFAULT_LANGUAGE_PARAM,
                                        fieldWrapper.getField().getId());
    }

}
