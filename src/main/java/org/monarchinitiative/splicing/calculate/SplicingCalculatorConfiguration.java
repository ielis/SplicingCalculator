package org.monarchinitiative.splicing.calculate;

import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.SerializationException;
import htsjdk.samtools.util.BlockCompressedOutputStream;
import org.monarchinitiative.splicing.io.GenomeSequenceAccessor;
import org.monarchinitiative.splicing.io.ResultsWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.io.*;
import java.util.Objects;

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
        return new GenomeSequenceAccessor(refGenomeFastaFile);
    }


    @Bean
    public File spliceSitesFile() {
        return new File(getClass().getResource("spliceSites.yaml").getFile());
    }


    @Bean
    public SplicingInformationContentAnnotator splicingInformationContentAnnotator(File spliceSitesFile) throws IOException {
        SplicingInformationContentAnnotator icAnnotator;
        try (InputStream is = new FileInputStream(spliceSitesFile)) {
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
    public ResultsWriter resultsWriter(File mainOutputFile) throws FileNotFoundException {
        if (!mainOutputFile.getParentFile().isDirectory() || !mainOutputFile.mkdirs())
            // try to create parent folders if they do not exist
            throw new RuntimeException("Unable to create file " + mainOutputFile.getAbsolutePath());

        if (mainOutputFile.getName().endsWith(".gz")) {
            LOGGER.info("Writing results in block compressed format to '{}'", mainOutputFile.getAbsolutePath());
            return new ResultsWriter(new BlockCompressedOutputStream(mainOutputFile));
        } else {
            LOGGER.info("Writing results to '{}'", mainOutputFile.getAbsolutePath());
            return new ResultsWriter(new FileOutputStream(mainOutputFile));

        }
    }

}
