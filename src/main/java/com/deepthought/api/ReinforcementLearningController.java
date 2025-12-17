package com.deepthought.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

import com.deepthought.brain.Brain;
import com.deepthought.data.db.DataDecomposer;
import com.deepthought.data.edges.Prediction;
import com.deepthought.data.models.Feature;
import com.deepthought.data.models.MemoryRecord;
import com.deepthought.data.repository.FeatureRepository;
import com.deepthought.data.repository.MemoryRecordRepository;
import com.deepthought.data.repository.PredictionRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;


/**
 * Legacy controller for prediction and learning operations.
 * 
 * Preconditions:
 * - Intended for internal use only and not exposed as part of the public HTTP API surface.
 * - Required repository and {@link Brain} dependencies must be available and injected.
 * 
 * Postconditions:
 * - Maintains backward-compatible behavior for internal callers that still depend on these operations.
 * - No guarantees are made about future availability or stability of this controller.
 */
public class ReinforcementLearningController {
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private FeatureRepository feature_repo;
	
	@Autowired
	private MemoryRecordRepository memory_repo;
	
	@Autowired
	private PredictionRepository prediction_repo;
	
	@Autowired
	private Brain brain;
	
    /**
     * Generates a prediction and persists a corresponding {@link MemoryRecord}.
     * 
     * Preconditions:
     * - input is non-null and represents either a JSON object string or an unstructured string.
     * - output_labels is non-null and contains at least one value describing desired output features.
     * - The underlying repositories and {@link Brain} instance are available and operational.
     * 
     * Postconditions:
     * - Returns a non-null {@link MemoryRecord} that has been saved through {@link MemoryRecordRepository}.
     * - The returned memory record is associated with prediction edges representing scores for each output feature.
     * 
     * @param input stringified data used for prediction, either JSON or unstructured text
     * @param output_labels list of output feature labels for which predictions are generated
     * @return a persisted {@link MemoryRecord} containing prediction metadata
     * @throws IllegalArgumentException if provided arguments violate expected constraints
     * @throws IllegalAccessException if underlying reflection-based access fails
     * @throws NullPointerException if required dependencies are unexpectedly null
     * @throws JSONException if the input cannot be parsed as JSON when expected
     */
	@Operation(summary = "Make a prediction and return a MemoryRecord", description = "", tags = { "Reinforcement Learning" })
    @RequestMapping(value ="/predict", method = RequestMethod.POST)
    public @ResponseBody MemoryRecord predict(@Schema(description = "JSON representation of data", example = "{'field_1':{'field_2':'hello'}}", required = true) @RequestParam(value="input", required=true) String input,
    										  @Schema(description = "List of output labels to be predicted", example = "label_1,label_2,label_n", required = true) @RequestParam(value="output_features", required=true) String[] output_labels) 
    												  throws IllegalArgumentException, IllegalAccessException, NullPointerException{
		List<Feature> input_features; 
		try {
    		//Break down object into list of features
        	input_features = DataDecomposer.decompose(new JSONObject(input));
    	}
    	catch(JSONException e) {
    		input_features = DataDecomposer.decompose(input);
    	}
		
    	List<Feature> output_features = new ArrayList<Feature>();
		for(String value : output_labels){
			Feature feature_record = feature_repo.findByValue(value);
			if(feature_record==null){
				value = value.replace("[", "");
	        	value = value.replace("]", "");
				Feature new_feature = new Feature(value);
				output_features.add(new_feature);
			}
			else {
				output_features.add(feature_record);
			}
		}
    	
    	log.debug("loading vocabulary");
    	//Vocabulary input_vocab = vocabulary_repo.findByLabel(input_vocab_label);
    	//for each feature, check if feature is in input_vocab
    	List<String> input_feature_keys = new ArrayList<String>();
    	
    	List<Feature> scrubbed_input_features = new ArrayList<Feature>();
    	List<String> seen_features = new ArrayList<String>();
		for(Feature input_feature : input_features){
			boolean input_equals_output = false;
	    	for(Feature output_feature : output_features){
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
    		input_feature_keys.add(feature.getValue());
    	}

    	//List<Feature> output_feature = new_features;
    	String[] output_feature_keys = new String[output_features.size()];
    	
    	//load feature vector for output_vocab
    	log.debug("loading output feature set");
    	
    	//generate policy for input vocab feature vector and output vocab feature vector
    	double[][] policy = brain.generateRawPolicy(scrubbed_input_features, output_features);

    	//generate prediction
    	log.debug("Predicting...  "+policy);
    	double[] prediction = brain.predict(policy);

    	//create prediction edges
    	for(int i=0; i< output_features.size(); i++){
    		output_feature_keys[i] = output_features.get(i).getValue();
      	}
    	
    	double max_pred = 0.0;
    	int max_idx = 0;
    	for(int idx = 0; idx<prediction.length; idx++){
    		if(prediction[idx] > max_pred){
    			max_idx = idx;
    			max_pred = prediction[idx];
    		}
    	}
    	
    	//create memory and save vocabularies, policy matrix and prediction vector
    	MemoryRecord memory = new MemoryRecord();
    	memory.setPolicyMatrix(policy);
    	memory.setInputFeatureValues(input_feature_keys);
    	memory.setOutputFeatureKeys(output_feature_keys);
    	//memory.setPrediction(prediction);
    	memory.setPredictedFeature(output_features.get(max_idx));
		memory = memory_repo.save(memory);
		
		//iterate over features to create prediction edges for the memory
		List<Prediction> prediction_edges = new ArrayList<>();
		for(int i=0; i< output_features.size(); i++) {
    		Prediction prediction_edge = new Prediction(memory, output_features.get(i), prediction[i]);
    		prediction_edges.add(prediction_repo.save(prediction_edge));
		}
		
		memory.setPredictions(prediction_edges);
       	return memory;
	}
    
    /**
     * Applies learning to the provided feature for a given memory.
     * 
     * Preconditions:
     * - memory_id refers to an existing {@link MemoryRecord} managed by the system.
     * - feature_value is non-null and non-empty and represents the label to learn from.
     * 
     * Postconditions:
     * - The underlying {@link Brain#learn(long, Feature)} method is invoked with the resolved feature.
     * - Learning state is updated within the brain implementation; no {@link MemoryRecord} is directly returned.
     * 
     * @param memory_id unique identifier for the target memory
     * @param feature_value feature value used to label and learn from the memory
     * @throws JSONException if any JSON processing error occurs in downstream logic
     * @throws IllegalArgumentException if provided arguments are invalid for learning
     * @throws IllegalAccessException if underlying reflection-based access fails
     * @throws NullPointerException if required dependencies are unexpectedly null
     * @throws IOException if I/O errors occur during learning
     */
	@Operation(summary = "Applies learning to provided feature for a given memory", description = "", tags = { "Reinforcement Learning" })
    @RequestMapping(value ="/learn", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.ACCEPTED, reason = "Successfully learned from feedback")
    public  @ResponseBody void learn(@Schema(description = "unique identifier for specific memory", example = "12345", required = true) @RequestParam(value="memory_id", required=true) long memory_id, 
    								 @Schema(description = "value of feature that you want to label memory with and learn from", example = "VERB", required = true) @RequestParam(value="feature_value", required=true) String feature_value) 
					 throws JSONException, IllegalArgumentException, IllegalAccessException, NullPointerException, IOException
    {    	
    	Feature feature = new Feature(feature_value);
    	Feature feature_record = feature_repo.findByValue(feature.getValue());
    	if(feature_record != null){
    		feature = feature_record;
    	}
    	//LOAD OBJECT DEFINITION LIST BY DECOMPOSING json_string
	    brain.learn(memory_id, feature); //feature_list, predicted, feature, isRewarded);
    }
    
	/**
	 * Performs a training iteration using a label and the supplied object data.
	 * 
	 * Preconditions:
	 * - json_object is non-null and represents a JSON object string.
	 * - label is non-null and non-empty and describes the target concept.
	 * 
	 * Postconditions:
	 * - The underlying {@link Brain#train(List, String)} method is invoked with features decomposed from the JSON.
	 * - No value is returned; any learning effects are applied within the brain implementation.
	 * 
	 * @param json_object JSON object string containing training data
	 * @param label target label for training
	 * @throws JSONException if the json_object cannot be parsed as JSON
	 * @throws IllegalArgumentException if arguments violate training constraints
	 * @throws IllegalAccessException if underlying reflection-based access fails
	 * @throws NullPointerException if required dependencies are unexpectedly null
	 * @throws IOException if I/O errors occur during training
	 */
	@Operation(summary = "Performs training iteration using label and given object data", description = "", tags = { "Reinforcement Learning" })
    @RequestMapping(value ="/train", method = RequestMethod.POST)
    public  @ResponseBody void train(@RequestParam(value="json_object", required=true) String json_object,
    								 @RequestParam String label)
						 throws JSONException, IllegalArgumentException, IllegalAccessException, NullPointerException, IOException
    {
    
    	JSONObject json_obj = new JSONObject(json_object);
    	List<Feature> feature_list = DataDecomposer.decompose(json_obj);
    	brain.train(feature_list, label);
    }
}

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
class EmptyVocabularyException extends RuntimeException {
	/**
	 * Exception indicating that an expected vocabulary of output features was empty or null.
	 * 
	 * Preconditions:
	 * - Thrown only in contexts where a non-empty vocabulary is required.
	 * 
	 * Postconditions:
	 * - Signals an internal server error to the caller via the associated HTTP status.
	 */
	private static final long serialVersionUID = 7200878662560716216L;

	public EmptyVocabularyException() {
		super("Features list for output vocabulary cannot be empty or null");
	}
}
