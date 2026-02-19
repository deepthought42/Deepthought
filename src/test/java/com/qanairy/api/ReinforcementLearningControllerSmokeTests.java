package com.qanairy.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.deepthought.models.Feature;
import com.deepthought.models.MemoryRecord;
import com.deepthought.models.edges.Prediction;
import com.deepthought.models.repository.FeatureRepository;
import com.deepthought.models.repository.MemoryRecordRepository;
import com.deepthought.models.repository.PredictionRepository;
import com.qanairy.brain.Brain;

/**
 * Regression smoke tests for the reinforcement learning REST API.
 * These tests exercise the /rl/predict and /rl/learn endpoints at the
 * controller level using mocks for the repositories and Brain, ensuring
 * the wiring still works with Spring Boot 3 / Spring Data Neo4j.
 */
@Tag("Regression")
public class ReinforcementLearningControllerSmokeTests {

	private ReinforcementLearningController controller;

	private FeatureRepository feature_repo;
	private MemoryRecordRepository memory_repo;
	private PredictionRepository prediction_repo;
	private Brain brain;

	@BeforeEach
	public void setUp() {
		feature_repo = org.mockito.Mockito.mock(FeatureRepository.class);
		memory_repo = org.mockito.Mockito.mock(MemoryRecordRepository.class);
		prediction_repo = org.mockito.Mockito.mock(PredictionRepository.class);
		brain = org.mockito.Mockito.mock(Brain.class);

		controller = new ReinforcementLearningController();

		// Manually inject mocks (field injection style)
		setField("feature_repo", feature_repo);
		setField("memory_repo", memory_repo);
		setField("prediction_repo", prediction_repo);
		setField("brain", brain);
	}

	private void setField(String name, Object value) {
		try {
			java.lang.reflect.Field f = ReinforcementLearningController.class.getDeclaredField(name);
			f.setAccessible(true);
			f.set(controller, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void predict_createsMemoryRecordAndPredictions() throws Exception {
		// Arrange input JSON and output labels
		String input = "{\"field\":\"value\"}";
		String[] output_labels = new String[] { "LABEL_A", "LABEL_B" };

		Feature labelA = new Feature("LABEL_A");
		Feature labelB = new Feature("LABEL_B");

		when(feature_repo.findByValue("LABEL_A")).thenReturn(labelA);
		when(feature_repo.findByValue("LABEL_B")).thenReturn(labelB);

		// Brain returns a simple 2x2 policy and prediction distribution
		double[][] policy = new double[][] { { 0.1, 0.9 }, { 0.2, 0.8 } };
		double[] prediction = new double[] { 0.3, 0.7 };

		when(brain.generatePolicy(any(), any())).thenReturn(policy);
		when(brain.predict(policy)).thenReturn(prediction);

		// Capture saved MemoryRecord so we can feed it back when creating Prediction edges
		ArgumentCaptor<MemoryRecord> memoryCaptor = ArgumentCaptor.forClass(MemoryRecord.class);
		when(memory_repo.save(memoryCaptor.capture())).thenAnswer(inv -> {
			MemoryRecord m = inv.getArgument(0);
			// Simulate Neo4j assigning an ID
			java.lang.reflect.Field idField = MemoryRecord.class.getDeclaredField("id");
			idField.setAccessible(true);
			if (idField.get(m) == null) {
				idField.set(m, 1L);
			}
			return m;
		});

		when(prediction_repo.save(any(Prediction.class))).thenAnswer(inv -> inv.getArgument(0));

		// Act
		MemoryRecord result = controller.predict(input, output_labels);

		// Assert
		assertNotNull(result);
		assertNotNull(result.getPredictedFeature());
		assertTrue(Arrays.asList(output_labels).contains(result.getPredictedFeature().getValue()));

		MemoryRecord savedMemory = memoryCaptor.getValue();
		assertNotNull(savedMemory);
		assertEquals(savedMemory.getInputFeatureValues().size() > 0, true);
		assertEquals(savedMemory.getOutputFeatureKeys().length, 2);

		// One MemoryRecord persisted and a Prediction edge for each output label
		verify(memory_repo, times(1)).save(any(MemoryRecord.class));
		verify(prediction_repo, times(2)).save(any(Prediction.class));
	}

	@Test
	public void learn_delegatesToBrainWithResolvedFeature() throws JSONException, IOException, IllegalAccessException {
		long memory_id = 42L;
		String feature_value = "CLICK";

		MemoryRecord memory = new MemoryRecord();
		java.lang.reflect.Field idField;
		try {
			idField = MemoryRecord.class.getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(memory, memory_id);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		when(memory_repo.findById(memory_id)).thenReturn(Optional.of(memory));

		Feature storedFeature = new Feature(feature_value);
		when(feature_repo.findByValue(feature_value)).thenReturn(storedFeature);

		// Act
		controller.learn(memory_id, feature_value);

		// Assert
		verify(brain, times(1)).learn(eq(memory_id), any(Feature.class));
	}
}

