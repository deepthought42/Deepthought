package com.qanairy.db;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.deepthought.models.Feature;

/**
 * Defines static methods to handle the decomposition of it's data into their constituent pieces.
 */
public class DataDecomposer {
    private static Logger log = LoggerFactory.getLogger(DataDecomposer.class);

    /**
	 * Decomposes object into data fragments
	 * 
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
     * @throws JSONException 
	 */
	public static List<Feature> decompose(JSONObject jsonObject) 
			throws IllegalArgumentException, IllegalAccessException, NullPointerException, JSONException{
		List<Feature> objDefList = new ArrayList<Feature>();
		Iterator<String> iter = jsonObject.keys();
		
		while(iter.hasNext()){
			String key = iter.next();
			log.info("Object Field Name :: "+key);
			Object value = jsonObject.get(key);
			if(value!=null){
	        	Feature objDef = null;
	        	log.info("System class :: "+value.getClass());
	        	log.info("VALUE BEING READ :::  "+value.toString());
	        	if(value.toString().length()>=3 && value.toString().substring(0, 2).equals("[{")){
	        		log.info("converting to json string");
	        		String json_string = value.toString().substring(1, value.toString().length()-1);
		        	JSONObject obj = new JSONObject(json_string);
		        	List<Feature> definition_list = decompose(obj);
		        	objDefList.addAll(definition_list);
	        	}
	        	else if(value.getClass().equals(ArrayList.class)){
		        	log.info("Deconstructing Array list");
		        	ArrayList<?> list = ((ArrayList<?>)value);
		        	//return all elements of array
		        	List<Feature> decomposedList = decomposeArrayList(list);
	        		objDefList.addAll(decomposedList);
		        }
	        	else if(value.getClass().equals(JSONObject.class)){
	        		JSONObject json = new JSONObject(value.toString());
	        		System.out.println("JSON :: "+json);
	        	}
		        else if(value.getClass().equals(String[].class)){
		        	log.info("Deconstructing String array");

		        	String[] array = (String[]) value;
		        	for(String stringVal : array){
		        		objDef = new Feature(stringVal.toString());
		        		objDefList.add(objDef);
		            }
		        }
		        else if(value.getClass().equals(Object[].class)){
		        	log.info("Deconstructing Object list");

		        	Object[] array = (Object[]) value;
		        	List<Feature> decomposedList = decomposeObjectArray(array);
		        	objDefList.addAll(decomposedList);
		        }
		        else if(value.getClass().equals(JSONArray.class)){
		        	log.info("Deconstructing JSONArray list");
		        	JSONArray array = new JSONArray(value.toString());
		        	for(int idx=0; idx<array.length(); idx++){
		        		objDefList.addAll(decompose(array.get(idx).toString()));
		        	}
		        }
		        else{
		        	String[] words = value.toString().split("\\s+");
		        	log.info("parsing string .....");
		        	for(String word : words){
		        		objDef = new Feature(word);
		        		objDefList.add(objDef);
		        	}		        	
		        }
	        	log.info("Ending list size :: "+objDefList.size());

			}
        }
		
		//log.info("Object definition List size :: "+objDefList.size());
		return objDefList;
		
	}

