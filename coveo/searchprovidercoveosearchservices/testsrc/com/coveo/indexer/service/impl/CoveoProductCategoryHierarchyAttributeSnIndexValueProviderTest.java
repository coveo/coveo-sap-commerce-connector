package com.coveo.indexer.service.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.category.CategoryService;
import de.hybris.platform.category.model.CategoryModel;
import de.hybris.platform.commerceservices.search.searchservices.provider.impl.AbstractProductSnIndexerValueProvider;
import de.hybris.platform.commerceservices.search.searchservices.provider.impl.ProductCategoryAttributeSnIndexerValueProvider;
import de.hybris.platform.commerceservices.search.searchservices.provider.impl.TestProductCategoryData;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.searchservices.core.SnException;
import de.hybris.platform.searchservices.core.service.SnExpressionEvaluator;
import de.hybris.platform.searchservices.core.service.SnQualifier;
import de.hybris.platform.searchservices.indexer.service.SnIndexerFieldWrapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@UnitTest
public class CoveoProductCategoryHierarchyAttributeSnIndexValueProviderTest {

    @Mock
    Locale localeEn;
    @Mock
    Locale localeFr;
    @Mock
    SnQualifier qualifierEn;
    @Mock
    SnQualifier qualifierFr;

    @Mock
    ProductModel productModel;
    private static final PK PRODUCT_PK = PK.fromLong(1234L);
    private static final String CURRENT = AbstractProductSnIndexerValueProvider.PRODUCT_SELECTOR_VALUE_CURRENT;

    @Mock
    CategoryModel rootCategory;
    private static final String ROOT_CATEGORY_CODE = "rootCategory";

    @Mock
    CategoryModel fineRootCategory;

    @Mock
    CategoryModel branchCategoryAB;
    private static final String BRANCH_CATEGORY_AB_CODE = "branchAB";
    private static final String BRANCH_CATEGORY_AB_NAME_EN = "Branch Category AB";
    private static final String BRANCH_CATEGORY_AB_NAME_FR = "Catégorie branche AB";
    private static final String EXPECTED_BRANCH_AB_PATH_EN = BRANCH_CATEGORY_AB_NAME_EN;
    private static final String EXPECTED_BRANCH_AB_PATH_FR = BRANCH_CATEGORY_AB_NAME_FR;

    @Mock
    CategoryModel twigCategoryA;
    private static final String TWIG_CATEGORY_CODE_A = "twigA";
    private static final String TWIG_CATEGORY_A_NAME_EN = "Twig Category A";
    private static final String TWIG_CATEGORY_A_NAME_FR = "Catégorie brindille A";
    private static final String EXPECTED_TWIG_A_PATH_EN = generateEvaluatedPath(BRANCH_CATEGORY_AB_NAME_EN, TWIG_CATEGORY_A_NAME_EN);
    private static final String EXPECTED_TWIG_A_PATH_FR = generateEvaluatedPath(BRANCH_CATEGORY_AB_NAME_FR, TWIG_CATEGORY_A_NAME_FR);

    @Mock
    CategoryModel twigCategoryB;
    private static final String TWIG_CATEGORY_CODE_B = "twigB";
    private static final String TWIG_CATEGORY_B_NAME_EN = "Twig Category B";
    private static final String TWIG_CATEGORY_B_NAME_FR = "Catégorie brindille B";
    private static final String EXPECTED_TWIG_B_PATH_EN = generateEvaluatedPath(BRANCH_CATEGORY_AB_NAME_EN, TWIG_CATEGORY_B_NAME_EN);
    private static final String EXPECTED_TWIG_B_PATH_FR = generateEvaluatedPath(BRANCH_CATEGORY_AB_NAME_FR, TWIG_CATEGORY_B_NAME_FR);


