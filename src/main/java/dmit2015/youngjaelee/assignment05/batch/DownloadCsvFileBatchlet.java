/**
 * @author Youngjae Lee
 * @version 2022-03-07
 *
 * description: Batchlet for downloading
 */

package dmit2015.youngjaelee.assignment05.batch;

import jakarta.batch.api.AbstractBatchlet;
import jakarta.batch.runtime.BatchStatus;
import jakarta.batch.runtime.context.JobContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;


@Named("downloadManager")
public class DownloadCsvFileBatchlet extends AbstractBatchlet {

    @Inject
    private JobContext _jobContext;

    @Inject
    @ConfigProperty(name="dmit2015.youngjaelee.DownloadPath")
    private String _fileDownloadPath;

    @Inject
    @ConfigProperty(name="dmit2015.youngjaelee.DownloadUri")
    private String _csvFileUri;

    @Override
    public String process() throws Exception {
        String batchStatus = BatchStatus.COMPLETED.toString();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(_csvFileUri))
                .build();
        Path downloadPath = Path.of(_fileDownloadPath);
        HttpResponse<Path> response = client.send(request,
                HttpResponse.BodyHandlers.ofFileDownload(downloadPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE));
        if (response.statusCode() != Response.Status.OK.getStatusCode()) {
            batchStatus = BatchStatus.FAILED.toString();
        }

        return batchStatus;
    }
}
