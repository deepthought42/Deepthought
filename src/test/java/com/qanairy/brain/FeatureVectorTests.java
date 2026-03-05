package com.qanairy.brain;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import com.deepthought.models.Feature;

public class FeatureVectorTests {

    @Test
    public void loadMarksInputFeaturesFoundInOutput() {
        List<Feature> input = Arrays.asList(new Feature("alpha"), new Feature("beta"), new Feature("gamma"));
        List<Feature> output = Arrays.asList(new Feature("beta"), new Feature("delta"));

        HashMap<String, Integer> record = FeatureVector.load(input, output);

        assertEquals(3, record.size());
        assertEquals(Integer.valueOf(0), record.get("alpha"));
        assertEquals(Integer.valueOf(1), record.get("beta"));
        assertEquals(Integer.valueOf(0), record.get("gamma"));
    }

    @Test
    public void loadReturnsEmptyRecordForEmptyInput() {
        HashMap<String, Integer> record = FeatureVector.load(Arrays.asList(), Arrays.asList(new Feature("beta")));

        assertEquals(0, record.size());
    }
}
