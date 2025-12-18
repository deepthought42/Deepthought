package com.deepthought.brain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.deepthought.data.edges.FeatureWeight;
import com.deepthought.data.models.Feature;
import com.deepthought.data.repository.FeatureRepository;

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
		for(int i = 0; i < input_features.size(); i++){
			Set<FeatureWeight> feature_weight_set = feature_weight_vectors.get(i);
			
			if(feature_weight_set != null) {
				for(FeatureWeight feature_weight : feature_weight_set){
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
				}
			}
		}
	
		return feature_weight_matrix;
	}

	/**
	 * Creates a mapping of vocabulary features to their presence indicator.
	 * 
	 * This method constructs a HashMap where each key represents a vocabulary feature value
	 * and the value is set to 1, indicating the presence of that feature in the vocabulary.
	 * 
	 * Preconditions:
	 * - vocabulary_features is non-null
	 * - All features in vocabulary_features have non-null value properties
	 * 
	 * Postconditions:
	 * - Returns a non-null HashMap where each key is a vocabulary feature value and each value is 1
	 * - The size of the returned map equals the size of vocabulary_features
	 * - All vocabulary features are represented in the returned map
	 * 
	 * @param vocabulary_features List of vocabulary features to map
	 * @return A HashMap where each key is a vocabulary feature value and each value is 1
	 * @throws NullPointerException if vocabulary_features is null
	 */
	public static HashMap<String, Integer> loadVocabularyFeatures(List<Feature> vocabulary_features){
		HashMap<String, Integer> vocabulary_record = new HashMap<String, Integer>();
		
		for(Feature vocabulary_feature : vocabulary_features){
			vocabulary_record.put(vocabulary_feature.getValue(), 1);
		}

		return vocabulary_record;
	}
}