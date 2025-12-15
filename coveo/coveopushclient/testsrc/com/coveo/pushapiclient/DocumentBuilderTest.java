package com.coveo.pushapiclient;

import com.google.gson.JsonElement;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
class DocumentBuilderTest {

    private DocumentBuilder docBuilder;
    private final String URI = "the_uri";
    private final String TITLE = "the_title";

    @BeforeEach
    public void setUp() {
        docBuilder = new DocumentBuilder(URI, TITLE);
    }

    @Test
    public void testWithPermanentIdGeneration() {
        docBuilder = new DocumentBuilder("https://foo.com", "bar");
        assertEquals(
                "aa2e0510b66edff7f05e2b30d4f1b3a4b5481c06b69f41751c54675c5afb",
                docBuilder.marshalJsonObject().get("permanentId").getAsString());
    }

    @Test
    public void testWithClickableUri() {
        docBuilder.withClickableUri("the click uri");

        assertEquals("the click uri",
                docBuilder.marshalJsonObject().get("clickableUri").getAsString());
    }

    @Test
    public void testDocumentId() {
        assertEquals(URI, docBuilder.marshalJsonObject().get("documentId").getAsString());
    }

    @Test
    public void testWithMetadata() {
        docBuilder.withMetadata(
                new HashMap<>() {
                    {
                        put("my_field_1", "1");
                        put("my_field_2", false);
                        put("my_field_3", 1234);
                        put("my_field_4", new String[]{"a", "b", "c"});
                    }
                });
        assertEquals("1", docBuilder.marshalJsonObject().get("my_field_1").getAsString());
        assertFalse(docBuilder.marshalJsonObject().get("my_field_2").getAsBoolean());
        assertEquals(1234, docBuilder.marshalJsonObject().get("my_field_3").getAsInt());
        assertEquals(List.of("a", "b", "c"), docBuilder.marshalJsonObject().get("my_field_4").getAsJsonArray()
                .asList().stream().map(JsonElement::getAsString).toList());
    }

    @ParameterizedTest
    @MethodSource("reservedKeys")
    public void testReservedMetadataKeys(String key) {
        RuntimeException ex = assertThrows(
                RuntimeException.class, () -> docBuilder.withMetadata(Map.of(key, "this should blow up")));
        assertTrue(ex.getMessage().contains(
                String.format("Cannot use %s as a metadata key: It is a reserved key name.", key)));
    }

    private static List<String> reservedKeys() {
        return DocumentBuilder.reservedKeynames;
    }

    @Test
    public void marshal() {
        assertTrue(docBuilder.marshal().contains(TITLE));
    }
}
