package org.pulse.npsservice.dto;

public record ErrorResponse(
        String message,
        int status
) {
}
