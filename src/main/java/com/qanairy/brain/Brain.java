package com.qanairy.brain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qanairy.db.DataDecomposer;
import com.qanairy.db.OrientConnectionFactory;
import com.qanairy.db.OrientDbPersistor;
import com.qanairy.models.ObjectDefinition;
import com.qanairy.models.Vocabulary;
import com.qanairy.models.repositories.ObjectDefinitionRepository;
import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;

/**
 * Provides ability to predict and learn from data
 * 
 */
public class Brain {
	private static Logger log = LoggerFactory.getLogger(Brain.class);
	
	public static HashMap<String, Double> predict(List<ObjectDefinition> object_definitions, String[] actions){
		// 1. identify vocabulary (NOTE: This is currently hard coded since we only currently care about 1 context)
		Vocabulary vocabulary = new Vocabulary(new ArrayList<String>(), "internet");
		// 2. create record based on vocabulary
		for(ObjectDefinition objDef : object_definitions){
			log.info("saving object definition");
			vocabulary.appendToVocabulary(objDef.getValue());
			OrientConnectionFactory orientPersistor = new OrientConnectionFactory();
			ObjectDefinitionRepository obj_def_record = new ObjectDefinitionRepository();
			obj_def_record.save(orientPersistor, objDef);
			//load policy for object definition
		}
		
		// 3. adjust action policies if more actions exist than the known actions
		
		// 4. load known vocabulary action policies into matrix
		// 5. generate vocabulary policy matrix from list of object_definitions

		double[][] vocab_actions = new double[vocabulary.getValueList().size()][actions.length];
		
		int idx = 0;
		for(String vocab_word : vocabulary.getValueList()){
			boolean[] state = new boolean[vocabulary.getValueList().size()];
			if(object_definitions.contains(vocab_word)){
				state[idx] = true;
				//load vocabulary object definition and make sure action list of probabilities is in the proper order

				for(int action_idx=0; action_idx<actions.length; action_idx++){
					//vocab_actions[idx][action_idx] = 0.1;  whats the dynamic value though??
				}
			}
			else{
				state[idx] = false;
				for(int action_idx=0; action_idx<actions.length; action_idx++){
					vocab_actions[idx][action_idx] = 0.0;
				}
			}
			idx++;
		}
		
		// 5. run predictions with the vocabulary vector as a record and multiply it by the action policies st. vocabulary_state*(policies)^Transpose = Y
		// 6. return predicted action vector
		
		return new HashMap<String, Double>();
	}
	
