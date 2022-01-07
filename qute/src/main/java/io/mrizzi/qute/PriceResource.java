package io.mrizzi.qute;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.scheduler.Scheduled;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@ServerEndpoint("/item/{id}/price")
@Produces(MediaType.TEXT_HTML)
@ApplicationScoped
public class PriceResource {

    private final AtomicInteger counter = new AtomicInteger();
    private final Map<Integer, List<Session>> sessions = new ConcurrentHashMap<>();
    private final Random random = new Random();

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance liItem(Item item);
    }

    @Inject
    ItemResource itemResource;

    public int get() {
        return counter.get();
    }

    @Scheduled(every="1s")
    void increment() {
        counter.incrementAndGet();
        Item item = itemResource.items.get(random.ints(0, itemResource.items.size()).findFirst().getAsInt());
        item.price = BigDecimal.valueOf(Math.random());
        broadcast(item);
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("id") Integer id) {
        sessions.computeIfAbsent(id, integer -> new ArrayList<>()).add(session);
    }

    @OnClose
    public void onClose(Session session, @PathParam("id") Integer id) {
        sessions.get(id).remove(session);
    }

    @OnError
    public void onError(Session session, @PathParam("id") Integer id, Throwable throwable) {
        sessions.get(id).remove(session);
        throwable.printStackTrace();
    }

    @OnMessage
    public void onMessage(String message, @PathParam("id") Integer id) {
    }

    private void broadcast(Item item) {
        sessions.getOrDefault(item.id, Collections.emptyList())
                .forEach(session -> session.getAsyncRemote()
                        .sendObject(Templates.liItem(item).render(), result -> {
                            if (result.getException() != null) {
                                System.out.println("Unable to send message: " + result.getException());
                            }
                        })
                );
    }
}
