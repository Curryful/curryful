package io.github.curryful.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import io.github.curryful.rest.middleware.PostMiddleware;
import io.github.curryful.rest.middleware.PreMiddleware;

public class RouterTest {

	@Test
	public void testProcessEndpointNotFound() {
		// Arrange
		var preMiddleware = new ArrayList<PreMiddleware>();
		var endpoints = new ArrayList<Endpoint>();
		var postMiddleware = new ArrayList<PostMiddleware>();
		var rawHttp = Arrays.asList("GET / HTTP/1.1", "Host: localhost:8080",
				"User-Agent: curl/7.68.0", "Accept: */*");

		// Act
		HttpResponse<?> result = Router.process.apply(preMiddleware).apply(endpoints)
				.apply(postMiddleware).apply(rawHttp);

		// Assert
		assertEquals(HttpResponseCode.NOT_FOUND, result.getCode());
	}
}

