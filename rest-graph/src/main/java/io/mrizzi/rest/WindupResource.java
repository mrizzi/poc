package io.mrizzi.rest;

import com.syncleus.ferma.DelegatingFramedGraph;
import com.syncleus.ferma.FramedGraph;
import com.syncleus.ferma.ReflectionCache;
import com.syncleus.ferma.framefactories.annotation.MethodHandler;
import com.syncleus.ferma.typeresolvers.PolymorphicTypeResolver;
import io.mrizzi.graph.AnnotationFrameFactory;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
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
import org.jboss.windup.reporting.category.IssueCategoryModel;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Path("/windup")
@Produces(MediaType.APPLICATION_JSON)
public class WindupResource {
    private static final Logger LOG = Logger.getLogger(WindupResource.class);

    @GET
    public Object issues() throws URISyntaxException {
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
            issueCategories.forEach(System.out::println);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }
}
