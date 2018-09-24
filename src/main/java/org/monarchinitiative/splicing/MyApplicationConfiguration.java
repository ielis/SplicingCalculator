package org.monarchinitiative.splicing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Beans for the app's function.
 *
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 */
@Configuration
public class MyApplicationConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(MyApplicationConfiguration.class);

    private final Environment env;


    public MyApplicationConfiguration(Environment env) {
        this.env = env;
        LOGGER.warn(env.toString());
    }


    @Bean
    public String helloWorldMessage() {
        return "Hello world!";
    }


}
