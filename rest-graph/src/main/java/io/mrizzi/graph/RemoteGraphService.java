package io.mrizzi.graph;

import io.quarkus.runtime.Startup;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.util.system.ConfigurationUtil;
import org.jboss.logging.Logger;
import org.jboss.windup.graph.model.WindupFrame;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import java.io.File;

import static org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource.traversal;

@Startup
@ApplicationScoped
public class RemoteGraphService extends GraphService {
    private static final Logger LOG = Logger.getLogger(RemoteGraphService.class);
    private static final String DEFAULT_CENTRAL_GRAPH_CONFIGURATION_FILE_NAME = "src/main/resources/conf/remote-graph.properties";

    @ConfigProperty(defaultValue = DEFAULT_CENTRAL_GRAPH_CONFIGURATION_FILE_NAME, name = "io.mrizzi.graph.central.properties.file.path")
    File centralGraphProperties;

    private JanusGraph janusGraph;
    private GraphTraversalSource g;
    private Cluster cluster;
    private Client client;

    @PostConstruct
    void init() throws Exception {
        final Configuration configuration = ConfigurationUtil.loadPropertiesConfig(centralGraphProperties);
        // using the remote driver for schema
        try {
            cluster = Cluster.open(configuration.getString("gremlin.remote.driver.clusterFile"));
            client = cluster.connect();
        } catch (Exception e) {
            throw new ConfigurationException(e);
        }

        final ResultSet resultSet = client.submit(createManagementRequest());
        resultSet.stream().map(Result::toString).forEach(LOG::info);

        g = traversal().withRemote("conf/remote-graph.properties");
    }

    @PreDestroy
    void destroy() throws Exception {
        LOG.infof("Is central Janus Graph transaction open? %b", g.tx().isOpen());
        g.close();
        LOG.infof("Is central Janus Graph transaction still open? %b", g.tx().isOpen());
    }

    private String createManagementRequest() {
        final StringBuilder request = new StringBuilder();
        request.append("JanusGraphManagement janusGraphManagement = graph.openManagement(); ");
        request.append("boolean created = false; ");
        request.append("if (!janusGraphManagement.containsPropertyKey(\"").append(WindupFrame.TYPE_PROP).append("\")) { ");
            request.append("PropertyKey typePropPropertyKey = janusGraphManagement.makePropertyKey(\"w:winduptype\").dataType(String.class).cardinality(Cardinality.LIST).make(); ");
            request.append("janusGraphManagement.buildIndex(\"w:winduptype\", Vertex.class).addKey(typePropPropertyKey).buildCompositeIndex(); ");
            request.append("janusGraphManagement.buildIndex(\"edge-typevalue\", Edge.class).addKey(typePropPropertyKey).buildCompositeIndex(); ");
            request.append("PropertyKey applicationIdPropertyKey = janusGraphManagement.makePropertyKey(\"applicationId\").dataType(String.class).cardinality(Cardinality.SINGLE).make(); ");
            request.append("janusGraphManagement.buildIndex(\"applicationId\", Vertex.class).addKey(applicationIdPropertyKey, Mapping.STRING.asParameter()).buildMixedIndex(\"search\"); ");
            request.append("janusGraphManagement.commit(); created = true; ");
        request.append("} ");
        return request.toString();
    }

    @Override
    public JanusGraph getCentralJanusGraph() {
        return (JanusGraph) g.getGraph();
    }

    public Graph getCentralGraph() {
        return g.getGraph();
    }

    @Override
    public GraphTraversalSource getCentralGraphTraversalSource() {
        return g;
    }
}
