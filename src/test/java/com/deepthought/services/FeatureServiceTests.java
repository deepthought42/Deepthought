package com.deepthought.services;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.deepthought.data.models.Feature;
import com.deepthought.data.repository.FeatureRepository;

/**
 * Test suite for FeatureService
 * 
 * Tests validate the FeatureService which provides:
 * 1. findByValue(String) - Finds a Feature by its value using FeatureRepository
 * 
 * FeatureService acts as a service layer wrapper around FeatureRepository,
 * providing transactional read-only access to Feature data.
 */
public class FeatureServiceTests {

	@Mock
	private FeatureRepository featureRepository;

	@InjectMocks
	private FeatureService featureService;

	/**
	 * Purpose: Set up test fixture before each test method
	 * 
	 * Steps:
	 * 1. Initialize Mockito mocks
	 * 2. Inject mocked repository into service
	 */
	@BeforeMethod
	public void setUp() {
		// Step 1 & 2: Initialize mocks and inject into service
		MockitoAnnotations.initMocks(this);
	}

	/**
	 * Purpose: Test findByValue with existing feature
	 * 
	 * Steps:
	 * 1. Create a Feature with a specific value
	 * 2. Mock repository to return the feature when findByValue is called
	 * 3. Call service findByValue method
	 * 4. Verify result matches expected feature
	 * 5. Verify repository method was called with correct parameter
	 */
	@Test
	public void testFindByValueWithExistingFeature() {
		// Step 1: Setup - Create feature
		String featureValue = "button";
		Feature expectedFeature = new Feature(featureValue);
		
		// Step 2: Setup - Mock repository behavior
		Mockito.when(featureRepository.findByValue(featureValue)).thenReturn(expectedFeature);
		
		// Step 3: Execute - Call service method
		Feature result = featureService.findByValue(featureValue);
		
		// Step 4: Verify - Result should match expected feature
		Assert.assertNotNull(result, "findByValue should return a Feature when it exists");
		Assert.assertEquals(result, expectedFeature, 
			"Result should be the same Feature instance returned by repository");
		Assert.assertEquals(result.getValue(), featureValue, 
			"Feature value should match the search value");
		
		// Step 5: Verify - Repository method was called correctly
		Mockito.verify(featureRepository, Mockito.times(1)).findByValue(featureValue);
	}

	/**
	 * Purpose: Test findByValue with non-existent feature
	 * 
	 * Steps:
	 * 1. Set up a value that doesn't exist in repository
	 * 2. Mock repository to return null for non-existent value
	 * 3. Call service findByValue method
	 * 4. Verify result is null
	 * 5. Verify repository method was called with correct parameter
	 */
	@Test
	public void testFindByValueWithNonExistentFeature() {
		// Step 1: Setup - Use value that doesn't exist
		String nonExistentValue = "nonexistent";
		
		// Step 2: Setup - Mock repository to return null
		Mockito.when(featureRepository.findByValue(nonExistentValue)).thenReturn(null);
		
		// Step 3: Execute - Call service method
		Feature result = featureService.findByValue(nonExistentValue);
		
		// Step 4: Verify - Result should be null
		Assert.assertNull(result, 
			"findByValue should return null when feature does not exist");
		
		// Step 5: Verify - Repository method was called correctly
		Mockito.verify(featureRepository, Mockito.times(1)).findByValue(nonExistentValue);
	}

	/**
	 * Purpose: Test findByValue with null input
	 * 
	 * Steps:
	 * 1. Mock repository to return null for null input
	 * 2. Call service findByValue with null
	 * 3. Verify result is null (handles null gracefully)
	 * 4. Verify repository method was called with null
	 */
	@Test
	public void testFindByValueWithNullInput() {
		// Step 1: Setup - Mock repository to return null for null input
		Mockito.when(featureRepository.findByValue(null)).thenReturn(null);
		
		// Step 2: Execute - Call service method with null
		Feature result = featureService.findByValue(null);
		
		// Step 3: Verify - Result should be null
		Assert.assertNull(result, 
			"findByValue should return null when input value is null");
		
		// Step 4: Verify - Repository method was called with null
		Mockito.verify(featureRepository, Mockito.times(1)).findByValue(null);
	}

