package me.mrizzi;

import me.mrizzi.event.schema.Movie;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/movies")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MovieResource {

    private static final Logger LOGGER = Logger.getLogger("MovieResource");

    @Inject
    @Channel("movies") Emitter<Movie> emitter;

    @POST
    public Response enqueueMovie(Movie movie) {
        LOGGER.infof("Sending movie %s to Kafka", movie.getTitle());
        Movie anotherMovie = Movie.newBuilder().setTitle("Back to the Future III").setYear(1990).build();
        emitter.send(anotherMovie);
        LOGGER.infof("Sent movie %s to Kafka", anotherMovie.getTitle());
        return Response.accepted().build();
    }

}
