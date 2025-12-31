package org.pulse.npsservice.service;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;
import com.azure.communication.email.models.EmailAddress;
import com.azure.communication.email.models.EmailMessage;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import org.pulse.npsservice.dto.DailyFeedbackCountDto;
import org.pulse.npsservice.dto.FeedbackDto;
import org.pulse.npsservice.dto.FeedbackTypeCountDto;
import org.pulse.npsservice.dto.WeeklyReportDto;

@ApplicationScoped
public class EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailService.class);

    @ConfigProperty(name = "azure.communication.email.connection-string")
    String connectionString;

    @ConfigProperty(name = "email.detractor.sender")
    String senderAddress;

    @ConfigProperty(name = "email.detractor.recipient")
    String detractorRecipient;

    @ConfigProperty(name = "email.weekly-report.recipient")
    String weeklyReportRecipient;

    private EmailClient emailClient;

    @PostConstruct
    public void init() {
        this.emailClient = new EmailClientBuilder().connectionString(this.connectionString).buildClient();
    }

    public void sendDetractorNotification(FeedbackDto feedback) {
        try {
            EmailMessage message = new EmailMessage()
                    .setSenderAddress(this.senderAddress)
                    .setToRecipients(new EmailAddress(this.detractorRecipient))
                    .setSubject("Detractor Alert - Low NPS Score Received")
                    .setBodyPlainText(this.buildDetractorEmailBody(feedback));
            this.emailClient.beginSend(message);
        } catch (Exception exception) {
            LOGGER.error("Failed to send detractor notification email", exception);
            throw new RuntimeException("Error sending detractor notification email", exception);
        }
    }

    public void sendWeeklyReport(WeeklyReportDto report) {
        try {
            EmailMessage message = new EmailMessage()
                    .setSenderAddress(this.senderAddress)
                    .setToRecipients(new EmailAddress(this.weeklyReportRecipient))
                    .setSubject("Weekly NPS Feedback Report - " + report.startDate() + " to " + report.endDate())
                    .setBodyPlainText(this.buildWeeklyReportEmailBody(report));
            this.emailClient.beginSend(message);
        } catch (Exception exception) {
            LOGGER.error("Failed to send weekly report email", exception);
            throw new RuntimeException("Error sending weekly report email", exception);
        }
    }

    private String buildDetractorEmailBody(FeedbackDto feedback) {
        return String.format("""
                A detractor feedback has been received:
                
                Score: %d/10
                Type: %s
                Comment: %s
                
                Please follow up with this customer as soon as possible.
                
                ---
                This is an automated notification from the Pulse NPS Service.
                """, feedback.score(), feedback.type(), feedback.comment());
    }

    private String buildWeeklyReportEmailBody(WeeklyReportDto report) {
        StringBuilder body = new StringBuilder();
        body.append(report.description()).append("\n\n");
        body.append("Report Period: ").append(report.startDate()).append(" to ").append(report.endDate()).append("\n");
        body.append("Generated: ").append(report.reportGeneratedAt()).append("\n\n");
        body.append("=".repeat(60)).append("\n");
        body.append("SUMMARY\n");
        body.append("=".repeat(60)).append("\n");
        body.append(String.format("Total Feedbacks: %d\n", report.totalFeedbacks()));
        body.append(String.format("Average Score: %.2f/10\n\n", report.averageScore()));
        body.append("Feedbacks by Urgency:\n");
        for (FeedbackTypeCountDto typeCount : report.feedbackCountByType()) {
            body.append(String.format("  • %s (%s): %d\n", typeCount.type(), typeCount.urgency(), typeCount.count()));
        }
        body.append("\n");
        body.append("=".repeat(60)).append("\n");
        body.append("DAILY BREAKDOWN\n");
        body.append("=".repeat(60)).append("\n");
        for (DailyFeedbackCountDto dailyCount : report.feedbackCountByDay()) {
            body.append(String.format("  %s: %d feedbacks\n", dailyCount.date(), dailyCount.count()));
        }
        body.append("\n");
        body.append("---\n");
        body.append("This is an automated weekly report from the Pulse NPS Service.\n");
        return body.toString();
    }
}
