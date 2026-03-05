package Qanairy.deepthought;

import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.testng.annotations.Test;

import com.deepthought.models.Feature;
import com.qanairy.brain.FeatureVector;

@Test(groups = "Regression")
public class FeatureVectorTests {

	@Test
	public void loadMarksExistingFeaturesAsOneAndMissingAsZero() {
		List<Feature> input = Arrays.asList(new Feature("form"), new Feature("button"), new Feature("link"));
		List<Feature> output = Arrays.asList(new Feature("button"), new Feature("image"));

		HashMap<String, Integer> record = FeatureVector.load(input, output);

		assertEquals(record.get("form").intValue(), 0);
		assertEquals(record.get("button").intValue(), 1);
		assertEquals(record.get("link").intValue(), 0);
	}

	@Test
	public void loadReturnsEmptyMapForEmptyInput() {
		HashMap<String, Integer> record = FeatureVector.load(Arrays.asList(), Arrays.asList(new Feature("x")));
		assertEquals(record.size(), 0);
	}
}
