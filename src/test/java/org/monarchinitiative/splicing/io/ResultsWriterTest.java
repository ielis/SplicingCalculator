package org.monarchinitiative.splicing.io;

import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.*;

public class ResultsWriterTest {


    @Test
    public void write() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ResultsWriter instance = new ResultsWriter(os);
//        instance.write();
    }
}