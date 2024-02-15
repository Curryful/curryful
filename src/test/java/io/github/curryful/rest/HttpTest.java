package io.github.curryful.rest;

import static io.github.curryful.rest.Http.getContent;
import static io.github.curryful.rest.Http.getHeaders;
import static io.github.curryful.rest.Http.getMethod;
import static io.github.curryful.rest.Http.getPath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Test;

public class HttpTest {

	private static final List<String> rawHttpPost = Arrays.asList("POST /test HTTP/1.1", "Host: localhost:8080", "User-Agent: curl/7.68.0", "Accept: */*", "Content-Length: 15", "Content-Type: application/json", "", "{\"key\":\"value\"}");
	private static final List<String> rawHttpGet = Arrays.asList("GET /test HTTP/1.1", "Host: localhost:8080", "User-Agent: curl/7.68.0", "Accept: */*");

	@Test
	public void testGetMethodPost() {
		// Act
		var result = getMethod.apply(rawHttpPost.stream());

		// Assert
		assertTrue(result.hasValue());
		assertEquals("POST", result.getValue());
	}

	@Test
	public void testGetMethodGet() {
		// Act
		var result = getMethod.apply(rawHttpGet.stream());

		// Assert
		assertTrue(result.hasValue());
		assertEquals("GET", result.getValue());
	}

	@Test
	public void testGetPath() {
		// Act
		var result = getPath.apply(rawHttpPost.stream());

		// Assert 
		assertTrue(result.hasValue());
		assertEquals("/test", result.getValue());
	}

	@Test
	public void testGetHeadersPost() {
		// Arrange
		final var expected = new HashMap<String, String>();
		expected.put("Host", "localhost:8080");
		expected.put("User-Agent", "curl/7.68.0");
		expected.put("Accept", "*/*");
		expected.put("Content-Length", "15");
		expected.put("Content-Type", "application/json");

		// Act
		var result = getHeaders.apply(rawHttpPost.stream());

		// Assert
		assertTrue(result.hasValue());
		assertEquals(expected, result.getValue());
	}
	
	@Test
	public void testGetHeadersGet() {
		// Arrange
		final var expected = new HashMap<String, String>();
		expected.put("Host", "localhost:8080");
		expected.put("User-Agent", "curl/7.68.0");
		expected.put("Accept", "*/*");

		// Act
		var result = getHeaders.apply(rawHttpGet.stream());

		// Assert
		assertTrue(result.hasValue());
		assertEquals(expected, result.getValue());
	}

	@Test
	public void testGetContentPost() {
		// Act
		var result = getContent.apply(rawHttpPost.stream());

		// Assert
		assertTrue(result.hasValue());
		assertEquals("{\"key\":\"value\"}", result.getValue());
	}

	@Test
	public void testGetContentGet() {
		// Act / Assert
		assertFalse(getContent.apply(rawHttpGet.stream()).hasValue());
	}
}

