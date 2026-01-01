package com.deepthought.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.deepthought.brain.Brain;
import com.deepthought.data.edges.Prediction;
import com.deepthought.data.models.Feature;
import com.deepthought.data.models.MemoryRecord;
import com.deepthought.data.repository.FeatureRepository;
import com.deepthought.data.repository.MemoryRecordRepository;
import com.deepthought.data.repository.PredictionRepository;

/**
 * Test suite for ReinforcementLearningController
 * 
 * Tests validate the ReinforcementLearningController which provides:
 * 1. POST /predict - Makes predictions and returns MemoryRecord
 * 2. POST /learn - Applies learning to a feature for a given memory
 * 3. POST /train - Performs training iteration using label and object data
 */
public class ReinforcementLearningControllerTests {

	@Mock
	private FeatureRepository featureRepository;

	@Mock
	private MemoryRecordRepository memoryRecordRepository;

	@Mock
	private PredictionRepository predictionRepository;

	@Mock
	private Brain brain;

	private ReinforcementLearningController controller;

	/**
	 * Purpose: Set up test fixture before each test method
	 * 
	 * Steps:
	 * 1. Initialize Mockito mocks
	 * 2. Create controller instance and inject mocks using reflection
	 */
	@BeforeMethod
	public void setUp() {
		// Step 1: Initialize mocks
		MockitoAnnotations.initMocks(this);

		// Step 2: Create controller and inject mocks using reflection
		controller = new ReinforcementLearningController();
		try {
			java.lang.reflect.Field featureRepoField = ReinforcementLearningController.class.getDeclaredField("feature_repo");
			featureRepoField.setAccessible(true);
			featureRepoField.set(controller, featureRepository);

			java.lang.reflect.Field memoryRepoField = ReinforcementLearningController.class.getDeclaredField("memory_repo");
			memoryRepoField.setAccessible(true);
			memoryRepoField.set(controller, memoryRecordRepository);

			java.lang.reflect.Field predictionRepoField = ReinforcementLearningController.class.getDeclaredField("prediction_repo");
			predictionRepoField.setAccessible(true);
			predictionRepoField.set(controller, predictionRepository);

			java.lang.reflect.Field brainField = ReinforcementLearningController.class.getDeclaredField("brain");
			brainField.setAccessible(true);
			brainField.set(controller, brain);
		} catch (Exception e) {
			throw new RuntimeException("Failed to inject mocks", e);
		}
	}

	// ==================== POST /predict Endpoint Tests ====================

