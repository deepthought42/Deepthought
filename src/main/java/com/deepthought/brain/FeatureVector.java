package com.deepthought.brain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.deepthought.data.edges.FeatureWeight;
import com.deepthought.data.models.Feature;
import com.deepthought.data.models.Vocabulary;
import com.deepthought.data.repository.FeatureRepository;
import com.deepthought.data.repository.VocabularyRepository;

/**
 * Utility class for creating feature vectors from input features and output features
 * based on feature weights stored in the knowledge graph. Provides methods for loading
 * policy matrices and vocabulary records.
 */
@Component
public class FeatureVector {
	private static Logger log = LoggerFactory.getLogger(FeatureVector.class);

	@Autowired
	private static FeatureRepository obj_def_repo;
	
	@Autowired
	private static VocabularyRepository vocabulary_repo;
	
	/**
	 * Loads a policy matrix that maps input features to their connected features and weights
	 * based on feature weights stored in the knowledge graph.
	 * 
	 * This method constructs a 2D array where each row represents an input feature and
	 * each column represents a unique connected feature (result feature from FeatureWeight relationships).
	 * The values in the matrix represent the weights from FeatureWeight edges connecting
	 * input features to their connected features.
	 * 
	 * Preconditions:
	 * - input_features is non-null
	 * - obj_def_repo is non-null and properly initialized
	 * - All features in input_features have non-null value properties
	 * - FeatureWeight result features have non-null value properties
	 * 
	 * Postconditions:
	 * - Returns a 2D array with dimensions [input_features.size()][unique_connected_features.size()]
	 * - Each row k corresponds to input_features.get(k)
	 * - Each column j corresponds to a unique connected feature label, with column index determined
	 *   by the order in which connected features were first encountered
	 * - result[k][j] contains the weight from FeatureWeight.getWeight() for the connection between
	 *   input feature k and the connected feature at column j, or 0.0 if no such relationship exists
	 * - All unique connected features across all input features are represented in the column dimension
	 * 
	 * @param input_features List of input features representing the current state/observations.
	 *                       Each feature's outgoing FeatureWeight relationships will be queried
	 *                       to determine connected features and their weights.
	 * @return A 2D array of doubles where result[k][j] represents the weight connecting input feature
	 *         k to the connected feature at column j. Unmapped positions contain 0.0.
	 * @throws NullPointerException if input_features is null or obj_def_repo is not initialized
	 */
	public static double[][] loadPolicy(List<Feature> input_features){
		log.info("concatenating features into 2d array");

		// 1. Load Set of feature weight vectors for each input feature. Capture each connected feature label in a HashMap where each key is the connected feature label and the value is an index value. When a key is added to the HashMap, the value should be set to the size of the set of values in the hashmap before the key is added. 
		HashMap<String, Integer> connected_feature_labels = new HashMap<String, Integer>();
		List<Set<FeatureWeight>> feature_weight_vectors = new ArrayList<>();
		for(Feature input_feature : input_features){
			Set<FeatureWeight> feature_weights = obj_def_repo.getFeatureWeights(input_feature.getValue());
			for(FeatureWeight feature_weight : feature_weights){
				if(!connected_feature_labels.containsKey(feature_weight.getResultFeature().getValue())){
					connected_feature_labels.put(feature_weight.getResultFeature().getValue(), connected_feature_labels.size());
				}
			}
			feature_weight_vectors.add(feature_weights);
		}

		// 2. Create 2D array with x-axis = input_features.size(), y-axis = connected feature.size()
		double[][] feature_weight_matrix = new double[input_features.size()][connected_feature_labels.size()];

		// 3. Populate the 2D array with the weights from the feature weight vectors.
		for(int i = 0; i < input_features.size(); i++){
			Set<FeatureWeight> feature_weight_set = feature_weight_vectors.get(i);
			
			for(FeatureWeight feature_weight : feature_weight_set){
				int connected_feature_label_idx = connected_feature_labels.get(feature_weight.getResultFeature().getValue());
				feature_weight_matrix[i][connected_feature_label_idx] = feature_weight.getWeight();
			}
		}

		// 4. Return the 2D array
		return feature_weight_matrix;
	}