    @Mock
    CategoryModel leafCategoryA;
    private static final String LEAF_CATEGORY_CODE_A = "leafA";
    private static final String LEAF_CATEGORY_A_NAME_EN = "Leaf Category A";
    private static final String LEAF_CATEGORY_A_NAME_FR = "Catégorie feuille A";
    private static final String EXPECTED_LEAF_A_PATH_1_EN = generateEvaluatedPath(BRANCH_CATEGORY_AB_NAME_EN, TWIG_CATEGORY_A_NAME_EN, LEAF_CATEGORY_A_NAME_EN);
    private static final String EXPECTED_LEAF_A_PATH_2_EN = generateEvaluatedPath(BRANCH_CATEGORY_AB_NAME_EN, TWIG_CATEGORY_B_NAME_EN, LEAF_CATEGORY_A_NAME_EN);
    private static final String EXPECTED_LEAF_A_PATH_1_FR = generateEvaluatedPath(BRANCH_CATEGORY_AB_NAME_FR, TWIG_CATEGORY_A_NAME_FR, LEAF_CATEGORY_A_NAME_FR);
    private static final String EXPECTED_LEAF_A_PATH_2_FR = generateEvaluatedPath(BRANCH_CATEGORY_AB_NAME_FR, TWIG_CATEGORY_B_NAME_FR, LEAF_CATEGORY_A_NAME_FR);

    private TestProductCategoryData productCategoryData;

    @Mock
    SnIndexerFieldWrapper fieldWrapper;
    @Mock
    CategoryService categoryService;
    @Mock
    SnExpressionEvaluator snExpressionEvaluator;

    @InjectMocks
    CoveoProductCategoryHierarchyAttributeSnIndexValueProvider coveoProductCategoryHierarchyAttributeSnIndexValueProvider = new CoveoProductCategoryHierarchyAttributeSnIndexValueProvider();

    @BeforeEach
    void setUp() {

        when(productModel.getPk()).thenReturn(PRODUCT_PK);

        Map<String, Set<ProductModel>> products = new HashMap<>();
        products.put(CURRENT, Set.of(productModel));
        Map<PK, Map<String, Set<CategoryModel>>> productCategories = new HashMap<>();
        productCategories.put(PRODUCT_PK, Map.of(ROOT_CATEGORY_CODE, Set.of(branchCategoryAB,
                twigCategoryA, twigCategoryB, leafCategoryA)));
        productCategoryData = new TestProductCategoryData(products, productCategories);

        Map<String, String> valueProviderParameters = new HashMap<>();
        valueProviderParameters.put(ProductCategoryAttributeSnIndexerValueProvider.EXPRESSION_PARAM, "name");
        valueProviderParameters.put(ProductCategoryAttributeSnIndexerValueProvider.ROOT_CATEGORY_PARAM,
                ROOT_CATEGORY_CODE);
        when(fieldWrapper.getValueProviderParameters()).thenReturn(valueProviderParameters);
    }

