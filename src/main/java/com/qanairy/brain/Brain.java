package com.qanairy.brain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
		
		//REINFORCEMENT LEARNING
		System.err.println( " Initiating learning");
		
		//Load memory from database
		Optional<MemoryRecord> memory_record = memory_repo.findById(memory_id);
		
		// 2a. load known action policies/probabilities for each object definition in the definition list
		MemoryRecord memory = memory_record.get();
		
		// 3. determine reward/regret score based on productivity status
		//Q-LEARNING VARIABLES
		final double learning_rate = .08;
		final double discount_factor = .08;

		//set estimated reward using prediction from memory.
		// if actual feature is the most likely feature then set to 1
		int pred_idx = 0;
		double largest_pred = 0.0;
		String most_likely_feature_value = "";
		for(String prediction_feature_value: memory.getOutputFeatureKeys()){
			if(prediction_feature_value.equals(actual_feature.getValue())){
				if(largest_pred < memory.getPrediction()[pred_idx]){
					largest_pred = memory.getPrediction()[pred_idx];
					most_likely_feature_value = prediction_feature_value;
				}
			}
			pred_idx++;
		}
		
		double estimated_reward = 0.0;
		
		if(most_likely_feature_value.equals(actual_feature.getValue())){
			estimated_reward = 1.0;
		}
		else{
			estimated_reward = -1.0;
		}
		
		// 3. determine reward/regret score based on productivity status
		double actual_reward = 0.0;
		
		
		
		
		System.err.println("Set reward :: "+actual_reward);
		
		QLearn q_learn = new QLearn(learning_rate, discount_factor);
		
		for(String input_key : memory.getInputFeatureValues()){
			for(String output_key : memory.getOutputFeatureKeys()){
				memory.getRewardedFeature();
				//if actual feature is equal to output feature and actual feature is equal to predicted feature  OR output key equals actual feature key
				if(output_key.equals(memory.getPredictedFeature().getValue()) && actual_feature.equals(memory.getPrediction()) ||
						output_key.equals(actual_feature.getValue())){
					actual_reward = 5.0;
				}
				else{
					//nothing changed so there was no reward for that combination. We want to remember this in the future
					// so we set it to a negative value to simulate regret
					actual_reward = -1.0;
				}
				List<Feature> features = feature_repo.getConnectedFeatures(input_key, output_key);
				System.err.println("features size :: "+features.size());
				System.err.println("feature weights size :: "+features.get(0).getFeatureWeights().size());
				FeatureWeight feature_weight = features.get(0).getFeatureWeights().get(0);
				double q_learn_val = q_learn.calculate(feature_weight.getWeight(), actual_reward, estimated_reward );
				//updated feature weight with q_learn_val
				feature_weight.setWeight(q_learn_val);
				
				System.err.println(" -> ADDED LAST ACTION TO ACTION MAP :: "+actual_feature.getValue()+"...Q LEARN VAL : "+q_learn_val);
				System.err.println("feature weight :: "+feature_weight.getWeight());
				
				feature_weight_repo.save(feature_weight);
				
				/*
				System.err.println("Object definition :: "+actual_feature);
				System.err.println("Object definition value :: "+actual_feature.getValue());
				
				feature_repo.save(actual_feature);
				*/
			}
		}
		
		
		
				
		
		//Learning process
		//	1. identify vocabulary that this set of object info belongs to
		//  2. Create record based on vocabulary
		//  3. load known vocabulary action policies
		//  4. perform matrix math to inline update vocabulary policies for record based on reward/penalty for productivity  
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
	
	@Deprecated
	public static synchronized void registerPath(Object path){
		/**
		 * THIS NEXT BLOCK IS A WORK IN PROGRESS THAT WILL NOT BE ADDED UNTIL AFTER THE PROTOTYPE IS COMPLETE
		 * 
		 * THIS NEEDS TO BE MOVED TO A DIFFERENT ACTOR FOR MACHINE LEARNING. THIS METHOD IS NOT LONGER USED
		 * 
		 * !!!!!!!!!!!!!!!     DO NOT DELETE           !!!
		 */
		/*
		for(PathObject<?> pathObj : path.getPath()){
		// generate vocabulary matrix using pathObject
			//decompose path obj
			DataDecomposer decomposer = new DataDecomposer();
			
			List<Feature> featureinitionList = null;
			try {
				featureinitionList = decomposer.decompose(pathObj.getData());
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullPointerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			//Load list for vocabublary, initializing all entries to 0
			ArrayList<Boolean> vocabularyExperienceRecord = new ArrayList<Boolean>(Arrays.asList(new Boolean[this.vocab.getValueList().size()]));
			Collections.fill(vocabularyExperienceRecord, Boolean.FALSE);
			
			//Find last action
			String last_action = "";

			//Get last action experienced in path
			for(int idx = path.getPath().size()-1; idx >= 0 ; idx--){
				if(path.getPath().get(idx).getData() instanceof Action){
					last_action = ((Action)path.getPath().get(idx).getData()).getName();
					break;
				}
			}
			
			//load vocabulary weights
			VocabularyWeights vocab_weights = VocabularyWeights.load("html_actions");
			
			//Run through object definitions and make sure that they are all included in the vocabulary
			Random rand = new Random();
			for(Feature feature : featureinitionList){
				if( !this.vocab.getValueList().contains(feature.getValue())){
					//then add object value to end of vocabulary
					this.vocab.appendToVocabulary(feature.getValue());
					
					//add a new 0.0 value weight to end of weights
<<<<<<< HEAD
					//this.vocab.appendToWeights(feature.getValue(), rand.nextFloat());
					
					//add new actions entry to match vocab
					ArrayList<Float> end_feature_weights = new ArrayList<Float>(Arrays.asList(new Float[ActionFactory.getActions().length]));
					
					
					//for(int weight_idx = 0 ; weight_idx < end_feature_weights.size(); weight_idx++){
						//System.err.println("SETTING ACTION WIGHT : "+rand.nextFloat());
					//	end_feature_weights.set(weight_idx, rand.nextFloat());
					//}
=======
					vocab_weights.appendToVocabulary(feature.getValue());

>>>>>>> f8550e37a7b03a9e5d435acb6d8ce040379bea09
					//add weights to vocabulary weights;
					for(String action : ActionFactory.getActions()){
						vocab_weights.appendToWeights(feature.getValue(), action, rand.nextFloat()); 
					}
					
					//end_feature_weights_list.add(end_feature_weights);
					vocabularyExperienceRecord.add(true);

				}
			}
			

			//PERSIST VOCABULARY
			this.vocab.save();
			vocab_weights.save();
			
			//Create experience record and weight record
			Boolean[][] experience_record = new Boolean[this.vocab.getValueList().size()][ActionFactory.getActions().length];
			double[][] weight_record = new double[this.vocab.getValueList().size()][ActionFactory.getActions().length];
			
			for(Feature feature : featureinitionList){
				int value_idx = this.vocab.getValueList().indexOf(feature.getValue() );
				for(int i=0; i < ActionFactory.getActions().length; i++){
					if(ActionFactory.getActions()[i].equals(last_action)){
						vocabularyExperienceRecord.set(value_idx, Boolean.TRUE);
						experience_record[value_idx][i] = Boolean.TRUE;
					}
					else{
						experience_record[value_idx][i] = Boolean.FALSE;
					}
					
					//place weights into 2 dimensional array for NN computations later
					weight_record[value_idx][i] = vocab_weights.getVocabulary_weights().get(feature.getValue()).get(ActionFactory.getActions()[i]);
				}
			}
			
			// home rolled NN single layer FOLLOWS
			
			//unroll vocabulary weights
			
			//run vocabularyExperienceRecord with loaded weights through NN
			Sigmoid sigmoid = new Sigmoid();
			
			//perform reinforcement learning based on last action taken and result against predictions
			
			// apply reinforcement learning
			// based on result of crawl and predicted values, updated predicted values
			String[] actions = ActionFactory.getActions();
			float[] predictions = new float[actions.length];
			for(int action_index = 0; action_index < predictions.length; action_index++){
				if(actions[action_index].equals(last_action) && isValuable.equals(Boolean.TRUE)){
					predictions[action_index] += 1;
				}
				else if(actions[action_index].equals(last_action) && isValuable.equals(Boolean.FALSE)){
					predictions[action_index] -= 1;
				}
			}
		
			// Backpropagate updated results back through layers
		}
		
		 */
		
	}
	
	/**
	 * Calculate all estimated element probabilities
	 * 
	 * @param page
	 * @param element_probabilities
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public ArrayList<HashMap<String, Double>> getEstimatedElementProbabilities(ArrayList<Feature> pageElements) 
			throws IllegalArgumentException, IllegalAccessException
	{
		/*
		ArrayList<HashMap<String, Double>> element_action_map_list = new ArrayList<HashMap<String, Double>>(0);
				
		for(PageElement elem : pageElements){
			HashMap<String, Double> full_action_map = new HashMap<String, Double>(0);
			//find vertex for given element
			List<Object> raw_features = DataDecomposer.decompose(elem);
			List<com.tinkerpop.blueprints.Vertex> feature_list
				= persistor.findAll(raw_features);
					
			//iterate over set to get all actions for object definition list
			for(com.tinkerpop.blueprints.Vertex v : feature_list){
				HashMap<String, Double> action_map = v.getProperty("actions");
				if(action_map != null && !action_map.isEmpty()){
					for(String action : action_map.keySet()){
						if(!full_action_map.containsKey(action)){
							//If it doesn't yet exist, then seed it with a random variable
							full_action_map.put(action, rand.nextDouble());
						}	
						else{
							double action_sum = full_action_map.get(action) + action_map.get(action);
							full_action_map.put(action, action_sum);
						}
					}
				}
			}
			
			for(String action : full_action_map.keySet()){
				double probability = 0.0;
				probability = full_action_map.get(action)/(double)feature_list.size();

				//cumulative_probability[action_idx] += probability;
				full_action_map.put(action, probability);
			}
			element_action_map_list.add(full_action_map);
		}
		
		return element_action_map_list;
		*/
		
		return null;
	}
	
	public double[][] generatePolicy(List<Feature> input_features, List<Feature> output_features){
		// 1. Create a memory record for prediction
		Random random = new Random();
		double[][] policy = new double[input_features.size()][output_features.size()];
		System.err.println("###################################################################");
		System.err.println("input features size :: "+input_features.size());
		System.err.println("output features size :: "+output_features.size());
		
		for(int in_idx = 0; in_idx < input_features.size(); in_idx++){
			System.err.println("Getting input feature weights for feature :: " +input_features.get(in_idx).getValue());
			
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
					if(input_feature_record == null){
						input_feature = feature_repo.save(input_feature);
					}
					else{
						input_feature = input_feature_record;
					}
					
					input_feature.getFeatureWeights().add(feature_weight);
					feature_repo.save(input_feature);
				}
				
				policy[in_idx][out_idx] = weight;
			}
		}
		System.err.println("###################################################################");

		
		return policy;
	}

	public void train(List<Feature> feature_list, String label) {
		//REINFORCEMENT LEARNING
		System.err.println( " Initiating learning");
		
		//learning model
		// 1. identify vocabulary (NOTE: This is currently hard coded since we only currently care about 1 context)
		Vocabulary vocabulary = new Vocabulary(new ArrayList<Feature>(), "internet");

		System.err.println("object definition list size :: "+feature_list.size());
		// 2. create record based on vocabulary
		for(Feature feature : feature_list){
			vocabulary.appendToVocabulary(feature);
		}
		
		System.err.println("vocabulary :: "+vocabulary);
		System.err.println("vocab value list size   :: "+vocabulary.getFeatures().size());
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
