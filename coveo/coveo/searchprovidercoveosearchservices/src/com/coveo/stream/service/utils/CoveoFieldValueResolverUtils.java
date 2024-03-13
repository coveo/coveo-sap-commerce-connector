package com.coveo.stream.service.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CoveoFieldValueResolverUtils {

    private static final Logger LOG = Logger.getLogger(CoveoFieldValueResolverUtils.class);

    public static String resolveFieldValue(String fieldName, Map<String, Object> documentFields) {
        Object fieldValue = documentFields.getOrDefault(fieldName, null);
        if (LOG.isTraceEnabled()) LOG.trace("FieldName = " + fieldName + " : FieldValue = " + fieldValue);
        if (fieldValue instanceof HashMap<?, ?> map) {
            if (!map.isEmpty()) {
                return map.entrySet().iterator().next().getValue().toString();
            }
        } else if (fieldValue != null) {
            return fieldValue.toString();
        }
        return StringUtils.EMPTY;
    }

    public static String resolveFieldValue(String fieldName, Map<String, Object> documentFields, Locale locale, Currency currency) {
        Object fieldValue = documentFields.getOrDefault(fieldName, null);
        if (LOG.isTraceEnabled()) LOG.trace("FieldName = " + fieldName + " : FieldValue = " + fieldValue);
        return resolveFieldValue(fieldValue, locale, currency);
    }

    public static String resolveFieldValue(Object fieldValue, Locale locale, Currency currency) {
        if (LOG.isTraceEnabled()) LOG.trace("FieldValue = " + fieldValue);
        if (fieldValue instanceof HashMap<?, ?> map) {
            if (locale != null && map.containsKey(locale)) {
                return map.get(locale).toString();
            } else if (currency != null && map.containsKey(currency.getCurrencyCode())) {
                return map.get(currency.getCurrencyCode()).toString();
            }
        } else if (fieldValue != null) {
            return fieldValue.toString();
        }
        return StringUtils.EMPTY;
    }

}
