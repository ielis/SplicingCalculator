package org.monarchinitiative.splicing.calculate;

import de.charite.compbio.jannovar.data.JannovarData;
import org.monarchinitiative.splicing.io.ResultsWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.monarchinitiative.splicing.calculate.CoolTranscriptModel.byComparingPositionsOnFwdStrand;

/**
 * Runner for the app's logic.
 *
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 */
@Component
public class SplicingCalculatorApplicationRunner implements ApplicationRunner {

    private final static Logger LOGGER = LoggerFactory.getLogger(SplicingCalculatorApplicationRunner.class);

    private final JannovarData jannovarData;

    private final TranscriptScorer transcriptScorer;

    private final ResultsWriter resultsWriter;


    public SplicingCalculatorApplicationRunner(JannovarData jannovarData, TranscriptScorer transcriptScorer, ResultsWriter resultsWriter) {
        this.jannovarData = jannovarData;
        this.transcriptScorer = transcriptScorer;
        this.resultsWriter = resultsWriter;
    }


    public void run(ApplicationArguments args) throws Exception {
        List<String> nonOptionArgs = args.getNonOptionArgs();
        if (nonOptionArgs.isEmpty() || nonOptionArgs.contains("help"))
            LOGGER.info(makeHelpMessage());
        else if (nonOptionArgs.contains("calculate")) {
            calculate();
        }
    }


    private void calculate() {
        jannovarData.getTmByAccession().values().stream()
                // score each transcript
                .map(transcriptScorer.scoreTranscriptModel())
                // sort by position on FWD strand of the chromosome
                .sorted(byComparingPositionsOnFwdStrand())
                // write
                .forEachOrdered(resultsWriter::write);


        LOGGER.info("Starting calculate task");

        LOGGER.info("Finished calculate task");
    }


    /**
     * @return String with help message for command line
     */
    private String makeHelpMessage() {
        return "\n\nUSAGE:\n\n" +
                "" +
                "Available actions - {calculate, help}\n\n" +
                "calculate - run calculate task\n" +
                "help      - display this message\n";
    }
}
