package com.deepthought.brain;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.deepthought.data.edges.FeatureWeight;
import com.deepthought.data.models.Feature;
import com.deepthought.data.models.Vocabulary;
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
	 * Loads a policy matrix that maps input features to their learned action probabilities
	 * based on feature weights stored in the knowledge graph.
	 * 
	 * <p>This method constructs a 2D array where each row represents an input feature and
	 * each column represents an output feature (action) from the vocabulary. The values
	 * in the matrix represent learned weights/probabilities for transitioning from input
	 * features to output features, as determined by FeatureWeight relationships in the
	 * knowledge graph.
	 * 
	 * <p><b>Preconditions:</b>
	 * <ul>
	 *   <li>{@code input_features != null} - input feature list must not be null</li>
	 *   <li>{@code output_features != null} - output feature list must not be null</li>
	 *   <li>{@code vocab != null} - vocabulary must not be null</li>
	 *   <li>{@code vocab.getValueList() != null} - vocabulary value list must be initialized</li>
	 *   <li>{@code obj_def_repo != null} - feature repository must be autowired and initialized</li>
	 *   <li>All features in {@code input_features} must have non-null {@code value} properties</li>
	 * </ul>
	 * 
	 * <p><b>Postconditions:</b>
	 * <ul>
	 *   <li>Returns a 2D array with dimensions {@code [input_features.size()][output_features.size()]}</li>
	 *   <li>Each row {@code k} corresponds to {@code input_features.get(k)}</li>
	 *   <li>Each column {@code j} corresponds to {@code output_features.get(j)}</li>
	 *   <li>{@code result[k][j]} contains the weight for transitioning from input feature {@code k}
	 *       to output feature {@code j}, or 0.0 if no such relationship exists in the graph</li>
	 *   <li>All input features have been persisted to the repository via {@code obj_def_repo.save()}</li>
	 *   <li>Only FeatureWeight relationships whose end features exist in the vocabulary are included</li>
	 * </ul>
	 * 
	 * <p><b>Side Effects:</b>
	 * <ul>
	 *   <li>Persists all input features to the database via {@code obj_def_repo.save()}</li>
	 *   <li>Logs an informational message about concatenating action features</li>
	 * </ul>
	 * 
	 * <p><b>Usage:</b>
	 * <pre>{@code
	 * List<Feature> inputs = Arrays.asList(new Feature("button"), new Feature("form"));
	 * List<Feature> outputs = Arrays.asList(new Feature("click"), new Feature("submit"));
	 * Vocabulary vocab = new Vocabulary("actions");
	 * vocab.appendToVocabulary(new Feature("click"));
	 * vocab.appendToVocabulary(new Feature("submit"));
	 * 
	 * double[][] policy = FeatureVector.loadPolicy(inputs, outputs, vocab);
	 * // policy[0][0] = weight for button -> click
	 * // policy[0][1] = weight for button -> submit
	 * // policy[1][0] = weight for form -> click
	 * // policy[1][1] = weight for form -> submit
	 * }</pre>
	 * 
	 * @param input_features List of input features representing the current state/observations.
	 *                       Each feature will be saved to the repository and its outgoing
	 *                       FeatureWeight relationships will be queried.
	 * @param output_features List of output features representing possible actions/outcomes.
	 *                        Used to determine the column dimension of the returned matrix.
	 *                        Note: actual weights are filtered by vocabulary membership.
	 * @param vocab Vocabulary object containing the ordered list of feature values that
	 *              determine valid action indices. Only FeatureWeight edges whose end
	 *              features are present in this vocabulary will be included in the policy.
	 * @return A 2D array of doubles where {@code result[k][j]} represents the learned weight
	 *         for transitioning from input feature {@code k} to the output feature at
	 *         vocabulary index {@code j}. Unmapped positions contain 0.0.
	 * @throws NullPointerException if any of the required parameters are null
	 * @throws IllegalStateException if {@code obj_def_repo} is not properly initialized
	 */
	public static double[][] loadPolicy(List<Feature> input_features, List<Feature> output_features, Vocabulary vocab){
		double[][] vocab_policy = new double[input_features.size()][output_features.size()];

		log.info("concatenating action features into 2d array for vocabulary");
		//set output_features for object definition to action probabilities
		for(int k = 0; k < input_features.size(); k++){
			Feature def = input_features.get(k);
			obj_def_repo.save(def);
			//load action policy for object definition
			Set<FeatureWeight> feature_weights = obj_def_repo.getFeatureWeights(def.getValue());
			double[] feature_weights_array = new double[feature_weights.size()];
			feature_weights.stream()
				.filter(fw -> vocab.getValueList().indexOf(fw.getEndFeature().getValue()) >= 0)
				.forEach(fw -> {
					int action_idx = vocab.getValueList().indexOf(fw.getEndFeature().getValue());
					feature_weights_array[action_idx] = fw.getWeight();
				});
			vocab_policy[k] = feature_weights_array;
		}
		return vocab_policy;
	}
	
	/**
	 * Loads a vocabulary record that maps input features to their presence/absence in a vocabulary
	 * based on feature weights stored in the knowledge graph.
	 *
	 * <p>This method constructs a HashMap where each key represents an input feature and the value
	 * represents whether the feature is present (1) or absent (0) in the vocabulary. The HashMap
	 * is populated by iterating through the input features and checking if they match any of the
	 * output features in the vocabulary.
	 *
	 * @param input_features List of input features representing the current state/observations.
	 *                       Each feature will be saved to the repository and its outgoing
	 *                       FeatureWeight relationships will be queried.
	 * @param output_features List of output features representing possible actions/outcomes.
	 *                        Used to determine the column dimension of the returned HashMap.
	 *                        Note: actual weights are filtered by vocabulary membership.
	 * @param vocab Vocabulary object containing the ordered list of feature values that
	 *              determine valid action indices. Only FeatureWeight edges whose end
	 *              features are present in this vocabulary will be included in the record.
	 * @return A HashMap where each key represents an input feature and the value represents
	 *         whether the feature is present (1) or absent (0) in the vocabulary.
	 * @throws NullPointerException if any of the required parameters are null
	 * @return HashMap<String, Integer> where the key is the input feature value and the value is 1 if the feature is present in the vocabulary, 0 otherwise
	 */
	public static HashMap<String, Integer> load(List<Feature> input_features, List<Feature> output_features){
		HashMap<String, Integer> vocabulary_record = new HashMap<String, Integer>();
		
    	for(Feature definition : input_features){
    		boolean has_match = false;
    		for(Feature record_definition : output_features){
    			if(record_definition.equals(definition)){
    				vocabulary_record.put(record_definition.getValue(), 1);
    				has_match = true;
    				break;
    			}
    		}
    		
    		if(!has_match){
    			vocabulary_record.put(definition.getValue(), 0);
    		}
       	}
    	
    	return vocabulary_record;
	}
}
