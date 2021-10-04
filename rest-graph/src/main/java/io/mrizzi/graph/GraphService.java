package io.mrizzi.graph;

import io.mrizzi.rest.WindupResource;
import io.quarkus.runtime.Startup;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.janusgraph.core.Cardinality;
import org.janusgraph.core.ConfiguredGraphFactory;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.util.system.ConfigurationUtil;
import org.jboss.logging.Logger;
import org.jboss.windup.graph.model.WindupFrame;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import java.io.File;

@Startup
@ApplicationScoped
public class GraphService {
    private static final Logger LOG = Logger.getLogger(WindupResource.class);
    private static final String DEFAULT_CENTRAL_GRAPH_CONFIGURATION_FILE_NAME = "src/main/resources/centralGraphConfiguration.properties";

    @ConfigProperty(defaultValue = DEFAULT_CENTRAL_GRAPH_CONFIGURATION_FILE_NAME, name = "io.mrizzi.graph.central.properties.file.path")
    File centralGraphProperties;

    private JanusGraph janusGraph;

    @PostConstruct
    void init() throws ConfigurationException {
        janusGraph = openCentralJanusGraph();
    }

    @PreDestroy
    void destroy() {
        LOG.infof("Is central Janus Graph transaction open? %b", janusGraph.tx().isOpen());
        LOG.infof("Closing Central Janus Graph properties file %s", centralGraphProperties);
        janusGraph.close();
        LOG.infof("Is central Janus Graph transaction still open? %b", janusGraph.tx().isOpen());
    }

    private JanusGraph openCentralJanusGraph() throws ConfigurationException {
        LOG.infof("Opening Central Janus Graph properties file %s", centralGraphProperties);
        final PropertiesConfiguration configuration = ConfigurationUtil.loadPropertiesConfig(centralGraphProperties);
        configuration.getKeys().forEachRemaining(LOG::info);
        configuration.setProperty("graph.unique-instance-id", "central_" + System.nanoTime());
        final JanusGraph janusGraph = JanusGraphFactory.open(configuration);
/*
        final JanusGraph janusGraph = JanusGraphFactory.build()
                .set("storage.backend", "berkeleyje")
                .set("storage.directory", "src/test/resources/graph2/central-graph")
                .set("storage.transactions", true)
                .open();
*/
//        final JanusGraph janusGraph = ConfiguredGraphFactory.open("central");
        LOG.infof("Central Graph vertex count at startup = %d", janusGraph.traversal().V().count().next());
        final JanusGraphManagement janusGraphManagement = janusGraph.openManagement();
        LOG.infof("Open instances: %s", janusGraphManagement.getOpenInstances());
        if (!janusGraphManagement.containsPropertyKey(WindupFrame.TYPE_PROP)) {
            final PropertyKey typePropPropertyKey = janusGraphManagement.makePropertyKey(WindupFrame.TYPE_PROP).dataType(String.class).cardinality(Cardinality.LIST).make();
            janusGraphManagement.buildIndex(WindupFrame.TYPE_PROP, Vertex.class).addKey(typePropPropertyKey).buildCompositeIndex();
            janusGraphManagement.buildIndex("edge-typevalue", Edge.class).addKey(typePropPropertyKey).buildCompositeIndex();
            janusGraphManagement.commit();
        }
        return janusGraph;
    }
    
    public JanusGraph getCentralJanusGraph() {
        return janusGraph;
    }
}
