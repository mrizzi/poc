package io.mrizzi.rest;

import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.janusgraph.core.JanusGraph;
import org.janusgraph.core.JanusGraphFactory;
import org.janusgraph.core.attribute.Geoshape;
import org.jboss.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Path("/hello")
public class HelloResource {

    private static final Logger LOG = Logger.getLogger(HelloResource.class);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Object> get() {
        Configuration configuration = new BaseConfiguration();
        configuration.addProperty("gremlin.graph", "org.janusgraph.core.JanusGraphFactory");
        configuration.addProperty("storage.backend", "inmemory");
        JanusGraph janusGraph = JanusGraphFactory.open(configuration);
        createElements(janusGraph);
        List<Object> jupiterBrothers = readElements(janusGraph);
        updateElements(janusGraph);
        deleteElements(janusGraph);
        janusGraph.close();
        return jupiterBrothers;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public String post() {
        return "Hello RESTEasy";
    }

    // Methods based on https://github.com/JanusGraph/janusgraph/tree/master/janusgraph-examples
    private void createElements(JanusGraph janusGraph) {
        LOG.info("creating elements");
        GraphTraversalSource g = janusGraph.traversal();
        Vertex saturn = g.addV("titan").property("name", "saturn", new Object[0]).property("age", 10000, new Object[0]).next();
        Vertex sky = g.addV("location").property("name", "sky", new Object[0]).next();
        Vertex sea = g.addV("location").property("name", "sea", new Object[0]).next();
        Vertex jupiter = g.addV("god").property("name", "jupiter", new Object[0]).property("age", 5000, new Object[0]).next();
        Vertex neptune = g.addV("god").property("name", "neptune", new Object[0]).property("age", 4500, new Object[0]).next();
        Vertex hercules = g.addV("demigod").property("name", "hercules", new Object[0]).property("age", 30, new Object[0]).next();
        Vertex alcmene = g.addV("human").property("name", "alcmene", new Object[0]).property("age", 45, new Object[0]).next();
        Vertex pluto = g.addV("god").property("name", "pluto", new Object[0]).property("age", 4000, new Object[0]).next();
        Vertex nemean = g.addV("monster").property("name", "nemean", new Object[0]).next();
        Vertex hydra = g.addV("monster").property("name", "hydra", new Object[0]).next();
        Vertex cerberus = g.addV("monster").property("name", "cerberus", new Object[0]).next();
        Vertex tartarus = g.addV("location").property("name", "tartarus", new Object[0]).next();
        g.V(new Object[]{jupiter}).as("a", new String[0]).V(new Object[]{saturn}).addE("father").from("a").next();
        g.V(new Object[]{jupiter}).as("a", new String[0]).V(new Object[]{sky}).addE("lives").property("reason", "loves fresh breezes", new Object[0]).from("a").next();
        g.V(new Object[]{jupiter}).as("a", new String[0]).V(new Object[]{neptune}).addE("brother").from("a").next();
        g.V(new Object[]{jupiter}).as("a", new String[0]).V(new Object[]{pluto}).addE("brother").from("a").next();
        g.V(new Object[]{neptune}).as("a", new String[0]).V(new Object[]{sea}).addE("lives").property("reason", "loves waves", new Object[0]).from("a").next();
        g.V(new Object[]{neptune}).as("a", new String[0]).V(new Object[]{jupiter}).addE("brother").from("a").next();
        g.V(new Object[]{neptune}).as("a", new String[0]).V(new Object[]{pluto}).addE("brother").from("a").next();
        g.V(new Object[]{hercules}).as("a", new String[0]).V(new Object[]{jupiter}).addE("father").from("a").next();
        g.V(new Object[]{hercules}).as("a", new String[0]).V(new Object[]{alcmene}).addE("mother").from("a").next();

        g.V(new Object[]{hercules}).as("a", new String[0]).V(new Object[]{nemean}).addE("battled").property("time", 1, new Object[0]).property("place", Geoshape.point(38.099998474121094D, 23.700000762939453D), new Object[0]).from("a").next();
        g.V(new Object[]{hercules}).as("a", new String[0]).V(new Object[]{hydra}).addE("battled").property("time", 2, new Object[0]).property("place", Geoshape.point(37.70000076293945D, 23.899999618530273D), new Object[0]).from("a").next();
        g.V(new Object[]{hercules}).as("a", new String[0]).V(new Object[]{cerberus}).addE("battled").property("time", 12, new Object[0]).property("place", Geoshape.point(39.0D, 22.0D), new Object[0]).from("a").next();

        g.V(new Object[]{pluto}).as("a", new String[0]).V(new Object[]{jupiter}).addE("brother").from("a").next();
        g.V(new Object[]{pluto}).as("a", new String[0]).V(new Object[]{neptune}).addE("brother").from("a").next();
        g.V(new Object[]{pluto}).as("a", new String[0]).V(new Object[]{tartarus}).addE("lives").property("reason", "no fear of death", new Object[0]).from("a").next();
        g.V(new Object[]{pluto}).as("a", new String[0]).V(new Object[]{cerberus}).addE("pet").from("a").next();
        g.V(new Object[]{cerberus}).as("a", new String[0]).V(new Object[]{tartarus}).addE("lives").from("a").next();
        try {
            g.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public List<Object> readElements(JanusGraph janusGraph) {
        try (GraphTraversalSource g = janusGraph.traversal()) {
            LOG.info("reading elements");
            Optional<Map<Object, Object>> v = g.V(new Object[0]).has("name", "jupiter").elementMap(new String[0]).tryNext();
            if (v.isPresent()) {
                LOG.info(v.get().toString());
            } else {
                LOG.warn("jupiter not found");
            }

            Optional<Map<Object, Object>> edge = g.V(new Object[0]).has("name", "hercules").outE(new String[]{"battled"}).as("e", new String[0]).inV().has("name", "hydra").select("e").elementMap(new String[0]).tryNext();
            if (edge.isPresent()) {
                LOG.info(edge.get().toString());
            } else {
                LOG.warn("hercules battled hydra not found");
            }

            List<Object> list = g.V(new Object[0]).has("age", P.gte(5000)).values(new String[]{"age"}).toList();
            LOG.info(list.toString());
            boolean plutoExists = g.V(new Object[0]).has("name", "pluto").hasNext();
            if (plutoExists) {
                LOG.info("pluto exists");
            } else {
                LOG.warn("pluto not found");
            }

            List<Object> brothers = g.V(new Object[0]).has("name", "jupiter").both(new String[]{"brother"}).values(new String[]{"name"}).dedup(new String[0]).toList();
            LOG.info("jupiter's brothers: " + brothers.toString());
            return brothers;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    public void updateElements(JanusGraph janusGraph) {
        try (GraphTraversalSource g = janusGraph.traversal()) {
            LOG.info("updating elements");
            long ts = System.currentTimeMillis();
            g.V(new Object[0]).has("name", "jupiter").property("ts", ts, new Object[0]).iterate();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void deleteElements(JanusGraph janusGraph) {
        try (GraphTraversalSource g = janusGraph.traversal()) {
            LOG.info("deleting elements");
            g.V(new Object[0]).has("name", "pluto").drop().iterate();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
