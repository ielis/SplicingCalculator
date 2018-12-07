package org.monarchinitiative.splicing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * This class is the first that displays help message, if given {@link ApplicationArguments} are empty or contain a word
 * <code>help</code>.
 */
@Component
public class HelpApplicationRunner implements ApplicationRunner, Ordered {

    private static final Logger LOGGER = LoggerFactory.getLogger(HelpApplicationRunner.class);

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<String> nonOptionArgs = args.getNonOptionArgs();
        if (nonOptionArgs.isEmpty() || nonOptionArgs.contains("help")) {
            LOGGER.info(makeHelpMessage());
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
                "analyze_selected_exons - analyze exons present in the TSV file provided by Peter and Guy\n" +
                "help      - display this message\n";
    }

    /**
     * @return {@link Integer#MIN_VALUE} so that this application runner is run as first all the time
     */
    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }
}
