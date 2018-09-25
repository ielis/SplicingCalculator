package org.monarchinitiative.splicing.calculate;

import de.charite.compbio.jannovar.data.ReferenceDictionary;
import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.HG19RefDictBuilder;
import de.charite.compbio.jannovar.reference.Strand;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;

public class SequenceIntervalTest {


    private static final ReferenceDictionary RD = HG19RefDictBuilder.build();

    @Test
    public void sequenceFor() {
        SequenceInterval interval = new SequenceInterval("ACGTTCGATC", new GenomeInterval(RD, Strand.FWD, 3, 100, 110));

        assertThat(interval.sequenceFor(new GenomeInterval(RD, Strand.FWD, 3, 104, 109)), is("TCGAT"));
        assertThat(interval.sequenceFor(new GenomeInterval(RD, Strand.FWD, 3, 100, 110)), is("ACGTTCGATC"));

        // we expect that the sequence will be flipped to reverse complement here
        assertThat(interval.sequenceFor(new GenomeInterval(RD, Strand.FWD, 3, 105, 110).withStrand(Strand.REV)), is("GATCG"));
        assertThat(interval.sequenceFor(new GenomeInterval(RD, Strand.FWD, 3, 100, 110).withStrand(Strand.REV)), is("GATCGAACGT"));

        // partial overlap or no overlap at all
        assertThat(interval.sequenceFor(new GenomeInterval(RD, Strand.FWD, 3, 105, 111)), is(nullValue()));
        assertThat(interval.sequenceFor(new GenomeInterval(RD, Strand.FWD, 3, 99, 105)), is(nullValue()));
        assertThat(interval.sequenceFor(new GenomeInterval(RD, Strand.FWD, 3, 90, 100)), is(nullValue()));
        assertThat(interval.sequenceFor(new GenomeInterval(RD, Strand.FWD, 3, 110, 120)), is(nullValue()));

    }

}