	public static double[][] loadPolicy_AI_Generated(List<Feature> input_features){
		log.info("concatenating features into 2d array");
	
		// Thread-safe HashMap for memoizing feature labels and their assigned indices
		ConcurrentHashMap<String, Integer> connected_feature_labels = new ConcurrentHashMap<>();
		
		// Atomic counter to assign indices atomically when new labels are encountered
		AtomicInteger indexCounter = new AtomicInteger(0);
		
		// 1. Retrieve feature weights for all input features in parallel
		// Each repository call is independent and can be executed concurrently

		// Fix: Make sure the generic type matches by using Set<? extends FeatureWeight>
		List<CompletableFuture<Set<FeatureWeight>>> featureWeightFutures =
			input_features.stream()
			.map(input_feature -> CompletableFuture.supplyAsync(() -> {
				Set<FeatureWeight> weights = obj_def_repo.getFeatureWeights(input_feature.getValue());
				if (weights == null) {
					// Always return non-null set to ensure downstream code works
					weights = new java.util.HashSet<>();
				}
				// Memoize connected feature labels in a threadsafe way
				for (FeatureWeight feature_weight : weights) {
					if (feature_weight != null && feature_weight.getResultFeature() != null) {
						String label = feature_weight.getResultFeature().getValue();
						if (label != null) {
							connected_feature_labels.computeIfAbsent(label, k -> indexCounter.getAndIncrement());
						}
					}
				}
				return weights;
			}))
			.collect(Collectors.toList());

		// Wait for all parallel retrievals to complete
		CompletableFuture.allOf(featureWeightFutures.toArray(new CompletableFuture[0])).join();
		// Extract results maintaining the order of input_features
		List<Set<FeatureWeight>> feature_weight_vectors = featureWeightFutures.stream()
			.map(CompletableFuture::join)
			.collect(Collectors.toList());
	
		// 2. Create 2D array with x-axis = input_features.size(), y-axis = connected feature.size()
		double[][] feature_weight_matrix = new double[input_features.size()][connected_feature_labels.size()];
	
		// 3. Populate the 2D array using memoized indices for constant-time lookups
		// Each row can be processed in parallel since they write to different row indices
		IntStream.range(0, input_features.size()).parallel().forEach(i -> {
			Set<FeatureWeight> feature_weight_set = feature_weight_vectors.get(i);
			
			if(feature_weight_set != null) {
				// Process each feature weight in parallel within the row
				// Each iteration writes to a different column index, so no contention
				feature_weight_set.parallelStream().forEach(feature_weight -> {
					if(feature_weight != null && feature_weight.getResultFeature() != null) {
						String label = feature_weight.getResultFeature().getValue();
						if(label != null) {
							Integer idx = connected_feature_labels.get(label);
							if(idx != null && idx >= 0 && idx < feature_weight_matrix[i].length) {
								// Targeted update: only update the specific cell using memoized index
								feature_weight_matrix[i][idx] = feature_weight.getWeight();
							}
						}
					}
				});
			}
		});
	
		return feature_weight_matrix;
	}

