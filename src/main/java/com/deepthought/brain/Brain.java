package com.deepthought.brain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.deepthought.data.edges.FeatureWeight;
import com.deepthought.data.models.Feature;
import com.deepthought.data.models.MemoryRecord;
import com.deepthought.data.models.Vocabulary;
import com.deepthought.data.repository.FeatureRepository;
import com.deepthought.data.repository.FeatureWeightRepository;
import com.deepthought.data.repository.MemoryRecordRepository;
import com.deepthought.services.VocabularyService;

import edu.stanford.nlp.util.ArrayUtils;

/**
 * Provides ability to predict and learn from data
 * 
 */
@Component
public class Brain {
	private static Logger log = LoggerFactory.getLogger(Brain.class);

	@Value("${learning.rate}")
	private double learning_rate;

	@Value("${discount.factor}")
	private double discount_factor;

	@Autowired
	private FeatureRepository feature_repo;
	
	@Autowired
	private FeatureWeightRepository feature_weight_repo;
	
	@Autowired
	private MemoryRecordRepository memory_repo;

	@Autowired
	private VocabularyService vocab_service;
	
	@Autowired
	private VocabularyPrediction vocabulary_prediction;
	
	public Brain(){}
	
	/**
	 * Generates a probability distribution prediction from a policy matrix by summing
	 * columns and normalizing the result. This creates a probability distribution over
	 * output actions based on aggregated input feature weights.
	 * 
	 * <p><b>Preconditions:</b>
	 * <ul>
	 *   <li>{@code policy != null} - policy matrix must not be null</li>
	 *   <li>{@code policy.length > 0} - policy matrix must have at least one row</li>
	 *   <li>{@code policy[0].length > 0} - policy matrix must have at least one column</li>
	 *   <li>All rows in {@code policy} must have the same length</li>
	 * </ul>
	 * 
	 * <p><b>Postconditions:</b>
	 * <ul>
	 *   <li>Returns a normalized probability distribution array with length equal to
	 *       {@code policy[0].length}</li>
	 *   <li>All values in the returned array are non-negative</li>
	 *   <li>The sum of all values in the returned array equals 1.0 (normalized)</li>
	 *   <li>{@code result[i]} represents the probability of action {@code i} based on
	 *       the aggregated weights from all input features</li>
	 * </ul>
	 * 
	 * <p><b>Side Effects:</b>
	 * <ul>
	 *   <li>None - this is a pure function with no side effects</li>
	 * </ul>
	 * 
	 * @param policy A 2D array where {@code policy[i][j]} represents the weight from
	 *               input feature {@code i} to output action {@code j}
	 * @return A normalized probability distribution array where each element represents
	 *         the probability of the corresponding output action
	 * @throws NullPointerException if {@code policy} is null
	 * @throws ArrayIndexOutOfBoundsException if {@code policy} is empty or has inconsistent row lengths
	 */
	public double[] predict(double[][] policy){
			
		double[] prediction = new double[policy[0].length];
		for(int idx1 = 0; idx1 < policy[0].length; idx1++){
			double sum = 0.0;
			for(int idx2 = 0; idx2 < policy.length; idx2++){
				sum += policy[idx2][idx1];
			}
			
			prediction[idx1] = sum;
		}
		prediction = ArrayUtils.normalize(prediction);
		
		return prediction;
	}
		
	/**
	 * Performs Q-learning updates on feature weights based on observed outcomes from a memory record.
	 * This method implements the learning process by:
	 * <ol>
	 *   <li>Loading the memory record from the database</li>
	 *   <li>Calculating rewards for each output feature based on actual vs predicted outcomes</li>
	 *   <li>Performing Q-learning updates to adjust feature weights accordingly</li>
	 * </ol>
	 * 
	 * <p><b>Preconditions:</b>
	 * <ul>
	 *   <li>{@code memory_id > 0} - memory ID must be a valid positive identifier</li>
	 *   <li>{@code actual_feature != null} - actual feature must not be null</li>
	 *   <li>{@code actual_feature.getValue() != null} - actual feature must have a non-null value</li>
	 *   <li>{@code memory_repo != null} - memory repository must be initialized</li>
	 *   <li>{@code feature_repo != null} - feature repository must be initialized</li>
	 *   <li>{@code feature_weight_repo != null} - feature weight repository must be initialized</li>
	 *   <li>A memory record with the given {@code memory_id} must exist in the database</li>
	 *   <li>The memory record must have at least one output feature key</li>
	 * </ul>
	 * 
	 * <p><b>Postconditions:</b>
	 * <ul>
	 *   <li>Feature weights in the knowledge graph have been updated based on Q-learning calculations</li>
	 *   <li>All FeatureWeight edges connected to input features from the memory record have been
	 *       adjusted according to the calculated rewards</li>
	 *   <li>New FeatureWeight edges may have been created if connections did not previously exist</li>
	 *   <li>All weight updates have been persisted to the database</li>
	 * </ul>
	 * 
	 * <p><b>Side Effects:</b>
	 * <ul>
	 *   <li>Updates FeatureWeight edges in the knowledge graph database</li>
	 *   <li>Creates new FeatureWeight edges if connections do not exist</li>
	 *   <li>Logs debug information about reward calculations and feature weight updates</li>
	 * </ul>
	 * 
	 * @param memory_id The unique identifier of the memory record to learn from
	 * @param actual_feature The actual feature that occurred, used to calculate rewards
	 * @throws IllegalArgumentException if the memory record is invalid or cannot be processed
	 * @throws IllegalAccessException if there is an access violation when updating the database
	 * @throws NullPointerException if any required parameter is null or the memory record is not found
	 * @throws IOException if there is an I/O error when accessing the database
	 */
	public void learn(long memory_id,
					  Feature actual_feature)
						  throws IllegalArgumentException, IllegalAccessException,
							  NullPointerException, IOException{
		//Load memory from database
		Optional<MemoryRecord> memory_record = memory_repo.findById(memory_id);
		
		// 2a. load known action policies/probabilities for each object definition in the definition list
		MemoryRecord memory = memory_record.get();
		
		

		//set estimated reward using prediction from memory.

		//replace with steps to estimate reward for an output feature independent of actual desired output feature
		double estimated_reward = 1.0;
		
		// 3. determine reward/regret score based on productivity status
		double actual_reward = 0.0;
		for(String output_key : memory.getOutputFeatureKeys()){
			
			actual_reward = calculateReward(memory,
											output_key,
											actual_feature);
			
			performQLearning(memory,
							output_key,
							actual_feature,
							actual_reward,
							estimated_reward);
			
		}
	}

