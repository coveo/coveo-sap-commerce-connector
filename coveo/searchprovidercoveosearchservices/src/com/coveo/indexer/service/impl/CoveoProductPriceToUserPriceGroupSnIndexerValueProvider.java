package com.coveo.indexer.service.impl;

import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.europe1.model.PriceRowModel;
import de.hybris.platform.searchservices.core.service.SnQualifier;
import de.hybris.platform.searchservices.core.service.SnSessionService;
import de.hybris.platform.searchservices.indexer.SnIndexerException;
import de.hybris.platform.searchservices.indexer.service.SnIndexerContext;
import de.hybris.platform.searchservices.indexer.service.SnIndexerFieldWrapper;
import de.hybris.platform.searchservices.indexer.service.impl.AbstractSnIndexerValueProvider;
import org.apache.commons.collections4.MapUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class provides an example of how you might implement a value provider that will push a localized dictionary to your coveo index.
 * The important point to note here is that the value provider must return a Map<String,Map<String, Object>> as the value.
 *
 * <pre>{@code
 * <alias name="coveoProductPriceToUserPriceGroupSnIndexerValueProvider" alias="coveoProductPriceToUserPriceGroupSnIndexerValueProvider" />
 * <bean id="coveoProductPriceToUserPriceGroupSnIndexerValueProvider"
 *      class="com.coveo.indexer.service.impl.CoveoProductPriceToUserPriceGroupSnIndexerValueProvider" parent="abstractSnIndexerValueProvider">
 * </bean>
 * }</pre>
 * And in your IMPEX file
 * <pre>{@code
 * INSERT_UPDATE SnField; indexType(id)[unique = true] ; id[unique = true] ; name                  ; fieldType(code) ; valueProvider                                           ; valueProviderParameters[map-delimiter = |] ; qualifierTypeId
 *                      ; coveo-electronics-product    ; priceToPriceGroup ; Price  To Price Group ; TEXT            ; coveoProductPriceToUserPriceGroupSnIndexerValueProvider ;                                            ; currency                                                          ;
 * }</pre>
 */
