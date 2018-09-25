package org.monarchinitiative.splicing;

import de.charite.compbio.jannovar.data.JannovarData;
import org.monarchinitiative.splicing.calculate.ScoredTranscriptModel;
import org.monarchinitiative.splicing.calculate.TranscriptScorer;
import org.monarchinitiative.splicing.io.ResultsWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Runner for the app's logic.
 *
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 */
@Component
public class SplicingCalculatorApplicationRunner implements ApplicationRunner {

    private final static Logger LOGGER = LoggerFactory.getLogger(SplicingCalculatorApplicationRunner.class);

    private int processed = 0;
    private int total;

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
        total = jannovarData.getTmByAccession().values().size();
        LOGGER.info("Starting splicing calculations for {} transcripts", total);
        jannovarData.getTmByAccession().values().stream()
                // score each transcript
                .map(transcriptScorer.scoreTranscriptModel())
                // report progress on console
                .peek(this::progress)
                // write the transcripts
                .forEach(resultsWriter::write);
        LOGGER.info("Done!");
    }


    private void progress(ScoredTranscriptModel s) {
        processed++;
        if (processed % 10000 == 0) {
            LOGGER.info(String.format("Processed %.2f%% of transcripts", ((double) processed * 100) / (double) total));
        }
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