	/**
	 * Calculates the reward signal for a given output key based on comparison between
	 * predicted feature, actual feature, and the output key. The reward structure encourages
	 * correct predictions and penalizes incorrect ones.
	 * 
	 * <p><b>Reward Structure:</b>
	 * <ul>
	 *   <li>Reward = 2.0: Both predicted and actual match the output key (perfect prediction)</li>
	 *   <li>Reward = 1.0: Actual feature matches output key (correct outcome)</li>
	 *   <li>Reward = -1.0: Predicted feature matches output key but actual does not (overconfident mistake)</li>
	 *   <li>Reward = -2.0: Neither predicted nor actual match output key (incorrect outcome)</li>
	 *   <li>Reward = 0.0: Default case (no change, neutral)</li>
	 * </ul>
	 * 
	 * <p><b>Preconditions:</b>
	 * <ul>
	 *   <li>{@code memory != null} - memory record must not be null</li>
	 *   <li>{@code memory.getPredictedFeature() != null} - memory must have a predicted feature</li>
	 *   <li>{@code memory.getPredictedFeature().getValue() != null} - predicted feature must have a value</li>
	 *   <li>{@code output_key != null} - output key must not be null</li>
	 *   <li>{@code actual_feature != null} - actual feature must not be null</li>
	 *   <li>{@code actual_feature.getValue() != null} - actual feature must have a value</li>
	 * </ul>
	 * 
	 * <p><b>Postconditions:</b>
	 * <ul>
	 *   <li>Returns a reward value between -2.0 and 2.0 inclusive</li>
	 *   <li>The reward reflects the alignment between predicted, actual, and output key</li>
	 * </ul>
	 * 
	 * <p><b>Side Effects:</b>
	 * <ul>
	 *   <li>Logs debug information about calculated reward value</li>
	 * </ul>
	 * 
	 * @param memory The memory record containing the predicted feature and context
	 * @param output_key The output key being evaluated for reward calculation
	 * @param actual_feature The actual feature that occurred, used to determine reward
	 * @return The calculated reward value in the range [-2.0, 2.0]
	 * @throws NullPointerException if any of the required parameters or their properties are null
	 */
	public double calculateReward(MemoryRecord memory,
									String output_key,
									Feature actual_feature){
		double reward = 0.0;
		//if predicted feature is equal to output feature and actual feature is equal to predicted feature  OR output key equals actual feature key
		if(output_key.equals(actual_feature.getValue()) && actual_feature.getValue().equals(memory.getPredictedFeature().getValue())){
			log.debug("REWARD   ::    2");
			reward = 2.0;
		}
		else if(output_key.equals(actual_feature.getValue())){
			log.debug("REWARD   ::   1");
			reward = 1.0;
		}
		//if output isn't equal to the actual feature or the predicted feature, don't affect weights
		else if(output_key.equals(memory.getPredictedFeature().getValue()) && !output_key.equals(actual_feature.getValue())){
			log.debug("REWARD   ::     -2");
			reward = -1.0;
		}
		else if(!output_key.equals(actual_feature.getValue())) {
			log.debug("REWARD   ::     -1");
			reward = -2.0;
		}
		else {
			log.debug("REWARD   ::    0");
			//nothing changed so there was no reward for that combination. We want to remember this in the future
			// so we set it to a negative value to simulate regret
			reward = 0.0;
		}

		return reward;
	}

