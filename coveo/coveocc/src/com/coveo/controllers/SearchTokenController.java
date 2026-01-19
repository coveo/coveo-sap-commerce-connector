package com.coveo.controllers;

import com.coveo.SearchTokenWsDTO;
import com.coveo.constants.CoveoccConstants;
import com.coveo.facades.SearchTokenFacade;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.webservicescommons.cache.CacheControl;
import de.hybris.platform.webservicescommons.cache.CacheControlDirective;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


import jakarta.annotation.Resource;
import java.util.concurrent.TimeUnit;

import static com.coveo.constants.CoveoccConstants.COVEOCC_USER_AGENT;
import static com.coveo.constants.CoveoccConstants.COVEOCC_USER_AGENT_PROPERTY;

@Controller
@RequestMapping(value="/{baseSiteId}/coveo")
public class SearchTokenController {
    private static final Logger LOG = Logger.getLogger(SearchTokenController.class);

    @Resource(name = "searchTokenFacade")
    private SearchTokenFacade searchTokenFacade;

    @Resource(name = "configurationService")
    private ConfigurationService configurationService;

    @GetMapping(value="token")
    @CacheControl(directive = CacheControlDirective.PRIVATE,maxAge = CoveoccConstants.SEARCH_TOKEN_MAX_AGE_SECONDS)
    @ResponseBody
    @Tag(name="Coveo Search Token")
    @Operation(operationId="getSearchToken",
            description="Gets a JWT for a user that can be used to access the Coveo Search API. "
                    +"If the user is currently logged in, the JWT will contain the users unique ID, otherwise the JWT will be for an anonymous user",
            summary="Get JWT for Coveo Search API")
    public ResponseEntity<SearchTokenWsDTO> getSearchToken(@Parameter(in=ParameterIn.PATH, required=true, description="The base site id the JWT is for")
                                                           @PathVariable final String baseSiteId)
    {
        if (LOG.isTraceEnabled()) LOG.trace("Getting search token for baseSiteId: " + baseSiteId);
        String userAgent = configurationService.getConfiguration().getString(COVEOCC_USER_AGENT_PROPERTY, COVEOCC_USER_AGENT);
        return searchTokenFacade.getSearchToken(baseSiteId, getUserId(),
                TimeUnit.SECONDS.toMillis(CoveoccConstants.SEARCH_TOKEN_MAX_AGE_SECONDS), userAgent);
    }

    private String getUserId() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getPrincipal().toString();
    }
}
