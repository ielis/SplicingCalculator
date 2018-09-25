package org.monarchinitiative.splicing.calculate;

import de.charite.compbio.jannovar.reference.TranscriptModel;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.quality.Strictness;
import org.monarchinitiative.splicing.TestingData;
import org.monarchinitiative.splicing.io.GenomeSequenceAccessor;
import org.monarchinitiative.splicing.io.PositionalWeightMatrixParser;

import java.io.InputStream;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;


public class TranscriptScorerTest {

    private static SplicingInformationContentAnnotator IC_ANNOTATOR;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule().strictness(Strictness.WARN);

    @Mock
    private GenomeSequenceAccessor sequenceAccessor;

    private TranscriptScorer instance;


    @BeforeClass
    public static void setUpBefore() throws Exception {
        try (InputStream is = TranscriptScorerTest.class.getResourceAsStream("spliceSites.yaml")) {
            PositionalWeightMatrixParser parser = new PositionalWeightMatrixParser(is);
            IC_ANNOTATOR = new SplicingInformationContentAnnotator(parser.getDonorMatrix(), parser.getAcceptorMatrix());
        }
    }


    @Before
    public void setUp() throws Exception {
        instance = new TranscriptScorer(IC_ANNOTATOR, sequenceAccessor);
    }


    @Test
    public void scoreTranscriptModelSingleExon() {
        TranscriptModel model = TestingData.getSingleExonTranscriptModel();
        Mockito.when(sequenceAccessor.fetchSequence("chr6", 26017259, 26018040))
                .thenReturn(TestingData.getHIST1H1ASeq());
        ScoredTranscriptModel result = instance.scoreTranscriptModel().apply(model);
        assertThat(result.getAcceptors(), hasItem(Double.NaN));
        assertThat(result.getAcceptors(), hasSize(1));
        assertThat(result.getDonors(), hasItem(Double.NaN));
        assertThat(result.getDonors(), hasSize(1));
    }


    @Test
    public void scoreTranscriptModelTwoExons() {
        TranscriptModel model = TestingData.getTwoExonTranscriptModel();
        Mockito.when(sequenceAccessor.fetchSequence("chr13", 20761601, 20767114))
                .thenReturn(TestingData.GJB2Seq());
        ScoredTranscriptModel result = instance.scoreTranscriptModel().apply(model);
        assertThat(result.getDonors(), hasItems(Double.NaN, 10.33267367738815));
        assertThat(result.getDonors(), hasSize(2));
        assertThat(result.getAcceptors(), hasItems(Double.NaN, 10.293715220799694));
        assertThat(result.getAcceptors(), hasSize(2));
    }


    @Test
    public void scoreTranscriptModelThreeExons() {
        TranscriptModel model = TestingData.getThreeExonTranscriptModel();
        Mockito.when(sequenceAccessor.fetchSequence("chr7", 39606002, 39612480))
                .thenReturn(TestingData.getYAE1Seq());
        ScoredTranscriptModel result = instance.scoreTranscriptModel().apply(model);
        assertThat(result.getDonors(), hasItems(Double.NaN, 4.898803136983307, 8.566571105600822));
        assertThat(result.getDonors(), hasSize(3));
        assertThat(result.getAcceptors(), hasItems(Double.NaN, 10.44145682272866, 8.727129021978236));
        assertThat(result.getAcceptors(), hasSize(3));
    }

}