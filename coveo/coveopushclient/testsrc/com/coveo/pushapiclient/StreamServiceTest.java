package com.coveo.pushapiclient;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.commons.configuration.Configuration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@UnitTest
class StreamServiceTest {

    @Mock
    private ConfigurationService configurationService;
    @Mock
    private Configuration configuration;
    @Mock
    private StreamEnabledSource source;

    private UpdateStreamService updateStreamService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(configurationService.getConfiguration()).thenReturn(configuration);
        updateStreamService = new UpdateStreamService(configurationService);
    }

    @Test
    void testInit_initializesPlatformClientAndInternalService() {
        when(configuration.getInt("coveopushclient.maxretries", 10)).thenReturn(10);
        when(configuration.getInt("coveopushclient.retryafter.milliseconds", 5000)).thenReturn(5000);

        updateStreamService.init(source, new String[]{"userAgent/v1.0", "anotherAgent/v2"});

        verify(configuration).getInt("coveopushclient.maxretries", 10);
        verify(configuration).getInt("coveopushclient.retryafter.milliseconds", 5000);

        Assertions.assertNotNull(updateStreamService.platformClient);
        Assertions.assertNotNull(updateStreamService.updateStreamServiceInternal);
    }
}
