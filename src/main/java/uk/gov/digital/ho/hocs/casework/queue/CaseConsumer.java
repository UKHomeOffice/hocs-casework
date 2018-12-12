package uk.gov.digital.ho.hocs.casework.queue;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.aws.sqs.SqsConstants;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.domain.HocsCaseDomain;
import uk.gov.digital.ho.hocs.casework.domain.HocsCommand;

import static uk.gov.digital.ho.hocs.casework.application.RequestData.transferHeadersToMDC;

@Component
public class CaseConsumer extends RouteBuilder {

    private final HocsCaseDomain hocsCaseDomain;
    private final String caseQueue;
    private final String dlq;
    private final int maximumRedeliveries;
    private final int redeliveryDelay;
    private final int backOffMultiplier;

    @Autowired
    public CaseConsumer(HocsCaseDomain hocsCaseDomain,
                        @Value("${case.queue}") String caseQueue,
                        @Value("${case.queue.dlq}") String dlq,
                        @Value("${case.queue.maximumRedeliveries}") int maximumRedeliveries,
                        @Value("${case.queue.redeliveryDelay}") int redeliveryDelay,
                        @Value("${case.queue.backOffMultiplier}") int backOffMultiplier) {
        this.hocsCaseDomain = hocsCaseDomain;
        this.caseQueue = caseQueue;
        this.dlq = dlq;
        this.maximumRedeliveries = maximumRedeliveries;
        this.redeliveryDelay = redeliveryDelay;
        this.backOffMultiplier = backOffMultiplier;
    }

    @Override
    public void configure() {

        errorHandler(deadLetterChannel(dlq)
                .loggingLevel(LoggingLevel.ERROR)
                .log("Failed to action command after configured back-off.")
                .useOriginalMessage()
                .retryAttemptedLogLevel(LoggingLevel.WARN)
                .maximumRedeliveries(maximumRedeliveries)
                .redeliveryDelay(redeliveryDelay)
                .backOffMultiplier(backOffMultiplier)
                .asyncDelayedRedelivery()
                .logRetryStackTrace(true));

        from(caseQueue)
                .setProperty(SqsConstants.RECEIPT_HANDLE, header(SqsConstants.RECEIPT_HANDLE))
                .process(transferHeadersToMDC())
                .log("Command received: ${body}")
                .unmarshal().json(JsonLibrary.Jackson, HocsCommand.class)
                .log("Command unmarshalled")
                .bean(hocsCaseDomain, "executeCommand")
                .log("Command processed")
                .setHeader(SqsConstants.RECEIPT_HANDLE, exchangeProperty(SqsConstants.RECEIPT_HANDLE));
    }

}