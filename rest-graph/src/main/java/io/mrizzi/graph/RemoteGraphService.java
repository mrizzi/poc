package io.mrizzi.graph;

import com.syncleus.ferma.DelegatingFramedGraph;
import com.syncleus.ferma.FramedGraph;
import com.syncleus.ferma.ReflectionCache;
import com.syncleus.ferma.framefactories.annotation.MethodHandler;
import com.syncleus.ferma.typeresolvers.PolymorphicTypeResolver;
import io.quarkus.runtime.Startup;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.tinkerpop.gremlin.driver.Client;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.Result;
import org.apache.tinkerpop.gremlin.driver.ResultSet;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.util.system.ConfigurationUtil;
import org.jboss.logging.Logger;
import org.jboss.windup.graph.MapInAdjacentPropertiesHandler;
import org.jboss.windup.graph.MapInAdjacentVerticesHandler;
import org.jboss.windup.graph.MapInPropertiesHandler;
import org.jboss.windup.graph.SetInPropertiesHandler;
import org.jboss.windup.graph.WindupAdjacencyMethodHandler;
import org.jboss.windup.graph.WindupPropertyMethodHandler;
import org.jboss.windup.graph.javahandler.JavaHandlerHandler;
import org.jboss.windup.graph.model.WindupEdgeFrame;
import org.jboss.windup.graph.model.WindupFrame;
import org.jboss.windup.graph.model.WindupVertexFrame;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static io.mrizzi.rest.WindupResource.PATH_PARAM_APPLICATION_ID;
import static org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource.traversal;

@Startup
@ApplicationScoped
public class RemoteGraphService {
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

    private String createManagementRequest() throws ConfigurationException, IOException {
        final StringBuilder request = new StringBuilder();
        request.append("JanusGraphManagement janusGraphManagement = graph.openManagement(); ");
        request.append("boolean created = false; ");
        request.append("if (!janusGraphManagement.containsPropertyKey(\"w:winduptype\")) { ");
            request.append("PropertyKey typePropPropertyKey = janusGraphManagement.makePropertyKey(\"w:winduptype\").dataType(String.class).cardinality(Cardinality.LIST).make(); ");
            request.append("janusGraphManagement.buildIndex(\"w:winduptype\", Vertex.class).addKey(typePropPropertyKey).buildCompositeIndex(); ");
            request.append("janusGraphManagement.buildIndex(\"edge-typevalue\", Edge.class).addKey(typePropPropertyKey).buildCompositeIndex(); ");
            request.append("PropertyKey applicationIdPropertyKey = janusGraphManagement.makePropertyKey(\"applicationId\").dataType(String.class).cardinality(Cardinality.SINGLE).make(); ");
            request.append("janusGraphManagement.buildIndex(\"applicationId\", Vertex.class).addKey(applicationIdPropertyKey, Mapping.STRING.asParameter()).buildMixedIndex(\"search\"); ");
            request.append("janusGraphManagement.commit(); created = true; ");
        request.append("} ");
        return request.toString();
    }

    public JanusGraph getCentralJanusGraph() {
        return (JanusGraph) g.getGraph();
    }

    public Graph getCentralGraph() {
        return g.getGraph();
    }

