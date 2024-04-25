package com.coveo.indexer.service.impl;

import de.hybris.platform.commerceservices.search.searchservices.strategies.SnStoreSelectionStrategy;
import de.hybris.platform.ordersplitting.model.WarehouseModel;
import de.hybris.platform.searchservices.core.service.SnContext;
import de.hybris.platform.searchservices.core.service.SnQualifier;
import de.hybris.platform.searchservices.core.service.SnQualifierProvider;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class provides an example of how you might implement a value provider that will push a dictionary to your coveo index.
 * The important point to note here is that the value provider must return a Map<String, Object> as the value.
 *
 * <pre>{@code
 * <alias name="coveoProductStockLevelSnIndexerValueProvider" alias="coveoProductStockLevelSnIndexerValueProvider" />
 * <bean id="coveoProductStockLevelSnIndexerValueProvider"
 *      class="com.coveo.indexer.service.impl.CoveoProductStockLevelSnIndexerValueProvider"
 *      parent="abstractSnIndexerValueProvider">
 *   <property name="snSessionService" ref="snSessionService" />
 * </bean>
 * }</pre>
 * And in your IMPEX file
 * <pre>{@code
 * INSERT_UPDATE SnField; indexType(id)[unique = true]; id[unique = true] ; name         ; fieldType(code) ; valueProvider                                           ; valueProviderParameters[map-delimiter = |] ; qualifierTypeId
 *                      ; coveo-electronics-product   ; stockLevels       ; Stock Levels ; TEXT            ; coveoProductPriceToUserPriceGroupSnIndexerValueProvider ;                                            ; warehouse                                                          ;
 * }</pre>
 */
public class CoveoWarehouseSnQualifierProvider implements SnQualifierProvider {
    protected static final Set<Class<?>> SUPPORTED_QUALIFIER_CLASSES = Set.of(WarehouseModel.class);
    protected static final String WAREHOUSE_QUALIFIERS_KEY = CoveoWarehouseSnQualifierProvider.class.getName() + ".warehouseQualifiers";

    private BaseStoreService baseStoreService;
    private SnStoreSelectionStrategy snStoreSelectionStrategy;

    @Override
    public Set<Class<?>> getSupportedQualifierClasses() {
        return SUPPORTED_QUALIFIER_CLASSES;
    }

    @Override
    public List<SnQualifier> getAvailableQualifiers(SnContext context) {
        Objects.requireNonNull(context, "context is null");

        List<SnQualifier> qualifiers = (List<SnQualifier>) context.getAttributes().get(WAREHOUSE_QUALIFIERS_KEY);
        if (qualifiers == null) {
            final List<BaseStoreModel> stores = snStoreSelectionStrategy.getStores(context.getIndexType().getId());
            List<WarehouseModel> warehouses = new ArrayList<>();
            for (BaseStoreModel store : stores) {
                warehouses.addAll(store.getWarehouses());
            }
            qualifiers = CollectionUtils.emptyIfNull(warehouses).stream().map(this::createQualifier).collect(Collectors.toList());

            context.getAttributes().put(WAREHOUSE_QUALIFIERS_KEY, qualifiers);
        }

        return qualifiers;
    }

    private WarehouseSnQualifier createQualifier(WarehouseModel warehouseModel) {
        return new CoveoWarehouseSnQualifierProvider.WarehouseSnQualifier(warehouseModel);
    }

    @Override
    public List<SnQualifier> getCurrentQualifiers(SnContext context) {
        Objects.requireNonNull(context, "context is null");
        final BaseStoreModel store = baseStoreService.getCurrentBaseStore();
        if (store == null) {
            return Collections.emptyList();
        }
        return CollectionUtils.emptyIfNull(store.getWarehouses()).stream().map(this::createQualifier).collect(Collectors.toList());
    }

    public BaseStoreService getBaseStoreService() {
        return baseStoreService;
    }

    @Required
    public void setBaseStoreService(final BaseStoreService baseStoreService) {
        this.baseStoreService = baseStoreService;
    }

    public SnStoreSelectionStrategy getSnStoreSelectionStrategy() {
        return snStoreSelectionStrategy;
    }

    @Required
    public void setSnStoreSelectionStrategy(final SnStoreSelectionStrategy snStoreSelectionStrategy) {
        this.snStoreSelectionStrategy = snStoreSelectionStrategy;
    }

    protected static class WarehouseSnQualifier implements SnQualifier {
        private WarehouseModel warehouseModel;

        public WarehouseSnQualifier(WarehouseModel warehouseModel) {
            Objects.requireNonNull(warehouseModel, "warehouse is null");
            this.warehouseModel = warehouseModel;
        }

        public WarehouseModel getWarehouseModel() {
            return warehouseModel;
        }

        @Override
        public String getId() {
            return warehouseModel.getCode();
        }

        public void setWarehouseModel(WarehouseModel warehouseModel) {
            this.warehouseModel = warehouseModel;
        }

        @Override
        public boolean canGetAs(final Class<?> qualifierClass) {
            for (final Class<?> supportedQualifierClass : SUPPORTED_QUALIFIER_CLASSES) {
                if (qualifierClass.isAssignableFrom(supportedQualifierClass)) {
                    return true;
                }
            }

            return false;
        }

        @Override
        public <Q> Q getAs(final Class<Q> qualifierClass) {
            Objects.requireNonNull(qualifierClass, "qualifierClass is null");

            if (qualifierClass.isAssignableFrom(WarehouseModel.class)) {
                return (Q) warehouseModel;
            }

            throw new IllegalArgumentException("Qualifier class not supported");
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }

            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }

            final CoveoWarehouseSnQualifierProvider.WarehouseSnQualifier that = (CoveoWarehouseSnQualifierProvider.WarehouseSnQualifier) obj;
            return new EqualsBuilder().append(this.warehouseModel, that.warehouseModel).isEquals();
        }

        @Override
        public int hashCode() {
            return warehouseModel.hashCode();
        }
    }
}
