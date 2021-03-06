package io.tackle.controls.resources;

import io.tackle.commons.resources.ListFilteredResource;
import org.jboss.resteasy.links.LinkResource;
import io.tackle.controls.entities.BusinessService;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.List;

@Path("business-service")
public class BusinessServiceListFilteredResource implements ListFilteredResource<BusinessService> {

    @Override
    public Class<BusinessService> getPanacheEntityType() {
        return BusinessService.class;
    }

    /**
     * workaround to have pagination and sorting as in REST Data Panache with also filtering.
     * <p>
     * The methods are copied from the `BusinessServiceResourceJaxRs_*` class created from REST Data Panache
     * at build time and "enhanced" here to manage also filtering.
     * <p>
     * This must be improved.
     */

    @GET
    @Path("")
    @Produces({"application/json"})
    @LinkResource(
            entityClassName = "io.tackle.controls.entities.BusinessService",
            rel = "list"
    )
    public Response list(@QueryParam("sort") List var1,
                         @QueryParam("page") @DefaultValue("0") int var2,
                         @QueryParam("size") @DefaultValue("20") int var3,
                         @QueryParam("filter") @DefaultValue("") String filter,
                         @Context UriInfo var4) throws Exception {
        return ListFilteredResource.super.list(var1, var2, var3, filter, var4, false);
    }

    // reported because HAL implementation was not able to find inherited @Path
    // in https://github.com/resteasy/Resteasy/blob/8e20aa272c828ebdf2ba5d0c874f5eb655029b87/resteasy-core/src/main/java/org/jboss/resteasy/specimpl/ResteasyUriBuilderImpl.java#L374
    @Path("")
    @GET
    @Produces({"application/hal+json"})
    public Response listHal(@QueryParam("sort") List var1,
                            @QueryParam("page") @DefaultValue("0") int var2,
                            @QueryParam("size") @DefaultValue("20") int var3,
                            @QueryParam("filter") @DefaultValue("") String filter,
                            @Context UriInfo var4) throws Exception {
        return ListFilteredResource.super.list(var1, var2, var3, filter, var4, true);
    }
}
