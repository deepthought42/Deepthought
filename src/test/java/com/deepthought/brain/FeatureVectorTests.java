package com.deepthought.brain;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.deepthought.data.models.Feature;
import com.deepthought.data.models.Vocabulary;
import com.deepthought.data.repository.FeatureRepository;

/**
 * Test suite for FeatureVector
 * 
 * Tests validate the FeatureVector class which provides:
 * 1. loadPolicy() - Loads policy matrix from input/output features and vocabulary
 * 2. load() - Creates vocabulary record mapping input features to output features
 * 
 * FeatureVector processes feature lists to create vocabulary mappings and policy matrices
 * for machine learning operations.
 * 
 * Note: Tests use singleThreaded=true to prevent interference with static field manipulation.
 */
@Test(singleThreaded = true)
public class FeatureVectorTests {

	@Mock
	private FeatureRepository featureRepository;

	/**
	 * Purpose: Set up test fixture before each test method
	 * 
	 * Steps:
	 * 1. Initialize Mockito mocks
	 * 2. Set up static repository field using reflection
	 */
	@BeforeMethod
	public void setUp() throws Exception {
		// Step 1: Initialize mocks
		MockitoAnnotations.initMocks(this);
		
		// Step 2: Set static repository field using reflection
		Field repoField = FeatureVector.class.getDeclaredField("obj_def_repo");
		repoField.setAccessible(true);
		repoField.set(null, featureRepository);
	}

	/**
	 * Purpose: Clean up test fixture after each test method
	 * 
	 * Steps:
	 * 1. Clear the static repository field to prevent test interference
	 */
	@AfterMethod
	public void tearDown() throws Exception {
		// Step 1: Clear static repository field to prevent test interference
		Field repoField = FeatureVector.class.getDeclaredField("obj_def_repo");
		repoField.setAccessible(true);
		repoField.set(null, null);
	}

	/**
	 * Purpose: Test load method with matching features
	 * 
	 * Steps:
	 * 1. Create input and output feature lists with some matching features
	 * 2. Call load method
	 * 3. Verify vocabulary record contains correct mappings (1 for matches, 0 for non-matches)
	 * 4. Verify all input features are present in the result
	 */
	@Test
	public void testLoadWithMatchingFeatures() {
		// Step 1: Setup - Create input and output feature lists
		List<Feature> inputFeatures = new ArrayList<>();
		inputFeatures.add(new Feature("button"));
		inputFeatures.add(new Feature("input"));
		inputFeatures.add(new Feature("submit"));
		
		List<Feature> outputFeatures = new ArrayList<>();
		outputFeatures.add(new Feature("button"));  // Match
		outputFeatures.add(new Feature("click"));   // No match
		outputFeatures.add(new Feature("submit"));  // Match
		
		// Step 2: Execute - Call load method
		HashMap<String, Integer> result = FeatureVector.load(inputFeatures, outputFeatures);
		
		// Step 3: Verify - Check mappings
		Assert.assertNotNull(result, "load should return a non-null HashMap");
		Assert.assertEquals(result.size(), inputFeatures.size(), 
			"Result should contain all input features");
		Assert.assertEquals(result.get("button"), Integer.valueOf(1), 
			"button should be marked as 1 (match found)");
		Assert.assertEquals(result.get("input"), Integer.valueOf(0), 
			"input should be marked as 0 (no match)");
		Assert.assertEquals(result.get("submit"), Integer.valueOf(1), 
			"submit should be marked as 1 (match found)");
	}

	/**
	 * Purpose: Test load method with no matching features
	 * 
	 * Steps:
	 * 1. Create input and output feature lists with no matching features
	 * 2. Call load method
	 * 3. Verify all input features are marked as 0 (no matches)
	 */
	@Test
	public void testLoadWithNoMatchingFeatures() {
		// Step 1: Setup - Create input and output feature lists with no matches
		List<Feature> inputFeatures = new ArrayList<>();
		inputFeatures.add(new Feature("button"));
		inputFeatures.add(new Feature("input"));
		
		List<Feature> outputFeatures = new ArrayList<>();
		outputFeatures.add(new Feature("click"));
		outputFeatures.add(new Feature("submit"));
		
		// Step 2: Execute - Call load method
		HashMap<String, Integer> result = FeatureVector.load(inputFeatures, outputFeatures);
		
		// Step 3: Verify - All should be marked as 0
		Assert.assertNotNull(result, "load should return a non-null HashMap");
		Assert.assertEquals(result.size(), inputFeatures.size(), 
			"Result should contain all input features");
		Assert.assertEquals(result.get("button"), Integer.valueOf(0), 
			"button should be marked as 0 (no match)");
		Assert.assertEquals(result.get("input"), Integer.valueOf(0), 
			"input should be marked as 0 (no match)");
	}

