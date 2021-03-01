# commons-rest project

TBD  

## Coverage

From project root execute

```bash
$  mvn clean test -Pjacoco && mvn jacoco:restore-instrumented-classes@default-restore-instrumented-classes jacoco:report-aggregate@report-aggregate -Pjacoco
```  

The report will be available at `commons-rest-sample/target/site/jacoco-aggregate/index.html` path.

## FAQ

### `RESTEASY012010: Cannot guess type for Response`

If you get the

```
HTTP Request to /foo failed, error id: b2589ba3-4a4a-41a8-9f03-6668bdb9278e-1: org.jboss.resteasy.spi.UnhandledException: com.fasterxml.jackson.databind.JsonMappingException: RESTEASY012020: Discovery failed for method io.tackle.commons.sample.resources.FooListFilteredResource.list: RESTEASY012010: Cannot guess type for Response.
```

exception, please check the `entityClassName` value for the `@LinkResource` annotation in your `FooListFilteredResource.list` method.

