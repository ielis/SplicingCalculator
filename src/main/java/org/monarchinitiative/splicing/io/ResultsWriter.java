package org.monarchinitiative.splicing.io;

import org.monarchinitiative.splicing.calculate.CoolTranscriptModel;

import java.io.*;

/**
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 */
public class ResultsWriter implements AutoCloseable{

    private final OutputStream outputStream;


    public ResultsWriter(File outFile) throws FileNotFoundException {
        outputStream = new FileOutputStream(outFile);
    }


    public ResultsWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void write(CoolTranscriptModel model) {

    }

    @Override
    public void close() throws Exception {
        outputStream.close();
    }
}
