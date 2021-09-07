package io.mrizzi.rest;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.util.system.ConfigurationUtil;
import org.jboss.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

@Path("/windup")
public class WindupResource {
    private static final Logger LOG = Logger.getLogger(WindupResource.class);

    @GET
    public Object issues() throws URISyntaxException {
        final URL url = getClass().getResource("/graph/TitanConfiguration.properties");
        final File properties = new File(url.toURI());
        try (
             JanusGraph janusGraph = JanusGraphFactory.open(ConfigurationUtil.loadPropertiesConfig(properties));
             GraphTraversalSource g = janusGraph.traversal()) {
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Hello Windup";
    }
}