public class CoveoProductPriceToUserPriceGroupSnIndexerValueProvider
        extends AbstractSnIndexerValueProvider<ProductModel, CoveoProductPriceToUserPriceGroupSnIndexerValueProvider.CurrencyToUserPriceGroupAndPriceMapping> {

    private static final Logger LOG = Logger.getLogger(CoveoProductPriceToUserPriceGroupSnIndexerValueProvider.class);
    protected static final Set<Class<?>> SUPPORTED_QUALIFIER_CLASSES = Set.of(CurrencyModel.class);

    private SnSessionService snSessionService;

    @Override
    protected Object getFieldValue(SnIndexerContext indexerContext, SnIndexerFieldWrapper fieldWrapper, ProductModel source, CurrencyToUserPriceGroupAndPriceMapping data) throws SnIndexerException {
        if (MapUtils.isEmpty(data.getPrices())) {
            return null;
        }

        final Map<String, UserPriceGroupToPrice> prices = data.getPrices();
        final Map<String, Map<String, Double>> value = new HashMap<>();
        if (fieldWrapper.isQualified()) {
            final List<SnQualifier> qualifiers = fieldWrapper.getQualifiers();
            for (final SnQualifier qualifier : qualifiers) {
                value.put(qualifier.getId(), prices.get(qualifier.getId()).getUserGroupPrices());
            }
        } else {
            for (Map.Entry<String, UserPriceGroupToPrice> entry : prices.entrySet()) {
                Map<String, Double> userPricingGroupToPriceMap = new HashMap<>(entry.getValue().getUserGroupPrices());
                value.put(entry.getKey(), userPricingGroupToPriceMap);
            }
        }
        if (LOG.isDebugEnabled()) LOG.debug("Product : " + source.getCode() + "; Price: " + value);
        return value;
    }

    @Override
    protected CurrencyToUserPriceGroupAndPriceMapping loadData(final SnIndexerContext indexerContext,
                                                               final Collection<SnIndexerFieldWrapper> fieldWrappers, final ProductModel source) throws SnIndexerException {
        Map<String, List<PriceRowModel>> currencyToPriceMapping = new HashMap<>();
        for (final SnIndexerFieldWrapper fieldWrapper : fieldWrappers) {
            if (fieldWrapper.isQualified()) {
                loadQualifiedPrices(fieldWrapper, source, currencyToPriceMapping);
                if (LOG.isTraceEnabled()) LOG.trace("Loaded qualified prices for field: " + currencyToPriceMapping);
            } else {
                loadAllPriceValues(source, currencyToPriceMapping);
            }
        }

        CurrencyToUserPriceGroupAndPriceMapping currencyToUserPriceGroupAndPriceMapping = new CurrencyToUserPriceGroupAndPriceMapping();
        for (Map.Entry<String, List<PriceRowModel>> entry : currencyToPriceMapping.entrySet()) {
            UserPriceGroupToPrice userPriceGroupToPrice = new UserPriceGroupToPrice();
            for (PriceRowModel priceRowModel : entry.getValue()) {
                String userGroupCode = (priceRowModel.getUg() == null) ? "" : priceRowModel.getUg().getCode();
                userPriceGroupToPrice.getUserGroupPrices().put(userGroupCode, priceRowModel.getPrice());
            }
            currencyToUserPriceGroupAndPriceMapping.getPrices().put(entry.getKey(), userPriceGroupToPrice);
        }
        return currencyToUserPriceGroupAndPriceMapping;
    }

    private void loadQualifiedPrices(SnIndexerFieldWrapper fieldWrapper, ProductModel source, Map<String, List<PriceRowModel>> currencyToPriceMapping) {
        try {
            snSessionService.initializeSession();

            final List<SnQualifier> qualifiers = fieldWrapper.getQualifiers();
            for (SnQualifier qualifier : qualifiers) {
                currencyToPriceMapping.computeIfAbsent(qualifier.getId(), key -> {
                    final CurrencyModel currency = qualifier.getAs(CurrencyModel.class);
                    return loadCurrencySpecificPriceValues(source, currency);
                });
            }
        } finally {
            snSessionService.destroySession();
        }
    }

    private List<PriceRowModel> loadCurrencySpecificPriceValues(ProductModel source, CurrencyModel currency) {

        List<PriceRowModel> priceRowModels = new ArrayList<>();
        for(PriceRowModel priceRowModel : source.getEurope1Prices()) {

            if(priceRowModel.getCurrency().equals(currency) && isPriceRowDateValid(priceRowModel)) {
                priceRowModels.add(priceRowModel);
            }
        }
        return priceRowModels;
    }


    private void loadAllPriceValues(ProductModel source, Map<String, List<PriceRowModel>> currencyToPriceMapping) {
        List<PriceRowModel> priceRowModels = new ArrayList<>();
        for(PriceRowModel priceRowModel : source.getEurope1Prices()) {

            if(isPriceRowDateValid(priceRowModel)) {
                priceRowModels.add(priceRowModel);
            }
        }
        for (PriceRowModel row : priceRowModels) {
            currencyToPriceMapping.computeIfAbsent(row.getCurrency().getIsocode(), k -> new ArrayList<>()).add(row);
        }
    }

    private boolean isPriceRowDateValid(PriceRowModel priceRowModel) {
        Date today = new Date();
        return (priceRowModel.getStartTime() == null || !priceRowModel.getStartTime().after(today)) &&
                (priceRowModel.getEndTime() == null || !priceRowModel.getEndTime().before(today));
    }

    @Override
    public Set<Class<?>> getSupportedQualifierClasses() throws SnIndexerException {
        return SUPPORTED_QUALIFIER_CLASSES;
    }

    public SnSessionService getSnSessionService() {
        return snSessionService;
    }

    @Autowired
    public void setSnSessionService(final SnSessionService snSessionService) {
        this.snSessionService = snSessionService;
    }

    protected static class CurrencyToUserPriceGroupAndPriceMapping {
        private Map<String, UserPriceGroupToPrice> prices = new HashMap<>();

        public Map<String, UserPriceGroupToPrice> getPrices() {
            return prices;
        }

        public void setPrices(final Map<String, UserPriceGroupToPrice> prices) {
            this.prices = prices;
        }
    }

    protected static class UserPriceGroupToPrice {
        private Map<String, Double> userGroupPrices = new HashMap<>();

        public Map<String, Double> getUserGroupPrices() {
            return userGroupPrices;
        }

        public void setUserGroupPrices(final Map<String, Double> userGroupPrices) {
            this.userGroupPrices = userGroupPrices;
        }
    }
}
