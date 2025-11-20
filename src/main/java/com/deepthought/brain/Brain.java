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
	 * Reads path and performs learning tasks
	 * 
	 * 
		//Learning process
		//	1. identify vocabulary that this set of object info belongs to
		//  2. Create record based on vocabulary
		//  3. load known vocabulary action policies
		//  4. perform matrix math to inline update vocabulary policies for record based on reward/penalty for productivity  
	 * 
	 * @param path an {@link ArrayList} of graph vertex indices. Order matters
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws NullPointerException
	 * @throws IOException 
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
	 * Calculates the reward for the given output key and actual feature
	 * 
	 * @param memory {@link MemoryRecord} object containing the memory record
	 * @param output_key {@link String} object containing the output key
	 * @param actual_feature {@link Feature} object containing the actual feature
	 * @return {@link double} object containing the reward
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
	 * Performs Q-learning to update the feature weights based on the observed feature and the
	 * contents of the memory record
	 *
	 * @param memory {@link MemoryRecord} object containing the memory record
	 * @param output_key {@link String} object containing the output key
	 * @param observed_feature {@link Feature} object containing the observed feature
	 * @param actual_reward {@link double} object containing the actual reward
	 * @param estimated_reward {@link double} object containing the estimated reward
	 * @return {@link List} of {@link FeatureWeight} objects containing the updated feature weights
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
	 * Generate a prediction for which vocabulary is being used based on the object list
	 * 
	 * @param object_list List of features that are being used to generate the prediction
	 * @return Vocabulary that is being used to generate the prediction
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
	 * Generate a record for the vocabulary based on the object list
	 * 
	 * @param object_list List of features that are being used to generate the record
	 * @param vocabulary Vocabulary that is being used to generate the record
	 * @return Record for the vocabulary
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
	 * Retrieves all {@linkplain Vocabulary vocabularies} that are required by the agent
	 * 
	 * @param vocabLabels
	 * @return
	 */
	public ArrayList<Vocabulary> loadVocabularies(String[] vocabLabels){
		ArrayList<Vocabulary> vocabularies = new ArrayList<Vocabulary>();
		for(String label : vocabLabels){
			vocabularies.add(vocab_service.load(label));
		}
		return vocabularies;
	}
	
	/**
	 * 
	 * @param object_list
	 * @param vocabulary
	 * @return
	 */
	private List<List<Feature>> loadActionPolicies(List<Feature> object_list, Vocabulary vocabulary){
		return null;
		
	}
	
	/**
	 * 
	 */
	private void backPropogate(){
		
	}
	
	/**
	 * 
	 */
	private void saveActionPolicies(){
		
	}
	
	public double[][] generatePolicy(List<Feature> input_features, List<Feature> output_features){
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
							weight = feature_weight.getVocabularyWeights().get(vocabulary.getLabel());
						}
					}
				}
				else{
					weight = random.nextDouble();
					FeatureWeight feature_weight = new FeatureWeight();
					feature_weight.setResultFeature(output_features.get(out_idx));
					feature_weight.setWeight(weight);
					feature_weight.setVocabularyWeights(vocabulary.getLabel(), weight);
					feature_weight.setInputFeature(input_features.get(in_idx));
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
	 * Predicts the next feature in a sequence given context features
	 * Used for sequential text generation
	 * 
	 * @param context_features List of features representing current context
	 * @param candidate_features List of candidate features to predict from
	 * @return Feature with highest probability
	 */
	public Feature predictNextFeature(List<Feature> context_features, List<Feature> candidate_features) {
		if(context_features.isEmpty() || candidate_features.isEmpty()) {
			return null;
		}
		
		double[][] policy = generatePolicy(context_features, candidate_features);
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
	 * Predicts next feature with probability distribution
	 * 
	 * @param context_features List of features representing current context
	 * @param candidate_features List of candidate features to predict from
	 * @return Array of probabilities corresponding to candidate_features
	 */
	public double[] predictNextFeatureDistribution(List<Feature> context_features, List<Feature> candidate_features) {
		if(context_features.isEmpty() || candidate_features.isEmpty()) {
			return new double[0];
		}
		
		double[][] policy = generatePolicy(context_features, candidate_features);
		return predict(policy);
	}
	
	/**
	 * Retrieves features connected to given feature within specified hops
	 * Used by GraphReasoningEngine for multi-hop traversal
	 * 
	 * @param feature Starting feature
	 * @param max_hops Maximum number of hops to traverse
	 * @return List of connected features with their cumulative weights
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
