package io.mrizzi.rest;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.sse.OutboundSseEvent;
import javax.ws.rs.sse.Sse;
import javax.ws.rs.sse.SseBroadcaster;
import javax.ws.rs.sse.SseEventSink;

@Singleton
@Path("/windup/analysisSse")
public class WindupBroadcasterResource {

    private Sse sse;
    private SseBroadcaster broadcaster;

    @Context
    public void setSse(final Sse sse) {
        this.sse = sse;
        this.broadcaster = sse.newBroadcaster();
    }

    public void broadcastMessage(String message) {
        final OutboundSseEvent event = sse.newEventBuilder()
                .name("message")
                .mediaType(MediaType.APPLICATION_JSON_TYPE)
                .data(String.class, message)
                .build();
        broadcaster.broadcast(event);
    }

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void listenToBroadcast(@Context SseEventSink eventSink) {
        this.broadcaster.register(eventSink);
    }
}
