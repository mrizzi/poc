package io.mrizzi.rest;

import com.syncleus.ferma.DelegatingFramedGraph;
import com.syncleus.ferma.FramedGraph;
import com.syncleus.ferma.ReflectionCache;
import com.syncleus.ferma.framefactories.annotation.MethodHandler;
import com.syncleus.ferma.typeresolvers.PolymorphicTypeResolver;
import io.mrizzi.graph.AnnotationFrameFactory;
import io.mrizzi.graph.GraphService;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Direction;
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
import org.jboss.windup.graph.GraphTypeManager;
import org.jboss.windup.graph.MapInAdjacentPropertiesHandler;
import org.jboss.windup.graph.MapInAdjacentVerticesHandler;
import org.jboss.windup.graph.MapInPropertiesHandler;
import org.jboss.windup.graph.SetInPropertiesHandler;
import org.jboss.windup.graph.WindupAdjacencyMethodHandler;
import org.jboss.windup.graph.WindupPropertyMethodHandler;
import org.jboss.windup.graph.frames.FramedVertexIterable;
import org.jboss.windup.graph.javahandler.JavaHandlerHandler;
import org.jboss.windup.graph.model.WindupEdgeFrame;
import org.jboss.windup.graph.model.WindupFrame;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.reporting.category.IssueCategoryModel;
import org.jboss.windup.reporting.model.InlineHintModel;
import org.jboss.windup.web.addons.websupport.rest.graph.GraphResource;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Path("/windup")
@Produces(MediaType.APPLICATION_JSON)
public class WindupResource {
    private static final Logger LOG = Logger.getLogger(WindupResource.class);
    private static final String DEFAULT_GRAPH_CONFIGURATION_FILE_NAME = "graphConfiguration.properties";
    private static final String DEFAULT_CENTRAL_GRAPH_CONFIGURATION_FILE_NAME = "centralGraphConfiguration.properties";
    private static final String PATH_PARAM_APPLICATION_ID = "applicationId";

    @ConfigProperty(defaultValue = DEFAULT_GRAPH_CONFIGURATION_FILE_NAME, name = "io.mrizzi.graph.properties.file.path")
    File graphProperties;

    @ConfigProperty(defaultValue = DEFAULT_CENTRAL_GRAPH_CONFIGURATION_FILE_NAME, name = "io.mrizzi.graph.central.properties.file.path")
    File centralGraphProperties;

    @Inject
    GraphService graphService;

