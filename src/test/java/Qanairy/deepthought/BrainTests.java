package Qanairy.deepthought;

import static org.testng.Assert.*;

import java.util.Arrays;
import java.util.HashMap;

import org.testng.annotations.Test;

import com.deepthought.models.Feature;
import com.qanairy.brain.ActionFactory;
import com.qanairy.brain.FeatureVector;

@Test(groups = "Regression")
public class BrainTests {

	public void actionFactory_returnsConfiguredActions() {
		String[] actions = ActionFactory.getActions();
		assertEquals(actions.length, 5);
		assertEquals(actions[0], "buy");
		assertEquals(actions[4], "short");
	}

	public void featureVectorLoad_marksMatchingFeatures() {
		HashMap<String, Integer> vector = FeatureVector.load(
				Arrays.asList(new Feature("alpha"), new Feature("beta"), new Feature("gamma")),
				Arrays.asList(new Feature("beta"), new Feature("delta")));

		assertEquals(vector.size(), 3);
		assertEquals(vector.get("alpha"), Integer.valueOf(0));
		assertEquals(vector.get("beta"), Integer.valueOf(1));
		assertEquals(vector.get("gamma"), Integer.valueOf(0));
	}
}
