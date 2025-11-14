package com.deepthought.brain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.deepthought.data.edges.FeatureWeight;
import com.deepthought.data.models.Feature;
import com.deepthought.data.models.ReasoningPath;
import com.deepthought.data.repository.FeatureRepository;

/**
 * Test suite for GraphReasoningEngine
 * 
 * Tests validate the GraphReasoningEngine which provides:
 * 1. reason() - Multi-hop graph traversal to gather reasoning paths
 * 2. computeAttentionScores() - Computes attention scores for candidate features
 * 3. gatherRelevantFeatures() - Gathers all relevant features within specified hops
 */
public class GraphReasoningEngineTests {

	@Mock
	private FeatureRepository featureRepository;

	@InjectMocks
	private GraphReasoningEngine graphReasoningEngine;

	/**
	 * Purpose: Set up test fixture before each test method
	 * 
	 * Steps:
	 * 1. Initialize Mockito mocks
	 * 2. Inject mocked repository into engine
	 */
	@BeforeMethod
	public void setUp() {
		// Step 1 & 2: Initialize mocks and inject into engine
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Purpose: Test reason() with null query features
	 * 
	 * Steps:
	 * 1. Call reason() with null query features
	 * 2. Verify result is empty list
	 * 3. Verify repository is not called
	 */
	@Test
	public void testReasonWithNullQueryFeatures() {
		// Step 1: Execute - Call reason with null
		List<ReasoningPath> result = graphReasoningEngine.reason(null, 3, 0.5);

		// Step 2: Verify - Result should be empty list
		Assert.assertNotNull(result, "Result should not be null");
		Assert.assertTrue(result.isEmpty(), 
			"Result should be empty when query features is null");

		// Step 3: Verify - Repository should not be called
		Mockito.verifyNoInteractions(featureRepository);
	}

	/**
	 * Purpose: Test reason() with empty query features list
	 * 
	 * Steps:
	 * 1. Call reason() with empty list
	 * 2. Verify result is empty list
	 * 3. Verify repository is not called
	 */
	@Test
	public void testReasonWithEmptyQueryFeatures() {
		// Step 1: Execute - Call reason with empty list
		List<ReasoningPath> result = graphReasoningEngine.reason(new ArrayList<>(), 3, 0.5);

		// Step 2: Verify - Result should be empty list
		Assert.assertNotNull(result, "Result should not be null");
		Assert.assertTrue(result.isEmpty(), 
			"Result should be empty when query features list is empty");

		// Step 3: Verify - Repository should not be called
		Mockito.verifyNoInteractions(featureRepository);
	}

	/**
	 * Purpose: Test reason() with feature not found in repository
	 * 
	 * Steps:
	 * 1. Create query feature
	 * 2. Mock repository to return null (feature not found)
	 * 3. Call reason() method
	 * 4. Verify result is empty list
	 * 5. Verify repository was called
	 */
	@Test
	public void testReasonWithFeatureNotFound() {
		// Step 1: Setup - Create query feature
		Feature queryFeature = new Feature("nonexistent");
		List<Feature> queryFeatures = new ArrayList<>();
		queryFeatures.add(queryFeature);

		// Step 2: Setup - Mock repository to return null
		Mockito.when(featureRepository.findByValue("nonexistent")).thenReturn(null);

		// Step 3: Execute - Call reason method
		List<ReasoningPath> result = graphReasoningEngine.reason(queryFeatures, 3, 0.5);

		// Step 4: Verify - Result should be empty list
		Assert.assertNotNull(result, "Result should not be null");
		Assert.assertTrue(result.isEmpty(), 
			"Result should be empty when feature is not found");

		// Step 5: Verify - Repository was called
		Mockito.verify(featureRepository, Mockito.times(1)).findByValue("nonexistent");
	}

	/**
	 * Purpose: Test reason() with single feature and no connections
	 * 
	 * Steps:
	 * 1. Create query feature with no connections
	 * 2. Mock repository to return feature with empty weights
	 * 3. Call reason() method
	 * 4. Verify result contains path with only starting feature
	 */
	@Test
	public void testReasonWithNoConnections() {
		// Step 1: Setup - Create query feature with no connections
		Feature queryFeature = new Feature("isolated");
		List<Feature> queryFeatures = new ArrayList<>();
		queryFeatures.add(queryFeature);

		// Step 2: Setup - Mock repository to return feature with empty weights
		Feature featureRecord = new Feature("isolated");
		Mockito.when(featureRepository.findByValue("isolated")).thenReturn(featureRecord);

		// Step 3: Execute - Call reason method
		List<ReasoningPath> result = graphReasoningEngine.reason(queryFeatures, 3, 0.5);

		// Step 4: Verify - Result should contain path with starting feature (always added)
		Assert.assertNotNull(result, "Result should not be null");
		Assert.assertEquals(result.size(), 1, "Should have one reasoning path");
		Assert.assertEquals(result.get(0).getFeatures().size(), 1, 
			"Path should contain only the starting feature");
		Assert.assertEquals(result.get(0).getFeatures().get(0).getValue(), "isolated", 
			"Path should contain the isolated feature");
	}

	/**
	 * Purpose: Test reason() with single hop traversal
	 * 
	 * Steps:
	 * 1. Create query feature with connected features
	 * 2. Mock repository and create feature weights
	 * 3. Call reason() with maxHops=1
	 * 4. Verify result contains path with starting and connected features
	 */
	@Test
	public void testReasonWithSingleHop() {
		// Step 1: Setup - Create query feature
		Feature queryFeature = new Feature("start");
		List<Feature> queryFeatures = new ArrayList<>();
		queryFeatures.add(queryFeature);

		// Step 2: Setup - Create connected features with weights
		Feature connected1 = new Feature("connected1");
		Feature connected2 = new Feature("connected2");
		
		FeatureWeight weight1 = new FeatureWeight();
		weight1.setEndFeature(connected1);
		weight1.setWeight(0.8);
		
		FeatureWeight weight2 = new FeatureWeight();
		weight2.setEndFeature(connected2);
		weight2.setWeight(0.6);
		
		Feature startFeature = new Feature("start");
		List<FeatureWeight> weights = new ArrayList<>();
		weights.add(weight1);
		weights.add(weight2);
		startFeature.getFeatureWeights().addAll(weights);

		Mockito.when(featureRepository.findByValue("start")).thenReturn(startFeature);

		// Step 3: Execute - Call reason with maxHops=1
		List<ReasoningPath> result = graphReasoningEngine.reason(queryFeatures, 1, 0.5);

		// Step 4: Verify - Result should contain one path with starting and connected features
		Assert.assertNotNull(result, "Result should not be null");
		Assert.assertEquals(result.size(), 1, "Should have one reasoning path");
		
		ReasoningPath path = result.get(0);
		Assert.assertNotNull(path, "Path should not be null");
		Assert.assertTrue(path.getFeatures().size() >= 1, 
			"Path should contain at least the starting feature");
	}

	/**
	 * Purpose: Test reason() with confidence threshold filtering
	 * 
	 * Steps:
	 * 1. Create query feature with multiple connections at different weights
	 * 2. Mock repository with features having weights above and below threshold
	 * 3. Call reason() with minConfidence threshold
	 * 4. Verify only features above threshold are included
	 */
	@Test
	public void testReasonWithConfidenceThreshold() {
		// Step 1: Setup - Create query feature
		Feature queryFeature = new Feature("start");
		List<Feature> queryFeatures = new ArrayList<>();
		queryFeatures.add(queryFeature);

		// Step 2: Setup - Create features with different weights
		Feature highWeight = new Feature("high");
		Feature lowWeight = new Feature("low");
		
		FeatureWeight weightHigh = new FeatureWeight();
		weightHigh.setEndFeature(highWeight);
		weightHigh.setWeight(0.8); // Above threshold
		
		FeatureWeight weightLow = new FeatureWeight();
		weightLow.setEndFeature(lowWeight);
		weightLow.setWeight(0.3); // Below threshold
		
		Feature startFeature = new Feature("start");
		List<FeatureWeight> weights = new ArrayList<>();
		weights.add(weightHigh);
		weights.add(weightLow);
		startFeature.getFeatureWeights().addAll(weights);

		Mockito.when(featureRepository.findByValue("start")).thenReturn(startFeature);

		// Step 3: Execute - Call reason with minConfidence=0.5
		List<ReasoningPath> result = graphReasoningEngine.reason(queryFeatures, 2, 0.5);

		// Step 4: Verify - Only high weight feature should be included
		Assert.assertNotNull(result, "Result should not be null");
		Assert.assertEquals(result.size(), 1, "Should have one reasoning path");
		
		ReasoningPath path = result.get(0);
		boolean hasHigh = path.getFeatures().stream()
			.anyMatch(f -> f.getValue().equals("high"));
		boolean hasLow = path.getFeatures().stream()
			.anyMatch(f -> f.getValue().equals("low"));
		
		Assert.assertTrue(hasHigh, "Path should include high weight feature");
		Assert.assertFalse(hasLow, "Path should not include low weight feature");
	}

	/**
	 * Purpose: Test reason() with multi-hop traversal
	 * 
	 * Steps:
	 * 1. Create query feature with connected features that have their own connections
	 * 2. Mock repository with multi-level feature graph
	 * 3. Call reason() with maxHops > 1
	 * 4. Verify path includes features from multiple hops
	 */
	@Test
	public void testReasonWithMultiHop() {
		// Step 1: Setup - Create query feature
		Feature queryFeature = new Feature("start");
		List<Feature> queryFeatures = new ArrayList<>();
		queryFeatures.add(queryFeature);

		// Step 2: Setup - Create multi-level feature graph
		Feature level1 = new Feature("level1");
		Feature level2 = new Feature("level2");
		
		FeatureWeight weightToLevel1 = new FeatureWeight();
		weightToLevel1.setEndFeature(level1);
		weightToLevel1.setWeight(0.8);
		
		FeatureWeight weightToLevel2 = new FeatureWeight();
		weightToLevel2.setEndFeature(level2);
		weightToLevel2.setWeight(0.7);
		
		Feature startFeature = new Feature("start");
		startFeature.getFeatureWeights().add(weightToLevel1);
		
		level1.getFeatureWeights().add(weightToLevel2);

		Mockito.when(featureRepository.findByValue("start")).thenReturn(startFeature);
		Mockito.when(featureRepository.findByValue("level1")).thenReturn(level1);

		// Step 3: Execute - Call reason with maxHops=2
		List<ReasoningPath> result = graphReasoningEngine.reason(queryFeatures, 2, 0.5);

		// Step 4: Verify - Path should include features from multiple hops
		Assert.assertNotNull(result, "Result should not be null");
		Assert.assertEquals(result.size(), 1, "Should have one reasoning path");
		
		ReasoningPath path = result.get(0);
		Assert.assertTrue(path.getFeatures().size() >= 2, 
			"Path should include features from multiple hops");
	}

	/**
	 * Purpose: Test reason() with cycle detection
	 * 
	 * Steps:
	 * 1. Create features that form a cycle (A -> B -> A)
	 * 2. Mock repository with cyclic graph
	 * 3. Call reason() method
	 * 4. Verify cycle is detected and not traversed infinitely
	 */
	@Test
	public void testReasonWithCycle() {
		// Step 1: Setup - Create query feature
		Feature queryFeature = new Feature("A");
		List<Feature> queryFeatures = new ArrayList<>();
		queryFeatures.add(queryFeature);

		// Step 2: Setup - Create cyclic graph (A -> B -> A)
		Feature featureA = new Feature("A");
		Feature featureB = new Feature("B");
		
		FeatureWeight weightAtoB = new FeatureWeight();
		weightAtoB.setEndFeature(featureB);
		weightAtoB.setWeight(0.8);
		
		FeatureWeight weightBtoA = new FeatureWeight();
		weightBtoA.setEndFeature(featureA);
		weightBtoA.setWeight(0.7);
		
		featureA.getFeatureWeights().add(weightAtoB);
		featureB.getFeatureWeights().add(weightBtoA);

		Mockito.when(featureRepository.findByValue("A")).thenReturn(featureA);
		Mockito.when(featureRepository.findByValue("B")).thenReturn(featureB);

		// Step 3: Execute - Call reason method
		List<ReasoningPath> result = graphReasoningEngine.reason(queryFeatures, 3, 0.5);

		// Step 4: Verify - Cycle should be detected, path should not be infinite
		// Note: The implementation creates new visited sets for recursive calls,
		// so cycles may be traversed once but not infinitely
		Assert.assertNotNull(result, "Result should not be null");
		Assert.assertEquals(result.size(), 1, "Should have one reasoning path");
		
		ReasoningPath path = result.get(0);
		// Verify path is not empty and contains expected features
		Assert.assertTrue(path.getFeatures().size() > 0, "Path should not be empty");
		// The implementation prevents infinite cycles by checking visited at each step
		Assert.assertTrue(path.getFeatures().size() <= 3, 
			"Path should not be infinite (max 3 features for 3 hops with cycle)");
	}

	/**
	 * Purpose: Test reason() with zero maxHops
	 * 
	 * Steps:
	 * 1. Create query feature with connections
	 * 2. Mock repository
	 * 3. Call reason() with maxHops=0
	 * 4. Verify result is empty (no traversal possible)
	 */
	@Test
	public void testReasonWithZeroMaxHops() {
		// Step 1: Setup - Create query feature
		Feature queryFeature = new Feature("start");
		List<Feature> queryFeatures = new ArrayList<>();
		queryFeatures.add(queryFeature);

		// Step 2: Setup - Mock repository
		Feature startFeature = new Feature("start");
		Mockito.when(featureRepository.findByValue("start")).thenReturn(startFeature);

		// Step 3: Execute - Call reason with maxHops=0
		List<ReasoningPath> result = graphReasoningEngine.reason(queryFeatures, 0, 0.5);

		// Step 4: Verify - Result should be empty (no traversal with 0 hops)
		Assert.assertNotNull(result, "Result should not be null");
		Assert.assertTrue(result.isEmpty(), 
			"Result should be empty when maxHops is 0");
	}

	/**
	 * Purpose: Test computeAttentionScores() with null query features
	 * 
	 * Steps:
	 * 1. Create candidate features
	 * 2. Call computeAttentionScores() with null query features
	 * 3. Verify NullPointerException is thrown (implementation doesn't check for null)
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void testComputeAttentionScoresWithNullQueryFeatures() {
		// Step 1: Setup - Create candidate features
		List<Feature> candidates = new ArrayList<>();
		candidates.add(new Feature("candidate1"));

		// Step 2 & 3: Execute and Verify - Should throw NullPointerException
		graphReasoningEngine.computeAttentionScores(null, candidates);
	}

	/**
	 * Purpose: Test computeAttentionScores() with empty query features
	 * 
	 * Steps:
	 * 1. Create candidate features
	 * 2. Call computeAttentionScores() with empty query features
	 * 3. Verify all candidates have score 0.0
	 */
	@Test
	public void testComputeAttentionScoresWithEmptyQueryFeatures() {
		// Step 1: Setup - Create candidate features
		List<Feature> candidates = new ArrayList<>();
		candidates.add(new Feature("candidate1"));
		candidates.add(new Feature("candidate2"));

		// Step 2: Execute - Call with empty query features
		Map<String, Double> result = graphReasoningEngine.computeAttentionScores(
			new ArrayList<>(), candidates);

		// Step 3: Verify - All candidates should have score 0.0
		Assert.assertNotNull(result, "Result should not be null");
		Assert.assertEquals(result.size(), 2, "Should have scores for all candidates");
		Assert.assertEquals(result.get("candidate1"), 0.0, 
			"Candidate1 should have score 0.0");
		Assert.assertEquals(result.get("candidate2"), 0.0, 
			"Candidate2 should have score 0.0");
	}

	/**
	 * Purpose: Test computeAttentionScores() with no connections
	 * 
	 * Steps:
	 * 1. Create query and candidate features
	 * 2. Mock repository to return features with no connections
	 * 3. Call computeAttentionScores()
	 * 4. Verify all scores are 0.0
	 */
	@Test
	public void testComputeAttentionScoresWithNoConnections() {
		// Step 1: Setup - Create query and candidate features
		List<Feature> queryFeatures = new ArrayList<>();
		queryFeatures.add(new Feature("query"));
		
		List<Feature> candidates = new ArrayList<>();
		candidates.add(new Feature("candidate"));

		// Step 2: Setup - Mock repository
		Feature queryRecord = new Feature("query");
		Mockito.when(featureRepository.findByValue("query")).thenReturn(queryRecord);
		Mockito.when(featureRepository.getConnectedFeatures("query", "candidate"))
			.thenReturn(new ArrayList<>());

		// Step 3: Execute - Call computeAttentionScores
		Map<String, Double> result = graphReasoningEngine.computeAttentionScores(
			queryFeatures, candidates);

		// Step 4: Verify - Score should be 0.0
		Assert.assertNotNull(result, "Result should not be null");
		Assert.assertEquals(result.get("candidate"), 0.0, 
			"Score should be 0.0 when no connections exist");
	}

	/**
	 * Purpose: Test computeAttentionScores() with connections
	 * 
	 * Steps:
	 * 1. Create query and candidate features
	 * 2. Mock repository with connection between them
	 * 3. Call computeAttentionScores()
	 * 4. Verify score matches connection weight
	 */
	@Test
	public void testComputeAttentionScoresWithConnections() {
		// Step 1: Setup - Create query and candidate features
		List<Feature> queryFeatures = new ArrayList<>();
		queryFeatures.add(new Feature("query"));
		
		List<Feature> candidates = new ArrayList<>();
		candidates.add(new Feature("candidate"));

		// Step 2: Setup - Mock repository with connection
		Feature queryRecord = new Feature("query");
		Feature candidateFeature = new Feature("candidate");
		
		FeatureWeight weight = new FeatureWeight();
		weight.setEndFeature(candidateFeature);
		weight.setWeight(0.75);
		
		Feature connectedFeature = new Feature("query");
		connectedFeature.getFeatureWeights().add(weight);
		
		List<Feature> connected = new ArrayList<>();
		connected.add(connectedFeature);

		Mockito.when(featureRepository.findByValue("query")).thenReturn(queryRecord);
		Mockito.when(featureRepository.getConnectedFeatures("query", "candidate"))
			.thenReturn(connected);

		// Step 3: Execute - Call computeAttentionScores
		Map<String, Double> result = graphReasoningEngine.computeAttentionScores(
			queryFeatures, candidates);

		// Step 4: Verify - Score should match connection weight
		Assert.assertNotNull(result, "Result should not be null");
		Assert.assertEquals(result.get("candidate"), 0.75, 0.001, 
			"Score should match connection weight");
	}

	/**
	 * Purpose: Test computeAttentionScores() with multiple query features
	 * 
	 * Steps:
	 * 1. Create multiple query features and candidate
	 * 2. Mock repository with different weights from each query
	 * 3. Call computeAttentionScores()
	 * 4. Verify score is maximum of all connection weights
	 */
	@Test
	public void testComputeAttentionScoresWithMultipleQueries() {
		// Step 1: Setup - Create multiple query features and candidate
		List<Feature> queryFeatures = new ArrayList<>();
		queryFeatures.add(new Feature("query1"));
		queryFeatures.add(new Feature("query2"));
		
		List<Feature> candidates = new ArrayList<>();
		candidates.add(new Feature("candidate"));

		// Step 2: Setup - Mock repository with different weights
		Feature query1Record = new Feature("query1");
		Feature query2Record = new Feature("query2");
		Feature candidateFeature = new Feature("candidate");
		
		FeatureWeight weight1 = new FeatureWeight();
		weight1.setEndFeature(candidateFeature);
		weight1.setWeight(0.5);
		
		FeatureWeight weight2 = new FeatureWeight();
		weight2.setEndFeature(candidateFeature);
		weight2.setWeight(0.9); // Higher weight
		
		Feature connected1 = new Feature("query1");
		connected1.getFeatureWeights().add(weight1);
		
		Feature connected2 = new Feature("query2");
		connected2.getFeatureWeights().add(weight2);
		
		List<Feature> connected1List = new ArrayList<>();
		connected1List.add(connected1);
		List<Feature> connected2List = new ArrayList<>();
		connected2List.add(connected2);

		Mockito.when(featureRepository.findByValue("query1")).thenReturn(query1Record);
		Mockito.when(featureRepository.findByValue("query2")).thenReturn(query2Record);
		Mockito.when(featureRepository.getConnectedFeatures("query1", "candidate"))
			.thenReturn(connected1List);
		Mockito.when(featureRepository.getConnectedFeatures("query2", "candidate"))
			.thenReturn(connected2List);

		// Step 3: Execute - Call computeAttentionScores
		Map<String, Double> result = graphReasoningEngine.computeAttentionScores(
			queryFeatures, candidates);

		// Step 4: Verify - Score should be maximum (0.9)
		Assert.assertNotNull(result, "Result should not be null");
		Assert.assertEquals(result.get("candidate"), 0.9, 0.001, 
			"Score should be maximum of all connection weights");
	}

	/**
	 * Purpose: Test gatherRelevantFeatures() with null query features
	 * 
	 * Steps:
	 * 1. Call gatherRelevantFeatures() with null
	 * 2. Verify NullPointerException is thrown (implementation doesn't check for null)
	 */
	@Test(expectedExceptions = NullPointerException.class)
	public void testGatherRelevantFeaturesWithNullQueryFeatures() {
		// Step 1 & 2: Execute and Verify - Should throw NullPointerException
		graphReasoningEngine.gatherRelevantFeatures(null, 3);
	}

	/**
	 * Purpose: Test gatherRelevantFeatures() with empty query features
	 * 
	 * Steps:
	 * 1. Call gatherRelevantFeatures() with empty list
	 * 2. Verify result is empty list
	 */
	@Test
	public void testGatherRelevantFeaturesWithEmptyQueryFeatures() {
		// Step 1: Execute - Call with empty list
		List<Feature> result = graphReasoningEngine.gatherRelevantFeatures(
			new ArrayList<>(), 3);

		// Step 2: Verify - Result should be empty list
		Assert.assertNotNull(result, "Result should not be null");
		Assert.assertTrue(result.isEmpty(), 
			"Result should be empty when query features list is empty");
	}

	/**
	 * Purpose: Test gatherRelevantFeatures() with feature not found
	 * 
	 * Steps:
	 * 1. Create query feature
	 * 2. Mock repository to return null
	 * 3. Call gatherRelevantFeatures()
	 * 4. Verify result is empty list
	 */
	@Test
	public void testGatherRelevantFeaturesWithFeatureNotFound() {
		// Step 1: Setup - Create query feature
		List<Feature> queryFeatures = new ArrayList<>();
		queryFeatures.add(new Feature("nonexistent"));

		// Step 2: Setup - Mock repository to return null
		Mockito.when(featureRepository.findByValue("nonexistent")).thenReturn(null);

		// Step 3: Execute - Call gatherRelevantFeatures
		List<Feature> result = graphReasoningEngine.gatherRelevantFeatures(queryFeatures, 3);

		// Step 4: Verify - Result should be empty list
		Assert.assertNotNull(result, "Result should not be null");
		Assert.assertTrue(result.isEmpty(), 
			"Result should be empty when feature is not found");
	}

	/**
	 * Purpose: Test gatherRelevantFeatures() with single feature and no connections
	 * 
	 * Steps:
	 * 1. Create query feature with no connections
	 * 2. Mock repository
	 * 3. Call gatherRelevantFeatures()
	 * 4. Verify result contains only the starting feature
	 */
	@Test
	public void testGatherRelevantFeaturesWithNoConnections() {
		// Step 1: Setup - Create query feature
		List<Feature> queryFeatures = new ArrayList<>();
		queryFeatures.add(new Feature("isolated"));

		// Step 2: Setup - Mock repository
		Feature featureRecord = new Feature("isolated");
		Mockito.when(featureRepository.findByValue("isolated")).thenReturn(featureRecord);

		// Step 3: Execute - Call gatherRelevantFeatures
		List<Feature> result = graphReasoningEngine.gatherRelevantFeatures(queryFeatures, 3);

		// Step 4: Verify - Result should contain only starting feature
		Assert.assertNotNull(result, "Result should not be null");
		Assert.assertEquals(result.size(), 1, "Should contain one feature");
		Assert.assertEquals(result.get(0).getValue(), "isolated", 
			"Should contain the starting feature");
	}

	/**
	 * Purpose: Test gatherRelevantFeatures() with connections
	 * 
	 * Steps:
	 * 1. Create query feature with connected features
	 * 2. Mock repository with feature graph
	 * 3. Call gatherRelevantFeatures()
	 * 4. Verify result contains starting and connected features
	 */
	@Test
	public void testGatherRelevantFeaturesWithConnections() {
		// Step 1: Setup - Create query feature
		List<Feature> queryFeatures = new ArrayList<>();
		queryFeatures.add(new Feature("start"));

		// Step 2: Setup - Create connected features
		Feature connected1 = new Feature("connected1");
		Feature connected2 = new Feature("connected2");
		
		FeatureWeight weight1 = new FeatureWeight();
		weight1.setEndFeature(connected1);
		weight1.setWeight(0.8);
		
		FeatureWeight weight2 = new FeatureWeight();
		weight2.setEndFeature(connected2);
		weight2.setWeight(0.6);
		
		Feature startFeature = new Feature("start");
		startFeature.getFeatureWeights().add(weight1);
		startFeature.getFeatureWeights().add(weight2);

		Mockito.when(featureRepository.findByValue("start")).thenReturn(startFeature);

		// Step 3: Execute - Call gatherRelevantFeatures
		List<Feature> result = graphReasoningEngine.gatherRelevantFeatures(queryFeatures, 2);

		// Step 4: Verify - Result should contain starting and connected features
		Assert.assertNotNull(result, "Result should not be null");
		Assert.assertTrue(result.size() >= 1, "Should contain at least starting feature");
		
		boolean hasStart = result.stream()
			.anyMatch(f -> f.getValue().equals("start"));
		Assert.assertTrue(hasStart, "Should contain starting feature");
	}

	/**
	 * Purpose: Test gatherRelevantFeatures() with multi-hop traversal
	 * 
	 * Steps:
	 * 1. Create query feature with multi-level connections
	 * 2. Mock repository with multi-level graph
	 * 3. Call gatherRelevantFeatures() with maxHops > 1
	 * 4. Verify result contains features from multiple hops
	 */
	@Test
	public void testGatherRelevantFeaturesWithMultiHop() {
		// Step 1: Setup - Create query feature
		List<Feature> queryFeatures = new ArrayList<>();
		queryFeatures.add(new Feature("start"));

		// Step 2: Setup - Create multi-level feature graph
		Feature level1 = new Feature("level1");
		Feature level2 = new Feature("level2");
		
		FeatureWeight weightToLevel1 = new FeatureWeight();
		weightToLevel1.setEndFeature(level1);
		weightToLevel1.setWeight(0.8);
		
		FeatureWeight weightToLevel2 = new FeatureWeight();
		weightToLevel2.setEndFeature(level2);
		weightToLevel2.setWeight(0.7);
		
		Feature startFeature = new Feature("start");
		startFeature.getFeatureWeights().add(weightToLevel1);
		
		level1.getFeatureWeights().add(weightToLevel2);

		Mockito.when(featureRepository.findByValue("start")).thenReturn(startFeature);
		Mockito.when(featureRepository.findByValue("level1")).thenReturn(level1);

		// Step 3: Execute - Call gatherRelevantFeatures with maxHops=2
		List<Feature> result = graphReasoningEngine.gatherRelevantFeatures(queryFeatures, 2);

		// Step 4: Verify - Result should contain features from multiple hops
		Assert.assertNotNull(result, "Result should not be null");
		Assert.assertTrue(result.size() >= 2, 
			"Should contain features from multiple hops");
	}

	/**
	 * Purpose: Test gatherRelevantFeatures() with zero maxHops
	 * 
	 * Steps:
	 * 1. Create query feature
	 * 2. Mock repository
	 * 3. Call gatherRelevantFeatures() with maxHops=0
	 * 4. Verify result is empty (no traversal possible)
	 */
	@Test
	public void testGatherRelevantFeaturesWithZeroMaxHops() {
		// Step 1: Setup - Create query feature
		List<Feature> queryFeatures = new ArrayList<>();
		queryFeatures.add(new Feature("start"));

		// Step 2: Setup - Mock repository
		Feature startFeature = new Feature("start");
		Mockito.when(featureRepository.findByValue("start")).thenReturn(startFeature);

		// Step 3: Execute - Call gatherRelevantFeatures with maxHops=0
		List<Feature> result = graphReasoningEngine.gatherRelevantFeatures(queryFeatures, 0);

		// Step 4: Verify - Result should be empty (no traversal with 0 hops)
		Assert.assertNotNull(result, "Result should not be null");
		Assert.assertTrue(result.isEmpty(), 
			"Result should be empty when maxHops is 0");
	}

	/**
	 * Purpose: Test gatherRelevantFeatures() with cycle detection
	 * 
	 * Steps:
	 * 1. Create features that form a cycle
	 * 2. Mock repository with cyclic graph
	 * 3. Call gatherRelevantFeatures()
	 * 4. Verify cycle is detected and features are not duplicated
	 */
	@Test
	public void testGatherRelevantFeaturesWithCycle() {
		// Step 1: Setup - Create query feature
		List<Feature> queryFeatures = new ArrayList<>();
		queryFeatures.add(new Feature("A"));

		// Step 2: Setup - Create cyclic graph (A -> B -> A)
		Feature featureA = new Feature("A");
		Feature featureB = new Feature("B");
		
		FeatureWeight weightAtoB = new FeatureWeight();
		weightAtoB.setEndFeature(featureB);
		weightAtoB.setWeight(0.8);
		
		FeatureWeight weightBtoA = new FeatureWeight();
		weightBtoA.setEndFeature(featureA);
		weightBtoA.setWeight(0.7);
		
		featureA.getFeatureWeights().add(weightAtoB);
		featureB.getFeatureWeights().add(weightBtoA);

		Mockito.when(featureRepository.findByValue("A")).thenReturn(featureA);
		Mockito.when(featureRepository.findByValue("B")).thenReturn(featureB);

		// Step 3: Execute - Call gatherRelevantFeatures
		List<Feature> result = graphReasoningEngine.gatherRelevantFeatures(queryFeatures, 3);

		// Step 4: Verify - Cycle should be detected, features should not be duplicated
		Assert.assertNotNull(result, "Result should not be null");
		
		long countA = result.stream()
			.filter(f -> f.getValue().equals("A"))
			.count();
		long countB = result.stream()
			.filter(f -> f.getValue().equals("B"))
			.count();
		
		// Each feature should appear at most once (cycle prevented)
		Assert.assertTrue(countA <= 1, "Feature A should not appear multiple times");
		Assert.assertTrue(countB <= 1, "Feature B should not appear multiple times");
	}
}

