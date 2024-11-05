package com.coveo.facades.impl;

import com.coveo.SearchTokenBody;
import com.coveo.SearchTokenUserId;
import com.coveo.SearchTokenWsDTO;
import com.coveo.constants.CoveoccConstants;
import com.coveo.facades.SearchTokenFacade;
import com.coveo.facades.utils.SensitiveDataMaskingUtils;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.user.UserGroupModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.europe1.enums.UserPriceGroup;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.site.BaseSiteService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.coveo.constants.CoveoccConstants.USER_AGENT;

public class DefaultSearchTokenFacade implements SearchTokenFacade {
    private static final Logger LOG = Logger.getLogger(DefaultSearchTokenFacade.class);

    @Value("${coveocc.searchtoken.path}")
    private String searchTokenPath;

    @Resource(name="baseSiteService")
    private BaseSiteService baseSiteService;

    @Resource(name="searchTokenRestTemplate")
    private RestTemplate searchTokenRestTemplate;

    @Resource(name="userService")
    private UserService userService;

    @Override
    public ResponseEntity<SearchTokenWsDTO> getSearchToken(String baseSiteId , String userId, String searchHub,
                                                           long maxAgeMilliseconds, String userAgent) {
        BaseSiteModel baseSite = baseSiteService.getBaseSiteForUID(baseSiteId);
        HttpHeaders headers = buildHeaders(baseSite.getCoveoApiKey(), userAgent);
        Set<String> userPriceGroupIds = getUserPriceGroups(userId);
        SearchTokenBody searchTokenBody = buildRequestBody(userId, userPriceGroupIds, searchHub);
        URI uri = URI.create(baseSite.getCoveoPlatformUrl() + searchTokenPath);
        HttpEntity<SearchTokenBody> requestEntity = new HttpEntity<>(searchTokenBody, headers);

        if(LOG.isTraceEnabled()) {
            LOG.trace("Making a request to get a search token: ");
            LOG.trace("URI: " + uri);
            LOG.trace("User ids: [ " + searchTokenBody.getUserIds().stream()
                    .map(userInfo -> SensitiveDataMaskingUtils.maskEmail(userInfo.getName()))
                    .collect(Collectors.joining(", ")) + " ]");
            LOG.trace("Valid for: " + searchTokenBody.getValidFor());
            LOG.trace("Accept: " + headers.getAccept() + "; Content-Type: " + headers.getContentType());
            LOG.trace("User-Agent: " + headers.get(USER_AGENT));
        }
        return searchTokenRestTemplate.exchange(uri, HttpMethod.POST, requestEntity, SearchTokenWsDTO.class);
    }

    /**
     * Method used to get all the user price groups for a given user.
     * These values can be used to build the search token request and
     * provide authorization in the JWT for user specific pricing
     *
     * @param userId
     * @return list of user price group ids
     */
    protected Set<String> getUserPriceGroups(String userId) {
        Set<String> userPriceGroupIds = new HashSet<>();
        UserModel user = userService.getUserForUID(userId);
        if (user.getEurope1PriceFactory_UPG() != null) {
            userPriceGroupIds.add(user.getEurope1PriceFactory_UPG().getCode());
        }

        Set<UserGroupModel> userGroups = userService.getAllUserGroupsForUser(userService.getUserForUID(userId));
        userGroups.stream()
                .map(UserGroupModel::getUserPriceGroup)
                .filter(Objects::nonNull)
                .map(UserPriceGroup::getCode)
                .forEach(userPriceGroupIds::add);
        return userPriceGroupIds;
    }

    private HttpHeaders buildHeaders(String coveoApiKey, String userAgent) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(coveoApiKey);
        headers.set(USER_AGENT, userAgent);
        return headers;
    }

    private static SearchTokenBody buildRequestBody(String userId, Set<String> userGroupIds, String searchHub) {
        SearchTokenBody searchTokenBody = new SearchTokenBody();
        List<SearchTokenUserId> userTokenIds = new ArrayList<>();
        userTokenIds.add(createSearchTokenUserId(userId, "User"));
        // if the user is a member of one of the configured groups
        // then we include those details in the request
        userGroupIds.forEach(userGroupId ->
                userTokenIds.add(createSearchTokenUserId(userGroupId, "Group")));
        searchTokenBody.setUserIds(userTokenIds);
        searchTokenBody.setSearchHub(searchHub);
        searchTokenBody.setValidFor(TimeUnit.SECONDS.toMillis(CoveoccConstants.SEARCH_TOKEN_MAX_AGE_SECONDS));
        return searchTokenBody;
    }

    private static SearchTokenUserId createSearchTokenUserId(String name, String type) {
        SearchTokenUserId user = new SearchTokenUserId();
        user.setName(name);
        user.setType(type);
        user.setProvider("Email Security Provider");
        return user;
    }
}
