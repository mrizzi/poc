package io.mrizzi.rest;

import com.syncleus.ferma.DelegatingFramedGraph;
import com.syncleus.ferma.FramedGraph;
import com.syncleus.ferma.ReflectionCache;
import com.syncleus.ferma.framefactories.annotation.MethodHandler;
import com.syncleus.ferma.typeresolvers.PolymorphicTypeResolver;
import io.mrizzi.graph.AnnotationFrameFactory;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
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
import org.jboss.windup.graph.javahandler.JavaHandlerHandler;
import org.jboss.windup.graph.model.WindupFrame;
import org.jboss.windup.graph.model.WindupVertexFrame;
import org.jboss.windup.reporting.category.IssueCategoryModel;
import org.jboss.windup.web.addons.websupport.rest.graph.GraphResource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
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

    @GET
    @Path("/issueCategory")
    public Response issuesCategories() throws URISyntaxException {
        final URL url = getClass().getResource("/graph/TitanConfiguration.properties");
        final File properties = new File(url.toURI());
        final Set<MethodHandler> handlers = new HashSet<>();
        handlers.add(new MapInPropertiesHandler());
        handlers.add(new MapInAdjacentPropertiesHandler());
        handlers.add(new MapInAdjacentVerticesHandler());
        handlers.add(new SetInPropertiesHandler());
        handlers.add(new JavaHandlerHandler());
        handlers.add(new WindupPropertyMethodHandler());
        handlers.add(new WindupAdjacencyMethodHandler());
        final ReflectionCache reflections = new ReflectionCache();
        final AnnotationFrameFactory frameFactory = new AnnotationFrameFactory(reflections, handlers);
        final GraphTypeManager graphTypeManager = new GraphTypeManager();
        final Map<String, Object> results = new HashMap<>();
        try (
             JanusGraph janusGraph = JanusGraphFactory.open(ConfigurationUtil.loadPropertiesConfig(properties));
             GraphTraversalSource g = janusGraph.traversal();
             FramedGraph framedGraph = new DelegatingFramedGraph<>(janusGraph, frameFactory, new PolymorphicTypeResolver(reflections))) {
            results.put("total_vertex_count", g.V().count().next());
//            List<Vertex> issueCategoriesVertex = g.V().has(WindupFrame.TYPE_PROP, GraphTypeManager.getTypeValue(IssueCategoryModel.class)).toList();
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
            result.add(convertToMap(/*ctx,*/ frame.getElement()));
        }
        return result;
    }

    protected Map<String, Object> convertToMap(/*GraphMarshallingContext ctx,*/ Vertex vertex)
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


        Map<String, Object> outVertices = new HashMap<>();
        result.put(GraphResource.VERTICES_OUT, outVertices);
        addEdges(/*ctx,*/ vertex, Direction.OUT, outVertices);

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

/*
            Vertex otherVertex = direction == Direction.OUT ? edge.inVertex() : edge.outVertex();

            // Recursion
            ctx.remainingDepth--;
            Map<String, Object> otherVertexMap = convertToMap(ctx, otherVertex);
            ctx.remainingDepth++;

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
*/
        }
    }
}