	/**
	 * Purpose: Test load method with all matching features
	 * 
	 * Steps:
	 * 1. Create input and output feature lists where all input features match
	 * 2. Call load method
	 * 3. Verify all input features are marked as 1 (all matches)
	 */
	@Test
	public void testLoadWithAllMatchingFeatures() {
		// Step 1: Setup - Create input and output feature lists with all matches
		List<Feature> inputFeatures = new ArrayList<>();
		inputFeatures.add(new Feature("button"));
		inputFeatures.add(new Feature("input"));
		
		List<Feature> outputFeatures = new ArrayList<>();
		outputFeatures.add(new Feature("button"));  // Match
		outputFeatures.add(new Feature("input"));   // Match
		outputFeatures.add(new Feature("submit"));  // Extra
		
		// Step 2: Execute - Call load method
		HashMap<String, Integer> result = FeatureVector.load(inputFeatures, outputFeatures);
		
		// Step 3: Verify - All should be marked as 1
		Assert.assertNotNull(result, "load should return a non-null HashMap");
		Assert.assertEquals(result.size(), inputFeatures.size(), 
			"Result should contain all input features");
		Assert.assertEquals(result.get("button"), Integer.valueOf(1), 
			"button should be marked as 1 (match found)");
		Assert.assertEquals(result.get("input"), Integer.valueOf(1), 
			"input should be marked as 1 (match found)");
	}

	/**
	 * Purpose: Test load method with empty input features list
	 * 
	 * Steps:
	 * 1. Create empty input features list and non-empty output features list
	 * 2. Call load method
	 * 3. Verify result is empty HashMap
	 */
	@Test
	public void testLoadWithEmptyInputFeatures() {
		// Step 1: Setup - Create empty input features list
		List<Feature> inputFeatures = new ArrayList<>();
		
		List<Feature> outputFeatures = new ArrayList<>();
		outputFeatures.add(new Feature("button"));
		outputFeatures.add(new Feature("input"));
		
		// Step 2: Execute - Call load method
		HashMap<String, Integer> result = FeatureVector.load(inputFeatures, outputFeatures);
		
		// Step 3: Verify - Result should be empty
		Assert.assertNotNull(result, "load should return a non-null HashMap");
		Assert.assertTrue(result.isEmpty(), 
			"Result should be empty when input features list is empty");
	}

	/**
	 * Purpose: Test load method with empty output features list
	 * 
	 * Steps:
	 * 1. Create non-empty input features list and empty output features list
	 * 2. Call load method
	 * 3. Verify all input features are marked as 0 (no matches possible)
	 */
	@Test
	public void testLoadWithEmptyOutputFeatures() {
		// Step 1: Setup - Create non-empty input features and empty output features
		List<Feature> inputFeatures = new ArrayList<>();
		inputFeatures.add(new Feature("button"));
		inputFeatures.add(new Feature("input"));
		
		List<Feature> outputFeatures = new ArrayList<>();
		
		// Step 2: Execute - Call load method
		HashMap<String, Integer> result = FeatureVector.load(inputFeatures, outputFeatures);
		
		// Step 3: Verify - All should be marked as 0
		Assert.assertNotNull(result, "load should return a non-null HashMap");
		Assert.assertEquals(result.size(), inputFeatures.size(), 
			"Result should contain all input features");
		Assert.assertEquals(result.get("button"), Integer.valueOf(0), 
			"button should be marked as 0 (no matches in empty output)");
		Assert.assertEquals(result.get("input"), Integer.valueOf(0), 
			"input should be marked as 0 (no matches in empty output)");
	}

