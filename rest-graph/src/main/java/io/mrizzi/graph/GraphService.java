package io.mrizzi.graph;

import io.mrizzi.rest.WindupResource;
import io.quarkus.runtime.Startup;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.janusgraph.core.Cardinality;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.PropertyKey;
import org.janusgraph.core.schema.JanusGraphManagement;
import org.janusgraph.util.system.ConfigurationUtil;
import org.jboss.logging.Logger;
import org.jboss.windup.graph.model.WindupEdgeFrame;
import org.jboss.windup.graph.model.WindupVertexFrame;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import java.io.File;

@Startup
@ApplicationScoped
public class GraphService {
    private static final Logger LOG = Logger.getLogger(WindupResource.class);
    private static final String DEFAULT_CENTRAL_GRAPH_CONFIGURATION_FILE_NAME = "centralGraphConfiguration.properties";

    @ConfigProperty(defaultValue = DEFAULT_CENTRAL_GRAPH_CONFIGURATION_FILE_NAME, name = "io.mrizzi.graph.central.properties.file.path")
    File centralGraphProperties;

    private JanusGraph janusGraph;

    @PostConstruct
    void init() throws ConfigurationException {
        janusGraph = openCentralJanusGraph();
    }

    @PreDestroy
    void destroy() {
        LOG.infof("Closing Central Janus Graph properties file %s", centralGraphProperties);
        janusGraph.close();
    }

    private JanusGraph openCentralJanusGraph() throws ConfigurationException {
        LOG.infof("Opening Central Janus Graph properties file %s", centralGraphProperties);
        final JanusGraph janusGraph = JanusGraphFactory.open(ConfigurationUtil.loadPropertiesConfig(centralGraphProperties));
        final JanusGraphManagement janusGraphManagement = janusGraph.openManagement();
        if (!janusGraphManagement.containsPropertyKey(WindupVertexFrame.TYPE_PROP)) {
            final PropertyKey vertexTypePropPropertyKey = janusGraphManagement.makePropertyKey(WindupVertexFrame.TYPE_PROP).dataType(String.class).cardinality(Cardinality.LIST).make();
            janusGraphManagement.buildIndex("", Vertex.class).addKey(vertexTypePropPropertyKey).buildCompositeIndex();
            final PropertyKey edgeTypePropPropertyKey = janusGraphManagement.makePropertyKey(WindupEdgeFrame.TYPE_PROP).dataType(String.class).cardinality(Cardinality.LIST).make();
            janusGraphManagement.buildIndex("edge-typevalue", Vertex.class).addKey(edgeTypePropPropertyKey).buildCompositeIndex();
        }
        janusGraphManagement.commit();
        return janusGraph;
    }
    
    public JanusGraph getCentralJanusGraph() {
        return janusGraph;
    }
}
