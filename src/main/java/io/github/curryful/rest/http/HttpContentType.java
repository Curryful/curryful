package io.github.curryful.rest.http;

public enum HttpContentType {

	APPLICATION_JSON("application/json"),
	TEXT_PLAIN("text/plain"),
	NONE("");

	private final String value;

	HttpContentType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}