	/**
	 * Purpose: Test findByValue with empty string
	 * 
	 * Steps:
	 * 1. Create a Feature with empty string value
	 * 2. Mock repository to return the feature for empty string
	 * 3. Call service findByValue with empty string
	 * 4. Verify result is the expected feature
	 * 5. Verify repository method was called with empty string
	 */
	@Test
	public void testFindByValueWithEmptyString() {
		// Step 1: Setup - Create feature with empty value
		String emptyValue = "";
		Feature expectedFeature = new Feature(emptyValue);
		
		// Step 2: Setup - Mock repository behavior
		Mockito.when(featureRepository.findByValue(emptyValue)).thenReturn(expectedFeature);
		
		// Step 3: Execute - Call service method
		Feature result = featureService.findByValue(emptyValue);
		
		// Step 4: Verify - Result should be the expected feature
		Assert.assertNotNull(result, 
			"findByValue should return a Feature even when value is empty string");
		Assert.assertEquals(result, expectedFeature, 
			"Result should match the Feature returned by repository");
		
		// Step 5: Verify - Repository method was called with empty string
		Mockito.verify(featureRepository, Mockito.times(1)).findByValue(emptyValue);
	}

	/**
	 * Purpose: Test findByValue with different feature values
	 * 
	 * Steps:
	 * 1. Create multiple features with different values
	 * 2. Mock repository to return different features for different values
	 * 3. Call service findByValue with different values
	 * 4. Verify each call returns the correct feature
	 * 5. Verify repository was called correctly for each value
	 */
	@Test
	public void testFindByValueWithDifferentValues() {
		// Step 1: Setup - Create multiple features
		String value1 = "button";
		String value2 = "input";
		String value3 = "submit";
		Feature feature1 = new Feature(value1);
		Feature feature2 = new Feature(value2);
		Feature feature3 = new Feature(value3);
		
		// Step 2: Setup - Mock repository to return different features
		Mockito.when(featureRepository.findByValue(value1)).thenReturn(feature1);
		Mockito.when(featureRepository.findByValue(value2)).thenReturn(feature2);
		Mockito.when(featureRepository.findByValue(value3)).thenReturn(feature3);
		
		// Step 3: Execute - Call service method with different values
		Feature result1 = featureService.findByValue(value1);
		Feature result2 = featureService.findByValue(value2);
		Feature result3 = featureService.findByValue(value3);
		
		// Step 4: Verify - Each result should match expected feature
		Assert.assertEquals(result1, feature1, 
			"First call should return feature1");
		Assert.assertEquals(result2, feature2, 
			"Second call should return feature2");
		Assert.assertEquals(result3, feature3, 
			"Third call should return feature3");
		
		// Step 5: Verify - Repository was called correctly for each value
		Mockito.verify(featureRepository, Mockito.times(1)).findByValue(value1);
		Mockito.verify(featureRepository, Mockito.times(1)).findByValue(value2);
		Mockito.verify(featureRepository, Mockito.times(1)).findByValue(value3);
	}

	/**
	 * Purpose: Test that service correctly delegates to repository
	 * 
	 * Steps:
	 * 1. Create a feature and mock repository response
	 * 2. Call service method multiple times
	 * 3. Verify service delegates to repository each time
	 * 4. Verify repository is called the correct number of times
	 */
	@Test
	public void testServiceDelegatesToRepository() {
		// Step 1: Setup - Create feature and mock repository
		String featureValue = "test";
		Feature expectedFeature = new Feature(featureValue);
		Mockito.when(featureRepository.findByValue(featureValue)).thenReturn(expectedFeature);
		
		// Step 2: Execute - Call service method multiple times
		featureService.findByValue(featureValue);
		featureService.findByValue(featureValue);
		featureService.findByValue(featureValue);
		
		// Step 3 & 4: Verify - Repository should be called 3 times
		Mockito.verify(featureRepository, Mockito.times(3)).findByValue(featureValue);
	}

