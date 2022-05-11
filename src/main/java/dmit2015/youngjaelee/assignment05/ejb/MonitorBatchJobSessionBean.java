package dmit2015.youngjaelee.assignment05.ejb;

import dmit2015.youngjaelee.assignment05.entity.CurrentCasesByLocalGeographicArea;
import dmit2015.youngjaelee.assignment05.repository.CurrentCasesByLocalGeographicAreaRepository;
import jakarta.annotation.Resource;
import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.batch.runtime.BatchStatus;
import jakarta.batch.runtime.JobExecution;
import jakarta.ejb.*;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.logging.Logger;


@Stateless
public class MonitorBatchJobSessionBean {

    private Logger _logger = Logger.getLogger(MonitorBatchJobSessionBean.class.getName());

    @Resource
    private TimerService _timerService;

    @Inject
    @ConfigProperty(name="batch.monitor.initialDuration")
    private long _initialDuration;  // in milliseconds

    @Inject
    @ConfigProperty(name="batch.monitor.intervalDuration")
    private long _intervalDuration;  // in milliseconds

    @Inject
    @ConfigProperty(name="dmit2015.youngjaelee.MailToAddresses")
    private String mailToAddress;

    @Inject
    private CurrentCasesByLocalGeographicAreaRepository _currentCasesByLocalGeographicAreaRepository;

    @Inject
    private EmailSessionBean mail;

    @Timeout
    public void checkBatchJobStatus(Timer timer) {
        // Extract the jobId from the timer
        long jobId = (long) timer.getInfo();
        JobOperator jobOperator = BatchRuntime.getJobOperator();
        JobExecution jobExecution = jobOperator.getJobExecution(jobId);
        if (jobExecution.getBatchStatus() == BatchStatus.COMPLETED) {
            timer.cancel();
            sendEmail(timer,true);
            _logger.info("BATCH job " + jobId + " COMPLETED");
            // send email to notified batch has COMPLETED
        } else if (jobExecution.getBatchStatus() == BatchStatus.FAILED) {
            // send email to notified batch job has FAILED
            timer.cancel();
            sendEmail(timer,false);
            _logger.info("BATCH job " + jobId + " FAILED");
        }
    }

    public Timer createTimer(long jobId) {
        return _timerService.createTimer(_initialDuration, _intervalDuration, jobId);
    }

    private void sendEmail(Timer timer, boolean status) {
        if (!mailToAddress.isBlank()) {
            String mailSubject = null;
            String mailText = null;

            if (status == true) {
                List<CurrentCasesByLocalGeographicArea> allItemsInDatabase = _currentCasesByLocalGeographicAreaRepository.list();

                String finalString = "";

                for (CurrentCasesByLocalGeographicArea item :allItemsInDatabase) {
                    finalString += "<tr>\n" +
                            "<td style=\"border: 1px solid black; border-collapse: collapse;\">" + item.getLocation() + "</td>\n" +
                    "<td style=\"border: 1px solid black; border-collapse: collapse;\">" + item.getDate() +"</td>\n" +
                    "<td style=\"border: 1px solid black; border-collapse: collapse;text-align: right;\">" + item.getActiveCases() +"</td>\n" +
                            "<td style=\"border: 1px solid black; border-collapse: collapse;text-align: right;\">" + item.getCaseDataPopulation() +"</td>\n" +
                    "</tr>";
                }

                mailSubject = "DMIT2015 Assignment 5 Batch Job COMPLETED";
                mailText = "<table style=\"border: 1px solid black; border-collapse: collapse;\">\n" +
                        "  <tr>\n" +
                        "    <th style=\"border: 1px solid black; border-collapse: collapse;\">Location</th>\n" +
                        "    <th style=\"border: 1px solid black; border-collapse: collapse;\">Date</th>\n" +
                        "    <th style=\"border: 1px solid black; border-collapse: collapse; text-align: right;\">Active Cases</th>\n" +
                        "    <th style=\"border: 1px solid black; border-collapse: collapse; text-align: right;\">Population</th>\n" +
                        "  </tr>" + finalString + "</table>";
            } else {
                mailSubject = "DMIT2015 Assignment 5 Batch Job FAILED";
                mailText = "job failed. Id - " + timer.getInfo().toString();
            }

            try {
                mail.sendHtmlEmail(mailToAddress, mailSubject, mailText);
                _logger.info("Successfully sent email to " + mailToAddress);
            } catch (Exception e) {
                e.printStackTrace();
                _logger.fine("Error sending email with exception " + e.getMessage());
            }
        }
    }
}

