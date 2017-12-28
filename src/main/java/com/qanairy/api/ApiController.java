package com.qanairy.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
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

import com.qanairy.brain.ActionFactory;
import com.qanairy.brain.Brain;
import com.qanairy.db.DataDecomposer;
import com.qanairy.models.ObjectDefinition;


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
    	/*log.info("digesting Object : " +obj);
    	List<ObjectDefinition> object_definitions = DataDecomposer.decompose(obj);
    	log.info("Finished decomposing object into value list with length :: "+object_definitions.size());
    	*/
    	
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
