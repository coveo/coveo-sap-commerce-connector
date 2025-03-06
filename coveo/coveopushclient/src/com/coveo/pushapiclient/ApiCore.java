package com.coveo.pushapiclient;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpResponse;
import java.util.function.Function;

class ApiCore {
  private static final String EMPTY_HEADER_EXCEPTION_MESSAGE = "HTTP/1.1 header parser received no bytes";
  private static final Logger LOGGER = Logger.getLogger(ApiCore.class);

  private final HttpClient httpClient;
  private final BackoffOptions options;

  public ApiCore() {
    this(HttpClient.newHttpClient());
  }

  public ApiCore(BackoffOptions options) {
    this(HttpClient.newHttpClient(), options);
  }

  public ApiCore(HttpClient httpClient) {
    this(httpClient, new BackoffOptionsBuilder().build());
  }

  public ApiCore(HttpClient httpClient, BackoffOptions options) {
    this.httpClient = httpClient;
    this.options = options;
  }

  public HttpResponse<String> callApiWithRetries(HttpRequest request)
      throws IOException, InterruptedException {
    IntervalFunction intervalFn =
        IntervalFunction.ofExponentialRandomBackoff(
            this.options.getRetryAfter(), this.options.getTimeMultiple());

    RetryConfig retryConfig =
        RetryConfig.<HttpResponse<String>>custom()
            .maxAttempts(this.options.getMaxRetries())
            .intervalFunction(intervalFn)
            .retryOnResult(response -> response != null && (
               response.statusCode() == 429 ||
               response.statusCode() == 503 ||
               response.statusCode() == 504 ||
               response.statusCode() == 408
            ))
            .retryOnException(e -> {
              if (e instanceof RuntimeException) {
                return e.getMessage() != null &&
                        e.getMessage().contains(EMPTY_HEADER_EXCEPTION_MESSAGE);
              }
              return false;
            })
            .build();

    if(LOGGER.isDebugEnabled()) {
      LOGGER.debug("Retrying with config: ");
      LOGGER.debug("Max attempts: " + retryConfig.getMaxAttempts());
      LOGGER.debug("Retry ms: {}" + this.options.getRetryAfter());
      LOGGER.debug("Backoff Multiplier: {}" + this.options.getTimeMultiple());
    }

    Retry retry = Retry.of("platformRequest", retryConfig);

    Function<HttpRequest, HttpResponse<String>> retryRequestFn =
        Retry.decorateFunction(retry, this::sendRequest);

    return retryRequestFn.apply(request);
  }

  public HttpResponse<String> sendRequest(HttpRequest request) {
    String uri = request.uri().toString();
    String reqMethod = request.method();
    if(LOGGER.isDebugEnabled()) this.LOGGER.debug(reqMethod + " " + uri);
    try {
      HttpResponse<String> response = this.httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      this.logResponse(response);
      return response;
    } catch (IOException | InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public HttpResponse<String> post(URI uri, String[] headers)
      throws IOException, InterruptedException {
    return this.post(uri, headers, HttpRequest.BodyPublishers.ofString(""));
  }

  public HttpResponse<String> post(URI uri, String[] headers, BodyPublisher body)
      throws IOException, InterruptedException {
    if(LOGGER.isDebugEnabled()) LOGGER.debug("POST " + uri);
    HttpRequest request = HttpRequest.newBuilder().headers(headers).uri(uri).POST(body).build();
    HttpResponse<String> response = this.callApiWithRetries(request);
    this.logResponse(response);
    return response;
  }

  public HttpResponse<String> put(URI uri, String[] headers, BodyPublisher body)
      throws IOException, InterruptedException {
    if(LOGGER.isDebugEnabled()) LOGGER.debug("PUT " + uri);
    HttpRequest request = HttpRequest.newBuilder().headers(headers).uri(uri).PUT(body).build();
    HttpResponse<String> response = this.callApiWithRetries(request);
    this.logResponse(response);
    return response;
  }

  public HttpResponse<String> delete(URI uri, String[] headers)
      throws IOException, InterruptedException {
    if(LOGGER.isDebugEnabled()) LOGGER.debug("DELETE " + uri);
    HttpRequest request = HttpRequest.newBuilder().headers(headers).uri(uri).DELETE().build();
    HttpResponse<String> response = this.callApiWithRetries(request);
    this.logResponse(response);
    return response;
  }

  public HttpResponse<String> delete(URI uri, String[] headers, BodyPublisher body)
      throws IOException, InterruptedException {
    if(LOGGER.isDebugEnabled()) LOGGER.debug("DELETE " + uri);
    HttpRequest request =
        HttpRequest.newBuilder().headers(headers).uri(uri).method("DELETE", body).build();
    HttpResponse<String> response = this.callApiWithRetries(request);
    this.logResponse(response);
    return response;
  }

  private void logResponse(HttpResponse<String> response) {
    if (response == null) {
      return;
    }

    int status = response.statusCode();

    if (status < 200 || status >= 300) {
      String method = response.request().method();
      String statusMessage = method + " status: " + status;
      String responseMessage = method + " response: " + response.body();
      LOGGER.error(statusMessage);
      LOGGER.error(responseMessage);
    } else {
      if(LOGGER.isDebugEnabled()) {
        LOGGER.debug(response.request().method() + " status: " + status);
        LOGGER.debug(response.request().method() + " response: " + response.body());
      }
    }
  }
}
