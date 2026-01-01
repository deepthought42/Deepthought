package com.deepthought.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.deepthought.api.v2.dto.MemoryRetrievalRequest;
import com.deepthought.api.v2.dto.MemoryRetrievalResponse;
import com.deepthought.api.v2.dto.SignalRequest;
import com.deepthought.api.v2.dto.SignalResponse;
import com.deepthought.data.models.Feature;
import com.deepthought.data.models.MemoryRecord;

/**
 * Factory class for creating test data objects and DTOs.
 * Provides helper methods to generate test data for API endpoint tests.
 */
public class TestDataFactory {

	/**
	 * Creates a SignalRequest with text only
	 */
	public static SignalRequest createSignalRequestWithText(String text) {
		SignalRequest request = new SignalRequest();
		request.setText(text);
		return request;
	}

	/**
	 * Creates a SignalRequest with text and sessionId
	 */
	public static SignalRequest createSignalRequestWithTextAndSession(String text, String sessionId) {
		SignalRequest request = new SignalRequest();
		request.setText(text);
		request.setSessionId(sessionId);
		return request;
	}

	/**
	 * Creates a SignalRequest with all fields populated
	 */
	public static SignalRequest createFullSignalRequest(String text, String sessionId, 
			List<String> imageReferences, List<String> audioReferences, List<String> otherSignals) {
		SignalRequest request = new SignalRequest();
		request.setText(text);
		request.setSessionId(sessionId);
		request.setImageReferences(imageReferences);
		request.setAudioReferences(audioReferences);
		request.setOtherSignals(otherSignals);
		return request;
	}

	/**
	 * Creates a SignalRequest with null text
	 */
	public static SignalRequest createSignalRequestWithNullText() {
		SignalRequest request = new SignalRequest();
		request.setText(null);
		return request;
	}

	/**
	 * Creates a SignalRequest with empty text
	 */
	public static SignalRequest createSignalRequestWithEmptyText() {
		SignalRequest request = new SignalRequest();
		request.setText("");
		return request;
	}

	/**
	 * Creates a SignalRequest with all null fields
	 */
	public static SignalRequest createEmptySignalRequest() {
		return new SignalRequest();
	}

	/**
	 * Creates a SignalResponse with all fields populated
	 */
	public static SignalResponse createSignalResponse(String outputText, String sessionId, 
			Double confidence, List<String> sources) {
		SignalResponse response = new SignalResponse();
		response.setOutputText(outputText);
		response.setSessionId(sessionId);
		response.setConfidence(confidence);
		response.setSources(sources);
		return response;
	}

	/**
	 * Creates a MemoryRetrievalRequest with from, to, and prompt
	 */
	public static MemoryRetrievalRequest createMemoryRetrievalRequest(Date from, Date to, String prompt) {
		MemoryRetrievalRequest request = new MemoryRetrievalRequest();
		request.setFrom(from);
		request.setTo(to);
		request.setPrompt(prompt);
		return request;
	}

	/**
	 * Creates a MemoryRetrievalRequest with from, to, prompt, and limit
	 */
	public static MemoryRetrievalRequest createMemoryRetrievalRequestWithLimit(Date from, Date to, 
			String prompt, Integer limit) {
		MemoryRetrievalRequest request = new MemoryRetrievalRequest();
		request.setFrom(from);
		request.setTo(to);
		request.setPrompt(prompt);
		request.setLimit(limit);
		return request;
	}

	/**
	 * Creates a MemoryRetrievalRequest with only from and to (no prompt)
	 */
	public static MemoryRetrievalRequest createMemoryRetrievalRequestWithoutPrompt(Date from, Date to) {
		MemoryRetrievalRequest request = new MemoryRetrievalRequest();
		request.setFrom(from);
		request.setTo(to);
		return request;
	}

	/**
	 * Creates a MemoryRetrievalRequest with null from date
	 */
	public static MemoryRetrievalRequest createMemoryRetrievalRequestWithNullFrom(Date to) {
		MemoryRetrievalRequest request = new MemoryRetrievalRequest();
		request.setFrom(null);
		request.setTo(to);
		return request;
	}

	/**
	 * Creates a MemoryRetrievalRequest with null to date
	 */
	public static MemoryRetrievalRequest createMemoryRetrievalRequestWithNullTo(Date from) {
		MemoryRetrievalRequest request = new MemoryRetrievalRequest();
		request.setFrom(from);
		request.setTo(null);
		return request;
	}

