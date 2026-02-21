package com.qanairy.api;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Optional;

import org.springframework.web.server.ResponseStatusException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.deepthought.models.repository.FeatureRepository;
import com.deepthought.models.repository.MemoryRecordRepository;
import com.deepthought.models.repository.PredictionRepository;
import com.qanairy.brain.Brain;

@Test(groups = "Regression")
public class ReinforcementLearningControllerTests {

	private ReinforcementLearningController controller;
	private MemoryRecordRepository memory_repo;

	@BeforeMethod
	public void setUp() throws Exception {
		controller = new ReinforcementLearningController();
		memory_repo = mock(MemoryRecordRepository.class);

		setField("memory_repo", memory_repo);
		setField("feature_repo", mock(FeatureRepository.class));
		setField("prediction_repo", mock(PredictionRepository.class));
		setField("brain", mock(Brain.class));
	}

	private void setField(String name, Object value) throws Exception {
		Field f = ReinforcementLearningController.class.getDeclaredField(name);
		f.setAccessible(true);
		f.set(controller, value);
	}

	@Test
	public void getMaxPredictionIndex_handlesAllNegativeValues() {
		double[] prediction = new double[] { -0.5, -0.2, -0.9 };
		int idx = ReinforcementLearningController.getMaxPredictionIndex(prediction);
		assertEquals(idx, 1);
	}

	@Test
	public void learn_returns404WhenMemoryDoesNotExist() throws IOException, IllegalAccessException {
		when(memory_repo.findById(999L)).thenReturn(Optional.empty());

		try {
			controller.learn(999L, "missing");
			fail("Expected ResponseStatusException");
		} catch (ResponseStatusException e) {
			assertEquals(e.getStatus().value(), 404);
			assertTrue(e.getReason().contains("999"));
		}
	}
}
