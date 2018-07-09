package com.qanairy.api;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.util.IterableUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.deepthought.models.Feature;
import com.deepthought.models.repository.FeatureRepository;

/**
 *	API endpoints for interacting with {@link Domain} data
 */
@RestController
@RequestMapping("/features")
public class FeatureController {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private FeatureRepository feature_repo;
	
	@RequestMapping(method = RequestMethod.GET)
    public @ResponseBody List<Feature> getAll(){
		return IterableUtils.toList(feature_repo.findAll());
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public void createFeature(@RequestParam(value="name", required=true) String name,
							 @RequestParam(value="value", required=true) String value){
		Feature feature = new Feature(name, value);
		feature_repo.save(feature);
	}
	
}
