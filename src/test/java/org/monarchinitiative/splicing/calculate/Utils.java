package org.monarchinitiative.splicing.calculate;

import de.charite.compbio.jannovar.data.JannovarData;
import de.charite.compbio.jannovar.data.JannovarDataSerializer;
import de.charite.compbio.jannovar.data.SerializationException;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.junit.Test;

import java.util.Collection;

/**
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 * @version 0.0.1
 * @since 0.0
 */
@Deprecated
public class Utils {


    @Test
    public void blah() throws SerializationException {
        JannovarData JD = new JannovarDataSerializer("/home/ielis/jannovar/v0.26/hg19_refseq.ser").load();
//        JD.getTmByAccession().keySet().stream().filter(s -> s.startsWith("NM_020192")).forEachOrdered(System.err::println);
        TranscriptModel hist1h1a_c = JD.getTmByAccession().get("NM_020192.3");
        System.out.println(hist1h1a_c);
    }
}
