/**
 * Author: Fernando Serena (fserena@ciclope.info)
 * Organization: Ciclope Group (UPM)
 * Project: GLORIA
 */
package eu.gloria.gs.services.api.resources;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import eu.gloria.gs.services.core.client.GSClientProvider;
import eu.gloria.gs.services.experiment.base.data.ReservationInformation;
import eu.gloria.gs.services.experiment.base.data.TimeSlot;
import eu.gloria.gs.services.experiment.base.reservation.ExperimentReservationArgumentException;
import eu.gloria.gs.services.experiment.base.reservation.MaxReservationTimeException;
import eu.gloria.gs.services.experiment.base.reservation.NoReservationsAvailableException;
import eu.gloria.gs.services.experiment.online.OnlineExperimentException;
import eu.gloria.gs.services.experiment.online.OnlineExperimentInterface;

/**
 * @author Fernando Serena (fserena@ciclope.info)
 * 
 */
@Path("/experiments")
public class Experiments {

	@Context
	HttpServletRequest request;

	private static OnlineExperimentInterface experiments;

	static {
		GSClientProvider.setHost("saturno.datsi.fi.upm.es");
		GSClientProvider.setPort("8443");
		experiments = GSClientProvider.getOnlineExperimentClient();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{name}")
	public String getMessage(@PathParam("name") String name) {
		return "{'name':" + name + "}";
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/online/list")
	public Response listExperiments() {

		if (request.getAttribute("user") != null) {

			GSClientProvider.setCredentials(
					(String) request.getAttribute("user"),
					(String) request.getAttribute("password"));
		}

		try {
			List<String> names = experiments.getAllOnlineExperiments();

			return Response.ok(names).build();

		} catch (OnlineExperimentException e) {
			return Response.serverError().entity(e.getMessage()).build();
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/online/active")
	public Response listActiveExperiments() {

		if (request.getAttribute("user") != null) {

			GSClientProvider.setCredentials(
					(String) request.getAttribute("user"),
					(String) request.getAttribute("password"));
		}

		try {
			List<ReservationInformation> reservations = experiments
					.getMyCurrentReservations();

			return Response.ok(reservations).build();

		} catch (OnlineExperimentException e) {
			return Response.serverError().entity(e.getMessage()).build();
		} catch (NoReservationsAvailableException e) {
			return Response.ok(new ArrayList<String>()).build();
		}
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/online/pending")
	public Response listPendingExperiments() {

		if (request.getAttribute("user") != null) {

			GSClientProvider.setCredentials(
					(String) request.getAttribute("user"),
					(String) request.getAttribute("password"));
		}

		try {
			List<ReservationInformation> reservations = experiments
					.getMyPendingReservations();

			return Response.ok(reservations).build();

		} catch (OnlineExperimentException e) {
			return Response.serverError().entity(e.getMessage()).build();
		} catch (NoReservationsAvailableException e) {
			return Response.ok(new ArrayList<String>()).build();
		}
	}

	@GET
	@Path("/online/reserve/{experiment}")
	public Response reserveExperiment(
			@PathParam("experiment") String experiment,
			@QueryParam("rts") List<String> rts,
			@QueryParam("from") String from, @QueryParam("to") String to) {

		if (request.getAttribute("user") != null) {

			GSClientProvider.setCredentials(
					(String) request.getAttribute("user"),
					(String) request.getAttribute("password"));
		}

		try {

			TimeSlot ts = new TimeSlot();

			DateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

			try {
				ts.setBegin(format.parse(from));
				ts.setEnd(format.parse(to));
			} catch (ParseException e) {
				return Response.status(Status.BAD_REQUEST)
						.entity(e.getMessage()).build();
			}

			experiments.reserveExperiment(experiment, rts, ts);
			return Response.ok().build();

		} catch (OnlineExperimentException e) {
			return Response.serverError().entity(e.getMessage()).build();
		} catch (NoReservationsAvailableException e) {
			return Response.status(Status.NOT_ACCEPTABLE)
					.entity(e.getMessage()).build();
		} catch (ExperimentReservationArgumentException e) {
			return Response.status(Status.NOT_ACCEPTABLE)
					.entity(e.getMessage()).build();
		} catch (MaxReservationTimeException e) {
			return Response.status(Status.NOT_ACCEPTABLE)
					.entity(e.getMessage()).build();
		}
	}
}
