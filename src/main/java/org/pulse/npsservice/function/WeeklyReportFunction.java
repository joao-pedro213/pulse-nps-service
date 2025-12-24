package org.pulse.npsservice.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.pulse.npsservice.handler.ResponseBuilder;
import org.pulse.npsservice.service.EmailService;
import org.pulse.npsservice.service.WeeklyReportService;

import java.util.Optional;

@ApplicationScoped
public class WeeklyReportFunction {
    private WeeklyReportService weeklyReportService;
    private EmailService emailService;
    private ResponseBuilder responseBuilder;

    @Inject
    public WeeklyReportFunction(
            WeeklyReportService weeklyReportService,
            EmailService emailService,
            ResponseBuilder responseBuilder) {
        this.weeklyReportService = weeklyReportService;
        this.emailService = emailService;
        this.responseBuilder = responseBuilder;
    }

    @FunctionName("weekly-report")
    public HttpResponseMessage generateAndSendReport(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.ANONYMOUS)
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        return this.weeklyReportService
                .generateLastWeekReport()
                .map(report -> {
                    this.emailService.sendWeeklyReport(report);
                    return this.responseBuilder.noContent(request);
                })
                .onFailure()
                .recoverWithItem(throwable -> this.responseBuilder.error(request, throwable))
                .await()
                .indefinitely();
    }
}
