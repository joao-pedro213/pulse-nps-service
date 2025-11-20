package org.pulse.npsservice.handler;

import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.pulse.npsservice.dto.ErrorResponse;

@ApplicationScoped
public class ResponseBuilder {
    private final ExceptionMapper exceptionMapper;

    @Inject
    public ResponseBuilder(ExceptionMapper exceptionMapper) {
        this.exceptionMapper = exceptionMapper;
    }

    public <T> HttpResponseMessage created(HttpRequestMessage<?> request, T body) {
        return request.createResponseBuilder(HttpStatus.CREATED).body(body).build();
    }

    public HttpResponseMessage error(HttpRequestMessage<?> request, Throwable throwable) {
        ErrorResponse errorResponse = this.exceptionMapper.mapToErrorResponse(throwable);
        return request.createResponseBuilder(HttpStatus.valueOf(errorResponse.status())).body(errorResponse).build();
    }
}