    public void updateCentralJanusGraph(String sourceGraph, String applicationId) {
        final ReflectionCache reflections = new ReflectionCache();
        final AnnotationFrameFactory frameFactory = new AnnotationFrameFactory(reflections, getMethodHandlers());
        final Map<Object, Object> verticesBeforeAndAfter = new HashMap<>();
        try (JanusGraph janusGraph = openJanusGraph(sourceGraph);
             FramedGraph framedGraph = new DelegatingFramedGraph<>(janusGraph, frameFactory, new PolymorphicTypeResolver(reflections))) {
            // Delete the previous graph for the PATH_PARAM_APPLICATION_ID provided
            LOG.infof("Delete the previous vertices with Application ID %s", applicationId);
            if (LOG.isDebugEnabled())
                LOG.debugf("Before deleting vertices with Application ID %s, central graph has %d vertices and %d edges", applicationId, g.V().count().next(), g.E().count().next());
            final GraphTraversal<Vertex, Vertex> previousVertexGraph = g.V();
            previousVertexGraph.has(PATH_PARAM_APPLICATION_ID, applicationId);
            previousVertexGraph.drop().iterate();
            if (LOG.isDebugEnabled())
                LOG.debugf("After deletion of vertices with Application ID %s, central graph has %d vertices and %d edges", applicationId, g.V().count().next(), g.E().count().next());

            final Iterator<WindupVertexFrame> vertexIterator = framedGraph.traverse(g -> g.V().has(WindupFrame.TYPE_PROP)).frame(WindupVertexFrame.class);
            while (vertexIterator.hasNext()) {
                WindupVertexFrame vertex = vertexIterator.next();
                LOG.debugf("Adding Vertex %s", vertex);
                GraphTraversal<Vertex, Vertex> importedVertex = g.addV();
                Iterator<VertexProperty<String>> types = vertex.getElement().properties(WindupFrame.TYPE_PROP);
                types.forEachRemaining(type -> type.ifPresent(value -> importedVertex.property(WindupFrame.TYPE_PROP, value)));
                vertex.getElement().keys()
                        .stream()
                        .filter(s -> !WindupFrame.TYPE_PROP.equals(s))
                        .forEach(property -> {
                            LOG.debugf("Vertex %d has property %s with values %s", vertex.getElement().id(), property, vertex.getProperty(/*).getElement().properties(*/property));
                            importedVertex.property(property, vertex.getProperty(/*).getElement().properties(*/property));
//                    importedVertex.setProperty(property, vertex.getProperty(/*).getElement().properties(*/property));
                        });
                importedVertex.property(PATH_PARAM_APPLICATION_ID, applicationId);
                verticesBeforeAndAfter.put(vertex.getElement().id(), importedVertex.next().id());
            }
            if (LOG.isDebugEnabled())
                LOG.debugf("Central Graph count after %d", g.V().count().next());
//            g.V().toList().forEach(v -> LOG.infof("%s with property %s", v, v.property(PATH_PARAM_APPLICATION_ID)));
            Iterator<WindupEdgeFrame> edgeIterator = framedGraph.traverse(GraphTraversalSource::E).frame(WindupEdgeFrame.class);
            while (edgeIterator.hasNext()) {
                WindupEdgeFrame edgeFrame = edgeIterator.next();
                LOG.debugf("Adding Edge %s", edgeFrame.toPrettyString());
                Edge edge = edgeFrame.getElement();

                Object outVertexId = edge.outVertex().id();
                Object importedOutVertexId = verticesBeforeAndAfter.get(outVertexId);
                if (outVertexId == null || importedOutVertexId == null)
                    LOG.warnf("outVertexId %s -> importedOutVertexId %s", outVertexId, importedOutVertexId);
                GraphTraversal<Vertex, Vertex> outVertexTraversal = g.V(importedOutVertexId);

                Object inVertexId = edge.inVertex().id();
                Object importedInVertexId = verticesBeforeAndAfter.get(inVertexId);
                if (inVertexId == null || importedInVertexId == null)
                    LOG.warnf("inVertexId %s -> importedInVertexId %s", inVertexId, importedInVertexId);
                GraphTraversal<Vertex, Vertex> edgeGraphTraversal = g.V(importedInVertexId);
                Vertex inVertex = null;
                if (edgeGraphTraversal.hasNext()) {
                    inVertex = edgeGraphTraversal.next();
                } else {
                    LOG.warnf("Missing IN vertex. It seems like the %s vertex has not been imported", inVertexId);
                    continue;
                }
                GraphTraversal<Vertex, Edge> importedEdgeTraversal = outVertexTraversal.addE(edge.label()).to(inVertex);

                Iterator<Property<String>> types = edge.properties(WindupEdgeFrame.TYPE_PROP);
                types.forEachRemaining(type -> type.ifPresent(value -> importedEdgeTraversal.property(WindupFrame.TYPE_PROP, value)));
                edge.keys()
                        .stream()
                        .filter(s -> !WindupEdgeFrame.TYPE_PROP.equals(s))
                        .forEach(property -> {
                            LOG.debugf("Edge %d has property %s with values %s", edge.id(), property, edgeFrame.getProperty(property));
                            importedEdgeTraversal.property(property, edgeFrame.getProperty(property));
                        });
                Edge importedEdge = importedEdgeTraversal.property(PATH_PARAM_APPLICATION_ID, applicationId).next();
                LOG.debugf("Added Edge %s", importedEdge);
            }
            g.tx().commit();
        } catch (Exception e) {
            LOG.errorf("Exception occurred: %s", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private Set<MethodHandler> getMethodHandlers() {
        final Set<MethodHandler> handlers = new HashSet<>();
        handlers.add(new MapInPropertiesHandler());
        handlers.add(new MapInAdjacentPropertiesHandler());
        handlers.add(new MapInAdjacentVerticesHandler());
        handlers.add(new SetInPropertiesHandler());
        handlers.add(new JavaHandlerHandler());
        handlers.add(new WindupPropertyMethodHandler());
        handlers.add(new WindupAdjacencyMethodHandler());
        return handlers;
    }

    private JanusGraph openJanusGraph(String sourceGraph) throws ConfigurationException {
        // temporary workaround to work locally
        sourceGraph += "/graph/TitanConfiguration.properties";
        LOG.infof("Opening Janus Graph properties file %s", sourceGraph);
        PropertiesConfiguration configuration = ConfigurationUtil.loadPropertiesConfig(sourceGraph);
        configuration.setProperty("storage.transactions", true);
        return JanusGraphFactory.open(configuration);
    }
}