	/**
	 * Performs Q-learning updates on feature weights based on observed outcomes and rewards.
	 * This method iterates through all input features in the memory record and updates the
	 * weights of FeatureWeight edges connecting to the output feature using the Q-learning algorithm.
	 * 
	 * <p>The Q-learning update formula is: {@code Q_new = Q_old + α * (R + γ * Q_estimated - Q_old)}
	 * where α is the learning rate and γ is the discount factor.
	 * 
	 * <p><b>Preconditions:</b>
	 * <ul>
	 *   <li>{@code memory != null} - memory record must not be null</li>
	 *   <li>{@code memory.getInputFeatureValues() != null} - memory must have input feature values</li>
	 *   <li>{@code memory.getInputFeatureValues()} must not be empty</li>
	 *   <li>{@code output_key != null} - output key must not be null</li>
	 *   <li>{@code observed_feature != null} - observed feature must not be null</li>
	 *   <li>{@code feature_repo != null} - feature repository must be initialized</li>
	 *   <li>{@code feature_weight_repo != null} - feature weight repository must be initialized</li>
	 *   <li>{@code learning_rate > 0} - learning rate must be positive</li>
	 *   <li>{@code discount_factor >= 0 && discount_factor <= 1} - discount factor must be in valid range</li>
	 * </ul>
	 * 
	 * <p><b>Postconditions:</b>
	 * <ul>
	 *   <li>Returns a list of FeatureWeight objects that were updated or created</li>
	 *   <li>All FeatureWeight edges connecting input features to the output feature have been
	 *       updated with new Q-learning calculated weights</li>
	 *   <li>New FeatureWeight edges have been created if connections did not previously exist</li>
	 *   <li>All weight updates have been persisted to the database via {@code feature_weight_repo.save()}</li>
	 *   <li>All input features have been persisted to the database</li>
	 * </ul>
	 * 
	 * <p><b>Side Effects:</b>
	 * <ul>
	 *   <li>Updates FeatureWeight edges in the knowledge graph database</li>
	 *   <li>Creates new FeatureWeight edges if connections do not exist (with random initial weights)</li>
	 *   <li>Persists FeatureWeight and Feature entities to the database</li>
	 *   <li>Logs informational and debug messages about weight updates</li>
	 *   <li>Updates {@code memory.observedFeature} field</li>
	 * </ul>
	 * 
	 * @param memory The memory record containing input features and context
	 * @param output_key The output feature key that the weights connect to
	 * @param observed_feature The observed feature (actual outcome)
	 * @param actual_reward The actual reward received (R in Q-learning formula)
	 * @param estimated_reward The estimated future reward (Q_estimated in Q-learning formula)
	 * @return A list of FeatureWeight objects that were updated or created during Q-learning
	 * @throws NullPointerException if any required parameter is null
	 * @throws IllegalStateException if repositories are not properly initialized
	 */
	public List<FeatureWeight> performQLearning(MemoryRecord memory,
												String output_key,
												Feature observed_feature,
												double actual_reward,
												double estimated_reward){
		QLearn q_learn = new QLearn(learning_rate, discount_factor);
		List<FeatureWeight> features_weights = new ArrayList<FeatureWeight>();
			for(String input_key : memory.getInputFeatureValues()){
				memory.setObservedFeature(observed_feature);
				log.info("input key :: "+input_key);
				log.info("output key :: " + output_key);
				List<Feature> features = feature_repo.getConnectedFeatures(input_key, output_key);
				FeatureWeight feature_weight = null;
				if(features.isEmpty()) {
					Random random = new Random();
					double weight = random.nextDouble();
					feature_weight = feature_repo.createWeightedConnection(input_key, output_key, weight);
					features_weights.add(feature_weight);
				}
				else {
					for(Feature feature : features){
						for(FeatureWeight feature_weight_temp: feature.getFeatureWeights()){
							double q_learn_val = Math.abs(q_learn.calculate(feature_weight_temp.getWeight(), actual_reward, estimated_reward ));
							//updated feature weight with q_learn_val
							feature_weight_temp.setWeight(q_learn_val);
							features_weights.add(feature_weight_temp);
							log.debug("feature ::    " + feature_weight_temp.getInputFeature().getValue() + "  :::   " + feature_weight_temp.getWeight());
							feature_weight_repo.save(feature_weight_temp);
						}
					}
				}
			}
		return features_weights;
	}
	
	/**
	 * Predicts which vocabulary is being used based on the feature list and loads it from the database.
	 * This method uses the vocabulary prediction service to determine the appropriate vocabulary
	 * and then loads the complete vocabulary record from the database.
	 * 
	 * <p><b>Preconditions:</b>
	 * <ul>
	 *   <li>{@code object_list != null} - feature list must not be null</li>
	 *   <li>{@code vocabulary_prediction != null} - vocabulary prediction service must be initialized</li>
	 *   <li>{@code vocab_service != null} - vocabulary service must be initialized</li>
	 * </ul>
	 * 
	 * <p><b>Postconditions:</b>
	 * <ul>
	 *   <li>Returns a Vocabulary object that was loaded from the database</li>
	 *   <li>The returned vocabulary corresponds to the predicted vocabulary label</li>
	 *   <li>If the vocabulary doesn't exist in the database, it will be created</li>
	 * </ul>
	 * 
	 * <p><b>Side Effects:</b>
	 * <ul>
	 *   <li>May create a new vocabulary in the database if it doesn't exist</li>
	 * </ul>
	 * 
	 * @param object_list List of features that are being used to generate the prediction
	 * @return Vocabulary that is being used to generate the prediction, loaded from the database
	 * @throws NullPointerException if {@code object_list} is null or required services are not initialized
	 */
	private Vocabulary predictVocabulary(List<Feature> object_list){
		//1. Predict the vocabulary using the object list
		Vocabulary vocabulary = vocabulary_prediction.predict(object_list);
		//2. Load the vocabulary from the database
		vocabulary = vocab_service.load(vocabulary.getLabel());
		//3. Return the vocabulary
		return vocabulary;
	}
	
