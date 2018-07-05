package com.qanairy.brain;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.deepthought.models.Action;
import com.deepthought.models.Feature;
import com.deepthought.models.Vocabulary;
import com.deepthought.models.repository.ActionRepository;
import com.deepthought.models.repository.FeatureRepository;

@Component
public class FeatureVector {

	@Autowired
	private static FeatureRepository obj_def_repo;
	
	@Autowired
	private static ActionRepository action_repo;
	
	public static double[][] loadPolicy(List<Feature> def_list, List<Feature> actions, HashMap<String, Integer> vocabulary_record, Vocabulary vocab){
		double[][] vocab_policy = new double[vocabulary_record.keySet().size()][actions.size()];


		System.err.println("concatenating action features into 2d array for vocabulary");
		//set actions for object definition to action probabilities
		int k = 0;
		for(Feature def : def_list){
			//load action policy for object definition
			obj_def_repo.save(def);
			/*
			Iterator<PolicyEdge> object_action_policy_iter = def.getPolicyEdges().iterator();
			
			while(object_action_policy_iter.hasNext()){
				PolicyEdge policy_edge = object_action_policy_iter.next();
				Action current_action = action_repo.load(policy_edge.getActionOut());
				int action_idx = vocab.getValueList().indexOf(current_action.getKey());
				if(action_idx >= 0){
					vocab_policy[k][action_idx] = policy_edge.getProbability();
				}
			}
			*/
			k++;
		}
		return vocab_policy;
	}
	
	
	public static HashMap<String, Integer> load(List<Feature> def_list, List<Feature> features){
		HashMap<String, Integer> vocabulary_record = new HashMap<String, Integer>();

    	int i = 0;
    	for(Feature definition : def_list){
    		boolean has_match = false;
    		for(Feature record_definition : features){
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
    	
    	return vocabulary_record;
	}
}
