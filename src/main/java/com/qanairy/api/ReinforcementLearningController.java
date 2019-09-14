package com.qanairy.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.deepthought.models.Feature;
import com.deepthought.models.MemoryRecord;
import com.deepthought.models.Vocabulary;
import com.deepthought.models.repository.FeatureRepository;
import com.deepthought.models.repository.MemoryRecordRepository;
import com.deepthought.models.repository.VocabularyRepository;
import com.qanairy.brain.Brain;
import com.qanairy.db.DataDecomposer;


/**
 *	API endpoints for learning and making predictions. This set of endpoints allows interacting with the knowledge graph
 *	 to generate, update and retrieve weight matices(models) for any given input feature set and output set
 */
@RestController
@RequestMapping("/rl")
public class ReinforcementLearningController {
	
	@SuppressWarnings("unused")
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private FeatureRepository feature_repo;
	
	@Autowired
	private VocabularyRepository vocabulary_repo;
	
	@Autowired
	private MemoryRecordRepository memory_repo;
	
	@Autowired
	private Brain brain;
	
    /**
     * 
     * @param obj
     * @param input_vocab_label
     * @param output_vocab_label
     * @param new_output_features
     * 
     * @return
     * 
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws NullPointerException
     * @throws JSONException
     */
    @RequestMapping(value ="/predict", method = RequestMethod.POST)
    public @ResponseBody MemoryRecord predict(@RequestParam(value="json_object", required=true) String obj,
			  								  @RequestParam(value="input_vocab_label", required=true) String input_vocab_label,
    										  @RequestParam(value="output_vocab_label", required=true) String output_vocab_label,
    										  @RequestParam(value="new_output_features", required=false) String[] new_output_features) 
    												  throws IllegalArgumentException, IllegalAccessException, NullPointerException, JSONException{
    	//Break down object into list of features
    	List<Feature> input_features = DataDecomposer.decompose(new JSONObject(obj));
    	List<Feature> new_features = new ArrayList<Feature>();
    	if(new_output_features != null && !ArrayUtils.isEmpty(new_output_features)){
			for(String value : new_output_features){
				Feature feature_record = feature_repo.findByValue(value);
				if(feature_record==null){
					value = value.replace("[", "");
		        	value = value.replace("]", "");
					Feature new_feature = new Feature(value);
					new_features.add(new_feature);
				}
				
			}
    	}
    	
    	log.debug("loading vocabulary");
    	//LOAD VOCABULARIES FOR INPUT AND OUTPUT
    	Vocabulary input_vocab = vocabulary_repo.findByLabel(input_vocab_label);
    	//for each feature, check if feature is in input_vocab
    	List<String> input_feature_keys = new ArrayList<String>();
    	
    	List<Feature> scrubbed_input_features = new ArrayList<Feature>();
    	List<String> seen_features = new ArrayList<String>();
		for(Feature input_feature : input_features){
			boolean input_equals_output = false;
	    	for(Feature output_feature : new_features){
    			if(output_feature.getValue().equalsIgnoreCase(input_feature.getValue())){
    				input_equals_output = true;
    			}
    		}
    		
    		if(!input_equals_output && input_feature.getValue() != null && !input_feature.getValue().equals("null") && !input_feature.getValue().trim().isEmpty()
    				&& !seen_features.contains(input_feature.getValue())){
    			scrubbed_input_features.add(input_feature);
    		}
    		
    		seen_features.add(input_feature.getValue());
    	}
    	
    	for(Feature feature : scrubbed_input_features){
    		Feature feature_record = vocabulary_repo.findFeatureByKey(input_vocab_label, feature.getValue());
    		//   if feature is not present in input_vocab then add feature to input_vocab
    		if(feature_record == null){
    			feature_record = feature_repo.findByValue(feature.getValue());
    			if(feature_record == null){
    				feature = feature_repo.save(feature);
    			}
    			else{
    				feature = feature_record;
    			}
    			
    			boolean feature_already_linked = false;
    			for(Feature out_feature : input_vocab.getFeatures()){
    				if(out_feature.equals(feature)){
    					feature_already_linked = true;
    					break;
    				}
    			}
    			
    			if(!feature_already_linked){
	    			input_vocab.getFeatures().add(feature);
	    			input_vocab = vocabulary_repo.save(input_vocab);
    			}
    		}
    		input_feature_keys.add(feature.getValue());

    	}
    	
		
    	
    	Vocabulary output_vocab = vocabulary_repo.findByLabel(output_vocab_label);
    	if(output_vocab == null){
    		if(new_output_features == null || ArrayUtils.isEmpty(new_output_features)){
    			throw new EmptyVocabularyException();
    		}
    		
    		output_vocab = new Vocabulary(new_features, output_vocab_label);
    		vocabulary_repo.save(output_vocab);
    	}
    	else{
    		for(Feature feature : new_features){
    			boolean feature_already_exists = false;
    			for(Feature existing_feature : output_vocab.getFeatures()){
    				if(existing_feature.getValue().equals(feature.getValue())){
    					feature_already_exists = true;
    					break;
    				}
    			}
    			
    			if(feature_already_exists){
    				continue;
    			}
    			
    			output_vocab.appendToVocabulary(feature);
    		}
    	}
    	
    	List<Feature> output_features = output_vocab.getFeatures();
    	String[] output_feature_keys = new String[output_features.size()];
    	for(int i=0; i< output_features.size(); i++){
    		output_feature_keys[i] = output_features.get(i).getValue();
    	}
    	
    	//load feature vector for output_vocab
    	log.debug("loading output feature set");
    	
    	//generate policy for input vocab feature vector and output vocab feature vector
		//double[][] vocab_policy = FeatureVector.loadPolicy(features, output_vocab.getFeatures(), vocabulary_record, output_vocab);
    	double[][] policy = brain.generatePolicy(scrubbed_input_features, output_features);

    	//generate prediction
    	log.debug("Predicting...  "+policy);
    	double[] prediction = brain.predict(policy);
    	
    	double max_pred = 0.0;
    	int max_idx = 0;
    	for(int idx = 0; idx<prediction.length; idx++){
    		if(prediction[idx] > max_pred){
    			max_idx = idx;
    		}
    	}
    	
    	//create memory and save vocabularies, policy matrix and prediction vector
    	MemoryRecord memory = new MemoryRecord();
    	memory.setInputVocabulary(input_vocab);
    	memory.setOutputVocabulary(output_vocab);
    	memory.setPolicyMatrix(policy);
    	memory.setInputFeatureValues(input_feature_keys);
    	memory.setOutputFeatureKeys(output_feature_keys);
    	memory.setPrediction(prediction);
    	memory.setPredictedFeature(output_features.get(max_idx));
		memory = memory_repo.save(memory);
		
    	
    	//return memory
    	return memory;
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
    public  @ResponseBody void learn(@RequestParam long memory_id, 
    								 @RequestParam String feature_value) throws JSONException, IllegalArgumentException, IllegalAccessException, NullPointerException, IOException{
    	//JSONObject json_obj = new JSONObject(json_object);
    	//List<Feature> feature_list = DataDecomposer.decompose(json_obj);
    	
    	Optional<MemoryRecord> optional_memory = memory_repo.findById(memory_id);
    	MemoryRecord memory = optional_memory.get();
    	
    	//log.info("object definition list size :: "+feature_list.size());
    	Feature feature = new Feature(feature_value);
    	Feature feature_record = feature_repo.findByValue(feature.getValue());
    	if(feature_record != null){
    		feature = feature_record;
    	}
    	//LOAD OBJECT DEFINITION LIST BY DECOMPOSING json_string
	    brain.learn(memory.getID(), feature); //feature_list, predicted, feature, isRewarded);
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

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
class EmptyVocabularyException extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7200878662560716216L;

	public EmptyVocabularyException() {
		super("Features list for output vocabulary cannot be empty or null");
	}
}
