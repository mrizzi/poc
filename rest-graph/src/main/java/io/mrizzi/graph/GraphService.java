package io.mrizzi.graph;

import io.mrizzi.rest.WindupResource;
import io.quarkus.runtime.Startup;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.util.system.ConfigurationUtil;
import org.jboss.logging.Logger;

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
        janusGraph.close();
    }

    private JanusGraph openCentralJanusGraph() throws ConfigurationException {
        LOG.debugf("Opening Central Janus Graph properties file %s", centralGraphProperties);
        return JanusGraphFactory.open(ConfigurationUtil.loadPropertiesConfig(centralGraphProperties));
    }
    
    public JanusGraph getCentralJanusGraph() {
        return janusGraph;
    }
}
