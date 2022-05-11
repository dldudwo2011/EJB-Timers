/**
 * @author Youngjae Lee
 * @version 2022-03-07
 *
 * description: Batch job resource
 */

package common.batch;

import jakarta.batch.operations.JobOperator;
import jakarta.batch.operations.JobStartException;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.batch.runtime.JobExecution;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.Set;

@Path("batch-jobs")						// All methods of this class are associated this URL path
@Consumes(MediaType.APPLICATION_JSON)	// All methods this class accept only JSON format data
@Produces(MediaType.APPLICATION_JSON)	// All methods returns data that has been converted to JSON format
public class BatchJobResource {

	@POST					// This method only accepts HTTP POST requests.
	@Path("{filename}")
	public Response startBatchJob(@PathParam("filename") String jobXMLName, @Context UriInfo uriInfo) {
		JobOperator jobOperator = BatchRuntime.getJobOperator();

		try {
			long jobId = jobOperator.start(jobXMLName, null);

			URI location = URI.create(uriInfo.getBaseUri().toString() + "batch-jobs/" + jobId );

			return Response
				.created(location)
				.build();
		} catch (JobStartException ex) {
			ex.printStackTrace();
			return Response.status(Response.Status.NOT_FOUND).entity(ex.getMessage()).build();
		} catch (Exception ex) {
			ex.printStackTrace();
			return Response.serverError().entity(ex.getMessage()).build();
		}
	}

	@GET 			// This method only accepts HTTP GET requests.
	@Path("{id}")	// This method accepts a path parameter and gives it a name of id
	public Response getBatchStatus(@PathParam("id") Long jobId) {
		JobOperator jobOperator = BatchRuntime.getJobOperator();
		try {
			JobExecution jobExecution = jobOperator.getJobExecution(jobId);
			String jobStatus = jobExecution.getBatchStatus().toString();
			return Response.ok(jobStatus).build();
		} catch (Exception ex) {
			ex.printStackTrace();
			return Response.status(Response.Status.NOT_FOUND).build();
		}
	}

	@GET 				// This method only accepts HTTP GET requests.
	@Path("jobnames")
	public Response getJobNames() {
		JobOperator jobOperator = BatchRuntime.getJobOperator();
		try {
			Set<String> jobNames = jobOperator.getJobNames();
			return Response.ok(jobNames).build();
		} catch (Exception ex) {
			ex.printStackTrace();
			return Response.serverError().entity(ex.getMessage()).build();
		}
	}

}