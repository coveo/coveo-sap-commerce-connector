package com.coveo.pushapiclient;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@UnitTest
class ApiCoreTest {
    @Mock private HttpClient mockHttpClient;
    @Mock private HttpRequest mockRequest;
    @Mock private HttpResponse<String> mockResponse;
    @Mock private BackoffOptions mockOptions;
    @InjectMocks private ApiCore apiCore;
    private HttpRequest request;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(mockOptions.getRetryAfter()).thenReturn(1000);
        when(mockOptions.getTimeMultiple()).thenReturn(2);
        when(mockOptions.getMaxRetries()).thenReturn(3);
        when(mockResponse.request()).thenReturn(mockRequest);
        when(mockRequest.method()).thenReturn("GET");
        request = HttpRequest.newBuilder().uri(URI.create("http://example.com")).build();
    }

    @Test
    void callApiWithRetries_successfulResponse() throws IOException, InterruptedException {
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        HttpResponse<String> response = apiCore.callApiWithRetries(request);

        assertEquals(200, response.statusCode());
        verify(mockHttpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void callApiWithRetries_retryOn429() throws IOException, InterruptedException {
        HttpResponse<String> firstResponse = getStringHttpResponse(429);
        HttpResponse<String> secondResponse = getStringHttpResponse(200);


        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(firstResponse)
                .thenReturn(secondResponse);

        HttpResponse<String> response = apiCore.callApiWithRetries(request);

        assertEquals(200, response.statusCode());
        verify(mockHttpClient, times(2)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));

    }

    private HttpResponse<String> getStringHttpResponse(int t) {
        HttpResponse<String> firstResponse = mock(HttpResponse.class);
        when(firstResponse.statusCode()).thenReturn(t);
        when(firstResponse.request()).thenReturn(mockRequest);
        return firstResponse;
    }

    @Test
    void callApiWithRetries_retryOn503() throws IOException, InterruptedException {
        HttpResponse<String> firstResponse = getStringHttpResponse(503);
        HttpResponse<String> secondResponse = getStringHttpResponse(200);

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(firstResponse)
                .thenReturn(secondResponse);

        HttpResponse<String> response = apiCore.callApiWithRetries(request);

        assertEquals(200, response.statusCode());
        verify(mockHttpClient, times(2)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));

    }

    @Test
    void callApiWithRetries_retryOn504() throws IOException, InterruptedException {
        HttpResponse<String> firstResponse = getStringHttpResponse(504);
        HttpResponse<String> secondResponse = getStringHttpResponse(200);

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(firstResponse)
                .thenReturn(secondResponse);

        HttpResponse<String> response = apiCore.callApiWithRetries(request);

        assertEquals(200, response.statusCode());
        verify(mockHttpClient, times(2)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));

    }

    @Test
    void callApiWithRetries_retryOn408() throws IOException, InterruptedException {
        HttpResponse<String> firstResponse = getStringHttpResponse(408);
        HttpResponse<String> secondResponse = getStringHttpResponse(200);

        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(firstResponse)
                .thenReturn(secondResponse);

        HttpResponse<String> response = apiCore.callApiWithRetries(request);

        assertEquals(200, response.statusCode());
        verify(mockHttpClient, times(2)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));

    }

    @Test
    void callApiWithRetries_retryOnIOException() throws IOException, InterruptedException {
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("HTTP/1.1 header parser received no bytes"))
                .thenReturn(mockResponse);
        when(mockResponse.statusCode()).thenReturn(200);

        HttpResponse<String> response = apiCore.callApiWithRetries(request);

        assertEquals(200, response.statusCode());
        verify(mockHttpClient, times(2)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void callApiWithRetries_retryOnIOException_failsAfterRetries() throws IOException, InterruptedException {
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("HTTP/1.1 header parser received no bytes"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> apiCore.callApiWithRetries(request));
        assertInstanceOf(IOException.class, exception.getCause());
        assertEquals("HTTP/1.1 header parser received no bytes", exception.getCause().getMessage());
        verify(mockHttpClient, times(3)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void callApiWithRetries_noRetryOnOtherIOException() throws IOException, InterruptedException {
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("Other IOException"));

        assertThrows(RuntimeException.class, () -> apiCore.callApiWithRetries(request));
        verify(mockHttpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));

    }

    @Test
    void callApiWithRetries_noRetryOnNonRetriableStatus() throws IOException, InterruptedException {
        when(mockResponse.statusCode()).thenReturn(418);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        HttpResponse<String> response = apiCore.callApiWithRetries(request);

        assertEquals(418, response.statusCode());
        verify(mockHttpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));

    }
}
