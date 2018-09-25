package org.monarchinitiative.splicing.io;

import org.junit.Test;
import org.monarchinitiative.splicing.TestingData;

import java.io.ByteArrayOutputStream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class ResultsWriterTest {


    @Test
    public void testWriteSingleExonTranscript() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ResultsWriter instance = new ResultsWriter(os);
        instance.write(TestingData.singleExonScoredTranscriptModel());

        assertThat(os.toString(), is("#CHR\tBEGIN\tEND\tBEGIN_T\tEND_T\tSTRAND\tSYMBOL\tACCESSION_ID\tDONOR\tACCEPTOR\n" +
                "6\t26018039\t26017258\t145097027\t145097808\t-\tHIST1H1A\tNM_005325.3\tNaN\tNaN\n"));
    }


    @Test
    public void testWriteTwoExonTranscript() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ResultsWriter instance = new ResultsWriter(os);
        instance.write(TestingData.twoExonScoredTranscriptModel());

        assertThat(os.toString(), is("#CHR\tBEGIN\tEND\tBEGIN_T\tEND_T\tSTRAND\tSYMBOL\tACCESSION_ID\tDONOR\tACCEPTOR\n" +
                "13\t20767113\t20766920\t94402764\t94402957\t-\tGJB2\tNM_004004.5\t10.33267367738815\tNaN\n" +
                "13\t20763741\t20761600\t94406136\t94408277\t-\tGJB2\tNM_004004.5\tNaN\t10.293715220799694\n"));
    }


    @Test
    public void testWriteThreeExonTranscript() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ResultsWriter instance = new ResultsWriter(os);
        instance.write(TestingData.threeExonScoredTranscriptModel());

        assertThat(os.toString(), is("#CHR\tBEGIN\tEND\tBEGIN_T\tEND_T\tSTRAND\tSYMBOL\tACCESSION_ID\tDONOR\tACCEPTOR\n" +
                "7\t39606002\t39606146\t39606002\t39606146\t+\tYAE1\tNM_020192.3\t4.898803136983307\tNaN\n" +
                "7\t39610104\t39610226\t39610104\t39610226\t+\tYAE1\tNM_020192.3\t8.566571105600822\t10.44145682272866\n" +
                "7\t39611875\t39612480\t39611875\t39612480\t+\tYAE1\tNM_020192.3\tNaN\t8.727129021978236\n"));
    }
}