	/**
	 * Purpose: Test load method with duplicate input features
	 * 
	 * Steps:
	 * 1. Create input features list with duplicates
	 * 2. Call load method
	 * 3. Verify each unique input feature appears once in result
	 * 4. Verify correct match status for each feature
	 */
	@Test
	public void testLoadWithDuplicateInputFeatures() {
		// Step 1: Setup - Create input features with duplicates
		List<Feature> inputFeatures = new ArrayList<>();
		inputFeatures.add(new Feature("button"));
		inputFeatures.add(new Feature("button"));  // Duplicate
		inputFeatures.add(new Feature("input"));
		
		List<Feature> outputFeatures = new ArrayList<>();
		outputFeatures.add(new Feature("button"));  // Match
		
		// Step 2: Execute - Call load method
		HashMap<String, Integer> result = FeatureVector.load(inputFeatures, outputFeatures);
		
		// Step 3: Verify - Each unique feature should appear once
		Assert.assertNotNull(result, "load should return a non-null HashMap");
		// Note: HashMap will overwrite duplicates, so we expect unique keys only
		Assert.assertTrue(result.size() <= inputFeatures.size(), 
			"Result size should be at most the input size (duplicates overwritten)");
		Assert.assertEquals(result.get("button"), Integer.valueOf(1), 
			"button should be marked as 1 (match found)");
		Assert.assertEquals(result.get("input"), Integer.valueOf(0), 
			"input should be marked as 0 (no match)");
	}

	/**
	 * Purpose: Test load method with features having same values but different instances
	 * 
	 * Steps:
	 * 1. Create input and output features with same values (different instances)
	 * 2. Call load method
	 * 3. Verify features are matched by value (equals method)
	 */
	@Test
	public void testLoadWithSameValueDifferentInstances() {
		// Step 1: Setup - Create features with same values but different instances
		List<Feature> inputFeatures = new ArrayList<>();
		Feature inputFeature1 = new Feature("button");
		inputFeatures.add(inputFeature1);
		
		List<Feature> outputFeatures = new ArrayList<>();
		Feature outputFeature1 = new Feature("button");  // Same value, different instance
		outputFeatures.add(outputFeature1);
		
		// Step 2: Execute - Call load method
		HashMap<String, Integer> result = FeatureVector.load(inputFeatures, outputFeatures);
		
		// Step 3: Verify - Features should match by value
		Assert.assertNotNull(result, "load should return a non-null HashMap");
		Assert.assertEquals(result.get("button"), Integer.valueOf(1), 
			"Features with same value should match even if different instances");
	}

	/**
	 * Purpose: Test loadPolicy method returns correct matrix dimensions
	 * 
	 * Steps:
	 * 1. Create input and output feature lists
	 * 2. Create a vocabulary
	 * 3. Call loadPolicy method
	 * 4. Verify matrix dimensions match input and output feature sizes
	 * 5. Verify repository save is called for each input feature
	 */
	@Test
	public void testLoadPolicyReturnsCorrectDimensions() {
		// Step 1: Setup - Create input and output feature lists
		List<Feature> inputFeatures = new ArrayList<>();
		inputFeatures.add(new Feature("button"));
		inputFeatures.add(new Feature("input"));
		
		List<Feature> outputFeatures = new ArrayList<>();
		outputFeatures.add(new Feature("click"));
		outputFeatures.add(new Feature("submit"));
		outputFeatures.add(new Feature("reset"));
		
		// Step 2: Setup - Create vocabulary
		Vocabulary vocab = new Vocabulary("test_vocab");
		vocab.appendToVocabulary(new Feature("click"));
		vocab.appendToVocabulary(new Feature("submit"));
		vocab.appendToVocabulary(new Feature("reset"));
		
		// Step 3: Execute - Call loadPolicy method
		double[][] result = FeatureVector.loadPolicy(inputFeatures, outputFeatures, vocab);
		
		// Step 4: Verify - Matrix dimensions should match
		Assert.assertNotNull(result, "loadPolicy should return a non-null matrix");
		Assert.assertEquals(result.length, inputFeatures.size(), 
			"Matrix rows should match input features size");
		Assert.assertEquals(result[0].length, outputFeatures.size(), 
			"Matrix columns should match output features size");
		
		// Step 5: Verify - Repository save should be called for each input feature
		Mockito.verify(featureRepository, Mockito.times(inputFeatures.size())).save(Mockito.any(Feature.class));
	}

