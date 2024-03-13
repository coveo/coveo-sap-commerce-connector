package com.coveo.stream.service;

import com.coveo.pushapiclient.DocumentBuilder;
import com.coveo.pushapiclient.exceptions.NoOpenFileContainerException;
import com.coveo.pushapiclient.exceptions.NoOpenStreamException;
import com.coveo.searchservices.data.CoveoSource;

import java.io.IOException;

public interface CoveoStreamService {
    public CoveoSource getCoveoSource();
    void pushDocument(DocumentBuilder document) throws IOException, InterruptedException;
    void closeStream() throws NoOpenStreamException, IOException, InterruptedException, NoOpenFileContainerException;
}
