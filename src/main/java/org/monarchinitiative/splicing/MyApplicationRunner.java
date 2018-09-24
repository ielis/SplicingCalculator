package org.monarchinitiative.splicing;

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
public class MyApplicationRunner implements ApplicationRunner {

    private final static Logger LOGGER = LoggerFactory.getLogger(MyApplicationRunner.class);

    private final String helloWorld;


    public MyApplicationRunner(String helloWorldMessage) {
        this.helloWorld = helloWorldMessage;
    }


    public void run(ApplicationArguments args) throws Exception {
        List<String> nonOptionArgs = args.getNonOptionArgs();
        if (nonOptionArgs.isEmpty() || nonOptionArgs.contains("help"))
            LOGGER.info(makeHelpMessage());
        else if (nonOptionArgs.contains("example")) {
            runExample();
        }
    }


    private void runExample() {
        /*
         *                                 WRITE THE LOGIC OF THE APP HERE
         */
        LOGGER.info(helloWorld);
    }


    /**
     * @return String with help message for command line
     */
    private String makeHelpMessage() {
        return "\n\nUSAGE:\n\n" +
                "" +
                "Available tasks - {example, help}\n\n" +
                "example - run example task\n" +
                "help    - display this message\n";
    }
}