	/**
	 * Generates a vocabulary record based on the object list and saves it to the database.
	 * This method creates a new vocabulary containing all features from the object list
	 * and persists it to the database.
	 * 
	 * <p><b>Preconditions:</b>
	 * <ul>
	 *   <li>{@code object_list != null} - feature list must not be null</li>
	 *   <li>{@code input_vocabulary != null} - input vocabulary must not be null (may be used in future)</li>
	 *   <li>{@code output_vocabulary != null} - output vocabulary must not be null (may be used in future)</li>
	 *   <li>{@code vocab_service != null} - vocabulary service must be initialized</li>
	 * </ul>
	 * 
	 * <p><b>Postconditions:</b>
	 * <ul>
	 *   <li>Returns a Vocabulary object containing all features from {@code object_list}</li>
	 *   <li>The vocabulary has been persisted to the database</li>
	 *   <li>All features in {@code object_list} have been added to the vocabulary</li>
	 * </ul>
	 * 
	 * <p><b>Side Effects:</b>
	 * <ul>
	 *   <li>Creates a new vocabulary in the database</li>
	 *   <li>Persists the vocabulary via {@code vocab_service.save()}</li>
	 * </ul>
	 * 
	 * <p><b>Note:</b> This method is marked as TODO for full implementation. Currently creates
	 * a vocabulary from the object list, but may need to incorporate input_vocabulary and
	 * output_vocabulary in future implementations.
	 * 
	 * @param object_list List of features that are being used to generate the record
	 * @param input_vocabulary Input vocabulary (may be used in future implementation)
	 * @param output_vocabulary Output vocabulary (may be used in future implementation)
	 * @return Vocabulary record containing features from the object list
	 * @throws NullPointerException if any required parameter is null
	 * @throws IllegalStateException if vocabulary service is not initialized
	 */
	private Vocabulary generateVocabRecord(List<Feature> object_list, Vocabulary input_vocabulary, Vocabulary output_vocabulary){
		//TODO: Implement this method
		//1. Generate a Vocabulary using the object list and the output vocabulary
		Vocabulary vocabulary = new Vocabulary();
		for(Feature feature : object_list){
			vocabulary.appendToVocabulary(feature);
		}
		//2. Save Vocabulary to the database
		vocab_service.save(vocabulary);
		//3. Return the Vocabulary
		
		return vocabulary;
	}
	
	/**
	 * Loads all vocabularies specified by their labels from the database.
	 * This method retrieves vocabulary records for each provided label, creating new
	 * vocabularies if they don't exist.
	 * 
	 * <p><b>Preconditions:</b>
	 * <ul>
	 *   <li>{@code vocabLabels != null} - vocabulary labels array must not be null</li>
	 *   <li>{@code vocabLabels.length > 0} - at least one vocabulary label must be provided</li>
	 *   <li>All elements in {@code vocabLabels} must not be null</li>
	 *   <li>{@code vocab_service != null} - vocabulary service must be initialized</li>
	 * </ul>
	 * 
	 * <p><b>Postconditions:</b>
	 * <ul>
	 *   <li>Returns an ArrayList of Vocabulary objects, one for each label in {@code vocabLabels}</li>
	 *   <li>The returned list has the same size as {@code vocabLabels.length}</li>
	 *   <li>All vocabularies have been loaded or created in the database</li>
	 *   <li>The order of vocabularies in the returned list corresponds to the order in {@code vocabLabels}</li>
	 * </ul>
	 * 
	 * <p><b>Side Effects:</b>
	 * <ul>
	 *   <li>May create new vocabularies in the database if they don't exist</li>
	 *   <li>Calls {@code vocab_service.load()} for each label</li>
	 * </ul>
	 * 
	 * @param vocabLabels Array of vocabulary labels to load
	 * @return ArrayList of Vocabulary objects corresponding to the provided labels
	 * @throws NullPointerException if {@code vocabLabels} is null or contains null elements
	 * @throws IllegalArgumentException if {@code vocabLabels} is empty
	 * @throws IllegalStateException if vocabulary service is not initialized
	 */
	public ArrayList<Vocabulary> loadVocabularies(String[] vocabLabels){
		ArrayList<Vocabulary> vocabularies = new ArrayList<Vocabulary>();
		for(String label : vocabLabels){
			vocabularies.add(vocab_service.load(label));
		}
		return vocabularies;
	}
	
	/**
	 * Loads action policies for the given object list and vocabulary.
	 * This method is not yet implemented and returns null.
	 * 
	 * <p><b>Preconditions:</b>
	 * <ul>
	 *   <li>{@code object_list != null} - feature list must not be null</li>
	 *   <li>{@code vocabulary != null} - vocabulary must not be null</li>
	 * </ul>
	 * 
	 * <p><b>Postconditions:</b>
	 * <ul>
	 *   <li>Currently always returns null (method not implemented)</li>
	 * </ul>
	 * 
	 * <p><b>Side Effects:</b>
	 * <ul>
	 *   <li>None - method is not yet implemented</li>
	 * </ul>
	 * 
	 * <p><b>Note:</b> This method is a placeholder for future implementation. It should
	 * load action policies (feature sequences) for the given vocabulary context.
	 * 
	 * @param object_list List of features representing the current context
	 * @param vocabulary Vocabulary that defines the action space
	 * @return List of action policy sequences (currently always null)
	 * @throws NullPointerException if any required parameter is null
	 */
	private List<List<Feature>> loadActionPolicies(List<Feature> object_list, Vocabulary vocabulary){
		return null;
		
	}
	