	/**
	 * Purpose: Test predict endpoint with valid JSON input and multiple output features
	 */
	@Test
	public void testPredictWithValidJsonAndMultipleOutputFeatures() throws Exception {
		// Setup
		String input = "{\"field1\":\"value1\",\"field2\":\"value2\"}";
		String[] outputLabels = {"label1", "label2"};

		List<Feature> inputFeatures = new ArrayList<>();
		inputFeatures.add(new Feature("field1"));
		inputFeatures.add(new Feature("value1"));
		inputFeatures.add(new Feature("field2"));
		inputFeatures.add(new Feature("value2"));

		List<Feature> outputFeatures = new ArrayList<>();
		outputFeatures.add(new Feature("label1"));
		outputFeatures.add(new Feature("label2"));

		double[][] policy = {{0.5, 0.3}, {0.4, 0.6}, {0.2, 0.8}, {0.1, 0.9}};
		double[] prediction = {0.3, 0.7};

		Mockito.when(featureRepository.findByValue("label1")).thenReturn(null);
		Mockito.when(featureRepository.findByValue("label2")).thenReturn(null);
		Mockito.when(brain.generateRawPolicy(Mockito.anyList(), Mockito.anyList())).thenReturn(policy);
		Mockito.when(brain.predict(policy)).thenReturn(prediction);

		MemoryRecord savedMemory = new MemoryRecord();
		try {
			java.lang.reflect.Field idField = MemoryRecord.class.getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(savedMemory, 1L);
		} catch (Exception e) {
			throw new RuntimeException("Failed to set memory ID", e);
		}
		Mockito.when(memoryRecordRepository.save(Mockito.any(MemoryRecord.class))).thenReturn(savedMemory);

		Prediction pred1 = new Prediction(savedMemory, outputFeatures.get(0), prediction[0]);
		Prediction pred2 = new Prediction(savedMemory, outputFeatures.get(1), prediction[1]);
		Mockito.when(predictionRepository.save(Mockito.any(Prediction.class)))
			.thenReturn(pred1)
			.thenReturn(pred2);

		// Execute
		MemoryRecord result = controller.predict(input, outputLabels);

		// Verify
		Assert.assertNotNull(result, "Result should not be null");
		Assert.assertEquals(result.getID(), Long.valueOf(1L), "Memory ID should be set");
		Assert.assertNotNull(result.getPolicyMatrix(), "Policy matrix should be set");
		Assert.assertNotNull(result.getInputFeatureValues(), "Input feature values should be set");
		Assert.assertNotNull(result.getOutputFeatureKeys(), "Output feature keys should be set");
		Assert.assertEquals(result.getOutputFeatureKeys().length, 2, "Should have 2 output features");
		Assert.assertNotNull(result.getPredictedFeature(), "Predicted feature should be set");
		Assert.assertEquals(result.getPredictedFeature().getValue(), "label2", "Should predict label2 (higher score)");
		Assert.assertNotNull(result.getPredictions(), "Predictions should be set");
		Assert.assertEquals(result.getPredictions().size(), 2, "Should have 2 prediction edges");

		Mockito.verify(brain, Mockito.times(1)).generateRawPolicy(Mockito.anyList(), Mockito.anyList());
		Mockito.verify(brain, Mockito.times(1)).predict(policy);
		Mockito.verify(memoryRecordRepository, Mockito.times(1)).save(Mockito.any(MemoryRecord.class));
		Mockito.verify(predictionRepository, Mockito.times(2)).save(Mockito.any(Prediction.class));
	}

	/**
	 * Purpose: Test predict endpoint with unstructured text input
	 */
	@Test
	public void testPredictWithUnstructuredText() throws Exception {
		// Setup
		String input = "This is unstructured text input";
		String[] outputLabels = {"label1"};

		double[][] policy = {{0.5}, {0.3}};
		double[] prediction = {0.8};

		Mockito.when(featureRepository.findByValue("label1")).thenReturn(null);
		Mockito.when(brain.generateRawPolicy(Mockito.anyList(), Mockito.anyList())).thenReturn(policy);
		Mockito.when(brain.predict(policy)).thenReturn(prediction);

		MemoryRecord savedMemory = new MemoryRecord();
		setMemoryRecordId(savedMemory, 2L);
		Mockito.when(memoryRecordRepository.save(Mockito.any(MemoryRecord.class))).thenReturn(savedMemory);

		Prediction pred = new Prediction(savedMemory, new Feature("label1"), prediction[0]);
		Mockito.when(predictionRepository.save(Mockito.any(Prediction.class))).thenReturn(pred);

		// Execute
		MemoryRecord result = controller.predict(input, outputLabels);

		// Verify
		Assert.assertNotNull(result, "Result should not be null");
		Assert.assertNotNull(result.getInputFeatureValues(), "Input feature values should be set");
	}

	/**
	 * Purpose: Test predict endpoint with existing features in repository
	 */
	@Test
	public void testPredictWithExistingFeatures() throws Exception {
		// Setup
		String input = "{\"test\":\"value\"}";
		String[] outputLabels = {"existing_label"};

		Feature existingFeature = new Feature("existing_label");
		try {
			java.lang.reflect.Field idField = Feature.class.getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(existingFeature, 100L);
		} catch (Exception e) {
			throw new RuntimeException("Failed to set feature ID", e);
		}

		double[][] policy = {{0.5}};
		double[] prediction = {0.9};

		Mockito.when(featureRepository.findByValue("existing_label")).thenReturn(existingFeature);
		Mockito.when(brain.generateRawPolicy(Mockito.anyList(), Mockito.anyList())).thenReturn(policy);
		Mockito.when(brain.predict(policy)).thenReturn(prediction);

		MemoryRecord savedMemory = new MemoryRecord();
		try {
			java.lang.reflect.Field idField = MemoryRecord.class.getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(savedMemory, 3L);
		} catch (Exception e) {
			throw new RuntimeException("Failed to set memory ID", e);
		}
		Mockito.when(memoryRecordRepository.save(Mockito.any(MemoryRecord.class))).thenReturn(savedMemory);

		Prediction pred = new Prediction(savedMemory, existingFeature, prediction[0]);
		Mockito.when(predictionRepository.save(Mockito.any(Prediction.class))).thenReturn(pred);

		// Execute
		MemoryRecord result = controller.predict(input, outputLabels);

		// Verify
		Assert.assertNotNull(result, "Result should not be null");
		Assert.assertEquals(result.getPredictedFeature(), existingFeature, "Should use existing feature");
		Mockito.verify(featureRepository, Mockito.times(1)).findByValue("existing_label");
	}

