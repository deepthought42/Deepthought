package com.deepthought.brain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.deepthought.models.Feature;
import com.deepthought.models.MemoryRecord;
import com.deepthought.models.Vocabulary;
import com.deepthought.models.edges.FeatureWeight;
import com.deepthought.models.repository.FeatureRepository;
import com.deepthought.models.repository.FeatureWeightRepository;
import com.deepthought.models.repository.MemoryRecordRepository;

import edu.stanford.nlp.util.ArrayUtils;

/**
 * Provides ability to predict and learn from data
 * 
 */
@Component
public class Brain {
	private static Logger log = LoggerFactory.getLogger(Brain.class);

	@Autowired
	private FeatureRepository feature_repo;
	
	@Autowired
	private FeatureWeightRepository feature_weight_repo;
	
	@Autowired
	private MemoryRecordRepository memory_repo;
	
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
		
		// 3. determine reward/regret score based on productivity status
		//Q-LEARNING VARIABLES
		final double learning_rate = .1;
		final double discount_factor = .1;

		//set estimated reward using prediction from memory.

		//replace with steps to estimate reward for an output feature independent of actual desired output feature
		double estimated_reward = 1.0;
		
		// 3. determine reward/regret score based on productivity status
		double actual_reward = 0.0;		
		QLearn q_learn = new QLearn(learning_rate, discount_factor);
		for(String output_key : memory.getOutputFeatureKeys()){
			
			//if predicted feature is equal to output feature and actual feature is equal to predicted feature  OR output key equals actual feature key
			if(output_key.equals(actual_feature.getValue()) && actual_feature.getValue().equals(memory.getPredictedFeature().getValue())){
				log.debug("REWARD   ::    2");
				actual_reward = 2.0;
			}
			else if(output_key.equals(actual_feature.getValue())){
				log.debug("REWARD   ::   1");
				actual_reward = 1.0;
			}
			//if output isn't equal to the actual feature or the predicted feature, don't affect weights
			else if(output_key.equals(memory.getPredictedFeature().getValue()) && !output_key.equals(actual_feature.getValue())){
				log.debug("REWARD   ::     -2");
				actual_reward = -1.0;
			}
			else if(!output_key.equals(actual_feature.getValue())) {
				log.debug("REWARD   ::     -1");
				actual_reward = -2.0;
			}
			else {
				log.debug("REWARD   ::    0");
				//nothing changed so there was no reward for that combination. We want to remember this in the future
				// so we set it to a negative value to simulate regret
				actual_reward = 0.0;
			}
			
			List<FeatureWeight> features_weights = new ArrayList<FeatureWeight>();
			for(String input_key : memory.getInputFeatureValues()){
				memory.setDesiredFeature(actual_feature);
				log.info("input key :: "+input_key);
				log.info("output key :: " + output_key);
				List<Feature> features = feature_repo.getConnectedFeatures(input_key, output_key);
				FeatureWeight feature_weight = null;
				if(features.isEmpty()) {
					Random random = new Random();
					double weight = random.nextDouble();
					
					feature_weight = feature_repo.createWeightedConnection(input_key, output_key, weight);
				}
				else {
					feature_weight = features.get(0).getFeatureWeights().get(0);
				}
				double q_learn_val = Math.abs(q_learn.calculate(feature_weight.getWeight(), actual_reward, estimated_reward ));
				//updated feature weight with q_learn_val
				feature_weight.setWeight(q_learn_val);
				features_weights.add(feature_weight);
				log.debug("feature ::    " + feature_weight.getFeature().getValue() + "  :::   " + feature_weight.getWeight());
				feature_weight_repo.save(feature_weight);
			}
		}		
	}
	
	/**
	 * 
	 * @param object_list
	 * @return
	 */
	private Vocabulary predictVocabulary(List<Feature> object_list){
		return null;
	}
	
	/**
	 * 
	 * @param object_list
	 * @param vocabulary
	 * @return
	 */
	private List<Feature> generateVocabRecord(List<Feature> object_list, Vocabulary vocabulary){
		return object_list;
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
			//vocabularies.add(Vocabulary.load(label));
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
						if(feature_weight.getEndFeature().equals(output_features.get(out_idx))){
							weight = feature_weight.getWeight();
						}
					}
				}
				else{
					weight = random.nextDouble();
					FeatureWeight feature_weight = new FeatureWeight();
					feature_weight.setEndFeature(output_features.get(out_idx));
					feature_weight.setWeight(weight);
					feature_weight.setFeature(input_features.get(in_idx));
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

}