    private void setUpPathEvaluation(boolean localizedField) throws SnException {
        /*
         * Category hierarchy set up as follows:
         *
         *       root
         *        |
         *    branchAB
         *    /     \
         * twigA   twigB
         *     \    /
         *     leafA
         *
         * so 6 possible paths:
         */

        when(rootCategory.getCode()).thenReturn(ROOT_CATEGORY_CODE);
        when(branchCategoryAB.getCode()).thenReturn(BRANCH_CATEGORY_AB_CODE);
        when(twigCategoryA.getCode()).thenReturn(TWIG_CATEGORY_CODE_A);
        when(twigCategoryB.getCode()).thenReturn(TWIG_CATEGORY_CODE_B);
        when(leafCategoryA.getCode()).thenReturn(LEAF_CATEGORY_CODE_A);

        List<CategoryModel> path2 = new LinkedList<>(List.of(rootCategory, branchCategoryAB));
        Map<Locale, Collection<String>> branchCatAName = new HashMap<>();
        Collection<String> enCatANames = List.of(BRANCH_CATEGORY_AB_NAME_EN);
        Collection<String> frCatANames = List.of(BRANCH_CATEGORY_AB_NAME_FR);
        branchCatAName.put(localeEn, enCatANames);
        branchCatAName.put(localeFr, frCatANames);
        if (localizedField) {
            when(snExpressionEvaluator.evaluate(eq(List.of(branchCategoryAB)), any(), any())).thenReturn(branchCatAName);
        } else {
            when(snExpressionEvaluator.evaluate(eq(List.of(branchCategoryAB)), any())).thenReturn(enCatANames);
        }
        when(categoryService.getPathsForCategory(branchCategoryAB)).thenReturn(List.of(path2));

        List<CategoryModel> path3 = new LinkedList<>(List.of(rootCategory, branchCategoryAB, twigCategoryA));
        Map<Locale, Collection<String>> twigCatAName = new HashMap<>();
        Collection<String> enTwigANames = List.of(BRANCH_CATEGORY_AB_NAME_EN,
                TWIG_CATEGORY_A_NAME_EN);
        Collection<String> frTwigANames = List.of(BRANCH_CATEGORY_AB_NAME_FR,
                TWIG_CATEGORY_A_NAME_FR);
        twigCatAName.put(localeEn, enTwigANames);
        twigCatAName.put(localeFr, frTwigANames);
        if (localizedField) {
            when(snExpressionEvaluator.evaluate(eq(List.of(branchCategoryAB, twigCategoryA)), any(), any())).thenReturn(twigCatAName);
        } else {
            when(snExpressionEvaluator.evaluate(eq(List.of(branchCategoryAB, twigCategoryA)), any())).thenReturn(enTwigANames);
        }
        when(categoryService.getPathsForCategory(twigCategoryA)).thenReturn(List.of(path3));

        List<CategoryModel> path4 = new LinkedList<>(List.of(rootCategory, branchCategoryAB, twigCategoryB));
        Map<Locale, Collection<String>> twigCatBName = new HashMap<>();
        Collection<String> enTwigBNames = List.of(BRANCH_CATEGORY_AB_NAME_EN,
                TWIG_CATEGORY_B_NAME_EN);
        Collection<String> frTwigBNames = List.of(BRANCH_CATEGORY_AB_NAME_FR,
                TWIG_CATEGORY_B_NAME_FR);
        twigCatBName.put(localeEn, enTwigBNames);
        twigCatBName.put(localeFr, frTwigBNames);
        if (localizedField) {
            when(snExpressionEvaluator.evaluate(eq(List.of(branchCategoryAB,twigCategoryB)), any(), any())).thenReturn(twigCatBName);
        } else {
            when(snExpressionEvaluator.evaluate(eq(List.of(branchCategoryAB,twigCategoryB)), any())).thenReturn(enTwigBNames);
        }
        when(categoryService.getPathsForCategory(twigCategoryB)).thenReturn(List.of(path4));

        List<CategoryModel> path5 = new LinkedList<>(List.of(rootCategory, branchCategoryAB, twigCategoryA,
                leafCategoryA));
        Map<Locale, Collection<String>> leafCatA1Name = new HashMap<>();
        Collection<String> enLeafA1Names = List.of(BRANCH_CATEGORY_AB_NAME_EN,
                TWIG_CATEGORY_A_NAME_EN, LEAF_CATEGORY_A_NAME_EN);
        Collection<String> frLeafA1Names = List.of(BRANCH_CATEGORY_AB_NAME_FR,
                TWIG_CATEGORY_A_NAME_FR, LEAF_CATEGORY_A_NAME_FR);
        leafCatA1Name.put(localeEn, enLeafA1Names);
        leafCatA1Name.put(localeFr, frLeafA1Names);
        if (localizedField) {
            when(snExpressionEvaluator.evaluate(eq(List.of(branchCategoryAB,twigCategoryA,leafCategoryA)), any(), any())).thenReturn(leafCatA1Name);
        } else {
            when(snExpressionEvaluator.evaluate(eq(List.of(branchCategoryAB,twigCategoryA,leafCategoryA)), any())).thenReturn(enLeafA1Names);
        }

        List<CategoryModel> path6 = new LinkedList<>(List.of(rootCategory, branchCategoryAB, twigCategoryB,
                leafCategoryA));
        Map<Locale, Collection<String>> leafCatA2Name = new HashMap<>();
        Collection<String> enLeafA2Names = List.of(BRANCH_CATEGORY_AB_NAME_EN,
                TWIG_CATEGORY_B_NAME_EN, LEAF_CATEGORY_A_NAME_EN);
        Collection<String> frLeafA2Names = List.of(BRANCH_CATEGORY_AB_NAME_FR,
                TWIG_CATEGORY_B_NAME_FR, LEAF_CATEGORY_A_NAME_FR);
        leafCatA2Name.put(localeEn, enLeafA2Names);
        leafCatA2Name.put(localeFr, frLeafA2Names);
        if (localizedField) {
            when(snExpressionEvaluator.evaluate(eq(List.of(branchCategoryAB,twigCategoryB,leafCategoryA)), any(), any())).thenReturn(leafCatA2Name);
        } else {
            when(snExpressionEvaluator.evaluate(eq(List.of(branchCategoryAB,twigCategoryB,leafCategoryA)), any())).thenReturn(enLeafA2Names);
        }
        when(categoryService.getPathsForCategory(leafCategoryA)).thenReturn(List.of(path5, path6));

    }

