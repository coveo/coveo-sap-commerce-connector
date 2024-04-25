package com.coveo.indexer.service.impl;

import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.commerceservices.search.searchservices.provider.impl.ProductCategoryAttributeSnIndexerValueProvider;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.searchservices.core.SnException;
import de.hybris.platform.searchservices.indexer.SnIndexerException;
import de.hybris.platform.searchservices.indexer.service.SnIndexerContext;
import de.hybris.platform.searchservices.indexer.service.SnIndexerFieldWrapper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class provides the ability to push the category hierarchy of a product to the index in the Coveo format.<br>
 * It can be used in the same way as the ProductCategoryAttributeSnIndexerValueProvider upon which it is based.<br>
 * In the qualifierId you should set the
 * <pre>{@code
 * rootCategory->{rootCategoryCode}
 * }</pre>
 * This will detail which category structure you want to push to the index.
 * You can then set the
 * <pre>{@code
 * expression->{attributeName}
 * }</pre>
 * This will detail which attribute on the category object you want to push to the index e.g. name.<br>
 * Note: The root category is excluded as all categories share the same root, and this is not required in the Coveo
 * index.<br><br>
 * An example of how this is configured is as follows
 * <pre>{@code
 *  <alias name="coveoProductCategoryHierarchyAttributeSnIndexValueProvider" alias="coveoProductCategoryHierarchyAttributeSnIndexValueProvider" />
 *  <bean id="coveoProductCategoryHierarchyAttributeSnIndexValueProvider"
 *       class="com.coveo.indexer.service.impl.CoveoProductCategoryHierarchyAttributeSnIndexValueProvider"
 *       parent="productCategoryAttributeSnIndexerValueProvider">
 *     <property name="configurationService" ref="configurationService"/>
 *  </bean>
 * }</pre>
 * And in your IMPEX file
 * <pre>{@code
 * INSERT_UPDATE SnField; indexType(id)[unique = true]; id[unique = true]             ; name                             ; fieldType(code); localized ; valueProvider                                              ; valueProviderParameters[map-delimiter = |] ; qualifierTypeId
 *                      ; $coveoProductIndexType      ; coveoProductCategoryHierarchy ; Coveo Product Category Hierarchy ; STRING         ; true      ; coveoProductCategoryHierarchyAttributeSnIndexValueProvider ;                                            ; rootCategory->1|expression->name                                                                      ;
 *}</pre>
 * This is an optional SnField and is applicable to product only.
 */
public class CoveoProductCategoryHierarchyAttributeSnIndexValueProvider extends ProductCategoryAttributeSnIndexerValueProvider {

    private static final Logger LOG =
            Logger.getLogger(CoveoProductCategoryHierarchyAttributeSnIndexValueProvider.class);

    public static final String INCLUDE_ROOT_CATEGORY_KEY = "coveo.categoryhierarchy.includerootcategory";

    @Override
    protected Object getFieldValue(final SnIndexerContext indexerContext, final SnIndexerFieldWrapper fieldWrapper,
                                   final ProductModel source, final ProductCategoryData data) throws SnIndexerException {
        try {
            final String expression = this.resolveExpression(fieldWrapper);
            final String productSelector = this.resolveProductSelector(fieldWrapper);
            final Set<ProductModel> products = data.getProducts().get(productSelector);
            final String rootCategory = this.resolveRootCategory(fieldWrapper);
            final Set<CategoryModel> categories = new LinkedHashSet<>();

            for (final ProductModel product : products) {
                categories.addAll(data.getCategories().get(product.getPk()).get(rootCategory));
            }

            if (CollectionUtils.isEmpty(categories)) {
                return null;
            }

            Collection<List<CategoryModel>> allCategoryPaths = new HashSet<>();

            for (CategoryModel category : categories) {
                Collection<List<CategoryModel>> categoryPaths = this.getCategoryService().getPathsForCategory(category);
                // remove root category as this will be the same for all paths and not required by Coveo
                categoryPaths.forEach(path -> path.remove(0));
                if (!categoryPaths.isEmpty()) {
                    allCategoryPaths.addAll(categoryPaths);
                }
            }


            if (fieldWrapper.isLocalized()) {
                final List<Locale> locales =
                        fieldWrapper.getQualifiers().stream().map(qualifier -> qualifier.getAs(Locale.class)).collect(Collectors.toList());
                Map<Locale, String> localizedCategoryHierarchies = new HashMap<>();
                for (List<CategoryModel> path : allCategoryPaths) {
                    Map<Locale, Collection<String>> localizedPathValues =
                            (Map<Locale, Collection<String>>) this.getSnExpressionEvaluator().evaluate(path,
                                    expression, locales);
                    localizedPathValues.forEach((locale, values) -> {
                        String pathHierarchy = String.join(" | ", values);
                        localizedCategoryHierarchies.merge(locale, pathHierarchy,
                                (oldVal, newVal) -> oldVal + " ; " + newVal);
                    });
                }
                return localizedCategoryHierarchies;
            } else {
                StringBuilder categoryHierarchies = new StringBuilder();
                for (List<CategoryModel> path : allCategoryPaths) {
                    Collection<String> pathValues =
                            (Collection<String>) this.getSnExpressionEvaluator().evaluate(path, expression);
                    String pathHierarchy = String.join(" | ", pathValues);
                    if (!categoryHierarchies.isEmpty()) {
                        categoryHierarchies.append(" ; ");
                    }
                    categoryHierarchies.append(pathHierarchy);
                }
                return categoryHierarchies.toString();
            }
        } catch (final SnException e) {
            throw new SnIndexerException(e);
        }
    }
}
