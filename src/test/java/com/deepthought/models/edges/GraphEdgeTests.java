package com.deepthought.models.edges;

import static org.testng.Assert.*;

import java.util.Arrays;

import org.testng.annotations.Test;

import com.deepthought.models.Feature;
import com.deepthought.models.MemoryRecord;

@Test(groups = "Regression")
public class GraphEdgeTests {

    public void featureWeight_settersAndGetters_work() {
        FeatureWeight edge = new FeatureWeight();
        Feature from = new Feature("from");
        Feature to = new Feature("to");

        edge.setFeature(from);
        edge.setEndFeature(to);
        edge.setWeight(0.65);

        assertEquals(edge.getFeature(), from);
        assertEquals(edge.getEndFeature(), to);
        assertEquals(edge.getWeight(), 0.65, 0.0001);
    }

    public void prediction_constructorAndGetters_work() {
        MemoryRecord memory = new MemoryRecord();
        Feature result = new Feature("result");
        Prediction prediction = new Prediction(memory, result, 0.9);

        assertEquals(prediction.getFeature(), result);
        assertEquals(prediction.getWeight(), 0.9, 0.0001);
        prediction.setWeight(0.2);
        assertEquals(prediction.getWeight(), 0.2, 0.0001);
    }

    public void featurePolicy_settersAndGetters_work() {
        FeaturePolicy policy = new FeaturePolicy();
        MemoryRecord memory = new MemoryRecord();

        policy.setMemoryRecord(memory);
        policy.setFeature(new Feature("feature"));
        policy.setPolicyFeatures(Arrays.asList("f1", "f2"));
        policy.setPolicyWeights(Arrays.asList(0.1, 0.9));
        policy.setReward(2.0);

        assertEquals(policy.getMemoryRecord(), memory);
        assertEquals(policy.getPolicyFeatures().size(), 2);
        assertEquals(policy.getPolicyWeights().get(1), Double.valueOf(0.9));
        assertEquals(policy.getReward(), 2.0, 0.0001);
    }
}