	/**
	 * Performs backpropagation to update weights across the knowledge graph.
	 * This method is not yet implemented.
	 * 
	 * <p><b>Preconditions:</b>
	 * <ul>
	 *   <li>Knowledge graph must be initialized</li>
	 *   <li>Feature weights must be available for backpropagation</li>
	 * </ul>
	 * 
	 * <p><b>Postconditions:</b>
	 * <ul>
	 *   <li>Currently has no effect (method not implemented)</li>
	 * </ul>
	 * 
	 * <p><b>Side Effects:</b>
	 * <ul>
	 *   <li>None - method is not yet implemented</li>
	 * </ul>
	 * 
	 * <p><b>Note:</b> This method is a placeholder for future implementation. It should
	 * propagate error signals backward through the knowledge graph to update weights
	 * based on prediction errors.
	 */
	private void backPropogate(){
		
	}
	
	/**
	 * Saves action policies to persistent storage.
	 * This method is not yet implemented.
	 * 
	 * <p><b>Preconditions:</b>
	 * <ul>
	 *   <li>Action policies must be generated and available</li>
	 *   <li>Storage repository must be initialized</li>
	 * </ul>
	 * 
	 * <p><b>Postconditions:</b>
	 * <ul>
	 *   <li>Currently has no effect (method not implemented)</li>
	 * </ul>
	 * 
	 * <p><b>Side Effects:</b>
	 * <ul>
	 *   <li>None - method is not yet implemented</li>
	 * </ul>
	 * 
	 * <p><b>Note:</b> This method is a placeholder for future implementation. It should
	 * persist action policies (learned feature sequences) to the database or storage system.
	 */
	private void saveActionPolicies(){
		
	}
	
	/**
	 * Generates a raw policy matrix that maps input features to output features based on
	 * FeatureWeight relationships in the knowledge graph. This is a vocabulary-agnostic policy
	 * matrix. If FeatureWeight connections don't exist, they are created with random initial weights.
	 * 
	 * <p>The policy matrix {@code policy[i][j]} represents the weight from input feature {@code i}
	 * to output feature {@code j}. Values are taken from existing FeatureWeight edges or randomly
	 * initialized if connections don't exist.
	 * 
	 * <p><b>Preconditions:</b>
	 * <ul>
	 *   <li>{@code input_features != null} - input feature list must not be null</li>
	 *   <li>{@code output_features != null} - output feature list must not be null</li>
	 *   <li>{@code input_features.size() > 0} - at least one input feature must be provided</li>
	 *   <li>{@code output_features.size() > 0} - at least one output feature must be provided</li>
	 *   <li>All features in both lists must have non-null {@code value} properties</li>
	 *   <li>{@code feature_repo != null} - feature repository must be initialized</li>
	 * </ul>
	 * 
	 * <p><b>Postconditions:</b>
	 * <ul>
	 *   <li>Returns a 2D array with dimensions {@code [input_features.size()][output_features.size()]}</li>
	 *   <li>Each row {@code i} corresponds to {@code input_features.get(i)}</li>
	 *   <li>Each column {@code j} corresponds to {@code output_features.get(j)}</li>
	 *   <li>{@code result[i][j]} contains the weight for transitioning from input feature {@code i}
	 *       to output feature {@code j}, or a random value if no connection existed</li>
	 *   <li>All FeatureWeight edges (existing or newly created) have been persisted to the database</li>
	 *   <li>All input features have been persisted to the database</li>
	 * </ul>
	 * 
	 * <p><b>Side Effects:</b>
	 * <ul>
	 *   <li>Creates new FeatureWeight edges if connections don't exist (with random initial weights)</li>
	 *   <li>Persists FeatureWeight and Feature entities to the database</li>
	 *   <li>Logs informational messages about policy matrix generation</li>
	 * </ul>
	 * 
	 * @param input_features List of input features representing the current state/observations
	 * @param output_features List of output features representing possible actions/outcomes
	 * @return A 2D policy matrix where {@code result[i][j]} represents the weight from input
	 *         feature {@code i} to output feature {@code j}
	 * @throws NullPointerException if any required parameter is null or contains null features
	 * @throws IllegalArgumentException if either list is empty
	 * @throws IllegalStateException if feature repository is not initialized
	 */
	public double[][] generateRawPolicy(List<Feature> input_features, List<Feature> output_features){
		// 1. Create a memory record for prediction
		Random random = new Random();
		double[][] policy = new double[input_features.size()][output_features.size()];
		log.info("###################################################################");
		log.info("input features size :: "+input_features.size());
		log.info("output features size :: "+output_features.size());
		
		for(int in_idx = 0; in_idx < input_features.size(); in_idx++){
			for(int out_idx = 0; out_idx < output_features.size(); out_idx++){
				List<Feature> features = feature_repo.getConnectedFeatures(input_features.get(in_idx).getValue(), output_features.get(out_idx).getValue());
				double weight = -1.0;

				if(!features.isEmpty()){
					for(FeatureWeight feature_weight : features.get(0).getFeatureWeights()){	
						if(feature_weight.getResultFeature().equals(output_features.get(out_idx))){
							weight = feature_weight.getWeight();
						}
					}
				}
				else{
					weight = random.nextDouble();
					FeatureWeight feature_weight = new FeatureWeight();
					feature_weight.setInputFeature(input_features.get(in_idx));
					feature_weight.setResultFeature(output_features.get(out_idx));
					feature_weight.setWeight(weight);
					Feature input_feature = input_features.get(in_idx);
					Feature input_feature_record = feature_repo.findByValue(input_feature.getValue());
					if(input_feature_record != null){
						input_feature = input_feature_record;
					}
					
					input_feature.getFeatureWeights().add(feature_weight);
					feature_repo.save(input_feature);
				}
				
				policy[in_idx][out_idx] = weight;
			}
		}
		log.info("###################################################################");

		
		return policy;
	}

