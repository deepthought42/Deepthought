package com.qanairy.api;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONException;
import org.json.JSONObject;
import org.omg.CORBA.UnknownUserException;
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
    public @ResponseBody MemoryRecord predict(@RequestParam(value="json_object", required=true) String obj,
			  								  @RequestParam(value="input_vocab_label", required=true) String input_vocab_label,
    										  @RequestParam(value="output_vocab_label", required=true) String output_vocab_label,
    										  @RequestParam(value="new_output_features", required=false) List<String> new_output_features) throws IllegalArgumentException, IllegalAccessException, NullPointerException, JSONException{
    	System.err.println("digesting Object : " +obj);
    	System.err.println("new output features :: "+new_output_features);
    	//Break down object into list of features
    	List<Feature> input_features = DataDecomposer.decompose(new JSONObject(obj));
    	
    	System.err.println("loading vocabulary");
    	//LOAD VOCABULARIES FOR INPUT AND OUTPUT
    	Vocabulary input_vocab = vocabulary_repo.findByLabel(input_vocab_label);
    	//for each feature, check if feature is in input_vocab
    	List<String> input_feature_keys = new ArrayList<String>();

    	for(Feature feature : input_features){
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
    	
		List<Feature> new_features = new ArrayList<Feature>();
    	if(new_output_features != null && !new_output_features.isEmpty()){
			for(String value : new_output_features){
				Feature feature_record = feature_repo.findByValue(value);
				if(feature_record==null){
					Feature new_feature = new Feature(value);
					new_features.add(new_feature);
				}
				
			}
    	}
    	
    	Vocabulary output_vocab = vocabulary_repo.findByLabel(output_vocab_label);
    	if(output_vocab == null){
    		if(new_output_features == null || new_output_features.isEmpty()){
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
    	List<String> output_feature_keys = new ArrayList<String>();
    	for(Feature out_feature : output_features){
    		output_feature_keys.add(out_feature.getValue());
    	}
    	
    	//load feature vector for output_vocab
    	System.err.println("loading output feature set");
    	
    	//generate policy for input vocab feature vector and output vocab feature vector
		//double[][] vocab_policy = FeatureVector.loadPolicy(features, output_vocab.getFeatures(), vocabulary_record, output_vocab);
    	double[][] policy = brain.generatePolicy(input_features, output_features);

    	//generate prediction
    	System.err.println("Predicting...");
    	double[] prediction = brain.predict(policy);
    	
    	Feature predicted_feature = null;
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
    	
    	
    	//System.err.println("object definition list size :: "+feature_list.size());
    	
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
