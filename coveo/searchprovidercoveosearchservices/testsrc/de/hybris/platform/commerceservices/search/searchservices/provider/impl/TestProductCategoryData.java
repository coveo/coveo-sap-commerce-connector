package de.hybris.platform.commerceservices.search.searchservices.provider.impl;

import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.product.ProductModel;

import java.util.Map;
import java.util.Set;

public class TestProductCategoryData extends ProductCategoryAttributeSnIndexerValueProvider.ProductCategoryData {
    public TestProductCategoryData(Map<String, Set<ProductModel>> products, Map<PK, Map<String, Set<CategoryModel>>> categories) {
        super();
        this.setCategories(categories);
        this.setProducts(products);
    }
}
