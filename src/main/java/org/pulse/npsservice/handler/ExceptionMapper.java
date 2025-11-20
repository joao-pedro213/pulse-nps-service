package org.pulse.npsservice.handler;

import com.microsoft.azure.functions.HttpStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.pulse.npsservice.dto.ErrorResponse;

import java.util.stream.Collectors;

@ApplicationScoped
public class ExceptionMapper {

    public ErrorResponse mapToErrorResponse(Throwable throwable) {
        return switch (throwable) {
            case ConstraintViolationException constraintViolationException -> this.handleConstraintViolationException(constraintViolationException);
            case IllegalArgumentException illegalArgumentException -> this.handleIllegalArgumentException(illegalArgumentException);
            default -> this.handleUnknownException(throwable);
        };
    }

    private ErrorResponse handleConstraintViolationException(ConstraintViolationException exception) {
        String errors = exception.getConstraintViolations()
                .stream()
                .map(this::formatConstraintViolation)
                .collect(Collectors.joining(", "));
        return new ErrorResponse(errors, HttpStatus.BAD_REQUEST.value());
    }

    private String formatConstraintViolation(ConstraintViolation<?> violation) {
        String propertyPath = violation.getPropertyPath().toString();
        String message = violation.getMessage();
        return propertyPath.isEmpty() ? message : propertyPath + ": " + message;
    }

    private ErrorResponse handleIllegalArgumentException(IllegalArgumentException exception) {
        return new ErrorResponse(exception.getMessage(), HttpStatus.BAD_REQUEST.value());
    }

    private ErrorResponse handleUnknownException(Throwable throwable) {
        throwable.printStackTrace();
        return new ErrorResponse("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
