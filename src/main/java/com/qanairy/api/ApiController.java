package com.qanairy.api;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.omg.CORBA.UnknownUserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.deepthought.models.Action;
import com.deepthought.models.ObjectDefinition;
import com.deepthought.models.Vocabulary;
import com.deepthought.models.dto.IObjectDefinition;
import com.deepthought.models.dto.IPolicyEdge;
import com.deepthought.models.repositories.ActionRepository;
import com.deepthought.models.repositories.ObjectDefinitionRepository;
import com.qanairy.brain.ActionFactory;
import com.qanairy.brain.Brain;
import com.qanairy.db.DataDecomposer;
import com.qanairy.db.OrientConnectionFactory;


/**
 *	API endpoints for interacting with {@link Domain} data
 */
@Controller
@RequestMapping("/")
public class ApiController {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * Create a new {@link Domain domain}
     * @throws NullPointerException 
     * @throws IllegalAccessException 
     * @throws IllegalArgumentException 
     * 
     * @throws UnknownUserException 
     * @throws UnknownAccountException 
     * @throws MalformedURLException 
     */
    @RequestMapping(value ="/predict", method = RequestMethod.POST)
    
    public @ResponseBody HashMap<String, Double> predict(@RequestBody HashMap<?,?> obj) throws IllegalArgumentException, IllegalAccessException, NullPointerException{
    	log.info("digesting Object : " +obj);
    	List<ObjectDefinition> object_definitions = DataDecomposer.decompose(obj);
    	log.info("Finished decomposing object into value list with length :: "+object_definitions.size());
    	
    	log.info("loading vocabulary");
    	//LOAD VOCABULARY
    	String label = "html";
    	ObjectDefinitionRepository obj_def_repo = new ObjectDefinitionRepository();
    	
    	OrientConnectionFactory connection = new OrientConnectionFactory();
    	List<ObjectDefinition> def_list = obj_def_repo.findAll(connection);
    	
    	//Vocabulary vocab = Vocabulary.load(label);
    
    	log.info("Setting object definitions as features in vocabulary" );
    	//SETTING VOCABULARY FEATURES TO 1 FOR EACH OBJECT DEFINITION
    	HashMap<String, Integer> vocabulary_record = new HashMap<String, Integer>();

    	int i = 0;
    	for(ObjectDefinition definition : def_list){
    		boolean has_match = false;
    		for(ObjectDefinition record_definition : object_definitions){
    			if(record_definition.equals(definition)){
    				vocabulary_record.put(record_definition.getValue(), 1);
    				has_match = true;
    				break;
    			}
    		}
    		
    		if(!has_match){
    			vocabulary_record.put(definition.getValue(), 0);
    		}
    		
    		i++;
    	}
    	
    	log.info("loading universal action set");
    	//Setting action features to probabilities set for each object definitions actions
    	ActionRepository action_repo = new ActionRepository();
    	List<Action> actions = action_repo.findAll(connection);
    	Vocabulary action_vocab = Vocabulary.load("actions");
    	
    	for(Action action : actions){
    		action_vocab.appendToVocabulary(action.getKey());
    	}
		double[][] vocab_policy = new double[vocabulary_record.keySet().size()][actions.size()];
		
		log.info("concatenating action features into 2d array for vocabulary");
		//set actions for object definition to action probabilities
		int k = 0;
		for(ObjectDefinition def : def_list){
			//load action policy for object definition
			IObjectDefinition iobj = obj_def_repo.convertToRecord(connection, def);
			Iterator<IPolicyEdge> object_action_policy_iter = iobj.getPolicyEdges().iterator();
			
			while(object_action_policy_iter.hasNext()){
				IPolicyEdge policy_edge = object_action_policy_iter.next();
				Action current_action = action_repo.convertFromRecord(policy_edge.getActionOut());
				int action_idx = action_vocab.getValueList().indexOf(current_action.getKey());
				if(action_idx >= 0){
					vocab_policy[k][action_idx] = policy_edge.getProbability();
				}
			}
			k++;
		}
    	
    	log.info("Predicting...");
    	HashMap<String, Double> prediction_vector = Brain.predict(DataDecomposer.decompose(obj), ActionFactory.getActions());
		log.info("prediction found. produced vector :: "+prediction_vector.size());
		
    	return prediction_vector;
    }


    @RequestMapping(value ="/learn", method = RequestMethod.POST)
    public  @ResponseBody List<?> learn(@RequestBody Object obj, HashMap<?,?> predicted, Object action){
	    
    	return null;
    }
}