    private void setUpPathEvaluationWithFineRoot() throws SnException {
        /*
         * Category hierarchy set up as follows:
         *     fineRoot
         *        |
         *       root
         *        |
         *    branchAB
         *    /     \
         * twigA   twigB
         *     \    /
         *     leafA
         *
         * so 6 possible paths:
         */
        when(rootCategory.getCode()).thenReturn(ROOT_CATEGORY_CODE);
        when(branchCategoryAB.getCode()).thenReturn(BRANCH_CATEGORY_AB_CODE);
        when(twigCategoryA.getCode()).thenReturn(TWIG_CATEGORY_CODE_A);
        when(twigCategoryB.getCode()).thenReturn(TWIG_CATEGORY_CODE_B);
        when(leafCategoryA.getCode()).thenReturn(LEAF_CATEGORY_CODE_A);


        List<CategoryModel> path2 = new LinkedList<>(List.of(fineRootCategory, rootCategory, branchCategoryAB));
        Collection<String> enCatANames = List.of(BRANCH_CATEGORY_AB_NAME_EN);
        when(snExpressionEvaluator.evaluate(eq(List.of(branchCategoryAB)), any())).thenReturn(enCatANames);
        when(categoryService.getPathsForCategory(branchCategoryAB)).thenReturn(List.of(path2));

        List<CategoryModel> path3 = new LinkedList<>(List.of(fineRootCategory, rootCategory, branchCategoryAB, twigCategoryA));
        Collection<String> enTwigANames = List.of(BRANCH_CATEGORY_AB_NAME_EN,
                TWIG_CATEGORY_A_NAME_EN);
        when(snExpressionEvaluator.evaluate(eq(List.of(branchCategoryAB,twigCategoryA)), any())).thenReturn(enTwigANames);
        when(categoryService.getPathsForCategory(twigCategoryA)).thenReturn(List.of(path3));

        List<CategoryModel> path4 = new LinkedList<>(List.of(fineRootCategory, rootCategory, branchCategoryAB, twigCategoryB));
        Collection<String> enTwigBNames = List.of(BRANCH_CATEGORY_AB_NAME_EN,
                TWIG_CATEGORY_B_NAME_EN);
        when(snExpressionEvaluator.evaluate(eq(List.of(branchCategoryAB,twigCategoryB)), any())).thenReturn(enTwigBNames);
        when(categoryService.getPathsForCategory(twigCategoryB)).thenReturn(List.of(path4));

        List<CategoryModel> path5 = new LinkedList<>(List.of(fineRootCategory, rootCategory, branchCategoryAB, twigCategoryA,
                leafCategoryA));
        Collection<String> enLeafA1Names = List.of(BRANCH_CATEGORY_AB_NAME_EN,
                TWIG_CATEGORY_A_NAME_EN, LEAF_CATEGORY_A_NAME_EN);
        when(snExpressionEvaluator.evaluate(eq(List.of(branchCategoryAB,twigCategoryA,leafCategoryA)), any())).thenReturn(enLeafA1Names);

        List<CategoryModel> path6 = new LinkedList<>(List.of(fineRootCategory, rootCategory, branchCategoryAB, twigCategoryB,
                leafCategoryA));
        Collection<String> enLeafA2Names = List.of(BRANCH_CATEGORY_AB_NAME_EN,
                TWIG_CATEGORY_B_NAME_EN, LEAF_CATEGORY_A_NAME_EN);
        when(snExpressionEvaluator.evaluate(eq(List.of(branchCategoryAB,twigCategoryB,leafCategoryA)), any())).thenReturn(enLeafA2Names);
        when(categoryService.getPathsForCategory(leafCategoryA)).thenReturn(List.of(path5, path6));

    }

