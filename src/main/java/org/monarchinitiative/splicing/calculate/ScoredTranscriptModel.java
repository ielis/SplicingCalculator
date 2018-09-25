package org.monarchinitiative.splicing.calculate;

import de.charite.compbio.jannovar.reference.TranscriptModel;

import java.util.List;

/**
 * This class is a POJO for grouping {@link TranscriptModel} with splice scores of exonic donor/acceptor sites.
 *
 * @author <a href="mailto:daniel.danis@jax.org">Daniel Danis</a>
 */
public class ScoredTranscriptModel {

    private final TranscriptModel transcriptModel;

    /**
     * The scores for each exon are stored in these lists. If the exon is the first exon of the transcript, the accpetor
     * score should be {@link Double#NaN}. If the exon is the last of the transcript, the donor score should be
     * {@link Double#NaN}.
     */
    private final List<Double> donors, acceptors;


    public ScoredTranscriptModel(TranscriptModel transcriptModel, List<Double> donors, List<Double> acceptors) {
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


    @Override
    public int hashCode() {
        int result = transcriptModel != null ? transcriptModel.hashCode() : 0;
        result = 31 * result + (donors != null ? donors.hashCode() : 0);
        result = 31 * result + (acceptors != null ? acceptors.hashCode() : 0);
        return result;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScoredTranscriptModel that = (ScoredTranscriptModel) o;

        if (transcriptModel != null ? !transcriptModel.equals(that.transcriptModel) : that.transcriptModel != null)
            return false;
        if (donors != null ? !donors.equals(that.donors) : that.donors != null) return false;
        return acceptors != null ? acceptors.equals(that.acceptors) : that.acceptors == null;
    }


    @Override
    public String toString() {
        return "ScoredTranscriptModel{" +
                "transcriptModel=" + transcriptModel +
                ", donors=" + donors +
                ", acceptors=" + acceptors +
                '}';
    }
}