	/**
	 * Purpose: Test loadPolicy method with empty input features
	 * 
	 * Steps:
	 * 1. Create empty input features list
	 * 2. Create output features and vocabulary
	 * 3. Call loadPolicy method
	 * 4. Verify matrix has 0 rows
	 */
	@Test
	public void testLoadPolicyWithEmptyInputFeatures() {
		// Step 1: Setup - Create empty input features
		List<Feature> inputFeatures = new ArrayList<>();
		
		List<Feature> outputFeatures = new ArrayList<>();
		outputFeatures.add(new Feature("click"));
		
		Vocabulary vocab = new Vocabulary("test_vocab");
		vocab.appendToVocabulary(new Feature("click"));
		
		// Step 2: Execute - Call loadPolicy method
		double[][] result = FeatureVector.loadPolicy(inputFeatures, outputFeatures, vocab);
		
		// Step 3: Verify - Matrix should have 0 rows
		Assert.assertNotNull(result, "loadPolicy should return a non-null matrix");
		Assert.assertEquals(result.length, 0, 
			"Matrix should have 0 rows when input features is empty");
		
		// Step 4: Verify - Repository should not be called
		Mockito.verify(featureRepository, Mockito.never()).save(Mockito.any(Feature.class));
	}

	/**
	 * Purpose: Test loadPolicy method with empty output features
	 * 
	 * Steps:
	 * 1. Create input features and empty output features
	 * 2. Create vocabulary
	 * 3. Call loadPolicy method
	 * 4. Verify matrix has correct rows but 0 columns
	 */
	@Test
	public void testLoadPolicyWithEmptyOutputFeatures() {
		// Step 1: Setup - Create input features and empty output features
		List<Feature> inputFeatures = new ArrayList<>();
		inputFeatures.add(new Feature("button"));
		inputFeatures.add(new Feature("input"));
		
		List<Feature> outputFeatures = new ArrayList<>();
		
		Vocabulary vocab = new Vocabulary("test_vocab");
		
		// Step 2: Execute - Call loadPolicy method
		double[][] result = FeatureVector.loadPolicy(inputFeatures, outputFeatures, vocab);
		
		// Step 3: Verify - Matrix should have correct rows but 0 columns
		Assert.assertNotNull(result, "loadPolicy should return a non-null matrix");
		Assert.assertEquals(result.length, inputFeatures.size(), 
			"Matrix should have rows matching input features size");
		// Verify that each row has 0 columns (Java allows creating arrays with 0 columns)
		if (result.length > 0) {
			Assert.assertEquals(result[0].length, 0, 
				"Matrix should have 0 columns when output features is empty");
		}
		
		// Step 4: Verify - Repository should be called for each input feature
		Mockito.verify(featureRepository, Mockito.times(inputFeatures.size())).save(Mockito.any(Feature.class));
	}

	/**
	 * Purpose: Test loadPolicy method saves all input features to repository
	 * 
	 * Steps:
	 * 1. Create multiple input features
	 * 2. Create output features and vocabulary
	 * 3. Call loadPolicy method
	 * 4. Verify repository save is called for each input feature
	 * 5. Verify correct features are saved
	 */
	@Test
	public void testLoadPolicySavesAllInputFeatures() {
		// Step 1: Setup - Create multiple input features
		Feature feature1 = new Feature("button");
		Feature feature2 = new Feature("input");
		Feature feature3 = new Feature("form");
		
		List<Feature> inputFeatures = new ArrayList<>();
		inputFeatures.add(feature1);
		inputFeatures.add(feature2);
		inputFeatures.add(feature3);
		
		List<Feature> outputFeatures = new ArrayList<>();
		outputFeatures.add(new Feature("click"));
		
		Vocabulary vocab = new Vocabulary("test_vocab");
		vocab.appendToVocabulary(new Feature("click"));
		
		// Step 2: Execute - Call loadPolicy method
		FeatureVector.loadPolicy(inputFeatures, outputFeatures, vocab);
		
		// Step 3 & 4: Verify - Repository save should be called for each input feature
		Mockito.verify(featureRepository, Mockito.times(3)).save(Mockito.any(Feature.class));
		Mockito.verify(featureRepository).save(feature1);
		Mockito.verify(featureRepository).save(feature2);
		Mockito.verify(featureRepository).save(feature3);
	}