	/**
	 * Predicts best action based on disparate action information
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	public static HashMap<String, Double> predict(HashMap<?,?> obj) throws IllegalArgumentException, IllegalAccessException {
		List<ObjectDefinition> object_definitions = DataDecomposer.decompose(obj);
		
		// 1. identify vocabulary (NOTE: This is currently hard coded since we only currently care about 1 context)
		Vocabulary vocabulary = new Vocabulary(new ArrayList<String>(), "internet");
		// 2. create record based on vocabulary
		for(ObjectDefinition objDef : object_definitions){
			vocabulary.appendToVocabulary(objDef.getValue());
			//load policy for object definition
		}
				
		//get List of all possible actions
		String[] actions = ActionFactory.getActions();
		
		double[] action_weight = new double[actions.length];
		Random rand = new Random();

		//COMPUTE ALL EDGE PROBABILITIES
		for(int index = 0; index < actions.length; index++){
			OrientDbPersistor orientPersistor = new OrientDbPersistor();
			Iterator<Vertex> vertices = orientPersistor.findVertices(obj).iterator();
			if(!vertices.hasNext()){
				/*return rand.nextInt(actions.length);*/
				return new HashMap<String, Double>();
			}

			Vertex vertex = vertices.next();

			Iterable<Edge> edges = vertex.getEdges(Direction.OUT, actions[index]);
			if(edges.iterator().hasNext()){
				for(Edge edge : edges){
					if(edge.getLabel().isEmpty()){
						//guess the label
					}
					else{
						String label = edge.getLabel();
						int action_count = edge.getProperty("count");
						int probability = edge.getProperty("probability");
						log.info("Label :: "+label+" ; count :: "+ action_count + " ; P() :: " + probability + "%");	
					}
				}
			}
			else{
				log.info("+++   No edges found. Setting weight randomly ++");
				action_weight[index] = rand.nextDouble();
			}
		}
		
		//Call predict method and get anticipated reward for given action against all datums
		//	-- method needed for prediction 
		//START PREDICT METHOD
			
		//Flip a coin to determine whether we should exploit/optimize or explore
		double coin = rand.nextDouble();
		if(coin > .5){
			//Get Best action_weight prediction
			double max = -1.0;
			int maxIdx = 0;
		    for(int j = 0; j < action_weight.length; j++){
		    	if(action_weight[j] > max){
		    		log.info("MAX WEIGHT FOR NOW :: "+max);
		    		max=action_weight[j];
		    		maxIdx = j;
		    	}
		    }
		    
		    log.info("-----------    max computed action is ....." + actions[maxIdx]);
		    return new HashMap<String, Double>();
		}
		else{
			log.info("Coin was flipped and exploration was chosen. OH MY GOD I HAVE NO IDEA WHAT TO DO!");
			return new HashMap<String, Double>();
		}
		//END PREDICT METHOD
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
	public static void learn(List<ObjectDefinition> object_definition_list,
					  Map<String,Double> predicted_action_vector,
					  String actual_action,
					  boolean isProductive)
						  throws IllegalArgumentException, IllegalAccessException, 
							  NullPointerException, IOException{
		//REINFORCEMENT LEARNING
		log.info( " Initiating learning");
		
		//learning model
		// 1. identify vocabulary (NOTE: This is currently hard coded since we only currently care about 1 context)
		Vocabulary vocabulary = new Vocabulary(new ArrayList<String>(), "internet");

		// 2. create record based on vocabulary
		for(ObjectDefinition objDef : object_definition_list){
			vocabulary.appendToVocabulary(objDef.getValue());
		}
		
		// 2. create state vertex from vocabulary and
		int idx = 0;
		for(String vocab_word : vocabulary.getValueList()){
			boolean[] state = new boolean[vocabulary.getValueList().size()];
			if(object_definition_list.contains(vocab_word)){
				state[idx] = true;
			}
			else{
				state[idx] = false;
			}
			idx++;
		}
		
		// 2a. load known action policies/probabilities for each object definition in the definition

		// 3. determine reward/regret score based on productivity status
		double actual_reward = 0.0;
		
		if(isProductive){
			actual_reward = 10.0;
		}
		else{
			//nothing changed so there was no reward for that combination. We want to remember this in the future
			// so we set it to a negative value to simulate regret
			actual_reward = -1.0;
		}
		
		// 4. apply reward/regret to predicted_action_vector
		// 5. perform backpropagation through network supporting result
		//Q-LEARNING VARIABLES
		final double learning_rate = .08;
		final double discount_factor = .08;
		
		//machine learning algorithm should produce this value
		double estimated_reward = 1.0;
		
		QLearn q_learn = new QLearn(learning_rate, discount_factor);
		
		//Reinforce probabilities for the component objects of this element
		for(ObjectDefinition objDef : object_definition_list){
			HashMap<String, Double> action_map = objDef.getActions();
			
			//NEED TO LOOK UP OBJECT DEFINITION IN MEMORY, IF IT EXISTS, THEN IT SHOULD BE LOADED AND USED, 
			//IF NOT THEN IT SHOULD BE CREATED, POPULATED, AND SAVED
			Iterator<com.tinkerpop.blueprints.Vertex> v_mem_iter = null; //persistor.findAll(objDef).iterator();
			com.tinkerpop.blueprints.Vertex memory_vertex = null;
			if(v_mem_iter.hasNext()){
				memory_vertex = v_mem_iter.next();
				action_map = memory_vertex.getProperty("actions");
				if(action_map == null){
					action_map = objDef.getActions();
				}
			}
			double last_reward = 0.0;

			/*
			if(action_map.containsKey(last_action)){
				log.info("Last action : "+last_action + " exists in action_map for object");
				last_reward = action_map.get(last_action);
			}
			*/
			log.info("last reward : "+last_reward);
			log.info("actual_reward : "+actual_reward);
			log.info("estimated_reward : "+estimated_reward);
			
			double q_learn_val = q_learn.calculate(last_reward, actual_reward, estimated_reward );
			//action_map.put(last_action, q_learn_val);
			//log.info(" -> ADDED LAST ACTION TO ACTION MAP :: "+last_action+"...Q LEARN VAL : "+q_learn_val);

			//objDef.setActions(action_map);
			//com.tinkerpop.blueprints.Vertex v = objDef.findAndUpdateOrCreate(persistor);
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
				log.info("Adding GOES_TO transition");
				Edge e = persistor.addEdge(prev_vertex, current_vertex, "Component", "GOES_TO");
				e.setProperty("path_ids", path_ids);
			}
			else if(prev_obj != null && prev_obj.getData() instanceof Action){
				Vertex prev_vertex = persistor.find(prev_obj);
				Vertex current_vertex = persistor.find(obj);
				ArrayList<Integer> path_ids = new ArrayList<Integer>();
				path_ids.add(path.hashCode());
				log.info("Adding GOES_TO transition");
				Edge e = persistor.addEdge(prev_vertex, current_vertex, "Transition", "GOES_TO");
				e.setProperty("path_ids", path_ids);
			}
			
			
			
			log.info("SAVING NOW...");
			persistor.save();
			*/
							
		
		
		
		
		
		//MemoryState memState = new MemoryState(last_page.hashCode());
		//com.tinkerpop.blueprints.Vertex state_vertex = null;
		//try{
		//	state_vertex = memState.createAndLoadState(last_page, null, persistor);
		//}catch(IllegalArgumentException e){}

		//get all objects for the chosen page_element	
		
		
		
		/* ORIGINAL CODE FROM ORIGINAL LEARN METHOD		
		double actual_reward = 0.0;
	
		if(!last_page.equals(current_page)){
			actual_reward = 10.0;
			
			com.tinkerpop.blueprints.Vertex new_state_vertex = null;
			MemoryState new_memory_state = new MemoryState(current_page.hashCode());
			
			new_state_vertex = new_memory_state.createAndLoadState(current_page, state_vertex, persistor);

			//w edge to memory
			
		}
		else{
			//nothing changed so there was no reward for that combination. We want to remember this in the future
			// so we set it to a negative value to simulate regret
			actual_reward = -1.0;
		}
		
		//get all objects for the chosen page_element
		//Q-LEARNING VARIABLES
		final double learning_rate = .08;
		final double discount_factor = .08;
		
		//machine learning algorithm should produce this value
		double estimated_reward = 1.0;
		
		QLearn q_learn = new QLearn(learning_rate, discount_factor);
		//Reinforce probabilities for the component objects of this element
		for(ObjectDefinition objDef : best_definitions){
			HashMap<String, Double> action_map = objDef.getActions();
			
			//NEED TO LOOK UP OBJECT DEFINITION IN MEMORY, IF IT EXISTS, THEN IT SHOULD BE LOADED AND USED, 
			//IF NOT THEN IT SHOULD BE CREATED POPULATED AND SAVED
			Iterator<com.tinkerpop.blueprints.Vertex> v_mem_iter = persistor.find(objDef).iterator();
			com.tinkerpop.blueprints.Vertex memory_vertex = null;
			if(v_mem_iter.hasNext()){
				memory_vertex = v_mem_iter.next();
				action_map = memory_vertex.getProperty("actions");
				if(action_map == null){
					action_map = objDef.getActions();
				}
			}
			double last_reward = 0.0;

			if(action_map.containsKey(last_action)){
				log.info("Last action : "+last_action + " exists in action_map for object");
				last_reward = action_map.get(last_action);
			}
			
			log.info("last reward : "+last_reward);
			log.info("actual_reward : "+actual_reward);
			log.info("estimated_reward : "+estimated_reward);
			
			double q_learn_val = q_learn.calculate(last_reward, actual_reward, estimated_reward );
			action_map.put(last_action, q_learn_val);
			log.info(" -> ADDED LAST ACTION TO ACTION MAP :: "+last_action+"...Q LEARN VAL : "+q_learn_val);

			objDef.setActions(action_map);
			com.tinkerpop.blueprints.Vertex v = objDef.findAndUpdateOrCreate(persistor);
		}
		*/
	}
	
	/**
	 * 
	 * @param object_list
	 * @return
	 */
	private Vocabulary predictVocabulary(List<ObjectDefinition> object_list){
		return null;
	}
	
	/**
	 * 
	 * @param object_list
	 * @param vocabulary
	 * @return
	 */
	private List<ObjectDefinition> generateVocabRecord(List<ObjectDefinition> object_list, Vocabulary vocabulary){
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
			vocabularies.add(Vocabulary.load(label));
		}
		return vocabularies;		
	}
	
	/**
	 * 
	 * @param object_list
	 * @param vocabulary
	 * @return
	 */
	private List<List<ObjectDefinition>> loadActionPolicies(List<ObjectDefinition> object_list, Vocabulary vocabulary){
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
			
			List<ObjectDefinition> objDefinitionList = null;
			try {
				objDefinitionList = decomposer.decompose(pathObj.getData());
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
			for(ObjectDefinition objDef : objDefinitionList){
				if( !this.vocab.getValueList().contains(objDef.getValue())){
					//then add object value to end of vocabulary
					this.vocab.appendToVocabulary(objDef.getValue());
					
					//add a new 0.0 value weight to end of weights
<<<<<<< HEAD
					//this.vocab.appendToWeights(objDef.getValue(), rand.nextFloat());
					
					//add new actions entry to match vocab
					ArrayList<Float> action_weights = new ArrayList<Float>(Arrays.asList(new Float[ActionFactory.getActions().length]));
					
					
					//for(int weight_idx = 0 ; weight_idx < action_weights.size(); weight_idx++){
						//log.info("SETTING ACTION WIGHT : "+rand.nextFloat());
					//	action_weights.set(weight_idx, rand.nextFloat());
					//}
=======
					vocab_weights.appendToVocabulary(objDef.getValue());

>>>>>>> f8550e37a7b03a9e5d435acb6d8ce040379bea09
					//add weights to vocabulary weights;
					for(String action : ActionFactory.getActions()){
						vocab_weights.appendToWeights(objDef.getValue(), action, rand.nextFloat()); 
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
			
			for(ObjectDefinition objDef : objDefinitionList){
				int value_idx = this.vocab.getValueList().indexOf(objDef.getValue() );
				for(int i=0; i < ActionFactory.getActions().length; i++){
					if(ActionFactory.getActions()[i].equals(last_action)){
						vocabularyExperienceRecord.set(value_idx, Boolean.TRUE);
						experience_record[value_idx][i] = Boolean.TRUE;
					}
					else{
						experience_record[value_idx][i] = Boolean.FALSE;
					}
					
					//place weights into 2 dimensional array for NN computations later
					weight_record[value_idx][i] = vocab_weights.getVocabulary_weights().get(objDef.getValue()).get(ActionFactory.getActions()[i]);
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


}
