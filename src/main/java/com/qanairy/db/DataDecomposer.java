package com.qanairy.db;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qanairy.models.ObjectDefinition;

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
	 */
	public static List<ObjectDefinition> decompose(Object obj) throws IllegalArgumentException, IllegalAccessException, NullPointerException{
		List<ObjectDefinition> objDefList = new ArrayList<ObjectDefinition>();
		
		Class<?> objClass = obj.getClass();
	    Field[] fields = objClass.getFields();
        log.info("LIST CLASS:: "+ objClass);
        log.info("FIELD COUNT : "+ fields.length);
	    for(Field field : fields) {
	        Object value = field.get(obj);
	        if(value!=null){
	        	ObjectDefinition objDef = null;
	        		
	        	if(value.getClass().equals(ArrayList.class)){
		        	log.info("Deconstructing Array list");
		        	ArrayList<?> list = ((ArrayList<?>)value);
		        	//return all elements of array
		        	List<ObjectDefinition> decomposedList = decomposeArrayList(list);
	        		objDefList.addAll(decomposedList);
		        }
	        	else if(value.getClass().equals(ArrayList.class)){
		        	log.info("Deconstructing Array list");
		        	ArrayList<?> list = ((ArrayList<?>)value);
		        	//return all elements of array
		        	List<ObjectDefinition> decomposedList = decomposeArrayList(list);
	        		objDefList.addAll(decomposedList);
		        }
		        else if(value.getClass().equals(String[].class)){
		        	log.info("Deconstructing String array");

		        	String[] array = (String[]) value;
		        	for(String stringVal : array){
		        		objDef = new ObjectDefinition(stringVal.toString(), stringVal.getClass().getSimpleName().replace(".", "").replace("[","").replace("]",""));
		        		objDefList.add(objDef);
		            }
		        }
		        else if(value.getClass().equals(Object[].class)){
		        	log.info("Deconstructing Object list");

		        	Object[] array = (Object[]) value;
		        	List<ObjectDefinition> decomposedList = decomposeObjectArray(array);
		        	objDefList.addAll(decomposedList);
		        }
		        else{
		        	log.info("Creating object definition for field");

	        		objDef = new ObjectDefinition(value.toString(), field.getType().getSimpleName().replace(".", "").replace("[","").replace("]",""));
		        	objDefList.add(objDef);
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
	public static List<ObjectDefinition> decompose(HashMap<?,?> map) throws IllegalArgumentException, IllegalAccessException, NullPointerException{
		List<ObjectDefinition> objDefList = new ArrayList<ObjectDefinition>();
		
		Class<?> objClass = map.getClass();
        log.info("LIST CLASS:: "+ objClass);

		for(Object key : map.keySet()){
			Object value = map.get(key);
			if(value!=null){
	        	ObjectDefinition objDef = null;
	        		
	        	if(value.getClass().equals(ArrayList.class)){
		        	log.info("Deconstructing Array list");
		        	ArrayList<?> list = ((ArrayList<?>)value);
		        	//return all elements of array
		        	List<ObjectDefinition> decomposedList = decomposeArrayList(list);
	        		objDefList.addAll(decomposedList);
		        }
	        	else if(value.getClass().equals(ArrayList.class)){
		        	log.info("Deconstructing Array list");
		        	ArrayList<?> list = ((ArrayList<?>)value);
		        	//return all elements of array
		        	List<ObjectDefinition> decomposedList = decomposeArrayList(list);
	        		objDefList.addAll(decomposedList);
		        }
		        else if(value.getClass().equals(String[].class)){
		        	log.info("Deconstructing String array");

		        	String[] array = (String[]) value;
		        	for(String stringVal : array){
		        		objDef = new ObjectDefinition(stringVal.toString(), stringVal.getClass().getSimpleName().replace(".", "").replace("[","").replace("]",""));
		        		objDefList.add(objDef);
		            }
		        }
		        else if(value.getClass().equals(Object[].class)){
		        	log.info("Deconstructing Object list");

		        	Object[] array = (Object[]) value;
		        	List<ObjectDefinition> decomposedList = decomposeObjectArray(array);
		        	objDefList.addAll(decomposedList);
		        }
		        else{
		        	log.info("Creating object definition for field");

	        		objDef = new ObjectDefinition(value.toString(), key.getClass().getSimpleName().replace(".", "").replace("[","").replace("]",""));
		        	objDefList.add(objDef);
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
	private static List<ObjectDefinition> decomposeObjectArray(Object[] array) throws IllegalArgumentException, IllegalAccessException{
    	List<ObjectDefinition> objDefList = new ArrayList<ObjectDefinition>();
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
	private static List<ObjectDefinition> decomposeArrayList(ArrayList<?> list) throws IllegalArgumentException, IllegalAccessException, NullPointerException {
    	List<ObjectDefinition> objDefList = new ArrayList<ObjectDefinition>();
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
