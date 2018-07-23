package com.qanairy.brain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.deepthought.models.Action;
import com.deepthought.models.Feature;
import com.deepthought.models.MemoryRecord;
import com.deepthought.models.Vocabulary;
import com.deepthought.models.edges.ActionWeight;
import com.deepthought.models.edges.FeaturePolicy;
import com.deepthought.models.edges.Prediction;
import com.deepthought.models.repository.FeatureRepository;
import com.deepthought.models.repository.MemoryRecordRepository;
import com.deepthought.models.repository.VocabularyRepository;


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
	private MemoryRecordRepository memory_repo;
	
	@Autowired
	private VocabularyRepository vocabulary_repo;
	
	public Brain(){}
	
	public Map<Action, Double> predict(List<Feature> features, List<Feature> result_features, Vocabulary vocabulary){
			
			//Call predict method and get anticipated reward for given action against all datums
			//	-- method needed for prediction 
			//START PREDICT METHOD
				
			//Flip a coin to determine whether we should exploit/optimize or explore
			/*double coin = rand.nextDouble();
			if(coin > .5){
				//Get Best action_weight prediction
				double max = -1.0;
				int maxIdx = 0;
			    for(int j = 0; j < action_weights.size(); j++){
			    	if(action_weights.get(j) > max){
			    		System.err.println("MAX WEIGHT FOR NOW :: "+max);
			    		max=action_weights.get(j);
			    		maxIdx = j;
			    	}
			    }
			    
			    System.err.println("-----------    max computed action is ....." + actions[maxIdx]);
			    return new HashMap<String, Double>();
			}
			else{
				System.err.println("Coin was flipped and exploration was chosen. OH MY GOD I HAVE NO IDEA WHAT TO DO!");
				return new HashMap<String, Double>();
			}*/
		

		
		
		
		// 1. Create a memory record for prediction
		MemoryRecord memory = new MemoryRecord();
		
		// 2. add vocabulary to memory record
		
		// 3. if any features are not in vocabulary add them to the vocabulary
		for(Feature feature : features){
			boolean feature_already_exists = false;
			System.err.println("Vocabulary features size :: "+vocabulary.getFeatures().size());
			if(vocabulary.getFeatures().isEmpty()){
				System.err.println("vocab features is empty");
			}
			else{
				for(Feature vocab_feature : vocabulary.getFeatures()){
					System.err.println("Feature :: "+feature);
					System.err.println("Vocab feature :: "+vocab_feature);
					if(vocab_feature.equals(feature)){
						feature_already_exists = true;
						break;
					}
				}
			}
			
			if(!feature_already_exists){
				Feature feature_record = feature_repo.findByKey(feature.getKey());
				if(feature_record != null){
					feature = feature_record;
				}
				else{
					feature = feature_repo.save(feature);
				}
				vocabulary.getFeatures().add(feature);
			}
		}
		vocabulary = vocabulary_repo.save(vocabulary);
		
		// 3.1. if any result features are not in vocabulary add them to the vocabulary
		for(Feature feature : result_features){
			boolean feature_already_exists = false;
			for(Feature vocab_feature : vocabulary.getFeatures()){
				if(vocabulary.getFeatures().isEmpty()){
					System.err.println("vocab features is empty");
				}
				else{
					if(vocab_feature.equals(feature)){
						feature_already_exists = true;
						break;
					}
				}
			}
			
			if(!feature_already_exists){
				Feature feature_record = feature_repo.findByKey(feature.getKey());
				if(feature_record != null){
					feature = feature_record;
				}
				else{
					feature = feature_repo.save(feature);
				}
				vocabulary.getFeatures().add(feature);
			}
		}
		vocabulary = vocabulary_repo.save(vocabulary);
		
		memory.setStartVocabulary(vocabulary);
		memory = memory_repo.save(memory);
		
		List<Feature> result_feature = new ArrayList<Feature>();
		Map<Feature, Map<String, Double>> unordered_feature_policy = new HashMap<Feature, Map<String, Double>>();
		// 4. for each feature
		for(Feature feature : features){
			System.err.println("Feature iteration :: "+feature);
			feature = feature_repo.findByKey(feature.getKey());
			
			Map<String, Double> action_policy = new HashMap<String, Double>();
			List<String> policy_actions = new ArrayList<String>();
			List<Double> policy_weights = new ArrayList<Double>();
			
			// 4.1) load action weights for policy
			List<ActionWeight> action_weights = feature.getActionWeights();
			for(ActionWeight weight : action_weights){
				List<String> labels = weight.getLabels();
				double probability = weight.getWeight();
				policy_actions.add(weight.getAction().getKey());
				policy_weights.add(probability);
				action_policy.put(weight.getAction().getKey(), probability);
				if(!result_feature.contains(weight.getAction())){
					result_feature.add(weight.getAction());
				}
				System.err.println("Label :: "+labels.size() + " ; P() :: " + probability + "%");
			}
			
			if(policy_actions.size() > 0 && policy_weights.size() > 0){
				// 4.2) Add feature action policy to memory record
				FeaturePolicy feature_policy = new FeaturePolicy();
				feature_policy.setMemoryRecord(memory);
				feature_policy.setFeature(feature);
				feature_policy.setPolicyFeatures(policy_actions);
				feature_policy.setPolicyWeights(policy_weights);
				memory.getFeaturePolicies().add(feature_policy);
			}
			// 4.3) append policy to policy matrix
			
			unordered_feature_policy.put(feature, action_policy);
		}
		memory = memory_repo.save(memory);


		System.err.println("Total known actions :: "+known_actions.size());

		// 5. Generate feature vector
		Set<Feature> feature_vector = unordered_feature_policy.keySet();
		
		// 5.1 build ordered feature matrix
		
		double[][] weight_matrix = new double[feature_vector.size()][known_actions.size()];
		
		int feature_idx = 0;
		for(Feature feature : feature_vector){
			Map<String, Double> action_policy = unordered_feature_policy.get(feature);
			System.err.println("loading action policy for : "+feature.getValue());
			int action_idx = 0;
			
			for(Action action : known_actions){
				System.err.println("Action key :: "+action.getKey());
				System.err.println("action_policy :: "+action_policy);
				Double action_weight = action_policy.get(action.getKey());
				if(action_weight == null){
					System.err.println("Action weight is null");
					weight_matrix[feature_idx][action_idx] = 0.0;
				}else{
					System.err.println("Action weight is "+action_weight);

					weight_matrix[feature_idx][action_idx] = action_weight;
				}
				action_idx++;
			}
			feature_idx++;
		}
			
		// 6. generate prediction using feature vector and policy matrix (1*(policies)^Transpose = Y)
		Map<Action, Double> prediction_map = new HashMap<Action, Double>();
		
		for(int i=0; i<weight_matrix[0].length; i++){
			double action_weight = 0.0;
			for(int j=0; j<weight_matrix.length; j++){
				System.err.println("Adding to action weight :: "+ weight_matrix[j][i]);
				action_weight += weight_matrix[j][i];
			}
			Prediction prediction = new Prediction();
			prediction.setAction(known_actions.get(i));
			prediction.setMemoryRecord(memory);
			prediction.setWeight(action_weight);
			memory.getActionPrediction().add(prediction);
			memory = memory_repo.save(memory);
			prediction_map.put(known_actions.get(i), action_weight);
		}
		
		//	6.1) Store predictions in memory record
		
		// 7. return prediction map
		
		
		return prediction_map;
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
	public void learn(List<Feature> feature_list,
					  Map<String,Double> predicted_action_vector,
					  Action actual_action,
					  boolean isRewarded)
						  throws IllegalArgumentException, IllegalAccessException, 
							  NullPointerException, IOException{
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
		
		// 2a. load known action policies/probabilities for each object definition in the definition list

		// 3. determine reward/regret score based on productivity status
		double actual_reward = 0.0;
		
		if(isRewarded){
			actual_reward = 5.0;
		}
		else{
			//nothing changed so there was no reward for that combination. We want to remember this in the future
			// so we set it to a negative value to simulate regret
			actual_reward = -1.0;
		}
		
		System.err.println("Set award :: "+actual_reward);
		
		// 4. apply reward/regret to predicted_action_vector
		// 5. perform backpropagation through network supporting result
		
		System.err.println("Setting up for q-learning");
		//Q-LEARNING VARIABLES
		final double learning_rate = .08;
		final double discount_factor = .08;
		
		//machine learning algorithm should produce this value
		double estimated_reward = 1.0;
		
		QLearn q_learn = new QLearn(learning_rate, discount_factor);
		
		System.err.println("doing stuff with object definition list "+feature_list.size());
		//Reinforce probabilities for the component objects of this element
		for(Feature feature : feature_list){
			System.err.println("Object defintion to be updated ..."+feature.getValue());
			//NEED TO LOOK UP OBJECT DEFINITION IN MEMORY, IF IT EXISTS, THEN IT SHOULD BE LOADED AND USED, 
			//IF NOT THEN IT SHOULD BE CREATED, POPULATED, AND SAVED
			String key = feature.generateKey();
			System.err.println("Object definition key :: "+key);
			System.err.println("REPO :: "+feature_repo);
			Feature feature_record = feature_repo.findByKey(key);
			
			System.err.println("object def retrieved :: "+feature_record);
			if(feature_record != null){
				feature = feature_record;
			}
			List<ActionWeight> action_weight_list = feature.getActionWeights();
			System.err.println("action weight list size :: "+action_weight_list.size());
			double last_value = 0.0;
			System.err.println("Checking if known action...");
			ActionWeight known_action_weight = null;
			boolean is_known_action = false;
			for(ActionWeight action_weight : action_weight_list){
				if(action_weight.getAction().getKey().equals(actual_action.getKey())){
					System.err.println("Last action : "+actual_action.getKey() + " exists in action_map for object");
					last_value = action_weight.getWeight();
					known_action_weight = action_weight;
					is_known_action = true;
				}
			}
			
			System.err.println("last reward : "+last_value);
			System.err.println("actual_reward : "+actual_reward);
			System.err.println("estimated_reward : "+estimated_reward);
			
			if(!is_known_action){
				Random rand = new Random();
				last_value = rand.nextFloat();
			}
			double q_learn_val = q_learn.calculate(last_value, actual_reward, estimated_reward );
			
			if(known_action_weight == null){
				known_action_weight = new ActionWeight();
				known_action_weight.setAction(actual_action);
				known_action_weight.setFeature(feature);
				feature.getActionWeights().add(known_action_weight);
			}
			known_action_weight.setWeight(q_learn_val);

			System.err.println(" -> ADDED LAST ACTION TO ACTION MAP :: "+actual_action+"...Q LEARN VAL : "+q_learn_val);

			System.err.println("Object definition :: "+feature);
			System.err.println("Object definition key :: "+feature.getKey());
			System.err.println("Object definition value :: "+feature.getValue());
			feature_repo.save(feature);
		}
		
		
		//Learning process
		//	1. identify vocabulary that this set of object info belongs to
		//  2. Create record based on vocabulary
		//  3. load known vocabulary action policies
		//  4. perform matrix math to inline update vocabulary policies for record based on reward/penalty for productivity  
		//  5. 
		//  6. 
	
			//Save states
			/** Handled already in memory Registry I think...LEAVE THIS UNTIL VERIFIED ITS NOT NEEDED

			if(prev_obj != null && !(prev_obj.getData() instanceof Action)){
				Vertex prev_vertex = persistor.find(prev_obj);
				Vertex current_vertex = persistor.find(obj);
				ArrayList<Integer> path_ids = new ArrayList<Integer>();
				path_ids.add(path.hashCode());
				System.err.println("Adding GOES_TO transition");
				Edge e = persistor.addEdge(prev_vertex, current_vertex, "Component", "GOES_TO");
				e.setProperty("path_ids", path_ids);
			}
			else if(prev_obj != null && prev_obj.getData() instanceof Action){
				Vertex prev_vertex = persistor.find(prev_obj);
				Vertex current_vertex = persistor.find(obj);
				ArrayList<Integer> path_ids = new ArrayList<Integer>();
				path_ids.add(path.hashCode());
				System.err.println("Adding GOES_TO transition");
				Edge e = persistor.addEdge(prev_vertex, current_vertex, "Transition", "GOES_TO");
				e.setProperty("path_ids", path_ids);
			}
			
			
			
			System.err.println("SAVING NOW...");
			persistor.save();
			*/
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
		 * 
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
					ArrayList<Float> action_weights = new ArrayList<Float>(Arrays.asList(new Float[ActionFactory.getActions().length]));
					
					
					//for(int weight_idx = 0 ; weight_idx < action_weights.size(); weight_idx++){
						//System.err.println("SETTING ACTION WIGHT : "+rand.nextFloat());
					//	action_weights.set(weight_idx, rand.nextFloat());
					//}
=======
					vocab_weights.appendToVocabulary(feature.getValue());

>>>>>>> f8550e37a7b03a9e5d435acb6d8ce040379bea09
					//add weights to vocabulary weights;
					for(String action : ActionFactory.getActions()){
						vocab_weights.appendToWeights(feature.getValue(), action, rand.nextFloat()); 
					}
					
					//action_weights_list.add(action_weights);
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
	 * Calculates the rewards
	 * 
	 * @param action_rewards
	 * @param pageElement
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public HashMap<String, Double> calculateActionProbabilities(Feature pageElement) throws IllegalArgumentException, IllegalAccessException{
		/*List<Feature> definitions = DataDecomposer.decompose(pageElement);

		System.err.println(getSelf().hashCode() + " -> GETTING BEST ACTION PROBABILITY...");
		HashMap<String, Double> cumulative_action_map = new HashMap<String, Double>();
		
		for(Object obj : definitions){
			Iterable<com.tinkerpop.blueprints.Vertex> memory_vertex_iter = persistor.findVertices(obj);
			Iterator<com.tinkerpop.blueprints.Vertex> memory_iterator = memory_vertex_iter.iterator();
			
			while(memory_iterator.hasNext()){
				com.tinkerpop.blueprints.Vertex mem_vertex = memory_iterator.next();
				HashMap<String, Double> action_map = mem_vertex.getProperty("actions");
				double probability = 0.0;
				if(action_map != null){
					for(String action: action_map.keySet()){
						if(cumulative_action_map.containsKey(action)){
							probability += cumulative_action_map.get(action);
						}
						
						cumulative_action_map.put(action, probability);
					}
				}
				else{
					for(String action: ActionFactory.getActions()){						
						cumulative_action_map.put(action, probability);
					}
				}
			}
		}
		return cumulative_action_map;
		*/
		return null;
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
