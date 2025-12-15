package com.coveo.pushapiclient;

import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
class ApiUrlTest {
    @Test
    void testGetUrl_parsesValidStreamUrl() throws Exception {
        String streamUrl = "https://apidev.cloud.coveo.com/push/v1/organizations/organizationId/sources/sourceId/stream/open";
        URL url = new URL(streamUrl);
        ApiUrl apiUrl = new ApiUrl(url);

        assertEquals(streamUrl, apiUrl.getUrl());
    }

    @Test
    void testGetSourceId_parsesSourceIdFromValidStreamUrl() throws Exception {
        String SOURCE_ID = "sourceId";
        String streamUrl = String.format("https://apidev.cloud.coveo.com/push/v1/organizations/organizationId/sources/%s/stream/open", SOURCE_ID);
        URL url = new URL(streamUrl);
        ApiUrl apiUrl = new ApiUrl(url);

        assertEquals(SOURCE_ID, apiUrl.getSourceId());
    }

    @Test
    void testGetOrganizationId_parsesOrganizationIdFromValidStreamUrl() throws Exception {
        String ORGANIZATION_ID = "organizationId";
        String streamUrl = String.format("https://apidev.cloud.coveo.com/push/v1/organizations/%s/sources/sourceId/stream/open", ORGANIZATION_ID);
        URL url = new URL(streamUrl);
        ApiUrl apiUrl = new ApiUrl(url);

        assertEquals(ORGANIZATION_ID, apiUrl.getOrganizationId());
    }

    @Test
    void testInit_throwsMalformedUrlExceptionWhenStreamUrlOriginInvalid() throws Exception {
        String streamUrl = "https://api.coveo.com/push/v1/organizations/organizationId/sources/sourceId/stream/open";
        URL url = new URL(streamUrl);

        MalformedURLException ex = assertThrows(MalformedURLException.class, () -> new ApiUrl(url));
        assertTrue(ex.getMessage().contains("Invalid API URL host"));
    }

    @Test
    void testInit_throwsMalformedUrlExceptionWhenStreamUrlEndpointInvalid() throws Exception {
        String streamUrl = "https://api.coveo.com/push/organizations/organizationId/sources/sourceId/stream/open";
        URL url = new URL(streamUrl);

        MalformedURLException ex = assertThrows(MalformedURLException.class, () -> new ApiUrl(url));
        assertTrue(ex.getMessage().contains("Unable to find organization and source ids from the provided API url"));
    }

    @ParameterizedTest()
    @EnumSource(Environment.class)
    void testGetPlatformUrl_handlesEnvironmentSpecificUrls(Environment env) throws Exception {
        String streamUrl = String.format("https://api%s.cloud.coveo.com/push/v1/organizations/organizationId/sources/sourceId/stream/open", env.getValue());
        URL url = new URL(streamUrl);
        ApiUrl apiUrl = new ApiUrl(url);

        PlatformUrl platformUrl = apiUrl.getPlatformUrl();

        assertEquals(String.format("https://platform%s.cloud.coveo.com", env.getValue()), platformUrl.getPlatformUrl());
        assertEquals(String.format("https://api%s.cloud.coveo.com", env.getValue()), platformUrl.getApiUrl());
    }

    @Test
    void testInit_throwsMalformedUrlExceptionForStreamUrlInvalidEnvironment() throws Exception {
        String streamUrl = "https://apiprod.cloud.coveo.com/push/v1/organizations/organizationId/sources/sourceId/stream/open";
        URL url = new URL(streamUrl);

        MalformedURLException ex = assertThrows(MalformedURLException.class, () -> new ApiUrl(url));
        assertEquals("Invalid platform environment 'prod'", ex.getMessage());
    }

    @ParameterizedTest()
    @EnumSource(Region.class)
    void testGetPlatformUrl_handlesRegionSpecificUrls(Region region) throws Exception {
        String regionSuffix = region.equals(Region.US) ? "" : String.format("-%s", region.getValue());
        String streamUrl = String.format("https://api%s.cloud.coveo.com/push/v1/organizations/organizationId/sources/sourceId/stream/open", regionSuffix);
        URL url = new URL(streamUrl);
        ApiUrl apiUrl = new ApiUrl(url);

        PlatformUrl platformUrl = apiUrl.getPlatformUrl();

        assertEquals(String.format("https://platform%s.cloud.coveo.com", regionSuffix), platformUrl.getPlatformUrl());
        assertEquals(String.format("https://api%s.cloud.coveo.com", regionSuffix), platformUrl.getApiUrl());
    }

    @Test
    void testInit_throwsMalformedUrlExceptionForStreamUrlWithInvalidRegion() throws Exception {
        String streamUrl = "https://api-us.cloud.coveo.com/push/v1/organizations/organizationId/sources/sourceId/stream/open";
        URL url = new URL(streamUrl);

        MalformedURLException ex = assertThrows(MalformedURLException.class, () -> new ApiUrl(url));
        assertEquals("Invalid platform region 'us'", ex.getMessage());
    }
}