	/**
	 * Purpose: Test predict endpoint with invalid JSON (should fall back to text decomposition)
	 */
	@Test
	public void testPredictWithInvalidJson() throws Exception {
		// Setup
		String input = "invalid json { missing quote";
		String[] outputLabels = {"label1"};

		double[][] policy = {{0.5}};
		double[] prediction = {0.7};

		Mockito.when(featureRepository.findByValue("label1")).thenReturn(null);
		Mockito.when(brain.generateRawPolicy(Mockito.anyList(), Mockito.anyList())).thenReturn(policy);
		Mockito.when(brain.predict(policy)).thenReturn(prediction);

		MemoryRecord savedMemory = new MemoryRecord();
		try {
			java.lang.reflect.Field idField = MemoryRecord.class.getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(savedMemory, 4L);
		} catch (Exception e) {
			throw new RuntimeException("Failed to set memory ID", e);
		}
		Mockito.when(memoryRecordRepository.save(Mockito.any(MemoryRecord.class))).thenReturn(savedMemory);

		Prediction pred = new Prediction(savedMemory, new Feature("label1"), prediction[0]);
		Mockito.when(predictionRepository.save(Mockito.any(Prediction.class))).thenReturn(pred);

		// Execute - should not throw exception, should fall back to text decomposition
		MemoryRecord result = controller.predict(input, outputLabels);

		// Verify
		Assert.assertNotNull(result, "Result should not be null even with invalid JSON");
	}

	/**
	 * Purpose: Test predict endpoint with input features matching output features (should be scrubbed)
	 */
	@Test
	public void testPredictWithMatchingInputOutputFeatures() throws Exception {
		// Setup
		String input = "{\"label1\":\"value\"}";
		String[] outputLabels = {"label1"};

		double[][] policy = {{0.5}};
		double[] prediction = {0.8};

		Mockito.when(featureRepository.findByValue("label1")).thenReturn(null);
		Mockito.when(brain.generateRawPolicy(Mockito.anyList(), Mockito.anyList())).thenReturn(policy);
		Mockito.when(brain.predict(policy)).thenReturn(prediction);

		MemoryRecord savedMemory = new MemoryRecord();
		try {
			java.lang.reflect.Field idField = MemoryRecord.class.getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(savedMemory, 5L);
		} catch (Exception e) {
			throw new RuntimeException("Failed to set memory ID", e);
		}
		Mockito.when(memoryRecordRepository.save(Mockito.any(MemoryRecord.class))).thenReturn(savedMemory);

		Prediction pred = new Prediction(savedMemory, new Feature("label1"), prediction[0]);
		Mockito.when(predictionRepository.save(Mockito.any(Prediction.class))).thenReturn(pred);

		// Execute
		MemoryRecord result = controller.predict(input, outputLabels);

		// Verify
		Assert.assertNotNull(result, "Result should not be null");
		// label1 should be scrubbed from input features
		Assert.assertFalse(result.getInputFeatureValues().contains("label1"),
			"Input features should not contain output feature label1");
	}

