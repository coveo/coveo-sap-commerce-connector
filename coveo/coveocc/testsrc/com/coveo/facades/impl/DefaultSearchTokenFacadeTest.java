package com.coveo.facades.impl;

import com.coveo.SearchTokenBody;
import com.coveo.SearchTokenWsDTO;
import com.coveo.constants.CoveoccConstants;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.europe1.enums.UserPriceGroup;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.coveo.constants.CoveoccConstants.USER_AGENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
public class DefaultSearchTokenFacadeTest {

    private static final String TEST_COVEO_BASE_URL = "https://localhost:666";
    private static final String TEST_COVEO_API_KEY = "test-api-key";
    private static final String TEST_BASE_SITE_ID = "testBaseSite";
    private static final String TEST_USER_ID = "testUserId";
    private static final String TEST_SEARCH_HUB = "testSearchHub";
    private static final String TEST_TOKEN = "testToken";
    private static final Long TEST_MAX_AGE_MS = 3000L;
    private static final String PRICE_GROUP1 = "group1";
    private static final String PRICE_GROUP2 = "group2";

    @InjectMocks
    private final DefaultSearchTokenFacade searchTokenFacade = new DefaultSearchTokenFacade();

    @Mock
    private BaseSiteService baseSiteService;

    @Mock
    private RestTemplate searchTokenRestTemplate;

    @Mock
    private UserService userService;

    @Mock
    private BaseSiteModel mockBaseSite;

    @Captor
    private ArgumentCaptor<URI> uriCaptor;

    @Captor
    private ArgumentCaptor<HttpMethod> httpMethodCaptor;

    @Captor
    private ArgumentCaptor<HttpEntity<SearchTokenBody>> requestEntityCaptor;

    @Captor
    private ArgumentCaptor<Class<SearchTokenWsDTO>> classCaptor;

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(searchTokenFacade, "searchTokenPath", "/rest/search/v2/token");
        MockitoAnnotations.initMocks(this);
        when(mockBaseSite.getCoveoPlatformUrl()).thenReturn(TEST_COVEO_BASE_URL);
        when(mockBaseSite.getCoveoApiKey()).thenReturn(TEST_COVEO_API_KEY);
        when(baseSiteService.getBaseSiteForUID(TEST_BASE_SITE_ID)).thenReturn(mockBaseSite);
        UserPriceGroup priceGroup1 = UserPriceGroup.valueOf(PRICE_GROUP1);
        UserPriceGroup priceGroup2 = UserPriceGroup.valueOf(PRICE_GROUP2);
        UserModel user = new UserModel();
        user.setEurope1PriceFactory_UPG(priceGroup1);
        when(userService.getUserForUID(TEST_USER_ID)).thenReturn(user);
        UserGroupModel group1 = new UserGroupModel();
        group1.setUserPriceGroup(priceGroup1);
        UserGroupModel group2 = new UserGroupModel();
        group2.setUserPriceGroup(priceGroup2);
        UserGroupModel group3 = new UserGroupModel();
        Set<UserGroupModel> userGroups = new HashSet<>(Arrays.asList(group1, group2, group3));
        when(userService.getAllUserGroupsForUser(user)).thenReturn(userGroups);

        final SearchTokenWsDTO token = new SearchTokenWsDTO();
        token.setToken(TEST_TOKEN);
        final ResponseEntity<SearchTokenWsDTO> responseEntity = new ResponseEntity<>(token, HttpStatus.OK);

        when(searchTokenRestTemplate.exchange(anyObject(), eq(HttpMethod.POST), anyObject(), eq(SearchTokenWsDTO.class))).thenReturn(responseEntity);
    }

    @Test
    public void testTokenGeneration() {
        ResponseEntity<SearchTokenWsDTO> tokenResponse = searchTokenFacade.getSearchToken(TEST_BASE_SITE_ID, TEST_USER_ID, TEST_SEARCH_HUB, TEST_MAX_AGE_MS, CoveoccConstants.COVEOCC_USER_AGENT);

        assertThat(tokenResponse.getBody()).isNotNull();
        assertThat(tokenResponse.getBody().getToken()).isEqualTo(TEST_TOKEN);

        verify(searchTokenRestTemplate).exchange(uriCaptor.capture(), httpMethodCaptor.capture(), requestEntityCaptor.capture(), classCaptor.capture());
        assertThat(uriCaptor.getValue()).hasToString(TEST_COVEO_BASE_URL + "/rest/search/v2/token");
        assertThat(httpMethodCaptor.getValue()).isEqualTo(HttpMethod.POST);
        assertThat(classCaptor.getValue()).isEqualTo(SearchTokenWsDTO.class);

        final HttpEntity<SearchTokenBody> httpEntity = requestEntityCaptor.getValue();

        final List<String> auth = httpEntity.getHeaders().get("Authorization");
        assertThat(auth).isNotNull().hasSize(1);
        assertThat(auth.get(0)).contains(TEST_COVEO_API_KEY);

        final List<String> userAgent = httpEntity.getHeaders().get(USER_AGENT);
        assertThat(userAgent).isNotNull().hasSize(1);
        assertThat(userAgent.get(0)).contains(CoveoccConstants.COVEOCC_USER_AGENT);

        final SearchTokenBody body = httpEntity.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getSearchHub()).isEqualTo(TEST_SEARCH_HUB);
        assertThat(body.getUserIds()).hasSize(3).extracting("name", "type")
                .containsExactlyInAnyOrder(
                        tuple(TEST_USER_ID, "User"),
                        tuple(PRICE_GROUP1, "Group"),
                        tuple(PRICE_GROUP2, "Group"));
    }
}
