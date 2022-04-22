package io.mrizzi.quarkus.reactive.routes;

import io.quarkus.security.Authenticated;
import io.quarkus.vertx.web.Route;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.FileSystemAccess;
import io.vertx.ext.web.handler.StaticHandler;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StaticAuthenticatedDeclarativeRoute {

    @Route(path = "/auth-static/*", methods = Route.HttpMethod.GET)
    @Authenticated
    void secureStatic(RoutingContext rc) {
        StaticHandler.create(FileSystemAccess.RELATIVE, "content/").handle(rc);
    }

}