	/**
	 * Purpose: Test predict endpoint with features containing brackets (should be cleaned)
	 */
	@Test
	public void testPredictWithBracketsInOutputLabels() throws Exception {
		// Setup
		String input = "{\"test\":\"value\"}";
		String[] outputLabels = {"[label1]", "[label2]"};

		double[][] policy = {{0.5, 0.3}};
		double[] prediction = {0.6, 0.4};

		Mockito.when(featureRepository.findByValue("[label1]")).thenReturn(null);
		Mockito.when(featureRepository.findByValue("[label2]")).thenReturn(null);
		Mockito.when(brain.generateRawPolicy(Mockito.anyList(), Mockito.anyList())).thenReturn(policy);
		Mockito.when(brain.predict(policy)).thenReturn(prediction);

		MemoryRecord savedMemory = new MemoryRecord();
		setMemoryRecordId(savedMemory, 6L);
		Mockito.when(memoryRecordRepository.save(Mockito.any(MemoryRecord.class))).thenReturn(savedMemory);

		Prediction pred1 = new Prediction(savedMemory, new Feature("label1"), prediction[0]);
		Prediction pred2 = new Prediction(savedMemory, new Feature("label2"), prediction[1]);
		Mockito.when(predictionRepository.save(Mockito.any(Prediction.class)))
			.thenReturn(pred1)
			.thenReturn(pred2);

		// Execute
		MemoryRecord result = controller.predict(input, outputLabels);

		// Verify
		Assert.assertNotNull(result, "Result should not be null");
		// Brackets should be removed from output feature keys
		Assert.assertEquals(result.getOutputFeatureKeys()[0], "label1", "Brackets should be removed");
		Assert.assertEquals(result.getOutputFeatureKeys()[1], "label2", "Brackets should be removed");
	}

	/**
	 * Purpose: Test predict endpoint with null input parameter
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void testPredictWithNullInput() throws Exception {
		// Setup
		String[] outputLabels = {"label1"};

		// Execute - should throw NullPointerException
		controller.predict(null, outputLabels);
	}

	/**
	 * Purpose: Test predict endpoint with empty input string
	 */
	@Test
	public void testPredictWithEmptyInput() throws Exception {
		// Setup
		String input = "";
		String[] outputLabels = {"label1"};

		double[][] policy = {{0.5}};
		double[] prediction = {0.8};

		Mockito.when(featureRepository.findByValue("label1")).thenReturn(null);
		Mockito.when(brain.generateRawPolicy(Mockito.anyList(), Mockito.anyList())).thenReturn(policy);
		Mockito.when(brain.predict(policy)).thenReturn(prediction);

		MemoryRecord savedMemory = new MemoryRecord();
		try {
			java.lang.reflect.Field idField = MemoryRecord.class.getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(savedMemory, 7L);
		} catch (Exception e) {
			throw new RuntimeException("Failed to set memory ID", e);
		}
		Mockito.when(memoryRecordRepository.save(Mockito.any(MemoryRecord.class))).thenReturn(savedMemory);

		Prediction pred = new Prediction(savedMemory, new Feature("label1"), prediction[0]);
		Mockito.when(predictionRepository.save(Mockito.any(Prediction.class))).thenReturn(pred);

		// Execute
		MemoryRecord result = controller.predict(input, outputLabels);

		// Verify
		Assert.assertNotNull(result, "Result should not be null even with empty input");
	}

	/**
	 * Purpose: Test predict endpoint with null output_features array
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void testPredictWithNullOutputFeatures() throws Exception {
		// Setup
		String input = "{\"test\":\"value\"}";

		// Execute - should throw NullPointerException
		controller.predict(input, null);
	}

	/**
	 * Purpose: Test predict endpoint with empty output_features array
	 */
	@Test
	public void testPredictWithEmptyOutputFeatures() throws Exception {
		// Setup
		String input = "{\"test\":\"value\"}";
		String[] outputLabels = {};

		// Execute - should handle gracefully
		MemoryRecord result = controller.predict(input, outputLabels);

		// Verify
		Assert.assertNotNull(result, "Result should not be null");
		Assert.assertEquals(result.getOutputFeatureKeys().length, 0, "Should have no output features");
	}