	/**
	 * Generates a vocabulary-specific policy matrix that maps input features to output features
	 * based on FeatureWeight relationships in the knowledge graph. Weights are retrieved from
	 * the vocabulary-specific weight map in FeatureWeight edges. If connections don't exist,
	 * they are created with random initial weights and assigned to the vocabulary.
	 * 
	 * <p>The policy matrix {@code policy[i][j]} represents the vocabulary-specific weight from
	 * input feature {@code i} to output feature {@code j}. Values are taken from the
	 * vocabulary-specific weights stored in FeatureWeight edges.
	 * 
	 * <p><b>Preconditions:</b>
	 * <ul>
	 *   <li>{@code input_features != null} - input feature list must not be null</li>
	 *   <li>{@code output_features != null} - output feature list must not be null</li>
	 *   <li>{@code vocabulary_label != null} - vocabulary label must not be null</li>
	 *   <li>{@code !vocabulary_label.isEmpty()} - vocabulary label must not be empty</li>
	 *   <li>{@code input_features.size() > 0} - at least one input feature must be provided</li>
	 *   <li>{@code output_features.size() > 0} - at least one output feature must be provided</li>
	 *   <li>All features in both lists must have non-null {@code value} properties</li>
	 *   <li>{@code feature_repo != null} - feature repository must be initialized</li>
	 * </ul>
	 * 
	 * <p><b>Postconditions:</b>
	 * <ul>
	 *   <li>Returns a 2D array with dimensions {@code [input_features.size()][output_features.size()]}</li>
	 *   <li>Each row {@code i} corresponds to {@code input_features.get(i)}</li>
	 *   <li>Each column {@code j} corresponds to {@code output_features.get(j)}</li>
	 *   <li>{@code result[i][j]} contains the vocabulary-specific weight for transitioning from
	 *       input feature {@code i} to output feature {@code j}, or a random value if no connection existed</li>
	 *   <li>All FeatureWeight edges (existing or newly created) have vocabulary weights assigned
	 *       and have been persisted to the database</li>
	 *   <li>All input features have been persisted to the database</li>
	 * </ul>
	 * 
	 * <p><b>Side Effects:</b>
	 * <ul>
	 *   <li>Creates new FeatureWeight edges if connections don't exist (with random initial weights)</li>
	 *   <li>Assigns vocabulary-specific weights to FeatureWeight edges</li>
	 *   <li>Persists FeatureWeight and Feature entities to the database</li>
	 *   <li>Logs informational messages about policy matrix generation</li>
	 * </ul>
	 * 
	 * @param input_features List of input features representing the current state/observations
	 * @param output_features List of output features representing possible actions/outcomes
	 * @param vocabulary_label The label of the vocabulary for which to generate the policy matrix
	 * @return A 2D policy matrix where {@code result[i][j]} represents the vocabulary-specific
	 *         weight from input feature {@code i} to output feature {@code j}
	 * @throws NullPointerException if any required parameter is null or contains null features
	 * @throws IllegalArgumentException if either list is empty or vocabulary_label is empty
	 * @throws IllegalStateException if feature repository is not initialized
	 */
	public double[][] generateVocabularyPolicy(List<Feature> input_features, List<Feature> output_features, String vocabulary_label){
		// 1. Create a memory record for prediction
		Random random = new Random();
		double[][] policy = new double[input_features.size()][output_features.size()];
		log.info("###################################################################");
		log.info("input features size :: "+input_features.size());
		log.info("output features size :: "+output_features.size());
		
		for(int in_idx = 0; in_idx < input_features.size(); in_idx++){
			Feature input_feature = input_features.get(in_idx);
			for(int out_idx = 0; out_idx < output_features.size(); out_idx++){
				Feature output_feature = output_features.get(out_idx);
				List<Feature> features = feature_repo.getConnectedFeatures(input_feature.getValue(), output_feature.getValue());
				double weight = -1.0;

				if(!features.isEmpty()){
					for(FeatureWeight feature_weight : features.get(0).getFeatureWeights()){
						if(feature_weight.getResultFeature().equals(output_features.get(out_idx))){
							weight = feature_weight.getVocabularyWeights().get(vocabulary_label);
						}
					}
				}
				else{
					weight = random.nextDouble();
					FeatureWeight feature_weight = new FeatureWeight();
					feature_weight.setResultFeature(output_feature);
					feature_weight.setWeight(weight);
					feature_weight.setVocabularyWeight(vocabulary_label, weight);
					feature_weight.setInputFeature(input_feature);
					Feature input_feature_record = feature_repo.findByValue(input_feature.getValue());
					if(input_feature_record != null){
						input_feature = input_feature_record;
					}
					
					input_feature.getFeatureWeights().add(feature_weight);
					feature_repo.save(input_feature);
				}
				
				policy[in_idx][out_idx] = weight;
			}
		}
		log.info("###################################################################");

		
		return policy;
	}