    public void setUpLocalization() {
        when(fieldWrapper.isLocalized()).thenReturn(true);
        when(fieldWrapper.getQualifiers()).thenReturn(List.of(qualifierEn, qualifierFr));
        when(qualifierEn.getAs(Locale.class)).thenReturn(localeEn);
        when(qualifierFr.getAs(Locale.class)).thenReturn(localeFr);
    }

    public void setUpNonLocalization() {
        when(fieldWrapper.isLocalized()).thenReturn(false);
    }

    @Test
    void getNonLocalisedFieldValue() throws SnException {
        setUpNonLocalization();
        setUpPathEvaluation(false);
        String value = (String) coveoProductCategoryHierarchyAttributeSnIndexValueProvider.getFieldValue(null,
                fieldWrapper, productModel, productCategoryData);
        String[] paths = value.split(";");
        validateEnPaths(paths);
    }

    @Test
    void getLocalisedFieldValue() throws SnException {
        setUpLocalization();
        setUpPathEvaluation(true);

        Map<Locale, String> value =
                (Map<Locale, String>) coveoProductCategoryHierarchyAttributeSnIndexValueProvider.getFieldValue(null,
                        fieldWrapper, productModel, productCategoryData);
        assertEquals(2, value.size());
        assertTrue(value.containsKey(localeEn));
        assertTrue(value.containsKey(localeFr));
        String localisedValueEn = value.get(localeEn);
        String[] pathsEn = localisedValueEn.split(";");
        validateEnPaths(pathsEn);
        String localisedValueFr = value.get(localeFr);
        String[] pathsFr = localisedValueFr.split(";");
        validateFrPaths(pathsFr);
    }

    @Test
    void testCategoryFilteringForNonLocalizedPath() throws SnException {
        setUpPathEvaluationWithFineRoot();
        String value =
                (String) coveoProductCategoryHierarchyAttributeSnIndexValueProvider.getFieldValue(null,
                        fieldWrapper, productModel, productCategoryData);
        String[] pathsEn = value.split(";");
        validateEnPaths(pathsEn);
    }

    private void validateFrPaths(String[] pathsFr) {
        List<String> pathsAsList = Arrays.asList(pathsFr);
        assertTrue(pathsAsList.contains(EXPECTED_BRANCH_AB_PATH_FR));
        assertTrue(pathsAsList.contains(EXPECTED_TWIG_A_PATH_FR));
        assertTrue(pathsAsList.contains(EXPECTED_TWIG_B_PATH_FR));
        assertTrue(pathsAsList.contains(EXPECTED_LEAF_A_PATH_1_FR));
        assertTrue(pathsAsList.contains(EXPECTED_LEAF_A_PATH_2_FR));
    }

    private static void validateEnPaths(String[] paths) {
        List<String> pathsAsList = Arrays.asList(paths);
        assertTrue(pathsAsList.contains(EXPECTED_BRANCH_AB_PATH_EN));
        assertTrue(pathsAsList.contains(EXPECTED_TWIG_A_PATH_EN));
        assertTrue(pathsAsList.contains(EXPECTED_TWIG_B_PATH_EN));
        assertTrue(pathsAsList.contains(EXPECTED_LEAF_A_PATH_1_EN));
        assertTrue(pathsAsList.contains(EXPECTED_LEAF_A_PATH_2_EN));
    }

    private static String generateEvaluatedPath(String... categories) {
        return String.join("|", categories);
    }
}