	/**
	 * Purpose: Test predict endpoint when brain service throws exception
	 */
	@Test(expectedExceptions = RuntimeException.class)
	public void testPredictWhenBrainThrowsException() throws Exception {
		// Setup
		String input = "{\"test\":\"value\"}";
		String[] outputLabels = {"label1"};

		Mockito.when(featureRepository.findByValue("label1")).thenReturn(null);
		Mockito.when(brain.generateRawPolicy(Mockito.anyList(), Mockito.anyList()))
			.thenThrow(new RuntimeException("Brain service error"));

		// Execute - should propagate exception
		controller.predict(input, outputLabels);
	}

	/**
	 * Purpose: Test predict endpoint with "null" string as feature value (should be filtered)
	 */
	@Test
	public void testPredictWithNullStringValue() throws Exception {
		// Setup
		String input = "{\"test\":\"null\",\"valid\":\"value\"}";
		String[] outputLabels = {"label1"};

		double[][] policy = {{0.5}};
		double[] prediction = {0.8};

		Mockito.when(featureRepository.findByValue("label1")).thenReturn(null);
		Mockito.when(brain.generateRawPolicy(Mockito.anyList(), Mockito.anyList())).thenReturn(policy);
		Mockito.when(brain.predict(policy)).thenReturn(prediction);

		MemoryRecord savedMemory = new MemoryRecord();
		setMemoryRecordId(savedMemory, 8L);
		Mockito.when(memoryRecordRepository.save(Mockito.any(MemoryRecord.class))).thenReturn(savedMemory);

		Prediction pred = new Prediction(savedMemory, new Feature("label1"), prediction[0]);
		Mockito.when(predictionRepository.save(Mockito.any(Prediction.class))).thenReturn(pred);

		// Execute
		MemoryRecord result = controller.predict(input, outputLabels);

		// Verify
		Assert.assertNotNull(result, "Result should not be null");
		// "null" string should be filtered from input features
		Assert.assertFalse(result.getInputFeatureValues().contains("null"),
			"Input features should not contain 'null' string");
	}

	// ==================== POST /learn Endpoint Tests ====================

	/**
	 * Purpose: Test learn endpoint with valid memory_id and existing feature
	 */
	@Test
	public void testLearnWithValidMemoryIdAndExistingFeature() throws Exception {
		// Setup
		long memoryId = 1L;
		String featureValue = "VERB";

		Feature existingFeature = new Feature("VERB");
		try {
			java.lang.reflect.Field idField = Feature.class.getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(existingFeature, 200L);
		} catch (Exception e) {
			throw new RuntimeException("Failed to set feature ID", e);
		}

		Mockito.when(featureRepository.findByValue("VERB")).thenReturn(existingFeature);
		Mockito.doNothing().when(brain).learn(memoryId, existingFeature);

		// Execute
		controller.learn(memoryId, featureValue);

		// Verify
		Mockito.verify(featureRepository, Mockito.times(1)).findByValue("VERB");
		Mockito.verify(brain, Mockito.times(1)).learn(memoryId, existingFeature);
	}

	/**
	 * Purpose: Test learn endpoint with valid memory_id and new feature
	 */
	@Test
	public void testLearnWithValidMemoryIdAndNewFeature() throws Exception {
		// Setup
		long memoryId = 2L;
		String featureValue = "NOUN";

		Mockito.when(featureRepository.findByValue("NOUN")).thenReturn(null);
		Mockito.doNothing().when(brain).learn(Mockito.eq(memoryId), Mockito.any(Feature.class));

		// Execute
		controller.learn(memoryId, featureValue);

		// Verify
		Mockito.verify(featureRepository, Mockito.times(1)).findByValue("NOUN");
		Mockito.verify(brain, Mockito.times(1)).learn(Mockito.eq(memoryId), Mockito.any(Feature.class));
	}

	/**
	 * Purpose: Test learn endpoint with feature value containing brackets (should be cleaned)
	 */
	@Test
	public void testLearnWithBracketsInFeatureValue() throws Exception {
		// Setup
		long memoryId = 3L;
		String featureValue = "[VERB]";

		Mockito.when(featureRepository.findByValue("[VERB]")).thenReturn(null);
		Mockito.doNothing().when(brain).learn(Mockito.eq(memoryId), Mockito.any(Feature.class));

		// Execute
		controller.learn(memoryId, featureValue);

		// Verify
		Mockito.verify(featureRepository, Mockito.times(1)).findByValue("[VERB]");
		Mockito.verify(brain, Mockito.times(1)).learn(Mockito.eq(memoryId), Mockito.any(Feature.class));
	}

