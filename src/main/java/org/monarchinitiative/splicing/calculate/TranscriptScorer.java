package org.monarchinitiative.splicing.calculate;

import de.charite.compbio.jannovar.reference.GenomeInterval;
import de.charite.compbio.jannovar.reference.Strand;
import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.monarchinitiative.splicing.io.GenomeSequenceAccessor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 */
public class TranscriptScorer {

    /* THESE VALUES NEED TO BE SYNCHRONIZED WITH LENGTHS OF SPLICE DONOR AND ACCEPTOR SITES DEFINED IN
    org/monarchinitiative/splicing/spliceSites.yaml FILE */

    /** How many nucleotides from the exon's 3' part are part of the splice donor site */
    public static final int SPLICE_DONOR_SITE_EXONIC_NTS = 3;

    public static final int SPLICE_DONOR_SITE_LENGTH = 9;

    /** How many nucleotides from the intron's 3' part are part of the splice acceptor site */
    public static final int SPLICE_ACCEPTOR_SITE_INTRONIC_NTS = 25;

    public static final int SPLICE_ACCEPTOR_SITE_LENGTH = 27;

    private final SplicingInformationContentAnnotator informationContentAnnotator;

    private final GenomeSequenceAccessor sequenceAccessor;


    public TranscriptScorer(SplicingInformationContentAnnotator informationContentAnnotator, GenomeSequenceAccessor sequenceAccessor) {
        this.informationContentAnnotator = informationContentAnnotator;
        this.sequenceAccessor = sequenceAccessor;
    }


    Function<TranscriptModel, CoolTranscriptModel> scoreTranscriptModel() {
        return tm -> {
            // transcript interval
            GenomeInterval ti = tm.getTXRegion().withStrand(Strand.FWD);
            // nucleotide sequence
            String chr = ti.getRefDict().getContigIDToName().get(ti.getChr());
            chr = (chr.startsWith("chr")) ? chr : "chr" + chr;
            SequenceInterval sequenceInterval = new SequenceInterval(sequenceAccessor.fetchSequence(chr, ti.getBeginPos(), ti.getEndPos()), ti);

            List<Double> donors = new ArrayList<>(tm.getExonRegions().size());
            List<Double> acceptors = new ArrayList<>(tm.getExonRegions().size());

            if (tm.getExonRegions().size() < 2) { // single-exon gene
                donors.add(Double.NaN);
                acceptors.add(Double.NaN);

            } else { // multi (min 2) exon gene
                GenomeInterval donor, acceptor;
                String donorSeq, acceptorSeq;
                // process the first exon
                donor = new GenomeInterval(tm.getExonRegions().get(0).getGenomeEndPos().shifted(-SPLICE_DONOR_SITE_EXONIC_NTS), SPLICE_DONOR_SITE_LENGTH);
                donorSeq = sequenceInterval.sequenceFor(donor);
                donors.add(informationContentAnnotator.getSpliceDonorScore(donorSeq));
                acceptors.add(Double.NaN); // the first exon does not have splice acceptor site

                for (int i = 1; i < tm.getExonRegions().size() - 1; i++) { // process internal exons
                    // donor site
                    donor = new GenomeInterval(tm.getExonRegions().get(i).getGenomeEndPos().shifted(-SPLICE_DONOR_SITE_EXONIC_NTS), SPLICE_DONOR_SITE_LENGTH);
                    donorSeq = sequenceInterval.sequenceFor(donor);
                    donors.add(informationContentAnnotator.getSpliceDonorScore(donorSeq));
                    // acceptor site
                    acceptor = new GenomeInterval(tm.getExonRegions().get(i).getGenomeBeginPos().shifted(-SPLICE_ACCEPTOR_SITE_INTRONIC_NTS), SPLICE_ACCEPTOR_SITE_LENGTH);
                    acceptorSeq = sequenceInterval.sequenceFor(acceptor);
                    acceptors.add(informationContentAnnotator.getSpliceAcceptorScore(acceptorSeq));
                }

                // process the last exon
                donors.add(Double.NaN); // the last exon does not have splice donor site
                acceptor = new GenomeInterval(tm.getExonRegions().get(tm.getExonRegions().size()-1).getGenomeBeginPos().shifted(-SPLICE_ACCEPTOR_SITE_INTRONIC_NTS), SPLICE_ACCEPTOR_SITE_LENGTH);
                acceptorSeq = sequenceInterval.sequenceFor(acceptor);
                acceptors.add(informationContentAnnotator.getSpliceAcceptorScore(acceptorSeq));
            }

            return new CoolTranscriptModel(tm, donors, acceptors);
        };
    }
}
