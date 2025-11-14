package com.deepthought.brain;

import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.deepthought.data.models.Feature;
import com.deepthought.data.models.Vocabulary;
import com.deepthought.data.repository.FeatureRepository;

@Component
public class FeatureVector {
	private static Logger log = LoggerFactory.getLogger(FeatureVector.class);

	@Autowired
	private static FeatureRepository obj_def_repo;
	
	public static double[][] loadPolicy(List<Feature> input_features, List<Feature> output_features, Vocabulary vocab){
		double[][] vocab_policy = new double[input_features.size()][output_features.size()];


		log.info("concatenating action features into 2d array for vocabulary");
		//set output_features for object definition to action probabilities
		for(Feature def : input_features){
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
		}
		return vocab_policy;
	}
	
	
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
