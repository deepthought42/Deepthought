package com.qanairy.api;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.omg.CORBA.UnknownUserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.util.IterableUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.deepthought.models.Action;
import com.deepthought.models.Feature;
import com.deepthought.models.Vocabulary;
import com.deepthought.models.repository.ActionRepository;
import com.deepthought.models.repository.FeatureRepository;
import com.deepthought.models.repository.VocabularyRepository;
import com.qanairy.brain.ActionFactory;
import com.qanairy.brain.Brain;
import com.qanairy.brain.FeatureVector;
import com.qanairy.db.DataDecomposer;


/**
 *	API endpoints for interacting with {@link Domain} data
 */
@RestController
@RequestMapping("/rl")
public class ReinforcementLearningController {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ActionRepository action_repo;

	@Autowired
	private FeatureRepository object_definition_repo;
	
	@Autowired
	private VocabularyRepository vocabulary_repo;
	
	@Autowired
	private Brain brain;
	
    /**
     * Create a new {@link Domain domain}
     * @throws NullPointerException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     * @throws JSONException 
     * 
     * @throws UnknownUserException 
     * @throws UnknownAccountException 
     * @throws MalformedURLException 
     */
    @RequestMapping(value ="/predict", method = RequestMethod.POST)
    public @ResponseBody Map<Action, Double> predict(@RequestBody String obj) throws IllegalArgumentException, IllegalAccessException, NullPointerException, JSONException{
    	System.err.println("digesting Object : " +obj);
    	List<Feature> features = DataDecomposer.decompose(new JSONObject(obj));
    	System.err.println("Finished decomposing object into value list with length :: "+features.size());
    	
    	System.err.println("loading vocabulary");
    	//LOAD VOCABULARY
    	String label = "internet";

    	List<Feature> def_list = IterableUtils.toList(object_definition_repo.findAll());
    	
    	//Vocabulary vocab = Vocabulary.load(label);
    
    	System.err.println("Setting object definitions as features in vocabulary" );
    	//SETTING VOCABULARY FEATURES TO 1 FOR EACH OBJECT DEFINITION
    	HashMap<String, Integer> vocabulary_record =  FeatureVector.load(def_list, features);
    	
    	System.err.println("loading universal action set");
    	//Setting action features to probabilities set for each object definitions actions
    	List<Action> actions = IterableUtils.toList(action_repo.findAll());
    	/*Vocabulary action_vocab = Vocabulary.load("actions");
    	
    	for(Action action : actions){
    		action_vocab.appendToVocabulary(action.getKey());
    	}
		double[][] vocab_policy = FeatureVector.loadPolicy(def_list, features, vocabulary_record, action_vocab);
    	*/
    	
    	// 1. identify vocabulary (NOTE: This is currently hard coded since we only currently care about 1 vocabulary context)
		Vocabulary vocabulary = new Vocabulary(new ArrayList<Feature>(), "internet");
		Vocabulary vocab_record = vocabulary_repo.findByKey("internet");
		
		if(vocab_record != null){
			vocabulary = vocab_record;
		}
		else{
			vocabulary = vocabulary_repo.save(vocabulary);
		}
		
    	System.err.println("Predicting...");
    	Map<Action, Double> prediction_vector = brain.predict(features, actions, vocabulary);
		System.err.println("prediction found. produced vector :: "+prediction_vector.keySet().size());
		
    	return prediction_vector;
    }
    
    /**
     * 
     * @param json_string
     * @param predicted
     * @param action
     * @param isRewarded
     * @throws JSONException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws NullPointerException
     * @throws IOException
     */
    @RequestMapping(value ="/learn", method = RequestMethod.POST)
    public  @ResponseBody void learn(@RequestParam(value="json_object", required=true) String json_object, 
    								 @RequestParam String prediction_key, 
    								 @RequestParam String action_name,
    								 @RequestParam String action_value,
    								 @RequestParam boolean isRewarded) throws JSONException, IllegalArgumentException, IllegalAccessException, NullPointerException, IOException{
    	JSONObject json_obj = new JSONObject(json_object);
    	List<Feature> feature_list = DataDecomposer.decompose(json_obj);
    	
    	System.err.println("object definition list size :: "+feature_list.size());
    	Map<String, Double> predicted = new HashMap<String, Double>();
    	
    	Action action = new Action(action_name, action_value);
    	Action action_record = action_repo.findByKey(action.getKey());
    	if(action_record != null){
    		action = action_record;
    	}
    	//LOAD OBJECT DEFINITION LIST BY DECOMPOSING json_string
	    brain.learn(feature_list, predicted, action, isRewarded);
    }
    
    @RequestMapping(value ="/train", method = RequestMethod.POST)
    public  @ResponseBody void train(@RequestParam(value="json_object", required=true) String json_object, 
    								 @RequestParam String label) 
    										 throws JSONException, IllegalArgumentException, IllegalAccessException, NullPointerException, IOException{
    
    	JSONObject json_obj = new JSONObject(json_object);
    	List<Feature> feature_list = DataDecomposer.decompose(json_obj);
    	brain.train(feature_list, label);
    }
}
