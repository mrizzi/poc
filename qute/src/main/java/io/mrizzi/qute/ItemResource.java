package io.mrizzi.qute;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;

@Path("item")
public class ItemResource {

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance item(Item item);
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(@PathParam("id") Integer id) {
//        return Templates.item(service.findItem(id));
        Item item = new Item();
        item.name = id.toString();
        item.price = BigDecimal.valueOf(Math.random());
        return Templates.item(item);
    }
}
