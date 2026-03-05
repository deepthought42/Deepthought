package com.deepthought.models;

import static org.testng.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import com.deepthought.models.edges.FeatureWeight;

@Test(groups = "Regression")
public class FeatureTests {

    public void constructorsAndGetters_work() {
        Feature simple = new Feature("label");
        assertEquals(simple.getValue(), "label");
        assertNotNull(simple.getFeatureWeights());
        assertTrue(simple.getFeatureWeights().isEmpty());

        List<FeatureWeight> weights = new ArrayList<>();
        weights.add(new FeatureWeight());
        Feature withWeights = new Feature("weighted", weights);
        assertEquals(withWeights.getValue(), "weighted");
        assertEquals(withWeights.getFeatureWeights().size(), 1);
    }

    public void equalsAndToString_useFeatureValue() {
        Feature a = new Feature("same");
        Feature b = new Feature("same");
        Feature c = new Feature("different");

        assertTrue(a.equals(b));
        assertFalse(a.equals(c));
        assertEquals(a.toString(), "same");
    }
}
