# quarkus-operator project

## Create namespace and CRDs
```shell
$ kubectl create namespace tackle-operator
$ kubectl apply -f src/main/resources/k8s/crds/crds.yaml
```

## Start in dev mode
```shell
$ ./mvnw clean quarkus:dev
```

## Create CR
```shell
$ kubectl apply -f src/main/resources/k8s/tackle/tackle.yaml -n tackle-operator
```

## Delete CR
```shell
$ kubectl delete -f src/main/resources/k8s/tackle/tackle.yaml -n tackle-operator
```

## Delete CRDs
```shell
$ kubectl delete -f src/main/resources/k8s/crds/crds.yaml
```

## Operator Bundle

### Build
```shell
$ podman build --layers=false -f src/main/resources/releases/v1.0.0/Dockerfile -t quay.io/mrizzi/quarkus-operator-bundle:v1.0.0 src/main/resources/releases/v1.0.0/
$ podman push quay.io/mrizzi/quarkus-operator-bundle:v1.0.0
# install 'opm' CLI (ref. https://docs.openshift.com/container-platform/4.6/cli_reference/opm-cli.html) or clone 'operator-registry' repo (ref. https://github.com/operator-framework/operator-registry)
$ <path_to>/operator-registry/bin/opm index add --bundles quay.io/mrizzi/quarkus-operator-bundle:v1.0.0 --tag quay.io/mrizzi/quarkus-operator-test-catalog:v1.0.0 --container-tool podman --from-index quay.io/operatorhubio/catalog:latest
$ podman push quay.io/mrizzi/quarkus-operator-test-catalog:v1.0.0
```

### Install
```shell
$ minikube addons enable olm # only the first time
$ kubectl apply -f src/main/resources/releases/catalog-source.yaml
$ kubectl create -f src/main/resources/releases/quarkus-operator.yaml -n tackle-operator
$ kubectl apply -f src/main/resources/k8s/tackle/tackle.yaml -n tackle-operator
```

### Uninstall
```shell
$ kubectl delete -f src/main/resources/k8s/tackle/tackle.yaml -n tackle-operator
$ kubectl delete -f src/main/resources/releases/quarkus-operator.yaml -n tackle-operator
$ kubectl delete clusterserviceversions.operators.coreos.com quarkus-operator.v1.0.0 -n tackle-operato
$ kubectl delete -f src/main/resources/releases/catalog-source.yaml
```

## TO BE REMOVED

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.type=uber-jar
```

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/quarkus-operator-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.html.

