package org.pulse.npsservice.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.pulse.npsservice.dto.ErrorResponse;
import org.pulse.npsservice.dto.WeeklyReportDto;
import org.pulse.npsservice.service.EmailService;
import org.pulse.npsservice.service.WeeklyReportService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
class WeeklyReportFunctionTest {

    @Inject
    WeeklyReportFunction function;

    @InjectMock
    WeeklyReportService weeklyReportService;

    @InjectMock
    EmailService emailService;

    @Test
    void testWeeklyReportFunctionSuccess() {
        // Given
        @SuppressWarnings("unchecked") final HttpRequestMessage<Optional<String>> request = mock(HttpRequestMessage.class);
        doReturn(Optional.empty()).when(request).getBody();
        WeeklyReportDto mockReport = mock(WeeklyReportDto.class);
        when(this.weeklyReportService.generateLastWeekReport()).thenReturn(Uni.createFrom().item(mockReport));
        doNothing().when(this.emailService).sendWeeklyReport(any(WeeklyReportDto.class));
        doAnswer((Answer<HttpResponseMessage.Builder>) invocation -> {
            HttpStatus status = (HttpStatus) invocation.getArguments()[0];
            return new HttpResponseMessageMock.HttpResponseMessageBuilderMock().status(status);
        }).when(request).createResponseBuilder(any(HttpStatus.class));
        final ExecutionContext context = mock(ExecutionContext.class);

        // When
        final HttpResponseMessage response = this.function.generateAndSendReport(request, context);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatus());

        verify(this.emailService).sendWeeklyReport(mockReport);
    }

    @Test
    void testWeeklyReportFunctionWithServiceFailure() {
        // Given
        @SuppressWarnings("unchecked") final HttpRequestMessage<Optional<String>> req = mock(HttpRequestMessage.class);
        doReturn(Optional.empty()).when(req).getBody();
        when(this.weeklyReportService.generateLastWeekReport())
                .thenReturn(Uni.createFrom().failure(new RuntimeException("Database connection failed")));
        doAnswer(new Answer<HttpResponseMessage.Builder>() {
            @Override
            public HttpResponseMessage.Builder answer(InvocationOnMock invocation) {
                HttpStatus status = (HttpStatus) invocation.getArguments()[0];
                return new HttpResponseMessageMock.HttpResponseMessageBuilderMock().status(status);
            }
        }).when(req).createResponseBuilder(any(HttpStatus.class));
        final ExecutionContext context = mock(ExecutionContext.class);

        // When
        final HttpResponseMessage response = this.function.generateAndSendReport(req, context);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatus());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("Internal server error", errorResponse.message());
        assertEquals(500, errorResponse.status());
    }

    @Test
    void testWeeklyReportFunctionWithEmailFailure() {
        // Given
        @SuppressWarnings("unchecked") final HttpRequestMessage<Optional<String>> req = mock(HttpRequestMessage.class);
        doReturn(Optional.empty()).when(req).getBody();
        WeeklyReportDto mockReport = mock(WeeklyReportDto.class);
        when(this.weeklyReportService.generateLastWeekReport())
                .thenReturn(Uni.createFrom().item(mockReport));
        doThrow(new RuntimeException("Email service unavailable")).when(this.emailService).sendWeeklyReport(any(WeeklyReportDto.class));
        doAnswer((Answer<HttpResponseMessage.Builder>) invocation -> {
            HttpStatus status = (HttpStatus) invocation.getArguments()[0];
            return new HttpResponseMessageMock.HttpResponseMessageBuilderMock().status(status);
        }).when(req).createResponseBuilder(any(HttpStatus.class));
        final ExecutionContext context = mock(ExecutionContext.class);

        // When
        final HttpResponseMessage response = this.function.generateAndSendReport(req, context);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatus());
        assertNotNull(response.getBody());
        assertInstanceOf(ErrorResponse.class, response.getBody());
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals("Internal server error", errorResponse.message());
        assertEquals(500, errorResponse.status());
    }
}
