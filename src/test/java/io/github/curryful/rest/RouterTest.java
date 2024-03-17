package io.github.curryful.rest;

import static io.github.curryful.rest.Router.process;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import io.github.curryful.commons.collections.MutableMaybeHashMap;
import io.github.curryful.rest.middleware.PostMiddleware;
import io.github.curryful.rest.middleware.PreMiddleware;

public class RouterTest {

	@Test
	public void testProcessEndpointNotFound() {
		// Arrange
		var preMiddleware = new ArrayList<PreMiddleware>();
		var endpoints = new ArrayList<Endpoint>();
		var postMiddleware = new ArrayList<PostMiddleware>();
		var rawHttp = Arrays.asList("GET / HTTP/1.1");

		// Act
		HttpResponse<?> response = process.apply(preMiddleware).apply(endpoints)
				.apply(postMiddleware).apply(InetAddress.getLoopbackAddress()).apply(rawHttp);

		// Assert
		assertEquals(HttpResponseCode.NOT_FOUND, response.getCode());
	}

	public void testProcessMiddleware() {
		// Arrange
		var preMiddleware = new ArrayList<PreMiddleware>();
		preMiddleware.add(context -> {
			var headers = MutableMaybeHashMap.of(context.getHeaders());
			headers.put("User-Agent", "Test");
			return HttpContext.of(context.getMethod(), context.getActualUri(), context.getFormalUri(),
					context.getPathParameters(), context.getQueryParameters(), headers,
					context.getAddress(), context.getBody());
		});

		var endpoints = new ArrayList<Endpoint>();
		endpoints.add(Endpoint.of(Destination.of(HttpMethod.GET, "/"), context -> HttpResponse.of(HttpResponseCode.OK)));

		var postMiddleware = new ArrayList<PostMiddleware>();
		postMiddleware.add((context, response) -> {
			var newResponse = HttpResponse.of(response.getCode(),
					context.getHeaders().get("User-Agent").orElse("Unknown"));
			return newResponse;
		});

		var rawHttp = Arrays.asList("GET / HTTP/1.1");

		// Act
		HttpResponse<?> response = process.apply(preMiddleware).apply(endpoints)
				.apply(postMiddleware).apply(InetAddress.getLoopbackAddress()).apply(rawHttp);

		// Assert
		assertEquals("Test", response.getBody().getValue());
	}
}

