package com.coveo.pushapiclient;

import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@UnitTest
class PartialUpdateDocumentTest {
    @Test
    public void testCreatesPartialUpdateDocumentWithArrayAppendOperator() {
        String[] value = {"value1", "value2"};
        PartialUpdateDocument document =
                new PartialUpdateDocument("doc1", PartialUpdateOperator.ARRAYAPPEND, "field1", value);
        assertEquals("doc1", document.documentId);
        assertEquals(PartialUpdateOperator.ARRAYAPPEND, document.operator);
        assertEquals("field1", document.field);
        assertArrayEquals(value, (String[]) document.value);
    }

    @Test()
    public void testThrowsExceptionWhenValueIsNotArrayForArrayAppendOperator() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PartialUpdateDocument(
                        "doc1",
                        PartialUpdateOperator.ARRAYAPPEND, "field1", "value1"));
    }

    @Test
    public void testCreatesPartialUpdateDocumentWithFieldValueReplaceOperator() {
        String value = "value1";
        PartialUpdateDocument document =
                new PartialUpdateDocument("doc1", PartialUpdateOperator.FIELDVALUEREPLACE, "field1", value);
        assertEquals("doc1", document.documentId);
        assertEquals(PartialUpdateOperator.FIELDVALUEREPLACE, document.operator);
        assertEquals("field1", document.field);
        assertEquals(value, document.value);
    }

    @Test
    public void testAcceptsNullValues() {
        PartialUpdateDocument document =
                new PartialUpdateDocument("doc1", PartialUpdateOperator.FIELDVALUEREPLACE, "field1", null);
        assertEquals("doc1", document.documentId);
        assertEquals(PartialUpdateOperator.FIELDVALUEREPLACE, document.operator);
        assertEquals("field1", document.field);
        assertNull(document.value);
    }

    @Test
    public void testCreatesPartialUpdateDocumentWithDictionaryPutOperator() {
        Map<String, String> value = Map.of("key1", "value1");
        PartialUpdateDocument document =
                new PartialUpdateDocument("doc1", PartialUpdateOperator.DICTIONARYPUT, "field1", value);
        assertEquals("doc1", document.documentId);
        assertEquals(PartialUpdateOperator.DICTIONARYPUT, document.operator);
        assertEquals("field1", document.field);
        assertEquals(value, document.value);
    }

    @Test()
    public void testThrowsExceptionWhenValueIsNotJsonForDictionaryPutOperator() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PartialUpdateDocument(
                        "doc1",
                        PartialUpdateOperator.DICTIONARYPUT, "field1", "value1"));
    }

    @Test
    public void testCreatesPartialUpdateDocumentWithDictionaryRemoveOperator() {
        String value = "value1";
        PartialUpdateDocument document =
                new PartialUpdateDocument("doc1", PartialUpdateOperator.DICTIONARYREMOVE, "field1", value);
        assertEquals("doc1", document.documentId);
        assertEquals(PartialUpdateOperator.DICTIONARYREMOVE, document.operator);
        assertEquals("field1", document.field);
        assertEquals(value, document.value);

        String[] value2 = {"value1", "value2"};
        PartialUpdateDocument document2 =
                new PartialUpdateDocument("doc2", PartialUpdateOperator.DICTIONARYREMOVE, "field2", value2);
        assertEquals("doc2", document2.documentId);
        assertEquals(PartialUpdateOperator.DICTIONARYREMOVE, document2.operator);
        assertEquals("field2", document2.field);
        assertEquals(value2, document2.value);
    }

    @Test()
    public void testThrowsExceptionWhenValueIsNotStringOrArrayForDictionaryRemoveOperator() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PartialUpdateDocument(
                        "doc1",
                        PartialUpdateOperator.DICTIONARYREMOVE, "field1", 123));
    }

    @Test()
    public void testThrowsExceptionWhenInvalidOperatorIsUsed() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PartialUpdateDocument("doc1", null, "field1", "value1"));
    }

    @Test()
    public void testThrowsExceptionWhenDocumentIdIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PartialUpdateDocument(null, PartialUpdateOperator.ARRAYAPPEND, "field1", "value1"));
    }

    @Test()
    public void testThrowsExceptionWhenFieldIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PartialUpdateDocument("doc1", PartialUpdateOperator.ARRAYAPPEND, null, "value1"));

    }

    @Test()
    public void shouldThrowExceptionWhenOperatorIsNull() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new PartialUpdateDocument("doc1", null, "field1", "value1"));
    }
}
