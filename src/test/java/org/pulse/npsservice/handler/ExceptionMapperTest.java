package org.pulse.npsservice.handler;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.pulse.npsservice.dto.ErrorResponse;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@QuarkusTest
class ExceptionMapperTest {

    @Inject
    ExceptionMapper exceptionMapper;

    @Test
    void testMapConstraintViolationException() {
        // Given
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Score must be at least 1");
        when(violation.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
        when(violation.getPropertyPath().toString()).thenReturn("score");
        violations.add(violation);
        ConstraintViolationException exception = new ConstraintViolationException(violations);

        // When
        ErrorResponse response = this.exceptionMapper.mapToErrorResponse(exception);

        // Then
        assertNotNull(response);
        assertEquals(400, response.status());
        assertTrue(response.message().contains("Score must be at least 1"));
    }

    @Test
    void testMapIllegalArgumentException() {
        // Given
        String errorMessage = "Invalid argument provided";
        IllegalArgumentException exception = new IllegalArgumentException(errorMessage);

        // When
        ErrorResponse response = this.exceptionMapper.mapToErrorResponse(exception);

        // Then
        assertNotNull(response);
        assertEquals(400, response.status());
        assertEquals(errorMessage, response.message());
    }

    @Test
    void testMapUnknownException() {
        // Given
        RuntimeException exception = new RuntimeException("Unexpected error");

        // When
        ErrorResponse response = this.exceptionMapper.mapToErrorResponse(exception);

        // Then
        assertNotNull(response);
        assertEquals(500, response.status());
        assertEquals("Internal server error", response.message());
    }
}
