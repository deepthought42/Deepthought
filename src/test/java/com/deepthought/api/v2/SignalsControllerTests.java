package com.deepthought.api.v2;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.deepthought.api.TestDataFactory;
import com.deepthought.api.v2.dto.MemoryRetrievalRequest;
import com.deepthought.api.v2.dto.MemoryRetrievalResponse;
import com.deepthought.api.v2.dto.SignalRequest;
import com.deepthought.api.v2.dto.SignalResponse;
import com.deepthought.services.MemoryRetrievalService;
import com.deepthought.services.SignalProcessingService;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test suite for SignalsController
 * 
 * Tests validate the SignalsController which provides:
 * 1. POST /api/v2/signals - Processes signals and returns response
 * 2. POST /api/v2/memory/retrieve - Retrieves stored memories
 */
@WebMvcTest(SignalsController.class)
public class SignalsControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private SignalProcessingService signalProcessingService;

	@MockBean
	private MemoryRetrievalService memoryRetrievalService;

	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * Purpose: Set up test fixture before each test method
	 */
	@BeforeMethod
	public void setUp() {
		// Reset mocks if needed
		Mockito.reset(signalProcessingService, memoryRetrievalService);
	}

	// ==================== POST /api/v2/signals Endpoint Tests ====================

	/**
	 * Purpose: Test signals endpoint with text only
	 */
	@Test
	public void testProcessSignalsWithTextOnly() throws Exception {
		// Setup
		SignalRequest request = TestDataFactory.createSignalRequestWithText("Test message");
		SignalResponse mockResponse = TestDataFactory.createSignalResponse(
			"Generated response", "session-123", 0.85, TestDataFactory.createSources("source1", "source2"));
		
		Mockito.when(signalProcessingService.processSignals(Mockito.any(SignalRequest.class)))
			.thenReturn(mockResponse);
		
		// Execute
		MvcResult result = mockMvc.perform(
			org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v2/signals")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		).andReturn();
		
		// Verify
		Assert.assertEquals(result.getResponse().getStatus(), 200, "Should return HTTP 200");
		
		SignalResponse response = objectMapper.readValue(
			result.getResponse().getContentAsString(), SignalResponse.class);
		Assert.assertNotNull(response, "Response should not be null");
		Assert.assertEquals(response.getOutputText(), "Generated response", "Output text should match");
		Assert.assertEquals(response.getSessionId(), "session-123", "Session ID should match");
		Assert.assertEquals(response.getConfidence(), Double.valueOf(0.85), "Confidence should match");
		Assert.assertNotNull(response.getSources(), "Sources should not be null");
		Assert.assertEquals(response.getSources().size(), 2, "Should have 2 sources");
		
		Mockito.verify(signalProcessingService, Mockito.times(1)).processSignals(Mockito.any(SignalRequest.class));
	}

	/**
	 * Purpose: Test signals endpoint with text and sessionId
	 */
	@Test
	public void testProcessSignalsWithTextAndSessionId() throws Exception {
		// Setup
		SignalRequest request = TestDataFactory.createSignalRequestWithTextAndSession(
			"Test message", "existing-session-456");
		SignalResponse mockResponse = TestDataFactory.createSignalResponse(
			"Response", "existing-session-456", 0.75, new ArrayList<>());
		
		Mockito.when(signalProcessingService.processSignals(Mockito.any(SignalRequest.class)))
			.thenReturn(mockResponse);
		
		// Execute
		MvcResult result = mockMvc.perform(
			org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v2/signals")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		).andReturn();
		
		// Verify
		Assert.assertEquals(result.getResponse().getStatus(), 200, "Should return HTTP 200");
		SignalResponse response = objectMapper.readValue(
			result.getResponse().getContentAsString(), SignalResponse.class);
		Assert.assertEquals(response.getSessionId(), "existing-session-456", "Session ID should be preserved");
	}

	/**
	 * Purpose: Test signals endpoint with all signal types populated
	 */
	@Test
	public void testProcessSignalsWithAllSignalTypes() throws Exception {
		// Setup
		SignalRequest request = TestDataFactory.createFullSignalRequest(
			"Test message",
			"session-789",
			TestDataFactory.createImageReferences("image1.jpg", "image2.png"),
			TestDataFactory.createAudioReferences("audio1.mp3"),
			TestDataFactory.createOtherSignals("signal1", "signal2")
		);
		SignalResponse mockResponse = TestDataFactory.createSignalResponse(
			"Response", "session-789", 0.9, TestDataFactory.createSources("source1"));
		
		Mockito.when(signalProcessingService.processSignals(Mockito.any(SignalRequest.class)))
			.thenReturn(mockResponse);
		
		// Execute
		MvcResult result = mockMvc.perform(
			org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v2/signals")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		).andReturn();
		
		// Verify
		Assert.assertEquals(result.getResponse().getStatus(), 200, "Should return HTTP 200");
		Mockito.verify(signalProcessingService, Mockito.times(1)).processSignals(Mockito.any(SignalRequest.class));
	}

	/**
	 * Purpose: Test signals endpoint with null text field
	 */
	@Test
	public void testProcessSignalsWithNullText() throws Exception {
		// Setup
		SignalRequest request = TestDataFactory.createSignalRequestWithNullText();
		SignalResponse mockResponse = TestDataFactory.createSignalResponse(
			"Response", "session-null", 0.5, new ArrayList<>());
		
		Mockito.when(signalProcessingService.processSignals(Mockito.any(SignalRequest.class)))
			.thenReturn(mockResponse);
		
		// Execute
		MvcResult result = mockMvc.perform(
			org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v2/signals")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		).andReturn();
		
		// Verify
		Assert.assertEquals(result.getResponse().getStatus(), 200, "Should return HTTP 200");
		Mockito.verify(signalProcessingService, Mockito.times(1)).processSignals(Mockito.any(SignalRequest.class));
	}

	/**
	 * Purpose: Test signals endpoint with empty text field
	 */
	@Test
	public void testProcessSignalsWithEmptyText() throws Exception {
		// Setup
		SignalRequest request = TestDataFactory.createSignalRequestWithEmptyText();
		SignalResponse mockResponse = TestDataFactory.createSignalResponse(
			"", "session-empty", 0.0, new ArrayList<>());
		
		Mockito.when(signalProcessingService.processSignals(Mockito.any(SignalRequest.class)))
			.thenReturn(mockResponse);
		
		// Execute
		MvcResult result = mockMvc.perform(
			org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v2/signals")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		).andReturn();
		
		// Verify
		Assert.assertEquals(result.getResponse().getStatus(), 200, "Should return HTTP 200");
	}

	/**
	 * Purpose: Test signals endpoint with all fields null
	 */
	@Test
	public void testProcessSignalsWithAllFieldsNull() throws Exception {
		// Setup
		SignalRequest request = TestDataFactory.createEmptySignalRequest();
		SignalResponse mockResponse = TestDataFactory.createSignalResponse(
			null, null, null, null);
		
		Mockito.when(signalProcessingService.processSignals(Mockito.any(SignalRequest.class)))
			.thenReturn(mockResponse);
		
		// Execute
		MvcResult result = mockMvc.perform(
			org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v2/signals")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		).andReturn();
		
		// Verify
		Assert.assertEquals(result.getResponse().getStatus(), 200, "Should return HTTP 200");
	}

	/**
	 * Purpose: Test signals endpoint when service throws exception (should return 500)
	 */
	@Test
	public void testProcessSignalsWhenServiceThrowsException() throws Exception {
		// Setup
		SignalRequest request = TestDataFactory.createSignalRequestWithText("Test");
		
		Mockito.when(signalProcessingService.processSignals(Mockito.any(SignalRequest.class)))
			.thenThrow(new RuntimeException("Service error"));
		
		// Execute
		MvcResult result = mockMvc.perform(
			org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v2/signals")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		).andReturn();
		
		// Verify
		Assert.assertEquals(result.getResponse().getStatus(), 500, "Should return HTTP 500 on error");
		Mockito.verify(signalProcessingService, Mockito.times(1)).processSignals(Mockito.any(SignalRequest.class));
	}

	/**
	 * Purpose: Test signals endpoint with very long text
	 */
	@Test
	public void testProcessSignalsWithVeryLongText() throws Exception {
		// Setup
		StringBuilder longText = new StringBuilder();
		for (int i = 0; i < 10000; i++) {
			longText.append("This is a very long text. ");
		}
		SignalRequest request = TestDataFactory.createSignalRequestWithText(longText.toString());
		SignalResponse mockResponse = TestDataFactory.createSignalResponse(
			"Response", "session-long", 0.8, new ArrayList<>());
		
		Mockito.when(signalProcessingService.processSignals(Mockito.any(SignalRequest.class)))
			.thenReturn(mockResponse);
		
		// Execute
		MvcResult result = mockMvc.perform(
			org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v2/signals")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		).andReturn();
		
		// Verify
		Assert.assertEquals(result.getResponse().getStatus(), 200, "Should handle long text");
	}

	/**
	 * Purpose: Test signals endpoint with text containing special characters
	 */
	@Test
	public void testProcessSignalsWithSpecialCharacters() throws Exception {
		// Setup
		SignalRequest request = TestDataFactory.createSignalRequestWithText(
			"Test with special chars: <script>alert('xss')</script> & \"quotes\" 'apostrophes'");
		SignalResponse mockResponse = TestDataFactory.createSignalResponse(
			"Response", "session-special", 0.7, new ArrayList<>());
		
		Mockito.when(signalProcessingService.processSignals(Mockito.any(SignalRequest.class)))
			.thenReturn(mockResponse);
		
		// Execute
		MvcResult result = mockMvc.perform(
			org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v2/signals")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		).andReturn();
		
		// Verify
		Assert.assertEquals(result.getResponse().getStatus(), 200, "Should handle special characters");
	}

	/**
	 * Purpose: Test signals endpoint with empty imageReferences list
	 */
	@Test
	public void testProcessSignalsWithEmptyLists() throws Exception {
		// Setup
		SignalRequest request = TestDataFactory.createFullSignalRequest(
			"Test",
			"session-empty-lists",
			new ArrayList<>(), // empty imageReferences
			new ArrayList<>(), // empty audioReferences
			new ArrayList<>()  // empty otherSignals
		);
		SignalResponse mockResponse = TestDataFactory.createSignalResponse(
			"Response", "session-empty-lists", 0.6, new ArrayList<>());
		
		Mockito.when(signalProcessingService.processSignals(Mockito.any(SignalRequest.class)))
			.thenReturn(mockResponse);
		
		// Execute
		MvcResult result = mockMvc.perform(
			org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v2/signals")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		).andReturn();
		
		// Verify
		Assert.assertEquals(result.getResponse().getStatus(), 200, "Should handle empty lists");
	}

	// ==================== POST /api/v2/memory/retrieve Endpoint Tests ====================

	/**
	 * Purpose: Test memory retrieve endpoint with from, to, and prompt
	 */
	@Test
	public void testRetrieveMemoriesWithFromToAndPrompt() throws Exception {
		// Setup
		Date from = TestDataFactory.createDateDaysAgo(30);
		Date to = new Date();
		MemoryRetrievalRequest request = TestDataFactory.createMemoryRetrievalRequest(
			from, to, "test prompt");
		
		List<MemoryRetrievalResponse.MemoryItem> items = new ArrayList<>();
		items.add(TestDataFactory.createMemoryItem(1L, new Date(), 
			TestDataFactory.createInputFeatures("test", "prompt"), "predicted", 2.0));
		
		MemoryRetrievalResponse mockResponse = TestDataFactory.createMemoryRetrievalResponseSuccess(items);
		
		Mockito.when(memoryRetrievalService.retrieveMemories(Mockito.any(MemoryRetrievalRequest.class)))
			.thenReturn(mockResponse);
		
		// Execute
		MvcResult result = mockMvc.perform(
			org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v2/memory/retrieve")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		).andReturn();
		
		// Verify
		Assert.assertEquals(result.getResponse().getStatus(), 200, "Should return HTTP 200");
		
		MemoryRetrievalResponse response = objectMapper.readValue(
			result.getResponse().getContentAsString(), MemoryRetrievalResponse.class);
		Assert.assertNotNull(response, "Response should not be null");
		Assert.assertTrue(response.isSuccess(), "Success should be true");
		Assert.assertNotNull(response.getItems(), "Items should not be null");
		Assert.assertEquals(response.getItems().size(), 1, "Should have 1 item");
		
		Mockito.verify(memoryRetrievalService, Mockito.times(1))
			.retrieveMemories(Mockito.any(MemoryRetrievalRequest.class));
	}

	/**
	 * Purpose: Test memory retrieve endpoint with from, to, prompt, and limit
	 */
	@Test
	public void testRetrieveMemoriesWithLimit() throws Exception {
		// Setup
		Date from = TestDataFactory.createDateDaysAgo(7);
		Date to = new Date();
		MemoryRetrievalRequest request = TestDataFactory.createMemoryRetrievalRequestWithLimit(
			from, to, "test", 10);
		
		List<MemoryRetrievalResponse.MemoryItem> items = new ArrayList<>();
		for (int i = 0; i < 5; i++) {
			items.add(TestDataFactory.createMemoryItem((long)i, new Date(), 
				TestDataFactory.createInputFeatures("test"), "pred" + i, (double)i));
		}
		
		MemoryRetrievalResponse mockResponse = TestDataFactory.createMemoryRetrievalResponseSuccess(items);
		
		Mockito.when(memoryRetrievalService.retrieveMemories(Mockito.any(MemoryRetrievalRequest.class)))
			.thenReturn(mockResponse);
		
		// Execute
		MvcResult result = mockMvc.perform(
			org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v2/memory/retrieve")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		).andReturn();
		
		// Verify
		Assert.assertEquals(result.getResponse().getStatus(), 200, "Should return HTTP 200");
		MemoryRetrievalResponse response = objectMapper.readValue(
			result.getResponse().getContentAsString(), MemoryRetrievalResponse.class);
		Assert.assertTrue(response.isSuccess(), "Success should be true");
	}

	/**
	 * Purpose: Test memory retrieve endpoint with only from and to (no prompt)
	 */
	@Test
	public void testRetrieveMemoriesWithoutPrompt() throws Exception {
		// Setup
		Date from = TestDataFactory.createDateDaysAgo(14);
		Date to = new Date();
		MemoryRetrievalRequest request = TestDataFactory.createMemoryRetrievalRequestWithoutPrompt(from, to);
		
		MemoryRetrievalResponse mockResponse = TestDataFactory.createMemoryRetrievalResponseSuccess(new ArrayList<>());
		
		Mockito.when(memoryRetrievalService.retrieveMemories(Mockito.any(MemoryRetrievalRequest.class)))
			.thenReturn(mockResponse);
		
		// Execute
		MvcResult result = mockMvc.perform(
			org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v2/memory/retrieve")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		).andReturn();
		
		// Verify
		Assert.assertEquals(result.getResponse().getStatus(), 200, "Should return HTTP 200");
		MemoryRetrievalResponse response = objectMapper.readValue(
			result.getResponse().getContentAsString(), MemoryRetrievalResponse.class);
		Assert.assertTrue(response.isSuccess(), "Success should be true");
	}

	/**
	 * Purpose: Test memory retrieve endpoint with null from date (should return error in response body)
	 */
	@Test
	public void testRetrieveMemoriesWithNullFrom() throws Exception {
		// Setup
		Date to = new Date();
		MemoryRetrievalRequest request = TestDataFactory.createMemoryRetrievalRequestWithNullFrom(to);
		
		MemoryRetrievalResponse mockResponse = TestDataFactory.createMemoryRetrievalResponseError(
			"Both 'from' and 'to' times are required");
		
		Mockito.when(memoryRetrievalService.retrieveMemories(Mockito.any(MemoryRetrievalRequest.class)))
			.thenReturn(mockResponse);
		
		// Execute
		MvcResult result = mockMvc.perform(
			org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v2/memory/retrieve")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		).andReturn();
		
		// Verify
		Assert.assertEquals(result.getResponse().getStatus(), 200, "Should return HTTP 200 (error in body)");
		MemoryRetrievalResponse response = objectMapper.readValue(
			result.getResponse().getContentAsString(), MemoryRetrievalResponse.class);
		Assert.assertFalse(response.isSuccess(), "Success should be false");
		Assert.assertNotNull(response.getMessage(), "Error message should be present");
	}

	/**
	 * Purpose: Test memory retrieve endpoint with null to date (should return error in response body)
	 */
	@Test
	public void testRetrieveMemoriesWithNullTo() throws Exception {
		// Setup
		Date from = TestDataFactory.createDateDaysAgo(7);
		MemoryRetrievalRequest request = TestDataFactory.createMemoryRetrievalRequestWithNullTo(from);
		
		MemoryRetrievalResponse mockResponse = TestDataFactory.createMemoryRetrievalResponseError(
			"Both 'from' and 'to' times are required");
		
		Mockito.when(memoryRetrievalService.retrieveMemories(Mockito.any(MemoryRetrievalRequest.class)))
			.thenReturn(mockResponse);
		
		// Execute
		MvcResult result = mockMvc.perform(
			org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v2/memory/retrieve")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		).andReturn();
		
		// Verify
		Assert.assertEquals(result.getResponse().getStatus(), 200, "Should return HTTP 200 (error in body)");
		MemoryRetrievalResponse response = objectMapper.readValue(
			result.getResponse().getContentAsString(), MemoryRetrievalResponse.class);
		Assert.assertFalse(response.isSuccess(), "Success should be false");
	}

	/**
	 * Purpose: Test memory retrieve endpoint with from after to (should return error in response body)
	 */
	@Test
	public void testRetrieveMemoriesWithInvalidDateRange() throws Exception {
		// Setup
		Date from = new Date();
		Date to = TestDataFactory.createDateDaysAgo(7); // from is after to
		MemoryRetrievalRequest request = TestDataFactory.createMemoryRetrievalRequestWithInvalidDateRange(from, to);
		
		MemoryRetrievalResponse mockResponse = TestDataFactory.createMemoryRetrievalResponseError(
			"'from' must be before 'to'");
		
		Mockito.when(memoryRetrievalService.retrieveMemories(Mockito.any(MemoryRetrievalRequest.class)))
			.thenReturn(mockResponse);
		
		// Execute
		MvcResult result = mockMvc.perform(
			org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v2/memory/retrieve")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		).andReturn();
		
		// Verify
		Assert.assertEquals(result.getResponse().getStatus(), 200, "Should return HTTP 200 (error in body)");
		MemoryRetrievalResponse response = objectMapper.readValue(
			result.getResponse().getContentAsString(), MemoryRetrievalResponse.class);
		Assert.assertFalse(response.isSuccess(), "Success should be false");
		Assert.assertNotNull(response.getMessage(), "Error message should be present");
	}

	/**
	 * Purpose: Test memory retrieve endpoint with null prompt
	 */
	@Test
	public void testRetrieveMemoriesWithNullPrompt() throws Exception {
		// Setup
		Date from = TestDataFactory.createDateDaysAgo(7);
		Date to = new Date();
		MemoryRetrievalRequest request = TestDataFactory.createMemoryRetrievalRequest(from, to, null);
		
		MemoryRetrievalResponse mockResponse = TestDataFactory.createMemoryRetrievalResponseSuccess(new ArrayList<>());
		
		Mockito.when(memoryRetrievalService.retrieveMemories(Mockito.any(MemoryRetrievalRequest.class)))
			.thenReturn(mockResponse);
		
		// Execute
		MvcResult result = mockMvc.perform(
			org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v2/memory/retrieve")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		).andReturn();
		
		// Verify
		Assert.assertEquals(result.getResponse().getStatus(), 200, "Should return HTTP 200");
		MemoryRetrievalResponse response = objectMapper.readValue(
			result.getResponse().getContentAsString(), MemoryRetrievalResponse.class);
		Assert.assertTrue(response.isSuccess(), "Success should be true");
	}

	/**
	 * Purpose: Test memory retrieve endpoint with empty prompt string
	 */
	@Test
	public void testRetrieveMemoriesWithEmptyPrompt() throws Exception {
		// Setup
		Date from = TestDataFactory.createDateDaysAgo(7);
		Date to = new Date();
		MemoryRetrievalRequest request = TestDataFactory.createMemoryRetrievalRequest(from, to, "");
		
		MemoryRetrievalResponse mockResponse = TestDataFactory.createMemoryRetrievalResponseSuccess(new ArrayList<>());
		
		Mockito.when(memoryRetrievalService.retrieveMemories(Mockito.any(MemoryRetrievalRequest.class)))
			.thenReturn(mockResponse);
		
		// Execute
		MvcResult result = mockMvc.perform(
			org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v2/memory/retrieve")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		).andReturn();
		
		// Verify
		Assert.assertEquals(result.getResponse().getStatus(), 200, "Should return HTTP 200");
	}

	/**
	 * Purpose: Test memory retrieve endpoint with zero limit
	 */
	@Test
	public void testRetrieveMemoriesWithZeroLimit() throws Exception {
		// Setup
		Date from = TestDataFactory.createDateDaysAgo(7);
		Date to = new Date();
		MemoryRetrievalRequest request = TestDataFactory.createMemoryRetrievalRequestWithLimit(from, to, "test", 0);
		
		MemoryRetrievalResponse mockResponse = TestDataFactory.createMemoryRetrievalResponseSuccess(new ArrayList<>());
		
		Mockito.when(memoryRetrievalService.retrieveMemories(Mockito.any(MemoryRetrievalRequest.class)))
			.thenReturn(mockResponse);
		
		// Execute
		MvcResult result = mockMvc.perform(
			org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v2/memory/retrieve")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		).andReturn();
		
		// Verify
		Assert.assertEquals(result.getResponse().getStatus(), 200, "Should return HTTP 200");
	}

	/**
	 * Purpose: Test memory retrieve endpoint with negative limit
	 */
	@Test
	public void testRetrieveMemoriesWithNegativeLimit() throws Exception {
		// Setup
		Date from = TestDataFactory.createDateDaysAgo(7);
		Date to = new Date();
		MemoryRetrievalRequest request = TestDataFactory.createMemoryRetrievalRequestWithLimit(from, to, "test", -1);
		
		MemoryRetrievalResponse mockResponse = TestDataFactory.createMemoryRetrievalResponseSuccess(new ArrayList<>());
		
		Mockito.when(memoryRetrievalService.retrieveMemories(Mockito.any(MemoryRetrievalRequest.class)))
			.thenReturn(mockResponse);
		
		// Execute
		MvcResult result = mockMvc.perform(
			org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v2/memory/retrieve")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		).andReturn();
		
		// Verify
		Assert.assertEquals(result.getResponse().getStatus(), 200, "Should return HTTP 200");
	}

	/**
	 * Purpose: Test memory retrieve endpoint when service throws exception (should return 500)
	 */
	@Test
	public void testRetrieveMemoriesWhenServiceThrowsException() throws Exception {
		// Setup
		Date from = TestDataFactory.createDateDaysAgo(7);
		Date to = new Date();
		MemoryRetrievalRequest request = TestDataFactory.createMemoryRetrievalRequest(from, to, "test");
		
		Mockito.when(memoryRetrievalService.retrieveMemories(Mockito.any(MemoryRetrievalRequest.class)))
			.thenThrow(new RuntimeException("Service error"));
		
		// Execute
		MvcResult result = mockMvc.perform(
			org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v2/memory/retrieve")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		).andReturn();
		
		// Verify
		Assert.assertEquals(result.getResponse().getStatus(), 500, "Should return HTTP 500 on error");
		Mockito.verify(memoryRetrievalService, Mockito.times(1))
			.retrieveMemories(Mockito.any(MemoryRetrievalRequest.class));
	}

	/**
	 * Purpose: Test memory retrieve endpoint with from equals to date (boundary case)
	 */
	@Test
	public void testRetrieveMemoriesWithFromEqualsTo() throws Exception {
		// Setup
		Date sameDate = new Date();
		MemoryRetrievalRequest request = TestDataFactory.createMemoryRetrievalRequest(sameDate, sameDate, "test");
		
		MemoryRetrievalResponse mockResponse = TestDataFactory.createMemoryRetrievalResponseSuccess(new ArrayList<>());
		
		Mockito.when(memoryRetrievalService.retrieveMemories(Mockito.any(MemoryRetrievalRequest.class)))
			.thenReturn(mockResponse);
		
		// Execute
		MvcResult result = mockMvc.perform(
			org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v2/memory/retrieve")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		).andReturn();
		
		// Verify
		Assert.assertEquals(result.getResponse().getStatus(), 200, "Should return HTTP 200");
	}

	/**
	 * Purpose: Test memory retrieve endpoint with very large limit
	 */
	@Test
	public void testRetrieveMemoriesWithVeryLargeLimit() throws Exception {
		// Setup
		Date from = TestDataFactory.createDateDaysAgo(30);
		Date to = new Date();
		MemoryRetrievalRequest request = TestDataFactory.createMemoryRetrievalRequestWithLimit(
			from, to, "test", Integer.MAX_VALUE);
		
		MemoryRetrievalResponse mockResponse = TestDataFactory.createMemoryRetrievalResponseSuccess(new ArrayList<>());
		
		Mockito.when(memoryRetrievalService.retrieveMemories(Mockito.any(MemoryRetrievalRequest.class)))
			.thenReturn(mockResponse);
		
		// Execute
		MvcResult result = mockMvc.perform(
			org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v2/memory/retrieve")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		).andReturn();
		
		// Verify
		Assert.assertEquals(result.getResponse().getStatus(), 200, "Should handle very large limit");
	}

	/**
	 * Purpose: Test memory retrieve endpoint with prompt containing special characters
	 */
	@Test
	public void testRetrieveMemoriesWithSpecialCharactersInPrompt() throws Exception {
		// Setup
		Date from = TestDataFactory.createDateDaysAgo(7);
		Date to = new Date();
		MemoryRetrievalRequest request = TestDataFactory.createMemoryRetrievalRequest(
			from, to, "test <script>alert('xss')</script> & \"quotes\"");
		
		MemoryRetrievalResponse mockResponse = TestDataFactory.createMemoryRetrievalResponseSuccess(new ArrayList<>());
		
		Mockito.when(memoryRetrievalService.retrieveMemories(Mockito.any(MemoryRetrievalRequest.class)))
			.thenReturn(mockResponse);
		
		// Execute
		MvcResult result = mockMvc.perform(
			org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v2/memory/retrieve")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request))
		).andReturn();
		
		// Verify
		Assert.assertEquals(result.getResponse().getStatus(), 200, "Should handle special characters");
	}
}
