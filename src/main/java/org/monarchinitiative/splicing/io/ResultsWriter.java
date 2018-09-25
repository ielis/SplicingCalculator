package org.monarchinitiative.splicing.io;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.Strand;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.monarchinitiative.splicing.calculate.ScoredTranscriptModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 */
public class ResultsWriter implements AutoCloseable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResultsWriter.class);

    private static final String[] HEADER = {"CHR", "BEGIN", "END", "BEGIN_T", "END_T", "STRAND", "SYMBOL", "ACCESSION_ID", "DONOR", "ACCEPTOR"};

    private static final char D = '\t';

    private static final Charset CHARSET = Charset.forName("UTF-8");

    private final OutputStream outputStream;


    public ResultsWriter(File outFile) throws FileNotFoundException {
        this(new FileOutputStream(outFile));
    }


    public ResultsWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
        String header = Arrays.stream(HEADER).collect(Collectors.joining(String.valueOf(D), "#", System.lineSeparator()));
        try { // write header
            outputStream.write(header.getBytes(CHARSET));
        } catch (IOException e) {
            LOGGER.warn("Unable to write header '{}'", header.getBytes(CHARSET));
        }
    }


    /**
     * Melt the <code>model</code> into <em>one-exon-per-line</em> and write the lines.
     *
     * @param model {@link ScoredTranscriptModel} to be written out
     */
    public void write(ScoredTranscriptModel model) {

        TranscriptModel tm = model.getTranscriptModel();
        ReferenceDictionary rd = tm.getTXRegion().getRefDict();
        String chr = rd.getContigIDToName().get(tm.getChr());
        StringBuilder builder = new StringBuilder(1000); //

        for (int i = 0; i < tm.getExonRegions().size(); i++) {
            GenomeInterval exon = tm.getExonRegions().get(i);
            builder = builder.append(chr).append(D) // chromosome
                    .append(exon.getGenomeBeginPos().withStrand(Strand.FWD).getPos()).append(D) // begin (FWD)
                    .append(exon.getGenomeEndPos().withStrand(Strand.FWD).getPos()).append(D) // end (FWD)
                    .append(exon.getBeginPos()).append(D) // begin (transcript's strand)
                    .append(exon.getEndPos()).append(D) // end (transcript's strand)
                    .append(tm.getStrand().isForward() ? '+' : '-').append(D) // strand of the transcript
                    .append(tm.getGeneSymbol()).append(D) // e.g. GCK
                    .append(tm.getAccession()).append(D) // e.g. NM_000162.3
                    .append(model.getDonors().get(i)).append(D) // donor score of the exon
                    .append(model.getAcceptors().get(i)) // acceptor score of the exon
                    .append(System.lineSeparator());
        }

        try {
            outputStream.write(builder.toString().getBytes(CHARSET));
        } catch (IOException e) {
            LOGGER.warn("Error writing line '{}'", builder.toString());
        }
    }


    @Override
    public void close() throws Exception {
        LOGGER.debug("Closing {}", getClass().getSimpleName());
        outputStream.close();
    }
}
