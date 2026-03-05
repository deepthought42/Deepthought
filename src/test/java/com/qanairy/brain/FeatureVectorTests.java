package com.qanairy.brain;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import com.deepthought.models.Feature;
import com.deepthought.models.Vocabulary;
import com.deepthought.models.repository.FeatureRepository;

public class FeatureVectorTests {

    @Test
    public void testLoadMarksMatchingFeatures() {
        List<Feature> input = Arrays.asList(new Feature("alpha"), new Feature("beta"), new Feature("gamma"));
        List<Feature> output = Arrays.asList(new Feature("beta"), new Feature("delta"));

        HashMap<String, Integer> result = FeatureVector.load(input, output);

        assertEquals(Integer.valueOf(0), result.get("alpha"));
        assertEquals(Integer.valueOf(1), result.get("beta"));
        assertEquals(Integer.valueOf(0), result.get("gamma"));
    }

    @Test
    public void testLoadPolicyUsesRepositoryAndBuildsExpectedShape() throws Exception {
        FeatureRepository repository = mock(FeatureRepository.class);
        Field field = FeatureVector.class.getDeclaredField("obj_def_repo");
        field.setAccessible(true);
        field.set(null, repository);

        List<Feature> input = Arrays.asList(new Feature("alpha"), new Feature("beta"));
        List<Feature> output = Arrays.asList(new Feature("x"), new Feature("y"), new Feature("z"));

        double[][] policy = FeatureVector.loadPolicy(input, output, new Vocabulary("vocab"));

        assertEquals(2, policy.length);
        assertEquals(3, policy[0].length);
        verify(repository, times(2)).save(any(Feature.class));
    }
}