	/**
	 * Purpose: Test findByValue with special characters in value
	 * 
	 * Steps:
	 * 1. Create a feature with special characters in value
	 * 2. Mock repository to return the feature
	 * 3. Call service findByValue with special character value
	 * 4. Verify result matches expected feature
	 * 5. Verify repository method was called correctly
	 */
	@Test
	public void testFindByValueWithSpecialCharacters() {
		// Step 1: Setup - Create feature with special characters
		String specialValue = "test-value_123";
		Feature expectedFeature = new Feature(specialValue);
		
		// Step 2: Setup - Mock repository behavior
		Mockito.when(featureRepository.findByValue(specialValue)).thenReturn(expectedFeature);
		
		// Step 3: Execute - Call service method
		Feature result = featureService.findByValue(specialValue);
		
		// Step 4: Verify - Result should match expected feature
		Assert.assertNotNull(result, 
			"findByValue should handle special characters in value");
		Assert.assertEquals(result.getValue(), specialValue, 
			"Feature value should preserve special characters");
		
		// Step 5: Verify - Repository method was called correctly
		Mockito.verify(featureRepository, Mockito.times(1)).findByValue(specialValue);
	}

	/**
	 * Purpose: Test findByValue with whitespace in value
	 * 
	 * Steps:
	 * 1. Create a feature with whitespace in value
	 * 2. Mock repository to return the feature
	 * 3. Call service findByValue with whitespace value
	 * 4. Verify result matches expected feature
	 * 5. Verify repository method was called correctly
	 */
	@Test
	public void testFindByValueWithWhitespace() {
		// Step 1: Setup - Create feature with whitespace
		String whitespaceValue = "test value";
		Feature expectedFeature = new Feature(whitespaceValue);
		
		// Step 2: Setup - Mock repository behavior
		Mockito.when(featureRepository.findByValue(whitespaceValue)).thenReturn(expectedFeature);
		
		// Step 3: Execute - Call service method
		Feature result = featureService.findByValue(whitespaceValue);
		
		// Step 4: Verify - Result should match expected feature
		Assert.assertNotNull(result, 
			"findByValue should handle whitespace in value");
		Assert.assertEquals(result.getValue(), whitespaceValue, 
			"Feature value should preserve whitespace");
		
		// Step 5: Verify - Repository method was called correctly
		Mockito.verify(featureRepository, Mockito.times(1)).findByValue(whitespaceValue);
	}

	/**
	 * Purpose: Test findByValue case sensitivity
	 * 
	 * Steps:
	 * 1. Create features with different case values
	 * 2. Mock repository to return different features for different cases
	 * 3. Call service findByValue with different cases
	 * 4. Verify each call returns the correct feature based on case
	 * 5. Verify case sensitivity is preserved
	 */
	@Test
	public void testFindByValueCaseSensitivity() {
		// Step 1: Setup - Create features with different cases
		String lowerValue = "button";
		String upperValue = "BUTTON";
		String mixedValue = "Button";
		Feature lowerFeature = new Feature(lowerValue);
		Feature upperFeature = new Feature(upperValue);
		Feature mixedFeature = new Feature(mixedValue);
		
		// Step 2: Setup - Mock repository to return different features for different cases
		Mockito.when(featureRepository.findByValue(lowerValue)).thenReturn(lowerFeature);
		Mockito.when(featureRepository.findByValue(upperValue)).thenReturn(upperFeature);
		Mockito.when(featureRepository.findByValue(mixedValue)).thenReturn(mixedFeature);
		
		// Step 3: Execute - Call service method with different cases
		Feature resultLower = featureService.findByValue(lowerValue);
		Feature resultUpper = featureService.findByValue(upperValue);
		Feature resultMixed = featureService.findByValue(mixedValue);
		
		// Step 4 & 5: Verify - Each result should match its case-specific feature
		Assert.assertEquals(resultLower, lowerFeature, 
			"Lower case should return lower case feature");
		Assert.assertEquals(resultUpper, upperFeature, 
			"Upper case should return upper case feature");
		Assert.assertEquals(resultMixed, mixedFeature, 
			"Mixed case should return mixed case feature");
		
		// Verify case sensitivity is preserved
		Assert.assertNotEquals(resultLower, resultUpper, 
			"Different cases should return different features");
	}
}

