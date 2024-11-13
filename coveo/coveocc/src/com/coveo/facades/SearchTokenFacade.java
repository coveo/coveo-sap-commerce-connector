package com.coveo.facades;

import com.coveo.SearchTokenWsDTO;
import org.springframework.http.ResponseEntity;

public interface SearchTokenFacade {

    ResponseEntity<SearchTokenWsDTO> getSearchToken(String baseSiteId, String userId, long maxAgeMilliseconds, String userAgent);
}