	/**
	 * Loads a multi-dimensional array of weights for features connected to a Vocabulary node.
	 * 
	 * This method:
	 * 1. Finds the Vocabulary node by label
	 * 2. Loads all Feature nodes whose values are in the vocabulary's valueList (1-hop)
	 * 3. Loads all Feature nodes connected to those features via FeatureWeight edges (2-hop)
	 * 4. Constructs a 3D array where:
	 *    - First dimension: Features from vocabulary (1-hop)
	 *    - Second dimension: Features directly connected to vocabulary features (1-hop connections)
	 *    - Third dimension: Features connected to those features (2-hop connections)
	 *    - Values: Edge weights from FeatureWeight relationships, using vocabulary-specific weights when available
	 * 
	 * Preconditions:
	 * - vocabulary_label is non-null and not empty
	 * - obj_def_repo is non-null and properly initialized
	 * - vocabulary_repo is non-null and properly initialized
	 * - Vocabulary with the given label exists in the database
	 * 
	 * Postconditions:
	 * - Returns a 3D array with dimensions [vocab_features.size()][max_1hop_features][max_2hop_features]
	 * - result[i][j][k] contains the weight from FeatureWeight for vocabulary feature i -> 1-hop feature j -> 2-hop feature k
	 * - If no connection exists, the value is 0.0
	 * - Vocabulary-specific weights (from FeatureWeight.getVocabularyWeight) are used when available,
	 *   otherwise the general weight (FeatureWeight.getWeight) is used
	 * 
	 * @param vocabulary_label The label of the Vocabulary node to load features from
	 * @return A 3D array of doubles where result[i][j][k] represents the weight connecting
	 *         vocabulary feature i to 1-hop feature j to 2-hop feature k. Unmapped positions contain 0.0.
	 * @throws NullPointerException if vocabulary_label is null or repositories are not initialized
	 * @throws IllegalArgumentException if vocabulary with the given label is not found
	 */
	public static double[][][] loadVocabularyFeatures(String vocabulary_label){
		log.info("Loading vocabulary features for label: {}", vocabulary_label);
		
		if (vocabulary_label == null || vocabulary_label.trim().isEmpty()) {
			throw new IllegalArgumentException("Vocabulary label cannot be null or empty");
		}
		
		if (vocabulary_repo == null) {
			throw new NullPointerException("VocabularyRepository is not initialized");
		}
		
		if (obj_def_repo == null) {
			throw new NullPointerException("FeatureRepository is not initialized");
		}
		
		// 1. Find Vocabulary node by label
		Vocabulary vocabulary = vocabulary_repo.findByLabel(vocabulary_label)
			.orElseThrow(() -> new IllegalArgumentException("Vocabulary with label '" + vocabulary_label + "' not found"));
		
		// Initialize mappings if needed
		if (vocabulary.getValueList() == null || vocabulary.getValueList().isEmpty()) {
			log.warn("Vocabulary '{}' has no features in valueList", vocabulary_label);
			return new double[0][0][0];
		}
		
		vocabulary.initializeMappings();
		
		// 2. Get all features from vocabulary (1-hop features)
		List<Feature> vocab_features = new ArrayList<>();
		HashMap<String, Integer> vocab_feature_indices = new HashMap<>();
		for (String feature_value : vocabulary.getValueList()) {
			Feature feature = obj_def_repo.findByValue(feature_value);
			if (feature != null) {
				vocab_feature_indices.put(feature_value, vocab_features.size());
				vocab_features.add(feature);
			}
		}
		
		if (vocab_features.isEmpty()) {
			log.warn("No features found for vocabulary '{}'", vocabulary_label);
			return new double[0][0][0];
		}
		
		// 3. Get 1-hop connections: features connected to vocabulary features
		HashMap<String, Integer> one_hop_indices = new HashMap<>();
		List<List<Feature>> one_hop_features = new ArrayList<>();
		
		for (Feature vocab_feature : vocab_features) {
			Set<FeatureWeight> feature_weights = obj_def_repo.getFeatureWeights(vocab_feature.getValue());
			List<Feature> connected_features = new ArrayList<>();
			
			for (FeatureWeight fw : feature_weights) {
				if (fw != null && fw.getResultFeature() != null) {
					String connected_value = fw.getResultFeature().getValue();
					if (connected_value != null && !one_hop_indices.containsKey(connected_value)) {
						one_hop_indices.put(connected_value, one_hop_indices.size());
					}
					connected_features.add(fw.getResultFeature());
				}
			}
			one_hop_features.add(connected_features);
		}
		
		// 4. Get 2-hop connections: features connected to 1-hop features
		HashMap<String, Integer> two_hop_indices = new HashMap<>();
		List<List<List<Feature>>> two_hop_features = new ArrayList<>();
		
		for (List<Feature> one_hop_list : one_hop_features) {
			List<List<Feature>> two_hop_for_vocab_feature = new ArrayList<>();
			
			for (Feature one_hop_feature : one_hop_list) {
				Set<FeatureWeight> two_hop_weights = obj_def_repo.getFeatureWeights(one_hop_feature.getValue());
				List<Feature> two_hop_list = new ArrayList<>();
				
				for (FeatureWeight fw : two_hop_weights) {
					if (fw != null && fw.getResultFeature() != null) {
						String two_hop_value = fw.getResultFeature().getValue();
						if (two_hop_value != null && !two_hop_indices.containsKey(two_hop_value)) {
							two_hop_indices.put(two_hop_value, two_hop_indices.size());
						}
						two_hop_list.add(fw.getResultFeature());
					}
				}
				two_hop_for_vocab_feature.add(two_hop_list);
			}
			two_hop_features.add(two_hop_for_vocab_feature);
		}
		
		// 5. Create 3D array: [vocab_features][1-hop_features][2-hop_features]
		int vocab_size = vocab_features.size();
		int one_hop_size = one_hop_indices.size();
		int two_hop_size = two_hop_indices.size();
		
		double[][][] weight_matrix = new double[vocab_size][one_hop_size][two_hop_size];
		
		// 6. Populate the 3D array with weights
		for (int i = 0; i < vocab_size; i++) {
			Feature vocab_feature = vocab_features.get(i);
			List<Feature> one_hop_list = one_hop_features.get(i);
			
			for (int j = 0; j < one_hop_list.size(); j++) {
				Feature one_hop_feature = one_hop_list.get(j);
				Integer one_hop_idx = one_hop_indices.get(one_hop_feature.getValue());
				
				if (one_hop_idx == null) continue;
				
				// Get weight from vocab_feature to one_hop_feature
				Set<FeatureWeight> weights_to_one_hop = obj_def_repo.getFeatureWeights(vocab_feature.getValue());
				double weight_1hop = 0.0;
				for (FeatureWeight fw : weights_to_one_hop) {
					if (fw != null && fw.getResultFeature() != null && 
						fw.getResultFeature().getValue().equals(one_hop_feature.getValue())) {
						// Prefer vocabulary-specific weight, fall back to general weight
						weight_1hop = fw.getVocabularyWeight(vocabulary_label);
						if (weight_1hop == 0.0) {
							weight_1hop = fw.getWeight();
						}
						break;
					}
				}
				
				List<Feature> two_hop_list = two_hop_features.get(i).get(j);
				
				for (int k = 0; k < two_hop_list.size(); k++) {
					Feature two_hop_feature = two_hop_list.get(k);
					Integer two_hop_idx = two_hop_indices.get(two_hop_feature.getValue());
					
					if (two_hop_idx == null) continue;
					
					// Get weight from one_hop_feature to two_hop_feature
					Set<FeatureWeight> weights_to_two_hop = obj_def_repo.getFeatureWeights(one_hop_feature.getValue());
					double weight_2hop = 0.0;
					for (FeatureWeight fw : weights_to_two_hop) {
						if (fw != null && fw.getResultFeature() != null && 
							fw.getResultFeature().getValue().equals(two_hop_feature.getValue())) {
							// Prefer vocabulary-specific weight, fall back to general weight
							weight_2hop = fw.getVocabularyWeight(vocabulary_label);
							if (weight_2hop == 0.0) {
								weight_2hop = fw.getWeight();
							}
							break;
						}
					}
					
					// Store combined weight (product of 1-hop and 2-hop weights)
					weight_matrix[i][one_hop_idx][two_hop_idx] = weight_1hop * weight_2hop;
				}
			}
		}
		
		log.info("Loaded vocabulary features: {} vocab features, {} 1-hop features, {} 2-hop features", 
			vocab_size, one_hop_size, two_hop_size);
		
		return weight_matrix;
	}
}