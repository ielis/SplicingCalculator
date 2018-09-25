package org.monarchinitiative.splicing;

import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.SerializationException;
import org.monarchinitiative.splicing.calculate.SplicingInformationContentAnnotator;
import org.monarchinitiative.splicing.calculate.TranscriptScorer;
import org.monarchinitiative.splicing.io.GenomeSequenceAccessor;
import org.monarchinitiative.splicing.io.PositionalWeightMatrixParser;
import org.monarchinitiative.splicing.io.ResultsWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.*;
import java.net.URL;
import java.util.Objects;
import java.util.zip.GZIPOutputStream;

/**
 * Beans for the app's function.
 *
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 */
@Configuration
public class SplicingCalculatorConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(SplicingCalculatorConfiguration.class);

    private final Environment env;


    public SplicingCalculatorConfiguration(Environment env) {
        this.env = env;
    }


    @Bean
    public JannovarData jannovarData() throws SerializationException {
        return new JannovarDataSerializer(env.getProperty("jannovar.cache.file")).load();
    }


    @Bean
    public TranscriptScorer transcriptScorer(SplicingInformationContentAnnotator splicingInformationContentAnnotator,
                                             GenomeSequenceAccessor genomeSequenceAccessor) {
        return new TranscriptScorer(splicingInformationContentAnnotator, genomeSequenceAccessor);
    }


    @Bean
    public File refGenomeFastaFile() {
        return new File(Objects.requireNonNull(env.getProperty("ref.genome.fasta.file")));
    }


    @Bean
    public GenomeSequenceAccessor genomeSequenceAccessor(File refGenomeFastaFile) {
        LOGGER.info("Using reference genome FASTA at '{}'", refGenomeFastaFile.getAbsolutePath());
        return new GenomeSequenceAccessor(refGenomeFastaFile);
    }


    @Bean
    public URL spliceSitesUrl() {
        return getClass().getResource("spliceSites.yaml");
    }


    @Bean
    public SplicingInformationContentAnnotator splicingInformationContentAnnotator(URL spliceSitesUrl) throws IOException {
        SplicingInformationContentAnnotator icAnnotator;
        LOGGER.info("Parsing splice site definitions from '{}'", spliceSitesUrl.toExternalForm());
        try (InputStream is = new BufferedInputStream(spliceSitesUrl.openStream())) {
            PositionalWeightMatrixParser parser = new PositionalWeightMatrixParser(is);
            icAnnotator = new SplicingInformationContentAnnotator(parser.getDonorMatrix(), parser.getAcceptorMatrix());
        }
        return icAnnotator;
    }


    @Bean
    public File mainOutputFile() {
        return new File(Objects.requireNonNull(env.getProperty("main.output.file")));
    }


    @Bean
    public ResultsWriter resultsWriter(File mainOutputFile) throws IOException {
        if (!mainOutputFile.getParentFile().isDirectory() && !mainOutputFile.getParentFile().mkdirs())
            // try to create parent folders if they do not exist
            throw new RuntimeException("Unable to create file " + mainOutputFile.getAbsolutePath());

        if (mainOutputFile.getName().endsWith(".gz")) {
            LOGGER.info("Writing results in compressed format to '{}'", mainOutputFile.getAbsolutePath());
            return new ResultsWriter(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(mainOutputFile))));
        } else {
            LOGGER.info("Writing results to '{}'", mainOutputFile.getAbsolutePath());
            return new ResultsWriter(new BufferedOutputStream(new FileOutputStream(mainOutputFile)));
        }
    }

}