	/**
	 * Purpose: Test loadPolicy method initializes matrix with zeros
	 * 
	 * Steps:
	 * 1. Create input and output features
	 * 2. Create vocabulary
	 * 3. Call loadPolicy method
	 * 4. Verify all matrix values are initialized to 0.0
	 */
	@Test
	public void testLoadPolicyInitializesMatrixWithZeros() {
		// Step 1: Setup - Create input and output features
		List<Feature> inputFeatures = new ArrayList<>();
		inputFeatures.add(new Feature("button"));
		inputFeatures.add(new Feature("input"));
		
		List<Feature> outputFeatures = new ArrayList<>();
		outputFeatures.add(new Feature("click"));
		outputFeatures.add(new Feature("submit"));
		
		Vocabulary vocab = new Vocabulary("test_vocab");
		vocab.appendToVocabulary(new Feature("click"));
		vocab.appendToVocabulary(new Feature("submit"));
		
		// Step 2: Execute - Call loadPolicy method
		double[][] result = FeatureVector.loadPolicy(inputFeatures, outputFeatures, vocab);
		
		// Step 3: Verify - All values should be 0.0 (default for double array)
		Assert.assertNotNull(result, "loadPolicy should return a non-null matrix");
		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < result[i].length; j++) {
				Assert.assertEquals(result[i][j], 0.0, 
					"Matrix should be initialized with zeros at [" + i + "][" + j + "]");
			}
		}
	}

	/**
	 * Purpose: Test load method with single feature
	 * 
	 * Steps:
	 * 1. Create single input and output feature
	 * 2. Call load method
	 * 3. Verify result contains single entry with correct value
	 */
	@Test
	public void testLoadWithSingleFeature() {
		// Step 1: Setup - Create single features
		List<Feature> inputFeatures = new ArrayList<>();
		inputFeatures.add(new Feature("button"));
		
		List<Feature> outputFeatures = new ArrayList<>();
		outputFeatures.add(new Feature("button"));  // Match
		
		// Step 2: Execute - Call load method
		HashMap<String, Integer> result = FeatureVector.load(inputFeatures, outputFeatures);
		
		// Step 3: Verify - Single entry should be present
		Assert.assertNotNull(result, "load should return a non-null HashMap");
		Assert.assertEquals(result.size(), 1, 
			"Result should contain exactly one entry");
		Assert.assertEquals(result.get("button"), Integer.valueOf(1), 
			"button should be marked as 1 (match found)");
	}

	/**
	 * Purpose: Test load method handles case sensitivity in feature values
	 * 
	 * Steps:
	 * 1. Create input and output features with different cases
	 * 2. Call load method
	 * 3. Verify features are matched by exact value (case-sensitive)
	 */
	@Test
	public void testLoadCaseSensitivity() {
		// Step 1: Setup - Create features with different cases
		List<Feature> inputFeatures = new ArrayList<>();
		inputFeatures.add(new Feature("button"));
		inputFeatures.add(new Feature("BUTTON"));
		
		List<Feature> outputFeatures = new ArrayList<>();
		outputFeatures.add(new Feature("button"));  // Lowercase match
		
		// Step 2: Execute - Call load method
		HashMap<String, Integer> result = FeatureVector.load(inputFeatures, outputFeatures);
		
		// Step 3: Verify - Case-sensitive matching
		Assert.assertNotNull(result, "load should return a non-null HashMap");
		Assert.assertEquals(result.get("button"), Integer.valueOf(1), 
			"Lowercase button should match");
		Assert.assertEquals(result.get("BUTTON"), Integer.valueOf(0), 
			"Uppercase BUTTON should not match lowercase button");
	}
}

