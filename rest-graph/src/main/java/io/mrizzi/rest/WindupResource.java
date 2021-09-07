package io.mrizzi.rest;

import org.jboss.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/windup")
public class WindupResource {
    private static final Logger LOG = Logger.getLogger(WindupResource.class);

    @GET
    public Object issues() {
        return "Hello Windup";
    }
}
