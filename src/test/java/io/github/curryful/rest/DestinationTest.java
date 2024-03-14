package io.github.curryful.rest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class DestinationTest {
    
    @Test
    public void testIsFormalTrue() {
        // Arrange
        var formal = Destination.of(HttpMethod.GET, "/hello/:name");
        var actual = Destination.of(HttpMethod.GET, "/hello/John");

        // Act
        var result = Destination.isFormal(formal, actual);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testIsFormalTrueWithQuery() {
        // Arrange
        var formal = Destination.of(HttpMethod.GET, "/hello/:name");
        var actual = Destination.of(HttpMethod.GET, "/hello/John?lang=en");

        // Act
        var result = Destination.isFormal(formal, actual);

        // Assert
        assertTrue(result);
    }

    @Test
    public void testIsFormalFalse() {
        // Arrange
        var formal = Destination.of(HttpMethod.GET, "/hello/:name");
        var actual = Destination.of(HttpMethod.GET, "/hello");

        // Act
        var result = Destination.isFormal(formal, actual);

        // Assert
        assertFalse(result);
    }
}
