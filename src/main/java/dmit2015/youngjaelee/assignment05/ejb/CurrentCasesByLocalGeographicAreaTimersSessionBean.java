package dmit2015.youngjaelee.assignment05.ejb;

import dmit2015.youngjaelee.assignment05.entity.CurrentCasesByLocalGeographicArea;
import jakarta.annotation.Resource;
import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.ejb.*;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.logging.Logger;

@Singleton
@Startup
public class CurrentCasesByLocalGeographicAreaTimersSessionBean {
    @Resource   // This is a container created resource
    private TimerService _timerService;

    private Logger _logger = Logger.getLogger(CurrentCasesByLocalGeographicArea.class.getName());
    // @Inject // Use only if your project includes a LoggerProducer
    // private Logger _logger;

    @Inject
    private MonitorBatchJobSessionBean _monitorBatchJobSessionBean; // This is used to monitor the status of the batch job

    @Inject
    @ConfigProperty(name = "dmit2015.youngjaelee.BatchJobXmlFileName")
    private String jobXmlFileName;

    @Schedule(second = "0", minute ="0", hour = "16", dayOfWeek = "Mon-Fri", month = "*", year = "2022", info ="DMIT2015-OE01 Assignment 5", timezone="MDT", persistent = false)
    public void dmit2015SectionOE01ClassNotifiation(Timer timer) {
        try {

            // Get the JobOperator from the BatchRuntime
            JobOperator jobOperator = BatchRuntime.getJobOperator();
            // Create a new job instance and start the first execution of that instance asynchronously.
            long executionId = jobOperator.start(jobXmlFileName, null);

            _logger.info("Successfully started batch job with executionId " + executionId);

            // Create an interval timer to monitor the status of the batch job
            _monitorBatchJobSessionBean.createTimer(executionId);

            //Exception test
            throw new Exception("error");

        } catch (Exception e) {
            String errorMessage = e.getMessage();
            _logger.fine(errorMessage);
            sendErrorEmail("Error:" + errorMessage);
        }
    }

    @Inject
    @ConfigProperty(name="dmit2015.youngjaelee.MailToAddresses")
    private String mailToAddress;

    @Inject
    private EmailSessionBean mail;

    private void sendErrorEmail(String errorMessage) {
        if (!mailToAddress.isBlank()) {
            String mailSubject = "DMIT2015 Assignment 5 Batch Job EXCEPTION";
            String mailText = errorMessage;

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
