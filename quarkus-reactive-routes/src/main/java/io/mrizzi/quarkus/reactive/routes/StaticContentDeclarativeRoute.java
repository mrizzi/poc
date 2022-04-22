package io.mrizzi.quarkus.reactive.routes;

import io.quarkus.vertx.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.FileSystemAccess;
import io.vertx.ext.web.handler.StaticHandler;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StaticContentDeclarativeRoute {

    @Route(path = "/static/*", methods = Route.HttpMethod.GET)
    void staticContent(RoutingContext rc) {
        StaticHandler.create(FileSystemAccess.RELATIVE, "content/").handle(rc);
    }

    @Route(path = "/homepage", methods = Route.HttpMethod.GET)
    void indexContent(RoutingContext rc) {
        StaticHandler.create(FileSystemAccess.RELATIVE, "content/index.html").handle(rc);
    }

}
