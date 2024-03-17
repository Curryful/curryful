package io.github.curryful.rest;

import java.util.regex.Pattern;

/**
 * A destination is one of the parts describing an {@link Endpoint}.
 */
public final class Destination {

    private final HttpMethod method;
    private final String uri;

    private Destination(HttpMethod method, String uri) {
        this.method = method;
        this.uri = uri;
    }

	public static Destination of(HttpMethod method, String uri) {
		return new Destination(method, uri);
	}

    /**
     * Check if a destination is the formal of another destination
     * @param formal what might be the formal destination
     * @param actual the actual destination
     * @return true if it is the formal destination, false otherwise
     * @apiNote
     * <ul>
     * <li>/hello/:name is the formal of /hello/John</li>
     * <li>/hello/:name is the formal of /hello/John?lang=en</li>
     * <li>/hello/:name is NOT the formal of /hello</li>
     * </ul>
     */
	public static boolean isFormal(Destination formal, Destination actual) {
        var actualUriParts = actual.uri.split("\\?");

		return formal.getMethod().equals(actual.method) &&
                Pattern.compile(Uri.replaceFormalParametersWithRegex(formal.getUri()))
                        .matcher(actualUriParts[0])
                        .matches();
	}

    public HttpMethod getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }
}

