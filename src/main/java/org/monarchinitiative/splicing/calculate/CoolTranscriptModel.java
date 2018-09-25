package org.monarchinitiative.splicing.calculate;

import com.google.common.collect.ComparisonChain;
import de.charite.compbio.jannovar.reference.Strand;
import de.charite.compbio.jannovar.reference.TranscriptModel;

import java.util.Comparator;
import java.util.List;

/**
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 * @version 0.0.1
 * @since 0.0
 */
public class CoolTranscriptModel {

    private final TranscriptModel transcriptModel;

    private final List<Double> donors, acceptors;


    public static Comparator<CoolTranscriptModel> byComparingPositionsOnFwdStrand() {
        return (l, r) -> ComparisonChain.start()
                .compare(l.getTranscriptModel().getTXRegion().getChr(), r.getTranscriptModel().getTXRegion().getChr())
                .compare(l.getTranscriptModel().getTXRegion().getGenomeBeginPos().withStrand(Strand.FWD).getPos(), r.getTranscriptModel().getTXRegion().getGenomeBeginPos().withStrand(Strand.FWD).getPos())
                .compare(l.getTranscriptModel().getTXRegion().getGenomeEndPos().withStrand(Strand.FWD).getPos(), r.getTranscriptModel().getTXRegion().getGenomeEndPos().withStrand(Strand.FWD).getPos())
                .compare(l.getTranscriptModel().getAccession(), r.getTranscriptModel().getAccession())
                .result();
    }


    public CoolTranscriptModel(TranscriptModel transcriptModel, List<Double> donors, List<Double> acceptors) {
        this.transcriptModel = transcriptModel;
        this.donors = donors;
        this.acceptors = acceptors;
    }


    public TranscriptModel getTranscriptModel() {
        return transcriptModel;
    }


    public List<Double> getDonors() {
        return donors;
    }


    public List<Double> getAcceptors() {
        return acceptors;
    }

}
