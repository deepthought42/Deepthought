package com.qanairy.api.dto;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for ImageIngestRequest DTO.
 */
@Tag("Regression")
public class ImageIngestRequestTests {

	@Test
	public void defaultConstructor_createsEmptyRequest() {
		ImageIngestRequest request = new ImageIngestRequest();
		assertNull(request.getImage());
	}

	@Test
	public void constructorWithImage_setsImage() {
		String base64 = "dGVzdA==";
		ImageIngestRequest request = new ImageIngestRequest(base64);
		assertEquals(request.getImage(), base64);
	}

	@Test
	public void setImage_updatesValue() {
		ImageIngestRequest request = new ImageIngestRequest();
		request.setImage("abc123");
		assertEquals(request.getImage(), "abc123");
		request.setImage("xyz789");
		assertEquals(request.getImage(), "xyz789");
	}

	@Test
	public void setImage_allowsNull() {
		ImageIngestRequest request = new ImageIngestRequest("data");
		request.setImage(null);
		assertNull(request.getImage());
	}
}