	/**
	 * Trains the model using reinforcement learning on a list of features.
	 * This method initiates the learning process by creating a vocabulary from the feature list
	 * and preparing state representations for training. Currently uses a hardcoded vocabulary label.
	 * 
	 * <p><b>Preconditions:</b>
	 * <ul>
	 *   <li>{@code feature_list != null} - feature list must not be null</li>
	 *   <li>{@code label != null} - label must not be null</li>
	 *   <li>{@code feature_list.size() > 0} - at least one feature must be provided</li>
	 *   <li>All features in {@code feature_list} must have non-null {@code value} properties</li>
	 * </ul>
	 * 
	 * <p><b>Postconditions:</b>
	 * <ul>
	 *   <li>A vocabulary has been created containing all features from {@code feature_list}</li>
	 *   <li>State representations have been generated for training purposes</li>
	 *   <li>The vocabulary contains all features from the input list</li>
	 * </ul>
	 * 
	 * <p><b>Side Effects:</b>
	 * <ul>
	 *   <li>Creates a new Vocabulary object with hardcoded label "internet"</li>
	 *   <li>Logs informational messages about learning initiation and vocabulary creation</li>
	 *   <li>Prepares state vectors for each vocabulary feature</li>
	 * </ul>
	 * 
	 * <p><b>Note:</b> This method is currently a partial implementation. The vocabulary label is
	 * hardcoded as "internet" and state preparation is incomplete. Full training loop
	 * (Q-learning updates, backpropagation) is not yet implemented.
	 * 
	 * @param feature_list List of features to use for training
	 * @param label Training label/context (currently not fully utilized)
	 * @throws NullPointerException if {@code feature_list} is null or contains null features
	 * @throws IllegalArgumentException if {@code feature_list} is empty
	 */
	public void train(List<Feature> feature_list, String label) {
		//REINFORCEMENT LEARNING
		log.info( " Initiating learning");
		
		//learning model
		// 1. identify vocabulary (NOTE: This is currently hard coded since we only currently care about 1 context)
		Vocabulary vocabulary = new Vocabulary(new ArrayList<Feature>(), "internet");

		log.info("object definition list size :: "+feature_list.size());
		// 2. create record based on vocabulary
		for(Feature feature : feature_list){
			vocabulary.appendToVocabulary(feature);
		}
		
		log.info("vocabulary :: "+vocabulary);
		log.info("vocab value list size   :: "+vocabulary.getFeatures().size());
		// 2. create state vertex from vocabulary 
		int idx = 0;
		for(Feature vocab_feature : vocabulary.getFeatures()){
			boolean[] state = new boolean[vocabulary.getFeatures().size()];
			if(feature_list.contains(vocab_feature)){
				state[idx] = true;
			}
			else{
				state[idx] = false;
			}
			idx++;
		}
	}
	
	/**
	 * Predicts the next feature in a sequence given context features using policy matrix generation
	 * and probability distribution. This method is used for sequential text generation by selecting
	 * the candidate feature with the highest predicted probability.
	 * 
	 * <p><b>Preconditions:</b>
	 * <ul>
	 *   <li>{@code context_features != null} - context feature list must not be null</li>
	 *   <li>{@code candidate_features != null} - candidate feature list must not be null</li>
	 *   <li>{@code !context_features.isEmpty()} - at least one context feature must be provided</li>
	 *   <li>{@code !candidate_features.isEmpty()} - at least one candidate feature must be provided</li>
	 *   <li>All features in both lists must have non-null {@code value} properties</li>
	 *   <li>{@code feature_repo != null} - feature repository must be initialized</li>
	 * </ul>
	 * 
	 * <p><b>Postconditions:</b>
	 * <ul>
	 *   <li>Returns the candidate feature with the highest predicted probability, or null if
	 *       either list is empty</li>
	 *   <li>The returned feature is from the {@code candidate_features} list</li>
	 *   <li>The prediction is based on the policy matrix generated from context features</li>
	 * </ul>
	 * 
	 * <p><b>Side Effects:</b>
	 * <ul>
	 *   <li>May create new FeatureWeight edges if connections don't exist</li>
	 *   <li>Persists FeatureWeight and Feature entities to the database (via generateRawPolicy)</li>
	 *   <li>Logs informational messages about policy matrix generation (via generateRawPolicy)</li>
	 * </ul>
	 * 
	 * @param context_features List of features representing current context/sequence
	 * @param candidate_features List of candidate features to predict from
	 * @return Feature with highest predicted probability, or null if lists are empty
	 * @throws NullPointerException if any required parameter is null or contains null features
	 * @throws IllegalArgumentException if either list is empty
	 * @throws IllegalStateException if feature repository is not initialized
	 */
	public Feature predictNextFeature(List<Feature> context_features, List<Feature> candidate_features) {
		if(context_features.isEmpty() || candidate_features.isEmpty()) {
			return null;
		}
		
		double[][] policy = generateRawPolicy(context_features, candidate_features);
		double[] prediction = predict(policy);
		
		// Find feature with highest probability
		int max_idx = 0;
		double max_prob = prediction[0];
		for(int i = 1; i < prediction.length; i++) {
			if(prediction[i] > max_prob) {
				max_prob = prediction[i];
				max_idx = i;
			}
		}
		
		return candidate_features.get(max_idx);
	}
	