	/**
	 * Creates a MemoryRetrievalRequest with from after to (invalid)
	 */
	public static MemoryRetrievalRequest createMemoryRetrievalRequestWithInvalidDateRange(Date from, Date to) {
		MemoryRetrievalRequest request = new MemoryRetrievalRequest();
		request.setFrom(from);
		request.setTo(to);
		return request;
	}

	/**
	 * Creates a MemoryRetrievalResponse with success and items
	 */
	public static MemoryRetrievalResponse createMemoryRetrievalResponseSuccess(
			List<MemoryRetrievalResponse.MemoryItem> items) {
		return MemoryRetrievalResponse.success(items);
	}

	/**
	 * Creates a MemoryRetrievalResponse with error message
	 */
	public static MemoryRetrievalResponse createMemoryRetrievalResponseError(String message) {
		return MemoryRetrievalResponse.error(message);
	}

	/**
	 * Creates a MemoryItem for testing
	 */
	public static MemoryRetrievalResponse.MemoryItem createMemoryItem(Long id, Date timestamp, 
			List<String> inputFeatures, String predictedValue, Double relevanceScore) {
		MemoryRetrievalResponse.MemoryItem item = new MemoryRetrievalResponse.MemoryItem();
		item.setId(id);
		item.setTimestamp(timestamp);
		item.setInputFeatures(inputFeatures);
		item.setPredictedValue(predictedValue);
		item.setRelevanceScore(relevanceScore);
		return item;
	}

	/**
	 * Creates a Feature with a value
	 */
	public static Feature createFeature(String value) {
		return new Feature(value);
	}

	/**
	 * Creates a Feature with ID and value
	 */
	public static Feature createFeatureWithId(Long id, String value) {
		Feature feature = new Feature(value);
		try {
			java.lang.reflect.Field idField = Feature.class.getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(feature, id);
		} catch (Exception e) {
			throw new RuntimeException("Failed to set feature ID", e);
		}
		return feature;
	}

	/**
	 * Creates a MemoryRecord with basic data
	 */
	public static MemoryRecord createMemoryRecord(Long id, List<String> inputFeatures, 
			String[] outputFeatures, Feature predictedFeature) {
		MemoryRecord record = new MemoryRecord();
		try {
			java.lang.reflect.Field idField = MemoryRecord.class.getDeclaredField("id");
			idField.setAccessible(true);
			idField.set(record, id);
		} catch (Exception e) {
			throw new RuntimeException("Failed to set memory record ID", e);
		}
		record.setInputFeatureValues(inputFeatures);
		record.setOutputFeatureKeys(outputFeatures);
		record.setPredictedFeature(predictedFeature);
		record.setDate(new Date());
		return record;
	}

	/**
	 * Creates a list of test image references
	 */
	public static List<String> createImageReferences(String... references) {
		return new ArrayList<>(Arrays.asList(references));
	}

	/**
	 * Creates a list of test audio references
	 */
	public static List<String> createAudioReferences(String... references) {
		return new ArrayList<>(Arrays.asList(references));
	}

	/**
	 * Creates a list of test other signals
	 */
	public static List<String> createOtherSignals(String... signals) {
		return new ArrayList<>(Arrays.asList(signals));
	}

	/**
	 * Creates a list of test sources
	 */
	public static List<String> createSources(String... sources) {
		return new ArrayList<>(Arrays.asList(sources));
	}

	/**
	 * Creates a list of test input features
	 */
	public static List<String> createInputFeatures(String... features) {
		return new ArrayList<>(Arrays.asList(features));
	}

	/**
	 * Creates test dates for time range testing
	 */
	public static Date createDate(int year, int month, int day, int hour, int minute) {
		@SuppressWarnings("deprecation")
		Date date = new Date(year - 1900, month - 1, day, hour, minute);
		return date;
	}

	/**
	 * Creates a date that is N days before today
	 */
	public static Date createDateDaysAgo(int daysAgo) {
		long millis = System.currentTimeMillis() - (daysAgo * 24L * 60L * 60L * 1000L);
		return new Date(millis);
	}

	/**
	 * Creates a date that is N days in the future
	 */
	public static Date createDateDaysFuture(int daysFuture) {
		long millis = System.currentTimeMillis() + (daysFuture * 24L * 60L * 60L * 1000L);
		return new Date(millis);
	}
}
