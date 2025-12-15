package com.coveo.pushapiclient;

import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@UnitTest
class DocumentUploadQueueTest {
    @Mock
    private UploadStrategy uploadStrategy;

    @InjectMocks
    private DocumentUploadQueue queue;

    private AutoCloseable closeable;
    private DocumentBuilder documentToAdd;

    private final int oneMegaByte = 1 * 1024 * 1024;

    private String generateStringFromBytes(int numBytes) {
        byte[] bytes = new byte[numBytes];

        byte ascii_code = 65; // ASCII value for 'A'
        Arrays.fill(bytes, ascii_code);

        return new String(bytes);
    }

    private DocumentBuilder generateDocumentFromSize(int numBytes) {
        return new DocumentBuilder("https://my.document.uri?ref=1", "My bulky document")
                .withMetadata(Map.of("field", generateStringFromBytes(numBytes)));
    }

    @BeforeEach
    public void setup() {
        String twoMegaByteData = generateStringFromBytes(2 * oneMegaByte);

        documentToAdd =
                new DocumentBuilder("https://my.document.uri?ref=1", "My new document")
                        .withMetadata(Map.of("field", twoMegaByteData));

        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void closeService() throws Exception {
        closeable.close();
    }

    @Test
    public void testIsEmpty() {
        assertTrue(queue.isEmpty());
    }

    @Test
    public void testIsNotEmpty() throws Exception {
        queue.add(documentToAdd);
        assertFalse(queue.isEmpty());
    }

    @Test
    public void testShouldReturnBatch() throws Exception {
        BatchUpdate batchUpdate = new BatchUpdate(
                List.of(documentToAdd)
        );
        queue.add(documentToAdd);

        assertEquals(batchUpdate, queue.getBatch());
    }

    @Test
    public void testFlushShouldNotUploadDocumentsWhenRequiredSizeIsNotMet()
            throws Exception {
        queue.add(documentToAdd);

        verify(uploadStrategy, times(0)).apply(any(BatchUpdate.class));
    }

    @Test
    public void testShouldAutomaticallyFlushAccumulatedDocuments()
            throws Exception {
        DocumentBuilder bulkyDocument = generateDocumentFromSize(20 * oneMegaByte);
        BatchUpdate firstBatch = new BatchUpdate(
                List.of(bulkyDocument, bulkyDocument)
        );

        queue.add(bulkyDocument);
        queue.add(bulkyDocument);
        verify(uploadStrategy, times(0)).apply(any(BatchUpdate.class));

        queue.add(bulkyDocument);

        verify(uploadStrategy, times(1)).apply(any(BatchUpdate.class));
        verify(uploadStrategy, times(1)).apply(firstBatch);
    }

    @Test
    public void testShouldManuallyFlushAccumulatedDocuments()
            throws Exception {
        DocumentBuilder bulkyDocument = generateDocumentFromSize(20 * oneMegaByte);
        BatchUpdate firstBatch = new BatchUpdate(
                List.of(bulkyDocument, bulkyDocument));

        BatchUpdate secondBatch = new BatchUpdate(
                List.of(bulkyDocument));

        // Adding 3 documents of 20 MB to the queue. The queue size will reach 60 MB,
        // which exceeds the maximum queue size limit. Therefore, the 2 first added
        // documents will automatically be uploaded to the source.
        queue.add(bulkyDocument);
        queue.add(bulkyDocument);
        queue.add(bulkyDocument);

        queue.flush();

        // Additional flush will have no effect if documents where already flushed
        queue.flush();

        verify(uploadStrategy, times(2)).apply(any(BatchUpdate.class));
        verify(uploadStrategy, times(1)).apply(firstBatch);
        verify(uploadStrategy, times(1)).apply(secondBatch);
    }

    @Test
    public void testAddingEmptyDocument() throws Exception {
        DocumentBuilder nullDocument = null;

        queue.add(nullDocument);
        queue.flush();

        verify(uploadStrategy, times(0)).apply(any(BatchUpdate.class));
    }
}
