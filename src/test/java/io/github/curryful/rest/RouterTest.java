package io.github.curryful.rest;

import static io.github.curryful.rest.Router.route;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.net.InetAddress;

import org.junit.jupiter.api.Test;

import io.github.curryful.commons.collections.ImmutableArrayList;
import io.github.curryful.commons.collections.ImmutableMaybeHashMap;
import io.github.curryful.commons.collections.MutableArrayList;
import io.github.curryful.commons.collections.MutableMaybeHashMap;
import io.github.curryful.rest.http.HttpContext;
import io.github.curryful.rest.http.HttpMethod;
import io.github.curryful.rest.http.HttpResponse;
import io.github.curryful.rest.http.HttpResponseCode;
import io.github.curryful.rest.middleware.PostMiddleware;
import io.github.curryful.rest.middleware.PreMiddleware;

public class RouterTest {

	@Test
	public void testProcessEndpointNotFound() {
		// Arrange
		MutableArrayList<String> rawHttp = MutableArrayList.empty();
		rawHttp.add("GET / HTTP/1.1");

		// Act
		var response = route.apply(ImmutableArrayList.empty()).apply(ImmutableArrayList.empty())
				.apply(ImmutableArrayList.empty()).apply(InetAddress.getLoopbackAddress()).apply(rawHttp);

		// Assert
		assertFalse(response.isFailure());
		assertEquals(HttpResponseCode.NOT_FOUND, response.getValue().getCode());
	}

	public void testProcessMiddleware() {
		// Arrange
		MutableArrayList<PreMiddleware> preMiddleware = MutableArrayList.empty();
		preMiddleware.add(context -> {
			var headers = MutableMaybeHashMap.of(context.getHeaders());
			headers.put("User-Agent", "Test");
			return HttpContext.of(context.getMethod(), context.getActualUri(), context.getFormalUri(),
					context.getPathParameters(), context.getQueryParameters(), headers,
					context.getAddress(), context.getBody());
		});

		MutableArrayList<Endpoint> endpoints = MutableArrayList.empty();
		endpoints.add(Endpoint.of(Destination.of(HttpMethod.GET, "/"), context -> HttpResponse.of(HttpResponseCode.OK)));

		MutableArrayList<PostMiddleware> postMiddleware = MutableArrayList.empty();
		postMiddleware.add(context -> response -> {
			var newResponse = HttpResponse.of(response.getCode(), ImmutableMaybeHashMap.empty(),
					context.getHeaders().get("User-Agent").orElse("Unknown"), response.getContentType());
			return newResponse;
		});

		MutableArrayList<String> rawHttp = MutableArrayList.empty();
		rawHttp.add("GET / HTTP/1.1");

		// Act
		var response = route.apply(preMiddleware).apply(endpoints)
				.apply(postMiddleware).apply(InetAddress.getLoopbackAddress()).apply(rawHttp);

		// Assert
		assertFalse(response.isFailure());
		assertEquals("Test", response.getValue().getBody().getValue());
	}
}

