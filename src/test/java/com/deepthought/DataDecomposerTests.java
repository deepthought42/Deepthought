package com.deepthought;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.deepthought.data.db.DataDecomposer;
import com.deepthought.data.models.Feature;

public class DataDecomposerTests {

	/**
	 * Purpose: Test decomposition of JSONObject into Feature objects
	 * 
	 * Steps:
	 * 1. Create a JSONObject with string and nested object values
	 * 2. Decompose the JSONObject using DataDecomposer
	 * 3. Verify that all expected words from the JSON are present as Features
	 * 4. Assert that all expected keys were found in the decomposition
	 */
	@Test
	public void decomposeGenericObject(){
		// Step 1: Setup - Create JSONObject with various value types
		String[] expectedKeys = {"String", "value", "object", "key", "here"};
		JSONObject json_obj = new JSONObject();
		try {
			// Add a string value that will be split into words
			json_obj.put("string_val", "String value");
			// Add a nested JSONObject
			JSONObject obj = new JSONObject();
			obj.put("object_key", "object key here");
			json_obj.put("obj", obj);
		} catch (JSONException e) {
			Assert.fail("Failed to create JSONObject: " + e.getMessage());
		}
		
		try {
			// Step 2: Decompose the JSONObject into Feature list
			List<Feature> object_definition_list = DataDecomposer.decompose(json_obj);
			
			// Step 3: Convert Feature list to Map for easy lookup by value
			Map<String, Feature> featureMap = new HashMap<String, Feature>();
			for(Feature feature : object_definition_list){
				featureMap.put(feature.getValue(), feature);
			}
			
			// Step 4: Verify all expected keys are present in the decomposition
			for(String expectedKey : expectedKeys){
				Assert.assertTrue(featureMap.containsKey(expectedKey), 
					"Expected key '" + expectedKey + "' not found in decomposition");
				featureMap.remove(expectedKey);
			}
			
			// Assert that only expected keys were present (map should be empty)
			Assert.assertTrue(featureMap.isEmpty(), 
				"Unexpected features found in decomposition: " + featureMap.keySet());
		} catch (IllegalArgumentException e) {
			Assert.fail("IllegalArgumentException during decomposition: " + e.getMessage());
		} catch (IllegalAccessException e) {
			Assert.fail("IllegalAccessException during decomposition: " + e.getMessage());
		} catch (NullPointerException e) {
			Assert.fail("NullPointerException during decomposition: " + e.getMessage());
		} catch (JSONException e) {
			Assert.fail("JSONException during decomposition: " + e.getMessage());
		}
	}
	
	/**
	 * Purpose: Test decomposition of HashMap into Feature objects
	 * 
	 * Steps:
	 * 1. Create a HashMap with different value types (String, String array, ArrayList, Object array)
	 * 2. Decompose the HashMap using DataDecomposer.decompose(HashMap)
	 * 3. Verify that all expected words from map values are present as Features
	 * 4. Assert correct handling of various value types within the map
	 */
	@Test
	public void decomposeMap(){
		// Step 1: Setup - Create HashMap with various value types
		HashMap<String, Object> testMap = new HashMap<String, Object>();
		
		// Add string value that will be split into words
		testMap.put("stringKey", "Hello world test");
		
		// Add String array
		String[] stringArray = {"array", "value", "one", "array", "value", "two"};
		testMap.put("arrayKey", stringArray);
		
		// Add ArrayList
		ArrayList<String> stringList = new ArrayList<String>();
		stringList.add("list item one");
		stringList.add("list item two");
		testMap.put("listKey", stringList);
		
		// Add Object array
		Object[] objectArray = {"object", "array", "element"};
		testMap.put("objectArrayKey", objectArray);
		
		// Expected feature values from the decomposition
		// From "Hello world test": "Hello", "world", "test"
		// From stringArray: "array", "value", "one", "array", "value", "two"
		// From stringList: "list", "item", "one", "list", "item", "two"
		// From objectArray: "object", "array", "element"
		String[] expectedFeatures = {
			"Hello", "world", "test",
			"array", "value", "one", "two",
			"list", "item",
			"object", "element"
		};
		
		try {
			// Step 2: Decompose the HashMap into Feature list
			List<Feature> featureList = DataDecomposer.decompose(testMap);
			
			// Step 3: Convert to Map for easier validation
			Map<String, Integer> featureCounts = new HashMap<String, Integer>();
			for(Feature feature : featureList) {
				String value = feature.getValue();
				featureCounts.put(value, featureCounts.getOrDefault(value, 0) + 1);
			}
			
			// Step 4: Verify all expected features are present
			for(String expectedFeature : expectedFeatures) {
				Assert.assertTrue(featureCounts.containsKey(expectedFeature), 
					"Expected feature '" + expectedFeature + "' not found in decomposition");
			}
			
			// Verify we got features from all map entries
			Assert.assertTrue(featureList.size() > 0, 
				"Decomposition should return at least one feature");
			
		} catch (IllegalArgumentException e) {
			Assert.fail("IllegalArgumentException during map decomposition: " + e.getMessage());
		} catch (IllegalAccessException e) {
			Assert.fail("IllegalAccessException during map decomposition: " + e.getMessage());
		} catch (NullPointerException e) {
			Assert.fail("NullPointerException during map decomposition: " + e.getMessage());
		}
	}
	
