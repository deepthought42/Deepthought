package com.deepthought.models;

import static org.testng.Assert.*;

import java.util.Arrays;
import java.util.Date;

import org.testng.annotations.Test;

import com.deepthought.models.edges.TokenWeight;
import com.deepthought.models.edges.Prediction;

@Test(groups = "Regression")
public class MemoryRecordTokenTests {

    @Test
    public void memoryRecord_storesPolicyAndMetadata() {
        MemoryRecord record = new MemoryRecord();

        Date now = new Date();
        record.setDate(now);
        record.setInputTokenValues(Arrays.asList("alpha", "beta"));
        record.setOutputTokenKeys(new String[] { "result-a", "result-b" });

        double[][] policy = new double[][] { { 0.1, 0.9 }, { 0.2, 0.8 } };
        record.setPolicyMatrix(policy);

        assertEquals(record.getDate(), now);
        assertEquals(record.getInputTokenValues().size(), 2);
        assertEquals(record.getOutputTokenKeys()[1], "result-b");
        assertEquals(record.getPolicyMatrix()[0][1], 0.9, 0.0001);
    }

    @Test
    public void memoryRecord_linksPredictedAndDesiredTokens() {
        Token desired = new Token("desired");
        Token predicted = new Token("predicted");
        MemoryRecord record = new MemoryRecord();

        record.setDesiredToken(desired);
        record.setPredictedToken(predicted);

        Prediction edge = new Prediction(record, predicted, 0.7);
        record.setPredictions(Arrays.asList(edge));

        assertEquals(record.getDesiredToken(), desired);
        assertEquals(record.getPredictedToken(), predicted);
        assertEquals(record.getPredictions().size(), 1);
        assertEquals(record.getPredictions().get(0).getWeight(), 0.7, 0.0001);
    }

    @Test
    public void token_and_tokenWeight_exposeValuesAndEquality() {
        Token source = new Token("source");
        Token sameValue = new Token("source");
        Token target = new Token("target");

        TokenWeight weight = new TokenWeight();
        weight.setToken(source);
        weight.setEndToken(target);
        weight.setWeight(0.42);

        assertEquals(source.toString(), "source");
        assertTrue(source.equals(sameValue));
        assertFalse(source.equals(target));

        assertEquals(weight.getToken(), source);
        assertEquals(weight.getEndToken(), target);
        assertEquals(weight.getWeight(), 0.42, 0.0001);
    }
}
