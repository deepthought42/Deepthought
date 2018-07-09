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

import com.deepthought.models.Action;
import com.deepthought.models.repository.ActionRepository;
import com.deepthought.models.repository.FeatureRepository;


/**
 *	API endpoints for interacting with {@link Domain} data
 */
@RestController
@RequestMapping("/actions")
public class ActionController {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ActionRepository action_repo;

	@Autowired
	private FeatureRepository feature_repo;
	
	@RequestMapping(method = RequestMethod.GET)
    public @ResponseBody List<Action> getAll(){
		return IterableUtils.toList(action_repo.findAll());
	}
	
	@RequestMapping(method = RequestMethod.POST)
	public void createAction(@RequestParam(value="name", required=true) String name,
							 @RequestParam(value="value", required=true) String value){
		Action action = new Action(name, value);
		action_repo.save(action);
	}
}
