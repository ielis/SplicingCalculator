package org.monarchinitiative.splicing.calculate;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.Strand;

/**
 * POJO for grouping together a nucleotide sequence and Jannovar-based coordinate system.
 *
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 */
public class SequenceInterval {

    private final GenomeInterval interval;

    private final String sequence;


    /**
     * Convert nucleotide sequence to reverse complement.
     *
     * @param sequence of nucleotides, only {a,c,g,t,n,A,C,G,T,N} permitted.
     * @return reverse complement of given <code>sequence</code>
     */
    private static String reverseComplement(String sequence) {
        char[] oldSeq = sequence.toCharArray();
        char[] newSeq = new char[oldSeq.length];
        int idx = oldSeq.length - 1;
        for (int i = 0; i < oldSeq.length; i++) {
            if (oldSeq[i] == 'A') {
                newSeq[idx - i] = 'T';
            } else if (oldSeq[i] == 'a') {
                newSeq[idx - i] = 't';
            } else if (oldSeq[i] == 'T') {
                newSeq[idx - i] = 'A';
            } else if (oldSeq[i] == 't') {
                newSeq[idx - i] = 'a';
            } else if (oldSeq[i] == 'C') {
                newSeq[idx - i] = 'G';
            } else if (oldSeq[i] == 'c') {
                newSeq[idx - i] = 'g';
            } else if (oldSeq[i] == 'G') {
                newSeq[idx - i] = 'C';
            } else if (oldSeq[i] == 'g') {
                newSeq[idx - i] = 'c';
            } else if (oldSeq[i] == 'N') {
                newSeq[idx - i] = 'N';
            } else if (oldSeq[i] == 'n') {
                newSeq[idx - i] = 'n';
            } else throw new IllegalArgumentException(String.format("Illegal nucleotide %s in sequence %s",
                    oldSeq[i], sequence));
        }
        return new String(newSeq);
    }


    /**
     * By instantiating this class you claim that this particular <code>sequence</code> is located in given genome
     * <code>interval</code>.
     *
     * @param sequence {@link String} with nucleotide sequence
     * @param interval {@link GenomeInterval} coordinates of the <code>sequence</code>
     */
    public SequenceInterval(String sequence, GenomeInterval interval) {
        this.interval = interval;
        this.sequence = sequence;
        if (interval.length() != sequence.length())
            throw new IllegalArgumentException(String.format("Unequal lengths of the interval: '%d' and the sequence: '%d'", interval.length(), sequence.length()));
    }


    /**
     * Get nucleotide sequence for given <code>query</code>.
     *
     * @param query {@link GenomeInterval} for which the sequence should be fetched
     * @return nucleotide sequence with adjusted to the <code>query</code>'s {@link Strand} or <code>null</code> if
     * <code>query</code> does not overlap with this {@link SequenceInterval}
     */
    public String sequenceFor(GenomeInterval query) {
        if (!interval.contains(query))
            return null;

        GenomeInterval onStrand = query.withStrand(interval.getStrand());
        int beg = onStrand.getBeginPos() - interval.getBeginPos();
        int end = sequence.length() - interval.getEndPos() + onStrand.getEndPos();

        return query.getStrand().equals(interval.getStrand()) ? sequence.substring(beg, end)
                : reverseComplement(sequence.substring(beg, end));
    }


//    public SequenceInterval withStrand(Strand strand) {
//        if (interval.getStrand().equals(strand))
//            return new SequenceInterval(sequence, interval); // TODO - return just this?
//        else
//            return new SequenceInterval(reverseComplement(sequence), interval.withStrand(strand));
//    }


    @Override
    public int hashCode() {
        int result = interval != null ? interval.hashCode() : 0;
        result = 31 * result + (sequence != null ? sequence.hashCode() : 0);
        return result;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SequenceInterval that = (SequenceInterval) o;

        if (interval != null ? !interval.equals(that.interval) : that.interval != null) return false;
        return sequence != null ? sequence.equals(that.sequence) : that.sequence == null;
    }


    @Override
    public String toString() {
        return "SequenceInterval{" +
                "interval=" + interval +
                ", sequence='" + sequence + '\'' +
                '}';
    }
}