	/**
	 * Purpose: Test decomposition of ArrayList containing objects into Feature objects
	 * 
	 * Steps:
	 * 1. Create an ArrayList containing objects (in this case, simple objects with public fields)
	 * 2. Decompose the ArrayList using DataDecomposer (via decompose(Object) which handles ArrayList)
	 * 3. Verify that features are extracted from all objects in the list
	 * 4. Assert correct recursive decomposition of list elements
	 */
	@Test
	public void decomposeObjectList(){
		// Step 1: Setup - Create ArrayList with test objects
		ArrayList<TestObject> objectList = new ArrayList<TestObject>();
		
		// Create first test object with string field
		TestObject obj1 = new TestObject();
		obj1.testField = "first object value";
		objectList.add(obj1);
		
		// Create second test object with string field
		TestObject obj2 = new TestObject();
		obj2.testField = "second object test";
		objectList.add(obj2);
		
		// Create third test object with string field
		TestObject obj3 = new TestObject();
		obj3.testField = "third value";
		objectList.add(obj3);
		
		// Expected feature values from decomposition
		// From obj1: "first", "object", "value"
		// From obj2: "second", "object", "test"
		// From obj3: "third", "value"
		String[] expectedFeatures = {
			"first", "second", "third", "object", "value", "test"
		};
		
		try {
			// Step 2: Decompose the ArrayList by passing it as an Object
			// Note: DataDecomposer.decompose(Object) handles ArrayList through reflection
			List<Feature> featureList = DataDecomposer.decompose(objectList);
			
			// Step 3: Convert to Map for validation
			Map<String, Feature> featureMap = new HashMap<String, Feature>();
			for(Feature feature : featureList) {
				featureMap.put(feature.getValue(), feature);
			}
			
			// Step 4: Verify expected features are present
			for(String expectedFeature : expectedFeatures) {
				Assert.assertTrue(featureMap.containsKey(expectedFeature), 
					"Expected feature '" + expectedFeature + "' not found in list decomposition");
			}
			
			// Verify we got features from all list items
			Assert.assertTrue(featureList.size() >= expectedFeatures.length, 
				"Decomposition should extract features from all list elements");
			
		} catch (IllegalArgumentException e) {
			Assert.fail("IllegalArgumentException during list decomposition: " + e.getMessage());
		} catch (IllegalAccessException e) {
			Assert.fail("IllegalAccessException during list decomposition: " + e.getMessage());
		} catch (NullPointerException e) {
			Assert.fail("NullPointerException during list decomposition: " + e.getMessage());
		}
	}
	
	/**
	 * Purpose: Test decomposition of Object array into Feature objects
	 * 
	 * Steps:
	 * 1. Create an Object array containing objects with public fields
	 * 2. Decompose the Object array using DataDecomposer (via decompose(Object) or through HashMap)
	 * 3. Verify that features are extracted from all objects in the array
	 * 4. Assert correct handling of array elements through recursive decomposition
	 */
	@Test
	public void decomposeObjectArray(){
		// Step 1: Setup - Create Object array with test objects
		TestObject[] objectArray = new TestObject[3];
		
		// Create and populate first object
		TestObject obj1 = new TestObject();
		obj1.testField = "array element one";
		objectArray[0] = obj1;
		
		// Create and populate second object
		TestObject obj2 = new TestObject();
		obj2.testField = "array element two";
		objectArray[1] = obj2;
		
		// Create and populate third object
		TestObject obj3 = new TestObject();
		obj3.testField = "third element";
		objectArray[2] = obj3;
		
		// Expected feature values from decomposition
		// From obj1: "array", "element", "one"
		// From obj2: "array", "element", "two"
		// From obj3: "third", "element"
		String[] expectedFeatures = {
			"array", "element", "one", "two", "third"
		};
		
		try {
			// Step 2: Decompose by wrapping in HashMap (as Object array handling)
			// Note: Object arrays are typically handled through HashMap or direct decompose(Object)
			HashMap<String, Object> mapWithArray = new HashMap<String, Object>();
			mapWithArray.put("arrayKey", objectArray);
			
			List<Feature> featureList = DataDecomposer.decompose(mapWithArray);
			
			// Step 3: Convert to Map for validation
			Map<String, Feature> featureMap = new HashMap<String, Feature>();
			for(Feature feature : featureList) {
				featureMap.put(feature.getValue(), feature);
			}
			
			// Step 4: Verify expected features are present
			for(String expectedFeature : expectedFeatures) {
				Assert.assertTrue(featureMap.containsKey(expectedFeature), 
					"Expected feature '" + expectedFeature + "' not found in array decomposition");
			}
			
			// Verify we got features from all array elements
			Assert.assertTrue(featureList.size() >= expectedFeatures.length, 
				"Decomposition should extract features from all array elements");
			
		} catch (IllegalArgumentException e) {
			Assert.fail("IllegalArgumentException during array decomposition: " + e.getMessage());
		} catch (IllegalAccessException e) {
			Assert.fail("IllegalAccessException during array decomposition: " + e.getMessage());
		} catch (NullPointerException e) {
			Assert.fail("NullPointerException during array decomposition: " + e.getMessage());
		}
	}
	
	/**
	 * Simple test object with a public field for testing decomposition
	 * Used by decomposeObjectList and decomposeObjectArray tests
	 */
	static class TestObject {
		public String testField;
	}
}
