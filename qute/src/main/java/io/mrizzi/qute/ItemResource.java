package io.mrizzi.qute;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;

@Path("item")
public class ItemResource {

    private int id = 0;
    public final List<Item> items = Arrays.asList(
            new Item(id++, "Apple", BigDecimal.valueOf(Math.random())),
            new Item(id++, "Pear", BigDecimal.valueOf(Math.random())),
            new Item(id++, "Banana", BigDecimal.valueOf(Math.random())),
            new Item(id++, "Pineapple", BigDecimal.valueOf(Math.random()))
    );

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance item(Item item);
        public static native TemplateInstance items(List<Item> items);
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.TEXT_HTML)
    public CompletionStage<String> get(@PathParam("id") Integer id) {
/*
        try {
            Thread.sleep(600);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return Templates.item(items.stream().filter(item -> item.id.equals(id)).findFirst().orElseThrow()).renderAsync();
*/
        return Templates.item(items.get(id)).renderAsync();
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public CompletionStage<String> getAll() {
        return Templates.items(items).renderAsync();
    }
}
