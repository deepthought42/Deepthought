package com.qanairy.api;

import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.deepthought.models.ObjectDefinition;
import com.qanairy.db.DataDecomposer;


/**
 *	API endpoints for interacting with {@link Domain} data
 */
@RestController
@RequestMapping("/data")
public class DataController {
	
	private final Logger log = LoggerFactory.getLogger(this.getClass());
	
	@RequestMapping(method = RequestMethod.POST)
    public @ResponseBody List<ObjectDefinition> decompose(JSONObject obj) throws IllegalArgumentException, IllegalAccessException, NullPointerException, JSONException{
		return DataDecomposer.decompose(obj);
	}
}
