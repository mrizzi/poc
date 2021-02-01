package io.tackle.controls.toberemoved;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.quarkus.rest.data.panache.deployment.utils.ResourceName;
import io.tackle.controls.annotations.Filterable;
import io.tackle.controls.resources.TypedWebMethod;
import io.tackle.controls.resources.hal.HalCollectionEnrichedWrapper;
import io.tackle.controls.resources.filter.Filter;
import io.tackle.controls.resources.filter.FilterBuilder;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface ListFilteredResource<Entity extends PanacheEntity> extends TypedWebMethod<Entity> {

    /**
     * Tentative to define a "shared" method between entities to just implement once the filtering.
     */

    /**
     * workaround to have pagination and sorting as in REST Data Panache with also filtering.
     * <p>
     * The methods are copied from the `BusinessServiceResourceJaxRs_*` class created from REST Data Panache
     * at build time and "enhanced" here to manage also filtering.
     * <p>
     * This must be improved, refactored and even rewritten if needed but it's meant to work within
     * the Controls application proof of concept.
     */

    String QUERY_PARAM_SIZE = "size";
    String DEFAULT_VALUE_SIZE = "20";
    String QUERY_PARAM_PAGE = "page";
    String DEFAULT_VALUE_PAGE = "0";
    String QUERY_PARAM_SORT = "sort";
    String DEFAULT_VALUE_SORT = "id";
    String QUERY_PARAM_FILTER = "where";
    String DEFAULT_VALUE_FILTER = "";

    default Response list(@QueryParam(QUERY_PARAM_SORT) @DefaultValue(DEFAULT_VALUE_SORT) List var1,
                         @QueryParam(QUERY_PARAM_PAGE) @DefaultValue(DEFAULT_VALUE_PAGE) int var2,
                         @QueryParam(QUERY_PARAM_SIZE) @DefaultValue(DEFAULT_VALUE_SIZE) int var3,
                         @QueryParam(QUERY_PARAM_FILTER) @DefaultValue(DEFAULT_VALUE_FILTER) String filter,
                         @Context UriInfo var4) throws Exception {
        Sort var10 = Sort.by(new String[0]);
        LinkedList var6 = new LinkedList();
        // @DefaultValue doesn't work (need investigation) with List so forcing the DEFAULT_VALUE_SORT
        // https://docs.oracle.com/javaee/7/api/javax/ws/rs/DefaultValue.html
        if (var1.isEmpty()) var1 = Arrays.asList(DEFAULT_VALUE_SORT);
        Iterator var5 = var1.iterator();

        while (var5.hasNext()) {
            List var7 = Arrays.asList((Object[]) ((String) var5.next()).split(","));
            ((List) var6).addAll((Collection) var7);
        }

        Iterator var8 = ((List) var6).iterator();

        while (var8.hasNext()) {
            Object var9 = var8.next();
            if (!((String) var9).startsWith("-")) {
                var10.and((String) var9);
            } else {
                String var11 = ((String) var9).substring(1);
                Sort.Direction var12 = Sort.Direction.Descending;
                var10.and(var11, var12);
            }
        }

        int var13;
        if (var2 < 0) {
            var13 = 0;
        } else {
            var13 = var2;
        }

        int var14;
        if (var3 < 1) {
            var14 = 20;
        } else {
            var14 = var3;
        }

        // Solution based on using the different query parameters (continues below with same comment)
        final Filter queryFilter = FilterBuilder.withUriInfo(var4).andAcceptedFilters(getFilterableFields()).build();

        Page var16 = Page.of(var13, var14);
        int var28 = $$_page_count_list(var16);
        ArrayList var26 = new ArrayList(4);
        Page var17 = var16.first();
        UriBuilder var19 = var4.getAbsolutePathBuilder();
        int var18 = var17.index;
        Object[] var20 = new Object[]{var18};
        var19.queryParam("page", var20);
        int var21 = var17.size;
        Object[] var22 = new Object[]{var21};
        var19.queryParam("size", var22);
        Object[] var23 = new Object[0];
        // start - add missing info
        var19.queryParam("sort", var1.toArray());
        addQueryParams(var19, queryFilter);
        // end - add missing info
        Link.Builder var24 = Link.fromUri(var19.build(var23));
        var24.rel("first");
        Object[] var25 = new Object[0];
        Link var27 = var24.build(var25);
        ((List) var26).add(var27);
        int var29 = Integer.sum(var28, -1);
        Page var30 = var16.index(var29);
        UriBuilder var32 = var4.getAbsolutePathBuilder();
        int var31 = var30.index;
        Object[] var33 = new Object[]{var31};
        var32.queryParam("page", var33);
        int var34 = var30.size;
        Object[] var35 = new Object[]{var34};
        var32.queryParam("size", var35);
        Object[] var36 = new Object[0];
        // start - add missing info
        var32.queryParam("sort", var1.toArray());
        addQueryParams(var32, queryFilter);
        // end - add missing info
        Link.Builder var37 = Link.fromUri(var32.build(var36));
        var37.rel("last");
        Object[] var38 = new Object[0];
        Link var39 = var37.build(var38);
        ((List) var26).add(var39);
        int var40 = var16.index;
        int var41 = var17.index;
        if (var40 != var41) {
            Page var42 = var16.previous();
            UriBuilder var44 = var4.getAbsolutePathBuilder();
            int var43 = var42.index;
            Object[] var45 = new Object[]{var43};
            var44.queryParam("page", var45);
            int var46 = var42.size;
            Object[] var47 = new Object[]{var46};
            var44.queryParam("size", var47);
            Object[] var48 = new Object[0];
            // start - add missing info
            var44.queryParam("sort", var1.toArray());
            addQueryParams(var44, queryFilter);
            // end - add missing info
            Link.Builder var49 = Link.fromUri(var44.build(var48));
            var49.rel("previous");
            Object[] var50 = new Object[0];
            Link var51 = var49.build(var50);
            ((List) var26).add(var51);
        }

        int var52 = var16.index;
        int var53 = var30.index;
        if (var52 != var53) {
            Page var54 = var16.next();
            UriBuilder var56 = var4.getAbsolutePathBuilder();
            int var55 = var54.index;
            Object[] var57 = new Object[]{var55};
            var56.queryParam("page", var57);
            int var58 = var54.size;
            Object[] var59 = new Object[]{var58};
            var56.queryParam("size", var59);
            Object[] var60 = new Object[0];
            // start - add missing info
            var56.queryParam("sort", var1.toArray());
            addQueryParams(var56, queryFilter);
            // end - add missing info
            Link.Builder var61 = Link.fromUri(var56.build(var60));
            var61.rel("next");
            Object[] var62 = new Object[0];
            Link var63 = var61.build(var62);
            ((List) var26).add(var63);
        }

        Link[] var64 = new Link[((List) var26).size()];
        Object[] var65 = ((List) var26).toArray((Object[]) var64);
        // change to manage filtering
/*
        // Solution based on using the 'filter' query parameter
        final Map<String, Object> queryParams = getQueryParams(filter);
        Response.ResponseBuilder var66 = Response.ok(list(var16, var10, getWhereFromParams(queryParams), queryParams));
*/
        // Solution based on using the different query parameters
//        Response.ResponseBuilder var66 = Response.ok(list(var16, var10, queryFilter.getQuery(), queryFilter.getQueryParameters()));
        List entities = list(var16, var10, queryFilter.getQuery(), queryFilter.getQueryParameters());
        Response.ResponseBuilder var66 = Response.ok(entities);

        var66.links((Link[]) var65);
        return var66.build();
    }

    default Response listHal(@QueryParam(QUERY_PARAM_SORT) @DefaultValue(DEFAULT_VALUE_SORT) List var1,
                             @QueryParam(QUERY_PARAM_PAGE) @DefaultValue(DEFAULT_VALUE_PAGE) int var2,
                             @QueryParam(QUERY_PARAM_SIZE) @DefaultValue(DEFAULT_VALUE_SIZE) int var3,
                             @QueryParam(QUERY_PARAM_FILTER) @DefaultValue(DEFAULT_VALUE_FILTER) String filter,
                            @Context UriInfo var4) throws Exception {
        Sort var10 = Sort.by(new String[0]);
        LinkedList var6 = new LinkedList();
        // @DefaultValue doesn't work (need investigation) with List so forcing the DEFAULT_VALUE_SORT
        // https://docs.oracle.com/javaee/7/api/javax/ws/rs/DefaultValue.html
        if (var1.isEmpty()) var1 = Arrays.asList(DEFAULT_VALUE_SORT);
        Iterator var5 = var1.iterator();

        while (var5.hasNext()) {
            List var7 = Arrays.asList((Object[]) ((String) var5.next()).split(","));
            ((List) var6).addAll((Collection) var7);
        }

        Iterator var8 = ((List) var6).iterator();

        while (var8.hasNext()) {
            Object var9 = var8.next();
            if (!((String) var9).startsWith("-")) {
                var10.and((String) var9);
            } else {
                String var11 = ((String) var9).substring(1);
                Sort.Direction var12 = Sort.Direction.Descending;
                var10.and(var11, var12);
            }
        }

        int var13;
        if (var2 < 0) {
            var13 = 0;
        } else {
            var13 = var2;
        }

        int var14;
        if (var3 < 1) {
            var14 = 20;
        } else {
            var14 = var3;
        }

        // Solution based on using the different query parameters (continues below with same comment)
        final Filter queryFilter = FilterBuilder.withUriInfo(var4).andAcceptedFilters(getFilterableFields()).build();

        Page var16 = Page.of(var13, var14);
        int var28 =  $$_page_count_list(var16);
        ArrayList var26 = new ArrayList(4);
        Page var17 = var16.first();
        UriBuilder var19 = var4.getAbsolutePathBuilder();
        int var18 = var17.index;
        Object[] var20 = new Object[]{var18};
        var19.queryParam("page", var20);
        int var21 = var17.size;
        Object[] var22 = new Object[]{var21};
        var19.queryParam("size", var22);
        Object[] var23 = new Object[0];
        // start - add missing info
        var19.queryParam("sort", var1.toArray());
        addQueryParams(var19, queryFilter);
        // end - add missing info
        Link.Builder var24 = Link.fromUri(var19.build(var23));
        var24.rel("first");
        Object[] var25 = new Object[0];
        Link var27 = var24.build(var25);
        ((List) var26).add(var27);
        int var29 = Integer.sum(var28, -1);
        Page var30 = var16.index(var29);
        UriBuilder var32 = var4.getAbsolutePathBuilder();
        int var31 = var30.index;
        Object[] var33 = new Object[]{var31};
        var32.queryParam("page", var33);
        int var34 = var30.size;
        Object[] var35 = new Object[]{var34};
        var32.queryParam("size", var35);
        Object[] var36 = new Object[0];
        // start - add missing info
        var32.queryParam("sort", var1.toArray());
        addQueryParams(var32, queryFilter);
        // end - add missing info
        Link.Builder var37 = Link.fromUri(var32.build(var36));
        var37.rel("last");
        Object[] var38 = new Object[0];
        Link var39 = var37.build(var38);
        ((List) var26).add(var39);
        int var41 = var16.index;
        int var40 = var17.index;
        if (var41 != var40) {
            Page var42 = var16.previous();
            UriBuilder var44 = var4.getAbsolutePathBuilder();
            int var43 = var42.index;
            Object[] var45 = new Object[]{var43};
            var44.queryParam("page", var45);
            int var46 = var42.size;
            Object[] var47 = new Object[]{var46};
            var44.queryParam("size", var47);
            Object[] var48 = new Object[0];
            // start - add missing info
            var44.queryParam("sort", var1.toArray());
            addQueryParams(var44, queryFilter);
            // end - add missing info
            Link.Builder var49 = Link.fromUri(var44.build(var48));
            var49.rel("previous");
            Object[] var50 = new Object[0];
            Link var51 = var49.build(var50);
            ((List) var26).add(var51);
        }

        int var53 = var16.index;
        int var52 = var30.index;
        if (var53 != var52) {
            Page var54 = var16.next();
            UriBuilder var56 = var4.getAbsolutePathBuilder();
            int var55 = var54.index;
            Object[] var57 = new Object[]{var55};
            var56.queryParam("page", var57);
            int var58 = var54.size;
            Object[] var59 = new Object[]{var58};
            var56.queryParam("size", var59);
            Object[] var60 = new Object[0];
            // start - add missing info
            var56.queryParam("sort", var1.toArray());
            addQueryParams(var56, queryFilter);
            // end - add missing info
            Link.Builder var61 = Link.fromUri(var56.build(var60));
            var61.rel("next");
            Object[] var62 = new Object[0];
            Link var63 = var61.build(var62);
            ((List) var26).add(var63);
        }

        Link[] var64 = new Link[((List) var26).size()];
        Object[] var66 = ((List) var26).toArray((Object[]) var64);
        // change to manage filtering
/*
        // Solution based on using the 'filter' query parameter
        final Map<String, Object> queryParams = getQueryParams(filter);
        List var65 = list(var16, var10, getWhereFromParams(queryParams), queryParams);
        HalCollectionEnrichedWrapper var67 = new HalCollectionEnrichedWrapper((Collection) var65, getPanacheEntityType(),
                ResourceName.fromClass(getPanacheEntityType().getName()),
                    (long) getPanacheEntityType().getMethod("count", String.class, Map.class).invoke(null, getWhereFromParams(queryParams), queryParams));
*/
        // Solution based on using the different query parameters
        List var65 = list(var16, var10, queryFilter.getQuery(), queryFilter.getQueryParameters());
        HalCollectionEnrichedWrapper var67 = new HalCollectionEnrichedWrapper((Collection) var65, getPanacheEntityType(),
                ResourceName.fromClass(getPanacheEntityType().getName()),
                    (long) getPanacheEntityType().getMethod("count", String.class, Map.class).invoke(null, queryFilter.getQuery(), queryFilter.getQueryParameters()));
        var67.addLinks((Link[]) var66);
        Response.ResponseBuilder var68 = Response.ok(var67);
        var68.links((Link[]) var66);
        return var68.build();
    }

/*
    default String getWhereFromParams(Map<String, Object> params) {
        return params.entrySet().stream()
                .map(entry -> String.format("lower(%s) %s %s", entry.getKey(), "LIKE", "lower(:" + entry.getKey() + ")"))
                .collect(Collectors.joining(" OR "));
    }

    default Map<String, Object> getQueryParams(String filter) {
        final List<String> filterableFields = getFilterableFields(getPanacheEntityType());
        final Map<String, Object> queryParams = new HashMap<>(filterableFields.size());
        filterableFields.forEach(field -> queryParams.put(field, "%" + filter + "%"));
        return queryParams;
    }
*/

    default List<String> getFilterableFields(Class<Entity> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Filterable.class))
                .map(field -> {
                    final String value = field.getAnnotation(Filterable.class).filterName();
                    return value.isEmpty() ? field.getName() : value;
                })
                .collect(Collectors.toList());
    }

    default List<String> getFilterableFields() {
        return getFilterableFields(getPanacheEntityType());
    }

    default void addQueryParams(UriBuilder uriBuilder, Filter filter) {
        filter.getRawQueryParams().forEach((key, params) -> params.forEach(param -> uriBuilder.queryParam(key, param)));
    }

    /**
     * Methods added to be able to have the copied "list" methods in BusinessServiceListFilteredResource to work.
     */
    default int $$_page_count_list(Page var1) throws Exception {
        PanacheQuery var2 = (PanacheQuery) getPanacheEntityType().getMethod("findAll").invoke(null);
        var2.page(var1);
        return var2.pageCount();
    }

/*
    default List list(Page var1, Sort var2) throws Exception {
        PanacheQuery var3 = (PanacheQuery) getPanacheEntityType().getMethod("findAll", Sort.class).invoke(null, var2);
        var3.page(var1);
        return var3.list();
    }
*/

    default List list(Page var1, Sort var2, String query, Map<String, Object> params) throws Exception {
        PanacheQuery var3 = (PanacheQuery) getPanacheEntityType().getMethod("find", String.class, Sort.class, Map.class).invoke(null, query, var2, params);
        var3.page(var1);
        return var3.list();
    }

}