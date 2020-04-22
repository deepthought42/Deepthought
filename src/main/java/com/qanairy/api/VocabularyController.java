package com.qanairy.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.deepthought.models.Feature;
import com.deepthought.models.Vocabulary;
import com.deepthought.models.repository.FeatureRepository;
import com.deepthought.models.repository.VocabularyRepository;


/**
 *	API endpoints for interacting with {@link Vocabulary} data
 */
@RestController
@RequestMapping("/vocabulary")
public class VocabularyController {	
	@SuppressWarnings("unused")
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private VocabularyRepository vocab_repo;
	
	@Autowired
	private FeatureRepository feature_repo;
	
	/**
	 * Retrieves a vocabulary from the database using the given label
	 * 
	 * @param label the label of the {@link Vocabulary}
	 * 
	 * @return {@link Vocabulary} object
	 */
	@RequestMapping(method = RequestMethod.GET)
    public @ResponseBody Vocabulary getVocabulary(@RequestParam(name="label", required=true)String label) {
		return vocab_repo.findByLabel(label);
	}
	
	/**
	 * Create a new Vocabulary by assigning it a label. Will not work if a vocabulary already exists with this label
	 * 
	 * @param label the label of the {@link Vocabulary}
	 * 
	 * @return {@link Vocabulary} object
	 */
	@RequestMapping(method = RequestMethod.POST)
    public @ResponseBody Vocabulary createVocabulary(String label) {
		return vocab_repo.save(new Vocabulary(label));
	}
    	
	/**
	 * Add a new feature to an existing {@link Vocabulary}
	 * 
	 * @param key Vocabulary identifier key
	 * @param feature JSONObject conforming to format of {@link Feature} model
	 * 
	 * @return {@link Vocabulary} object
	 */
	@RequestMapping(path="/feature", method = RequestMethod.POST)
    public @ResponseBody Vocabulary addFeature(@RequestParam(name="label", required=true) String label, 
    										   @RequestParam(name="feature_value", required=true) String feature_value){
		Feature feature = feature_repo.findByValue(feature_value);
		if(feature == null){
			feature = new Feature(feature_value.toLowerCase());
			feature = feature_repo.save(feature);
		}
		Vocabulary vocab = vocab_repo.findByLabel(label);
		vocab.getFeatures().add(feature);
		return vocab_repo.save(vocab);
	}
}
