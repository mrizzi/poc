package io.mrizzi.quarkus.reactive.routes;

import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.FileSystemAccess;
import io.vertx.ext.web.handler.StaticHandler;

import javax.enterprise.event.Observes;

public class StaticContentRoute {
    public void init(@Observes Router router) {
        router.get("/static/*").handler(StaticHandler.create(FileSystemAccess.RELATIVE, "content/"));
//        router.get("/static/*").handler(StaticHandler.create(FileSystemAccess.ROOT, "/absolute/path/to/static/content/quarkus-reactive-routes/content/index.html"));
    }
}