	/**
	 * Predicts the probability distribution over candidate features given context features.
	 * This method generates a policy matrix and returns the normalized probability distribution
	 * for sequential text generation with sampling capabilities.
	 * 
	 * <p><b>Preconditions:</b>
	 * <ul>
	 *   <li>{@code context_features != null} - context feature list must not be null</li>
	 *   <li>{@code candidate_features != null} - candidate feature list must not be null</li>
	 *   <li>{@code !context_features.isEmpty()} - at least one context feature must be provided</li>
	 *   <li>{@code !candidate_features.isEmpty()} - at least one candidate feature must be provided</li>
	 *   <li>All features in both lists must have non-null {@code value} properties</li>
	 *   <li>{@code feature_repo != null} - feature repository must be initialized</li>
	 * </ul>
	 * 
	 * <p><b>Postconditions:</b>
	 * <ul>
	 *   <li>Returns a normalized probability distribution array with length equal to
	 *       {@code candidate_features.size()}</li>
	 *   <li>All values in the returned array are non-negative</li>
	 *   <li>The sum of all values in the returned array equals 1.0 (normalized)</li>
	 *   <li>{@code result[i]} represents the probability of {@code candidate_features.get(i)}</li>
	 *   <li>Returns an empty array if either input list is empty</li>
	 * </ul>
	 * 
	 * <p><b>Side Effects:</b>
	 * <ul>
	 *   <li>May create new FeatureWeight edges if connections don't exist</li>
	 *   <li>Persists FeatureWeight and Feature entities to the database (via generateRawPolicy)</li>
	 *   <li>Logs informational messages about policy matrix generation (via generateRawPolicy)</li>
	 * </ul>
	 * 
	 * @param context_features List of features representing current context/sequence
	 * @param candidate_features List of candidate features to predict from
	 * @return Array of probabilities corresponding to candidate_features, or empty array if lists are empty
	 * @throws NullPointerException if any required parameter is null or contains null features
	 * @throws IllegalArgumentException if either list is empty
	 * @throws IllegalStateException if feature repository is not initialized
	 */
	public double[] predictNextFeatureDistribution(List<Feature> context_features, List<Feature> candidate_features) {
		if(context_features.isEmpty() || candidate_features.isEmpty()) {
			return new double[0];
		}
		
		double[][] policy = generateRawPolicy(context_features, candidate_features);
		return predict(policy);
	}
	
	/**
	 * Retrieves all features connected to a given feature within a specified number of hops
	 * in the knowledge graph. This method performs recursive graph traversal starting from
	 * the given feature and collecting all features reachable within the hop limit.
	 * Used by GraphReasoningEngine for multi-hop traversal.
	 * 
	 * <p><b>Preconditions:</b>
	 * <ul>
	 *   <li>{@code feature != null} - starting feature must not be null</li>
	 *   <li>{@code feature.getValue() != null} - feature must have a non-null value</li>
	 *   <li>{@code max_hops > 0} - maximum hops must be positive</li>
	 *   <li>{@code feature_repo != null} - feature repository must be initialized</li>
	 * </ul>
	 * 
	 * <p><b>Postconditions:</b>
	 * <ul>
	 *   <li>Returns a list of unique features reachable from the starting feature within
	 *       {@code max_hops} hops</li>
	 *   <li>The returned list does not contain duplicates</li>
	 *   <li>The returned list does not include the starting feature itself</li>
	 *   <li>All features in the returned list are reachable via FeatureWeight edges</li>
	 *   <li>Returns an empty list if {@code feature} is null or {@code max_hops <= 0}</li>
	 *   <li>Returns an empty list if the feature is not found in the database</li>
	 * </ul>
	 * 
	 * <p><b>Side Effects:</b>
	 * <ul>
	 *   <li>Queries the feature repository for connected features</li>
	 *   <li>Performs recursive database queries for multi-hop traversal</li>
	 * </ul>
	 * 
	 * @param feature Starting feature for graph traversal
	 * @param max_hops Maximum number of hops (edges) to traverse from the starting feature
	 * @return List of unique features connected to the starting feature within max_hops,
	 *         or empty list if feature is null, max_hops <= 0, or feature not found
	 * @throws NullPointerException if {@code feature} is null or has null value
	 * @throws IllegalArgumentException if {@code max_hops <= 0}
	 * @throws IllegalStateException if feature repository is not initialized
	 */
	public List<Feature> getConnectedFeatures(Feature feature, int max_hops) {
		List<Feature> connected = new ArrayList<>();
		if(feature == null || max_hops <= 0) {
			return connected;
		}
		
		Feature feature_record = feature_repo.findByValue(feature.getValue());
		if(feature_record != null) {
			// Get directly connected features
			for(FeatureWeight weight : feature_record.getFeatureWeights()) {
				if(!connected.contains(weight.getResultFeature())) {
					connected.add(weight.getResultFeature());
				}
			}
			
			// Recursively get features at deeper hops
			if(max_hops > 1) {
				List<Feature> first_hop = new ArrayList<>(connected);
				for(Feature connected_feature : first_hop) {
					List<Feature> deeper = getConnectedFeatures(connected_feature, max_hops - 1);
					for(Feature deep_feature : deeper) {
						if(!connected.contains(deep_feature)) {
							connected.add(deep_feature);
						}
					}
				}
			}
		}
		
		return connected;
	}

}