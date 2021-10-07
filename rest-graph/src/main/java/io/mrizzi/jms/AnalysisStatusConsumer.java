package io.mrizzi.jms;

import io.mrizzi.graph.GraphService;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.jboss.windup.web.services.json.WindupExecutionJSONUtil;
import org.jboss.windup.web.services.model.WindupExecution;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.jms.ConnectionFactory;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ApplicationScoped
public class AnalysisStatusConsumer implements Runnable {

    @Inject
    ConnectionFactory connectionFactory;

    @Inject
    GraphService graphService;

    private final ExecutorService scheduler = Executors.newSingleThreadExecutor();

    private volatile String lastUpdate;

    public String getLastUpdate() {
        return lastUpdate;
    }

    void onStart(@Observes StartupEvent ev) {
        scheduler.submit(this);
    }

    void onStop(@Observes ShutdownEvent ev) {
        scheduler.shutdown();
    }

    @Override
    public void run() {
        try (JMSContext context = connectionFactory.createContext(Session.AUTO_ACKNOWLEDGE)) {
            JMSConsumer consumer = context.createConsumer(context.createQueue("statusUpdateQueue"));
            while (true) {
                Message message = consumer.receive();
                if (message == null) return;
                lastUpdate = message.getBody(String.class);
                System.out.println("lastUpdate = " + lastUpdate);
                WindupExecution windupExecution = WindupExecutionJSONUtil.readJSON(lastUpdate);
                switch (windupExecution.getState()) {
                    case COMPLETED:
                        System.out.println("COMPLETED = " + lastUpdate);
                        graphService.updateCentralJanusGraph(windupExecution.getOutputPath(), Long.toString(message.getLongProperty("projectId")));
                        // TODO delete the application file now
                        System.out.println("COMPLETED updateCentralJanusGraph");
                        break;
                    default:
                        break;
                }
            }
        } catch (JMSException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
