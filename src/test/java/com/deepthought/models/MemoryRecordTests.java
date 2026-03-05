package com.deepthought.models;

import static org.testng.Assert.*;

import java.util.Arrays;
import java.util.Date;

import org.testng.annotations.Test;

import com.deepthought.models.edges.Prediction;

@Test(groups = "Regression")
public class MemoryRecordTests {

    public void defaultConstructor_initializesCollectionsAndDate() {
        MemoryRecord memoryRecord = new MemoryRecord();
        assertNotNull(memoryRecord.getDate());
        assertNotNull(memoryRecord.getPredictions());
        assertTrue(memoryRecord.getPredictions().isEmpty());
    }

    public void settersAndGetters_roundTripValues() {
        MemoryRecord memoryRecord = new MemoryRecord();
        Date date = new Date(12345L);
        Feature desired = new Feature("desired");
        Feature predicted = new Feature("predicted");

        memoryRecord.setDate(date);
        memoryRecord.setDesiredFeature(desired);
        memoryRecord.setPredictedFeature(predicted);
        memoryRecord.setInputFeatureValues(Arrays.asList("a", "b"));
        memoryRecord.setOutputFeatureKeys(new String[] {"x", "y"});
        memoryRecord.setPolicyMatrix(new double[][] {{0.1, 0.2}, {0.3, 0.4}});
        memoryRecord.setPredictions(Arrays.asList(new Prediction(memoryRecord, predicted, 0.8)));

        assertEquals(memoryRecord.getDate(), date);
        assertEquals(memoryRecord.getDesiredFeature(), desired);
        assertEquals(memoryRecord.getPredictedFeature(), predicted);
        assertEquals(memoryRecord.getInputFeatureValues().size(), 2);
        assertEquals(memoryRecord.getOutputFeatureKeys().length, 2);
        assertEquals(memoryRecord.getPolicyMatrix()[1][1], 0.4, 0.0001);
        assertEquals(memoryRecord.getPredictions().size(), 1);
    }
}