	/**
	 * Purpose: Test learn endpoint with negative memory_id
	 */
	@Test
	public void testLearnWithNegativeMemoryId() throws Exception {
		// Setup
		long memoryId = -1L;
		String featureValue = "VERB";

		Mockito.when(featureRepository.findByValue("VERB")).thenReturn(null);
		Mockito.doNothing().when(brain).learn(Mockito.anyLong(), Mockito.any(Feature.class));

		// Execute - should handle gracefully
		controller.learn(memoryId, featureValue);

		// Verify
		Mockito.verify(brain, Mockito.times(1)).learn(memoryId, Mockito.any(Feature.class));
	}

	/**
	 * Purpose: Test learn endpoint with zero memory_id
	 */
	@Test
	public void testLearnWithZeroMemoryId() throws Exception {
		// Setup
		long memoryId = 0L;
		String featureValue = "VERB";

		Mockito.when(featureRepository.findByValue("VERB")).thenReturn(null);
		Mockito.doNothing().when(brain).learn(Mockito.anyLong(), Mockito.any(Feature.class));

		// Execute
		controller.learn(memoryId, featureValue);

		// Verify
		Mockito.verify(brain, Mockito.times(1)).learn(0L, Mockito.any(Feature.class));
	}

	/**
	 * Purpose: Test learn endpoint with empty feature_value string
	 */
	@Test
	public void testLearnWithEmptyFeatureValue() throws Exception {
		// Setup
		long memoryId = 4L;
		String featureValue = "";

		Mockito.when(featureRepository.findByValue("")).thenReturn(null);
		Mockito.doNothing().when(brain).learn(Mockito.anyLong(), Mockito.any(Feature.class));

		// Execute
		controller.learn(memoryId, featureValue);

		// Verify
		Mockito.verify(brain, Mockito.times(1)).learn(memoryId, Mockito.any(Feature.class));
	}

	/**
	 * Purpose: Test learn endpoint when brain.learn() throws IOException
	 */
	@Test(expectedExceptions = IOException.class)
	public void testLearnWhenBrainThrowsIOException() throws Exception {
		// Setup
		long memoryId = 5L;
		String featureValue = "VERB";

		Mockito.when(featureRepository.findByValue("VERB")).thenReturn(null);
		Mockito.doThrow(new IOException("I/O error")).when(brain).learn(Mockito.anyLong(), Mockito.any(Feature.class));

		// Execute - should propagate exception
		controller.learn(memoryId, featureValue);
	}

	// ==================== POST /train Endpoint Tests ====================

	/**
	 * Purpose: Test train endpoint with valid JSON object and label
	 */
	@Test
	public void testTrainWithValidJsonObject() throws Exception {
		// Setup
		String jsonObject = "{\"field1\":\"value1\",\"field2\":\"value2\"}";
		String label = "test_label";

		Mockito.doNothing().when(brain).train(Mockito.anyList(), Mockito.eq(label));

		// Execute
		controller.train(jsonObject, label);

		// Verify
		Mockito.verify(brain, Mockito.times(1)).train(Mockito.anyList(), Mockito.eq(label));
	}

	/**
	 * Purpose: Test train endpoint with complex nested JSON object
	 */
	@Test
	public void testTrainWithComplexNestedJson() throws Exception {
		// Setup
		String jsonObject = "{\"level1\":{\"level2\":{\"level3\":\"value\"}},\"array\":[1,2,3]}";
		String label = "nested_label";

		Mockito.doNothing().when(brain).train(Mockito.anyList(), Mockito.eq(label));

		// Execute
		controller.train(jsonObject, label);

		// Verify
		Mockito.verify(brain, Mockito.times(1)).train(Mockito.anyList(), Mockito.eq(label));
	}

