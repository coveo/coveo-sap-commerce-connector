package com.coveo.pushapiclient;

import com.coveo.pushapiclient.utils.StringSubscriber;
import com.google.gson.Gson;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@UnitTest
@ExtendWith(MockitoExtension.class)
class PlatformClientTest {
    @Mock ApiCore api;
    private final PlatformUrl URL = new PlatformUrl(Environment.PRODUCTION, Region.US);
    private final String API_KEY = "the_api_key";
    private final String ORGANIZATION_ID = "the_org_id";
    private final String SOURCE_ID = "my_source";
    private final String STREAM_ID = "stream_id";
    private PlatformClient client;

    private String[] getExpectedHeaders() {
        return new String[]{
                "Authorization", String.format("Bearer %s", API_KEY),
                "Content-Type", "application/json",
                "Accept", "application/json",
                "User-Agent", ""
        };
    }

    private ArgumentCaptor<HttpRequest.BodyPublisher> createBodyCaptor() {
         return ArgumentCaptor.forClass(HttpRequest.BodyPublisher.class);
    }

    private ArgumentCaptor<String[]> createheadersCaptor() {
        return ArgumentCaptor.forClass(String[].class);
    };

    private DocumentBuilder documentBuilder() {
        return new DocumentBuilder("the_uri", "the_title");
    }

    private Document document() {
        return documentBuilder().getDocument();
    }


    public FileContainer fileContainer() {
        FileContainer fileContainer = new FileContainer();
        fileContainer.fileId = "the_file_id";
        fileContainer.requiredHeaders = Map.of("foo", "bar");

        fileContainer.uploadUri = "https://upload.uri";
        return fileContainer;
    }

    public BatchUpdateRecord batchUpdateRecord() {
        BatchUpdate batchUpdate = new BatchUpdate(List.of(documentBuilder()));
        return batchUpdate.marshal();
    }

    @BeforeEach
    public void setupClient() {
        PlatformUrl url = new PlatformUrl(Environment.PRODUCTION, Region.US);
        this.client = new PlatformClient(API_KEY, ORGANIZATION_ID, url, api);
    }

    @Test
    public void testCreateCatalogSource() throws Exception {
        ArgumentCaptor<HttpRequest.BodyPublisher> bodyCaptor = createBodyCaptor();

        client.createSource("the_name", SourceType.CATALOG, SourceVisibility.SECURED);

        String sourcesUrl = String.format("%s/rest/organizations/%s/sources", URL.getPlatformUrl(), ORGANIZATION_ID);
        URI expectedUri = URI.create(sourcesUrl);



        verify(api).post(eq(expectedUri), eq(getExpectedHeaders()), bodyCaptor.capture());

        Map requestBody = StringSubscriber.toMap(Optional.ofNullable(bodyCaptor.getValue()));

        assertEquals("the_name", requestBody.get("name"));
        assertEquals(SourceVisibility.SECURED.toString(), requestBody.get("sourceVisibility"));
        assertEquals("CATALOG", requestBody.get("sourceType"));
        assertEquals(true, requestBody.get("pushEnabled"));
        assertEquals(true, requestBody.get("streamEnabled"));
    }

    @Test
    public void testCreateFileContainer() throws Exception {
        client.createFileContainer();
        URI expectedUri = URI.create(
                String.format("%s/push/v1/organizations/%s/files", URL.getApiUrl(), ORGANIZATION_ID));

        verify(api).post(expectedUri, getExpectedHeaders());
    }

    @Test
    public void testUploadContentToFileContainer() throws Exception {
        FileContainer fileContainer = fileContainer();
        ArgumentCaptor<HttpRequest.BodyPublisher> bodyCaptor = createBodyCaptor();

        client.uploadContentToFileContainer(fileContainer, new Gson().toJson(batchUpdateRecord()));

        URI expectedUri = URI.create(fileContainer.uploadUri);
        String[] expectedHeaders = fileContainer
                .requiredHeaders
                .entrySet()
                .stream()
                .flatMap(e -> Stream.of(e.getKey(), e.getValue()))
                .toArray(String[]::new);

        verify(api).put(eq(expectedUri), eq(expectedHeaders), bodyCaptor.capture());

        Map requestBody = StringSubscriber.toMap(Optional.ofNullable(bodyCaptor.getValue()));
        ArrayList<Map> addOrUpdate = (ArrayList<Map>) requestBody.get("addOrUpdate");
        ArrayList<Map> delete = (ArrayList<Map>) requestBody.get("delete");

        assertEquals(document().uri, addOrUpdate.get(0).get("documentId"));
    }

    @Test
    public void testPushFileContainerContentToStream() throws Exception {
        FileContainer fileContainer = fileContainer();

        client.pushFileContainerContentToStreamSource(SOURCE_ID, fileContainer);

        URI expectedUri = URI.create(String.format(
                "%s/push/v1/organizations/%s/sources/%s/stream/update?fileId=%s",
                URL.getApiUrl(), ORGANIZATION_ID, SOURCE_ID, fileContainer.fileId));

        verify(api).put(eq(expectedUri), eq(getExpectedHeaders()), any());
    }

    @Test
    public void testOpenStream() throws Exception {
        client.openStream(SOURCE_ID);

        URI expectedUri = URI.create(
                String.format("%s/push/v1/organizations/%s/sources/%s/stream/open",
                        URL.getApiUrl(), ORGANIZATION_ID, SOURCE_ID));

        verify(api).post(expectedUri, getExpectedHeaders());
    }

    @Test
    public void testRequireStreamChunk() throws Exception {
        client.requireStreamChunk(SOURCE_ID, STREAM_ID);
        URI expectedUri = URI.create(
                String.format("%s/push/v1/organizations/%s/sources/%s/stream/%s/chunk",
                        URL.getApiUrl(), ORGANIZATION_ID, SOURCE_ID, STREAM_ID));

        verify(api).post(expectedUri, getExpectedHeaders());
    }

    @Test
    public void testCloseStream() throws Exception {
        client.closeStream(SOURCE_ID, STREAM_ID);
        URI expectedUri = URI.create(
                String.format("%s/push/v1/organizations/%s/sources/%s/stream/%s/close",
                        URL.getApiUrl(), ORGANIZATION_ID, SOURCE_ID, STREAM_ID));

        verify(api).post(expectedUri, getExpectedHeaders());
    }

    @Test
    public void testCorrectUserAgentHeader() throws Exception {
        ArgumentCaptor<String[]> headersCaptor = createheadersCaptor();
        String[] userAgents = {"MyAgent/v1", "MyAgent/v2.1", "MyAgent/v3.1.1"};

        client.setUserAgents(userAgents);
        client.openStream(SOURCE_ID);
        verify(api).post(any(), headersCaptor.capture());

        List<String> headers = Arrays.stream(headersCaptor.getValue()).toList();
        assertTrue(headers.contains(String.join(" ", userAgents)));
    }

    @Test()
    public void testInvalidHeaderValue() {
        String[] userAgents = {"MyAgent/v1", "MyAgent/v2.1", "MyAgent/v3.1.1", "invalidHeaderValue"};
        assertThrows(IllegalArgumentException.class, () -> client.setUserAgents(userAgents));

    }

    @Test()
    public void testInvalidSemanticVersionHeaderValue() {
        String[] userAgents = {"MyAgent/v1.1.1.1"};
        assertThrows(IllegalArgumentException.class, () -> client.setUserAgents(userAgents));
    }
}
