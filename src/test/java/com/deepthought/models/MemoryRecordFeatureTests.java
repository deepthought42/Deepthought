package com.deepthought.models;

import static org.testng.Assert.*;

import java.util.Arrays;
import java.util.Date;

import org.testng.annotations.Test;

import com.deepthought.models.edges.FeatureWeight;
import com.deepthought.models.edges.Prediction;

@Test(groups = "Regression")
public class MemoryRecordFeatureTests {

    @Test
    public void memoryRecord_storesPolicyAndMetadata() {
        MemoryRecord record = new MemoryRecord();

        Date now = new Date();
        record.setDate(now);
        record.setInputFeatureValues(Arrays.asList("alpha", "beta"));
        record.setOutputFeatureKeys(new String[] { "result-a", "result-b" });

        double[][] policy = new double[][] { { 0.1, 0.9 }, { 0.2, 0.8 } };
        record.setPolicyMatrix(policy);

        assertEquals(record.getDate(), now);
        assertEquals(record.getInputFeatureValues().size(), 2);
        assertEquals(record.getOutputFeatureKeys()[1], "result-b");
        assertEquals(record.getPolicyMatrix()[0][1], 0.9, 0.0001);
    }

    @Test
    public void memoryRecord_linksPredictedAndDesiredFeatures() {
        Feature desired = new Feature("desired");
        Feature predicted = new Feature("predicted");
        MemoryRecord record = new MemoryRecord();

        record.setDesiredFeature(desired);
        record.setPredictedFeature(predicted);

        Prediction edge = new Prediction(record, predicted, 0.7);
        record.setPredictions(Arrays.asList(edge));

        assertEquals(record.getDesiredFeature(), desired);
        assertEquals(record.getPredictedFeature(), predicted);
        assertEquals(record.getPredictions().size(), 1);
        assertEquals(record.getPredictions().get(0).getWeight(), 0.7, 0.0001);
    }

    @Test
    public void feature_and_featureWeight_exposeValuesAndEquality() {
        Feature source = new Feature("source");
        Feature sameValue = new Feature("source");
        Feature target = new Feature("target");

        FeatureWeight weight = new FeatureWeight();
        weight.setFeature(source);
        weight.setEndFeature(target);
        weight.setWeight(0.42);

        assertEquals(source.toString(), "source");
        assertTrue(source.equals(sameValue));
        assertFalse(source.equals(target));

        assertEquals(weight.getFeature(), source);
        assertEquals(weight.getEndFeature(), target);
        assertEquals(weight.getWeight(), 0.42, 0.0001);
    }
}