	/**
	 * Decomposes object into data fragments
	 * 
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static List<Feature> decompose(String value) throws IllegalArgumentException, IllegalAccessException, NullPointerException{
		List<Feature> objDefList = new ArrayList<Feature>();
    	log.info("Creating object definition for String");
    	String[] words = value.toString().split("\\s+");
    	log.info("VALUE :: " +value.toString());
    	log.info("words :: "+words.length);
    	for(String word : words){
    		log.info("word :: "+word);
    		Feature objDef = new Feature(word);
    		objDefList.add(objDef);
    	}
	        	
		return objDefList;
	}

	/**
	 * Decomposes object into data fragments
	 * 
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static List<Feature> decompose(Object obj) throws IllegalArgumentException, IllegalAccessException, NullPointerException{
		List<Feature> objDefList = new ArrayList<Feature>();
		JSONObject jsonObject = new JSONObject();
		Iterator<String> iter = jsonObject.keys();
		
		while(iter.hasNext()){
			iter.next();
		}
		Class<?> objClass = obj.getClass();
	    Field[] fields = objClass.getFields();
        log.info("LIST CLASS:: "+ objClass);
        log.info("FIELD COUNT : "+ fields.length);
	    for(Field field : fields) {
	        Object value = field.get(obj);
	        if(value!=null){
	        	Feature objDef = null;
	        		
	        	if(value.getClass().equals(ArrayList.class)){
		        	log.info("Deconstructing Array list");
		        	ArrayList<?> list = ((ArrayList<?>)value);
		        	//return all elements of array
		        	List<Feature> decomposedList = decomposeArrayList(list);
	        		objDefList.addAll(decomposedList);
		        }
	        	else if(value.getClass().equals(ArrayList.class)){
		        	log.info("Deconstructing Array list");
		        	ArrayList<?> list = ((ArrayList<?>)value);
		        	//return all elements of array
		        	List<Feature> decomposedList = decomposeArrayList(list);
	        		objDefList.addAll(decomposedList);
		        }
		        else if(value.getClass().equals(String[].class)){
		        	log.info("Deconstructing String array");

		        	String[] array = (String[]) value;
		        	for(String stringVal : array){
		        		objDef = new Feature(stringVal.toString());
		        		objDefList.add(objDef);
		            }
		        }
		        else if(value.getClass().equals(Object[].class)){
		        	log.info("Deconstructing Object list");

		        	Object[] array = (Object[]) value;
		        	List<Feature> decomposedList = decomposeObjectArray(array);
		        	objDefList.addAll(decomposedList);
		        }
		        else{
		        	log.info("Creating object definition for field");
		        	String[] words = value.toString().split("\\s+");
		        	log.info("VALUE :: " +value.toString());
		        	log.info("words :: "+words.length);
		        	for(String word : words){
		        		log.info("word :: "+word);
		        		objDef = new Feature(word);
		        		objDefList.add(objDef);
		        	}
		        	
	        		//objDef = new Feature(value.toString(), field.getType().getSimpleName().replace(".", "").replace("[","").replace("]",""));
		        	//objDefList.add(objDef);
		        }
	        }
	    }
		return objDefList;
	}
	
	/**
	 * Decomposes object into data fragments
	 * 
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static List<Feature> decompose(HashMap<?,?> map) throws IllegalArgumentException, IllegalAccessException, NullPointerException{
		List<Feature> objDefList = new ArrayList<Feature>();
		
		Class<?> objClass = map.getClass();
        log.info("LIST CLASS:: "+ objClass);

		for(Object key : map.keySet()){
			Object value = map.get(key);
			if(value!=null){
	        	Feature objDef = null;
	        		
	        	if(value.getClass().equals(ArrayList.class)){
		        	log.info("Deconstructing Array list");
		        	ArrayList<?> list = ((ArrayList<?>)value);
		        	//return all elements of array
		        	List<Feature> decomposedList = decomposeArrayList(list);
	        		objDefList.addAll(decomposedList);
		        }
	        	else if(value.getClass().equals(ArrayList.class)){
		        	log.info("Deconstructing Array list");
		        	ArrayList<?> list = ((ArrayList<?>)value);
		        	//return all elements of array
		        	List<Feature> decomposedList = decomposeArrayList(list);
	        		objDefList.addAll(decomposedList);
		        }
		        else if(value.getClass().equals(String[].class)){
		        	log.info("Deconstructing String array");

		        	String[] array = (String[]) value;
		        	for(String stringVal : array){
		        		objDef = new Feature(stringVal.toString());
		        		objDefList.add(objDef);
		            }
		        }
		        else if(value.getClass().equals(Object[].class)){
		        	log.info("Deconstructing Object list");

		        	Object[] array = (Object[]) value;
		        	List<Feature> decomposedList = decomposeObjectArray(array);
		        	objDefList.addAll(decomposedList);
		        }
		        else{
		        	log.info("Creating object definition for field");
		        	String[] words = value.toString().split("\\s+");

		        	for(String word : words){
		        		objDef = new Feature(word);
		        		objDefList.add(objDef);
		        	}
		        }
	        }
		}
		return objDefList;
	}
	
	/**
	 * Decomposes an array of Objects into memory blocks
	 * @param array
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private static List<Feature> decomposeObjectArray(Object[] array) throws IllegalArgumentException, IllegalAccessException{
    	List<Feature> objDefList = new ArrayList<Feature>();
		if(array == null || array.length == 0){
			return objDefList;
		}
    	
        for(Object object : array){
        	objDefList.addAll(DataDecomposer.decompose(object));
        }
		return objDefList;
	}

	/**
	 * Iterates over ArrayList of objects, and decomposes each object
	 * 
	 * @param list
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	private static List<Feature> decomposeArrayList(ArrayList<?> list) throws IllegalArgumentException, IllegalAccessException, NullPointerException {
    	List<Feature> objDefList = new ArrayList<Feature>();
		if(list == null || list.isEmpty()){
			return objDefList;
		}
		
        for(Object object : list){
        	if(object != null){
        		objDefList.addAll(DataDecomposer.decompose(object));
        	}
        }
		return objDefList;
	}
}
