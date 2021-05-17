# kafka-and-avro-schemas project

## Dev experience

1. run the latest container image locally (ref. [Build with bundled schemas](#run-with-provided-bundled-schemas))
1. open http://localhost:8080/ui/
1. upload a schema:
   1. if the schema is a new one, use the `Upload Artifact` button
   1. if the schema is an update of an existing one, select the artifact from the artifacts' list and use the `Upload new version` button
1. overwrite the `schemas_bundle.zip` file in [src/main/schemas](src/main/schemas) folder with the updated registry using the export admin endpoint http://localhost:8080/apis/registry/v2/admin/export
1. open a PR to merge the updated `schemas_bundle.zip` file describing the added/updated schema
1. once the PR is merged into `main` branch, a new version of the Apicurio registry image with a bundled schema can be created
1. every consumer can start working on the latest schemas

## Apicurio Registry with bundled schemas

### Run with provided bundled schemas
```shell
$ podman run -it --name apicurio-registry --rm -p 8080:8080 quay.io/mrizzi/apicurio-registry-mem-bundled:latest-snapshot
```

### Build with bundled schemas
```shell
$ podman build -f ./src/main/docker/Dockerfile.bundle.jvm -t quay.io/mrizzi/apicurio-registry-mem-bundled:latest-snapshot ./src/main/schemas/
```

### Run with custom bundled schemas
```shell
$ podman run -it --name apicurio-registry --rm -e REGISTRY_IMPORT=/tmp/registry/schema_bundle.zip -p 8080:8080 -v ./src/main/schemas/:/tmp/registry/:Z quay.io/mrizzi/apicurio-registry-mem:latest-snapshot
```
