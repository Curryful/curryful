package io.github.curryful.rest;

import static io.github.curryful.rest.Uri.getPathParameters;
import static io.github.curryful.rest.Uri.getQueryParameters;
import static io.github.curryful.rest.Uri.replaceFormalParametersWithRegex;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class UriTest {

    @Test
    public void testReplaceFormalParametersWithRegexOne() {
        // Arrange
        var uri = "/hello/:name";

        // Act
        var result = replaceFormalParametersWithRegex.apply(uri);

        // Assert
        assertEquals("/hello/(?<name>[^/?]+)", result);
    }

    @Test
    public void testReplaceFormalParametersWithRegexMultiple() {
        // Arrange
        var uri = "/hello/:name/:age/:height";

        // Act
        var result = replaceFormalParametersWithRegex.apply(uri);

        // Assert
        assertEquals("/hello/(?<name>[^/?]+)/(?<age>[^/?]+)/(?<height>[^/?]+)", result);
    }

    @Test
    public void testReplaceFormalParametersWithRegexNone() {
        // Arrange
        var uri = "/hello";

        // Act
        var result = replaceFormalParametersWithRegex.apply(uri);

        // Assert
        assertEquals("/hello", result);
    }

    @Test
    public void testGetPathParametersOne() {
        // Arrange
        var formalUri = "/hello/:name";
        var actualUri = "/hello/John";

        // Act
        var result = getPathParameters.apply(formalUri).apply(actualUri);

        // Assert
        assertEquals(1, result.size());
        assertEquals("John", result.get("name").getValue());
    }

    @Test
    public void testGetPathParametersMultiple() {
        // Arrange
        var formalUri = "/hello/:name/:age";
        var actualUri = "/hello/John/30";

        // Act
        var result = getPathParameters.apply(formalUri).apply(actualUri);

        // Assert
        assertEquals(2, result.size());
        assertEquals("John", result.get("name").getValue());
        assertEquals("30", result.get("age").getValue());
    }

    @Test
    public void testGetPathParametersNone() {
        // Arrange
        var formalUri = "/hello";
        var actualUri = "/hello";

        // Act
        var result = getPathParameters.apply(formalUri).apply(actualUri);

        // Assert
        assertEquals(0, result.size());
    }

    @Test
    public void testGetQueryParametersOne() {
        // Arrange
        var uri = "/hello?name=John";

        // Act
        var result = getQueryParameters.apply(uri);

        // Assert
        assertEquals(1, result.size());
        assertEquals("John", result.get("name").getValue());
    }

    @Test
    public void testGetQueryParametersMultiple() {
        // Arrange
        var uri = "/hello?name=John&age=30&height=180";

        // Act
        var result = getQueryParameters.apply(uri);

        // Assert
        assertEquals(3, result.size());
        assertEquals("John", result.get("name").getValue());
        assertEquals("30", result.get("age").getValue());
        assertEquals("180", result.get("height").getValue());
    }

    @Test
    public void testGetQueryParametersNone() {
        // Arrange
        var uri = "/hello";

        // Act
        var result = getQueryParameters.apply(uri);

        // Assert
        assertEquals(0, result.size());
    }
}