    @GET
    @Path("/issueCategory")
    public Response issuesCategories() {
        final ReflectionCache reflections = new ReflectionCache();
        final AnnotationFrameFactory frameFactory = new AnnotationFrameFactory(reflections, getMethodHandlers());
        final Map<String, Object> results = new HashMap<>();
        try (
             JanusGraph janusGraph = openJanusGraph();
             GraphTraversalSource g = janusGraph.traversal();
             FramedGraph framedGraph = new DelegatingFramedGraph<>(janusGraph, frameFactory, new PolymorphicTypeResolver(reflections))) {
            results.put("total_vertex_count", g.V().count().next());
//            List<Vertex> issueCategoriesVertex = g.V().has(WindupFrame.TYPE_PROP, GraphTypeManager.getTypeValue(IssueCategoryModel.class)).toList();
            final GraphTypeManager graphTypeManager = new GraphTypeManager();
            List<Vertex> issueCategoriesVertex = graphTypeManager.hasType(g.V(), IssueCategoryModel.class).toList();
            results.put("issue_categories_size", issueCategoriesVertex.size());
            Iterable<Vertex> vertices = (Iterable<Vertex>) framedGraph.traverse((graphTraversalSource) -> graphTraversalSource.V().has(WindupFrame.TYPE_PROP, GraphTypeManager.getTypeValue(IssueCategoryModel.class))).getRawTraversal().toList();
            results.put("issues_category_description", framedGraph.frameElement(vertices.iterator().next(), IssueCategoryModel.class).getName());
            List<? extends IssueCategoryModel> issueCategories = framedGraph.traverse((graphTraversalSource) -> graphTraversalSource.V().has(WindupFrame.TYPE_PROP, GraphTypeManager.getTypeValue(IssueCategoryModel.class))).toList(IssueCategoryModel.class);
            issueCategories.forEach(LOG::info);
            return Response.ok(frameIterableToResult(1L, issueCategories, 1)).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.serverError().build();
    }

    @GET
    @Path("/issue")
    public Response issues(@QueryParam(PATH_PARAM_APPLICATION_ID) String applicationId) {
        final ReflectionCache reflections = new ReflectionCache();
        final AnnotationFrameFactory frameFactory = new AnnotationFrameFactory(reflections, getMethodHandlers());
/*        try (JanusGraph janusGraph = openJanusGraph();
            FramedGraph framedGraph = new DelegatingFramedGraph<>(janusGraph, frameFactory, new PolymorphicTypeResolver(reflections))) {
*/
        try {
            JanusGraph janusGraph = graphService.getCentralJanusGraph();
            FramedGraph framedGraph = new DelegatingFramedGraph<>(janusGraph, frameFactory, new PolymorphicTypeResolver(reflections));
            LOG.infof("Central Graph vertex count = %d", janusGraph.traversal().V().count().next());
//            janusGraph.vertices().forEachRemaining(LOG::info);
            LOG.warnf("...running the query...");
/*
            List<? extends InlineHintModel> issues = framedGraph.traverse(g -> g.V().has(WindupFrame.TYPE_PROP, GraphTypeManager.getTypeValue(InlineHintModel.class))).toList(InlineHintModel.class);
            LOG.infof("Found %d issues", issues.size());
            // TODO debug log loop to be removed
            issues.forEach(windupVertexFrame -> {
                List<Object> actualList = new ArrayList<>();
                windupVertexFrame.getElement().properties(WindupFrame.TYPE_PROP).forEachRemaining(type -> type.ifPresent(actualList::add));
                LOG.debugf("Vertex winduptype properties %s", actualList);
            });
            return Response.ok(frameIterableToResult(1L, issues, 1)).build();
*/
            final GraphTraversal<Vertex, Vertex> hints = new GraphTraversalSource(janusGraph).V();
            hints.has(WindupFrame.TYPE_PROP, GraphTypeManager.getTypeValue(InlineHintModel.class));
            if (StringUtils.isNotBlank(applicationId)) hints.has(PATH_PARAM_APPLICATION_ID, applicationId);
            final List<Vertex> issues = hints.toList();
            LOG.infof("Found %d issues", issues.size());
            return Response.ok(frameIterableToResult(1L, new FramedVertexIterable<>(framedGraph, issues, InlineHintModel.class), 1)).build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Response.serverError().build();
    }

    @PUT
    @Path("/application/{"+ PATH_PARAM_APPLICATION_ID + "}/analysis/")
    public Response updateGraph(@PathParam(PATH_PARAM_APPLICATION_ID) String applicationId) {
        final ReflectionCache reflections = new ReflectionCache();
        final AnnotationFrameFactory frameFactory = new AnnotationFrameFactory(reflections, getMethodHandlers());
        final Map<Object, Object> verticesBeforeAndAfter = new HashMap<>();
        try (JanusGraph janusGraph = openJanusGraph();
             FramedGraph framedGraph = new DelegatingFramedGraph<>(janusGraph, frameFactory, new PolymorphicTypeResolver(reflections));
             /*JanusGraph centralJanusGraph = openCentralJanusGraph();
             FramedGraph framedCentralJanusGraph = new DelegatingFramedGraph<>(centralJanusGraph, frameFactory, new PolymorphicTypeResolver(reflections));
             Transaction transaction = centralJanusGraph.tx();
             Graph graph = transaction.createThreadedTx()*/) {
            JanusGraph centralJanusGraph = graphService.getCentralJanusGraph();
            FramedGraph framedCentralJanusGraph = new DelegatingFramedGraph<>(centralJanusGraph, frameFactory, new PolymorphicTypeResolver(reflections));
            final Graph.Features features = centralJanusGraph.features();
            final Graph.Features.GraphFeatures graphFeatures = features.graph();
            Graph.Features.VertexPropertyFeatures vertexPropertyFeatures = features.vertex().properties();
            LOG.infof("supportsThreadedTransactions : %b", graphFeatures.supportsThreadedTransactions());
            LOG.infof("supportsTransactions : %b", graphFeatures.supportsTransactions());
            LOG.infof("supportsMixedListValues : %b", vertexPropertyFeatures.supportsMixedListValues());
            GraphTraversal<Vertex, Vertex> traversal = centralJanusGraph.traversal().V();
            LOG.infof("Central Graph count before %d", traversal.count().next());
/*
            GraphTraversal<Vertex, Vertex> traversal = janusGraph.traversal().V();
            while (traversal.hasNext()) {
                Object vertex = traversal.next();
                LOG.warnf("Adding Vertex %d with winduptype '%s'", vertex.id(), vertex.properties(WindupFrame.TYPE_PROP));
                centralJanusGraph.addVertex(vertex);
            }
*/
            final Iterator<WindupVertexFrame> vertexIterator = framedGraph.traverse(g -> g.V().has(WindupFrame.TYPE_PROP)).frame(WindupVertexFrame.class);
            while (vertexIterator.hasNext()) {
                WindupVertexFrame vertex = vertexIterator.next();
                LOG.debugf("Adding Vertex %s", vertex);
                Vertex importedVertex = centralJanusGraph.addVertex();
                verticesBeforeAndAfter.put(vertex.getElement().id(), importedVertex.id());
//                WindupVertexFrame importedVertex = framedCentralJanusGraph.addFramedVertex(WindupVertexFrame.class);
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
            }
            LOG.infof("Central Graph count after %d", centralJanusGraph.traversal().V().count().next());
//            centralJanusGraph.traversal().V().toList().forEach(v -> LOG.infof("%s with property %s", v, v.property(PATH_PARAM_APPLICATION_ID)));
            Iterator<WindupEdgeFrame> edgeIterator = framedGraph.traverse(GraphTraversalSource::E).frame(WindupEdgeFrame.class);
            while (edgeIterator.hasNext()) {
                WindupEdgeFrame edgeFrame = edgeIterator.next();
                LOG.debugf("Adding Edge %s", edgeFrame.toPrettyString());
                Edge edge = edgeFrame.getElement();

                GraphTraversalSource graphTraversalSource = centralJanusGraph.traversal();

                Object outVertexId = edge.outVertex().id();
                Object importedOutVertexId = verticesBeforeAndAfter.get(outVertexId);
                if (outVertexId == null || importedOutVertexId == null) LOG.warnf("outVertexId %s -> importedOutVertexId %s", outVertexId, importedOutVertexId);
                Vertex outVertex = graphTraversalSource.V(importedOutVertexId).next();

                Object inVertexId = edge.inVertex().id();
                Object importedInVertexId = verticesBeforeAndAfter.get(inVertexId);
                if (inVertexId == null || importedInVertexId == null) LOG.warnf("inVertexId %s -> importedInVertexId %s", inVertexId, importedInVertexId);
                GraphTraversal<Vertex, Vertex> edgeGraphTraversal = graphTraversalSource.V(importedInVertexId);
                Vertex inVertex = null;
                if (edgeGraphTraversal.hasNext()) {
                    inVertex = edgeGraphTraversal.next();
                } else {
                    LOG.warnf("Missing IN vertex. It seems like the %s vertex has not been imported", inVertexId);
                    continue;
                }
                Edge importedEdge = outVertex.addEdge(edge.label(), inVertex/*, edge.properties()*/);
//                framedCentralJanusGraph.addFramedEdge()
                LOG.debugf("Added Edge %s", importedEdge);

                Iterator<Property<String>> types = edge.properties(WindupEdgeFrame.TYPE_PROP);
                types.forEachRemaining(type ->  type.ifPresent(value -> importedEdge.property(WindupFrame.TYPE_PROP, value)));
                edge.keys()
                        .stream()
                        .filter(s -> !WindupEdgeFrame.TYPE_PROP.equals(s))
                        .forEach(property -> {
                            LOG.debugf("Edge %d has property %s with values %s", edge.id(), property, edgeFrame.getProperty(property));
                            importedEdge.property(property, edgeFrame.getProperty(property));
                        });
            }
            return Response.accepted().build();
        } catch (Exception e) {
            LOG.errorf("Exception occurred: %s", e.getMessage());
            e.printStackTrace();
        }
        return Response.serverError().build();
    }

    @GET
    @Path("/tmp")
    public Response tmp() {
        return updateGraph(Long.toString(System.currentTimeMillis()));
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

    private JanusGraph openJanusGraph() throws ConfigurationException {
        LOG.debugf("Opening Janus Graph properties file %s", graphProperties);
        return JanusGraphFactory.open(ConfigurationUtil.loadPropertiesConfig(graphProperties));
    }

    private JanusGraph openCentralJanusGraph() throws ConfigurationException {
        LOG.debugf("Opening Central Janus Graph properties file %s", centralGraphProperties);
        return JanusGraphFactory.open(ConfigurationUtil.loadPropertiesConfig(centralGraphProperties));
    }

        /**
         * Heavily inspired from https://github.com/windup/windup-web/blob/8f81bc56d34756ff3a9261edfccbe9b44af40fc2/addons/web-support/impl/src/main/java/org/jboss/windup/web/addons/websupport/rest/graph/AbstractGraphResource.java#L203
         * @param executionID
         * @param frames
         * @param depth
         * @return
         */
    protected List<Map<String, Object>> frameIterableToResult(long executionID, Iterable<? extends WindupVertexFrame> frames, int depth)
    {
//        GraphMarshallingContext ctx = new GraphMarshallingContext(executionID, null, depth, false, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true);

        List<Map<String, Object>> result = new ArrayList<>();
        for (WindupVertexFrame frame : frames)
        {
            result.add(convertToMap(/*ctx,*/ frame.getElement(), true));
        }
        return result;
    }

    protected Map<String, Object> convertToMap(/*GraphMarshallingContext ctx,*/ Vertex vertex, boolean addEdges)
    {
        Map<String, Object> result = new HashMap<>();

        result.put(GraphResource.TYPE, GraphResource.TYPE_VERTEX);
        result.put(GraphResource.KEY_ID, vertex.id());

        // Spare CPU cycles, save the planet. Visited vertices will only contain _id.
/*
        if (ctx.deduplicateVertices && !ctx.addVisited(vertex))
            return result;
*/

        for (String key : vertex.keys()) {
/*
            if (ctx.blacklistProperties.contains(key))
                continue;
*/

            if (WindupFrame.TYPE_PROP.equals(key))
            {
                List<String> types = new ArrayList<>();
                Iterator<VertexProperty<String>> typeProperties = vertex.properties(key);
                while (typeProperties.hasNext())
                {
                    types.add(typeProperties.next().value());
                }
                result.put(key, types);
            } else
            {
                result.put(key, vertex.property(key).orElse(null));
            }
        }


        if (addEdges) {
            Map<String, Object> outVertices = new HashMap<>();
            result.put(GraphResource.VERTICES_OUT, outVertices);
            addEdges(/*ctx,*/ vertex, Direction.OUT, outVertices);
        }

/*
        if (ctx.includeInVertices) {
            Map<String, Object> inVertices = new HashMap<>();
            result.put(GraphResource.VERTICES_IN, inVertices);
            addEdges(ctx, vertex, Direction.IN, inVertices);
        }
*/

        return result;
    }

    private void addEdges(/*GraphMarshallingContext ctx,*/ Vertex vertex, Direction direction, Map<String, Object> result)
    {
        final Iterator<Edge> edges = vertex.edges(direction);

        while (edges.hasNext())
        {
            Edge edge = edges.next();
            String label = edge.label();

            Map<String, Object> edgeDetails = (Map<String, Object>) result.get(label);
            // If the details are already there and we aren't recursing any further, then just skip
/*
            if (!whitelistedLabels.contains(label) && edgeDetails != null && ctx.remainingDepth <= 0)
                continue;
*/

            final List<Map<String, Object>> linkedVertices;
            if (edgeDetails == null)
            {
                edgeDetails = new HashMap<>();
                edgeDetails.put(GraphResource.DIRECTION, direction.toString());
                result.put(label, edgeDetails);

                // If we aren't serializing any further, then just provide a link
/*
                if (!whitelistedLabels.contains(label) && ctx.remainingDepth <= 0)
                {
                    edgeDetails.put(GraphResource.TYPE, GraphResource.TYPE_LINK);
                    String linkUri = getLink(ctx.executionID, vertex, direction.toString(), label);
                    edgeDetails.put(GraphResource.LINK, linkUri);
                    continue;
                }
*/

                linkedVertices = new ArrayList<>();
                edgeDetails.put(GraphResource.VERTICES, linkedVertices);
            }
            else
            {
                linkedVertices = (List<Map<String, Object>>) edgeDetails.get(GraphResource.VERTICES);
            }

            Vertex otherVertex = direction == Direction.OUT ? edge.inVertex() : edge.outVertex();

            // Recursion
//            ctx.remainingDepth--;
            Map<String, Object> otherVertexMap = convertToMap(/*ctx,*/ otherVertex, false);
//            ctx.remainingDepth++;

            // Add edge properties if any
            if (!edge.keys().isEmpty())
            {
                Map<String, Object> edgeData = new HashMap<>();
                edge.keys().forEach(key -> edgeData.put(key, edge.property(key).orElse(null)));
                otherVertexMap.put(GraphResource.EDGE_DATA, edgeData);

                /// Add the edge frame's @TypeValue.  Workaround until PR #1063.
                //edgeData.put(WindupFrame.TYPE_PROP, graphTypeManager.resolveTypes(edge, WindupEdgeFrame.class));
            }

            linkedVertices.add(otherVertexMap);
        }
    }
}