	/**
	 * Purpose: Test train endpoint with simple flat JSON object
	 */
	@Test
	public void testTrainWithSimpleFlatJson() throws Exception {
		// Setup
		String jsonObject = "{\"key\":\"value\"}";
		String label = "simple_label";

		Mockito.doNothing().when(brain).train(Mockito.anyList(), Mockito.eq(label));

		// Execute
		controller.train(jsonObject, label);

		// Verify
		Mockito.verify(brain, Mockito.times(1)).train(Mockito.anyList(), Mockito.eq(label));
	}

	/**
	 * Purpose: Test train endpoint with invalid JSON format (should throw JSONException)
	 */
	@Test(expectedExceptions = JSONException.class)
	public void testTrainWithInvalidJson() throws Exception {
		// Setup
		String jsonObject = "invalid json { missing quote";
		String label = "test_label";

		// Execute - should throw JSONException
		controller.train(jsonObject, label);
	}

	/**
	 * Purpose: Test train endpoint with empty json_object string
	 */
	@Test(expectedExceptions = JSONException.class)
	public void testTrainWithEmptyJsonObject() throws Exception {
		// Setup
		String jsonObject = "";
		String label = "test_label";

		// Execute - should throw JSONException
		controller.train(jsonObject, label);
	}

	/**
	 * Purpose: Test train endpoint with null label parameter
	 */
	@Test
	public void testTrainWithNullLabel() throws Exception {
		// Setup
		String jsonObject = "{\"test\":\"value\"}";
		String label = null;

		Mockito.doNothing().when(brain).train(Mockito.anyList(), Mockito.isNull());

		// Execute
		controller.train(jsonObject, label);

		// Verify
		Mockito.verify(brain, Mockito.times(1)).train(Mockito.anyList(), Mockito.isNull());
	}

	/**
	 * Purpose: Test train endpoint with empty label string
	 */
	@Test
	public void testTrainWithEmptyLabel() throws Exception {
		// Setup
		String jsonObject = "{\"test\":\"value\"}";
		String label = "";

		Mockito.doNothing().when(brain).train(Mockito.anyList(), Mockito.eq(""));

		// Execute
		controller.train(jsonObject, label);

		// Verify
		Mockito.verify(brain, Mockito.times(1)).train(Mockito.anyList(), Mockito.eq(""));
	}

	/**
	 * Purpose: Test train endpoint when brain.train() throws IOException
	 */
	@Test(expectedExceptions = IOException.class)
	public void testTrainWhenBrainThrowsIOException() throws Exception {
		// Setup
		String jsonObject = "{\"test\":\"value\"}";
		String label = "test_label";

		Mockito.doThrow(new IOException("I/O error")).when(brain).train(Mockito.anyList(), Mockito.anyString());

		// Execute - should propagate exception
		controller.train(jsonObject, label);
	}

	/**
	 * Purpose: Test train endpoint with JSON containing null values
	 */
	@Test
	public void testTrainWithJsonContainingNullValues() throws Exception {
		// Setup
		String jsonObject = "{\"field1\":null,\"field2\":\"value\"}";
		String label = "null_label";

		Mockito.doNothing().when(brain).train(Mockito.anyList(), Mockito.eq(label));

		// Execute
		controller.train(jsonObject, label);

		// Verify
		Mockito.verify(brain, Mockito.times(1)).train(Mockito.anyList(), Mockito.eq(label));
	}

	/**
	 * Purpose: Test train endpoint with JSON array
	 */
	@Test
	public void testTrainWithJsonArray() throws Exception {
		// Setup
		String jsonObject = "[{\"item1\":\"value1\"},{\"item2\":\"value2\"}]";
		String label = "array_label";

		// Note: JSONObject constructor expects an object, not an array
		// This will throw JSONException, which is expected behavior
		try {
			controller.train(jsonObject, label);
			Assert.fail("Should have thrown JSONException for array");
		} catch (JSONException e) {
			// Expected
		}
	}

	/**
	 * Helper method to set long value to the memoryRecord's id field via reflection
	 */
	private void setMemoryRecordId(MemoryRecord memoryRecord, long id) {
		try {
			java.lang.reflect.Field idField = MemoryRecord.class.getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(memoryRecord, id);
		} catch (Exception e) {
			throw new RuntimeException("Failed to set memory ID by helper", e);
		}
	}
}
