# rest-graph Project

- [REST endpoints](#rest-endpoints)
- [Run on Minikube](#run-on-minikube)
- [Test with the sample page](#test-with-the-sample-page)
  * [Test with the sample configuration](#test-with-the-sample-configuration)
  * [Test with the custom configuration](#test-with-the-custom-configuration)
  * [Test with Swagger UI](#test-with-swagger-ui)
- [Test with Tackle](#test-with-tackle)
- [Customizations](#customizations)
  * [Max size for uploaded applications](#max-size-for-uploaded-applications)

<small><i><a href='http://ecotrust-canada.github.io/markdown-toc/'>Table of contents generated with markdown-toc</a></i></small>

The Windup API component is a Kubernetes application meant to provide access to information created from Windup's rules execution during an analysis.  
The project is at an early stage so keep in mind API endpoints are not stable yet but in tech preview: we would like to listen to your feedback opening a new [Issue](https://github.com/mrizzi/poc/issues) to tell us what went well and what can be improved.  
Please check frequently for updates and new features additions.  

Features available:
* trigger the analysis of a compiled application posting the application archive (jar, war, ear)
* retrieve the hints identified for the application
* retrieve the hints for all the applications analyzed
* get analysis status updates while analysis is running

Features to be added: check the [Issues](https://github.com/mrizzi/poc/issues) for enhancements already planned and feel free to add your request for new features if it's not there yet.  

## REST endpoints

All the available endpoints are described with OpenAPI specifications in the [openapi.yaml](src/main/resources/META-INF/openapi.yaml) file.  
It can be analyzed, for example, with the online [Swagger Editor](https://editor.swagger.io/?url=https://raw.githubusercontent.com/mrizzi/poc/main/rest-graph/src/main/resources/META-INF/openapi.yaml)  
A Swagger UI is also embedded in the Windup API and accessible from the provided sample page as described below in the [Test with Swagger UI](#test-with-swagger-ui) paragraph.  

## Run on Minikube
Install a Minikube instance following instructions from.  
Once Minikube is up and running, create the `prototype` namespace executing
```shell
kubectl create namespace prototype
```
and then deploy the Windup API using
```shell
kubectl apply -n prototype -f https://raw.githubusercontent.com/mrizzi/poc/main/rest-graph/minikube.yaml
```
Now all the images for running the container is going to be pulled so it might take some time.  
You can know when the Windup API is available, waiting for the `prototype` deployment to meet the `Available` condition execution:
```shell
kubectl wait -n prototype --for condition=Available deployment prototype
```
As soon as the `prototype` deployment will be available, the following message will be displayed:
```shell
deployment.apps/prototype condition met
```
Now you can start testing the Windup API leveraging the provided sample page executing
```shell
minikube service -n prototype api
```
that will open your default browser directly with the provided sample page.  

If you want to remove all the resources create, you can run:
```shell
kubectl delete -n prototype -f https://raw.githubusercontent.com/mrizzi/poc/main/rest-graph/minikube.yaml
kubectl delete namespace prototype
```

## Test with the sample page

The provided sample page (see below screenshot) is meant to ease the initial testing with the Windup API.  

![Windup Sample Page](docs/images/windup-sample-page.png?raw=true "Windup Sample Page")

### Test with the sample configuration

The sample page provides a sample analysis configuration immediately usable clicking on the button `Request Analysis with sample configuration`.  
The sample configuration will analyze the [jee-example-app-1.0.0.ear](./src/main/resources/META-INF/resources/samples/jee-example-app-1.0.0.ear) towards the Red Hat JBoss EAP 7, Quarkus, Cloud-readiness and Red Hat Runtimes targets.  

### Test with the custom configuration

The `Custom Configuration` form in the sample page let the user trigger an analysis with the desired values for the input parameters.  

### Test with Swagger UI

In the sample page, it's also available, on the right side, the link `OpenAPI with Swagger UI` that will take you to the Swagger UI with preloaded the OpenAPI file for the available endpoints.  
This is useful for quickly execute some tests of the endpoints.

## Test with Tackle

An early (i.e. proof of concept) integration between [Tackle](https://github.com/konveyor/tackle), the tools that support the modernization and migration of applications to Kubernetes from [Konveyor](https://www.konveyor.io/) community, and Windup API is available.  
A kubernetes manifest for deploying the integrated versions of Tackle and Windup API is provided.  
It can be deployed executing
```shell
kubectl apply -n prototype -f https://raw.githubusercontent.com/mrizzi/poc/main/rest-graph/windup-api-with-tackle.yaml
```
Once deployed, the Tackle UI can be opened clicking on the link available in the `Endpoints` column in the `Ingress` page (see next screenshot).  

![Tackle Minikube Ingress](docs/images/tackle-minikube-ingress.png?raw=true "Tackle Minikube Ingress")  

In the Tackle UI, once a new application has been created, the `Analyze` command -for triggering an analysis with Windup API- is available in the right menu in the application row (see next screenshot).  

![Tackle analyze application](docs/images/tackle-select-analyze.png?raw=true "Tackle analyze application")  

## Customizations

### Max size for uploaded applications

Windup API default configuration allows the upload of applications up to 100 MB.  
This size can be changed applying a different value to the `QUARKUS_HTTP_LIMITS_MAX_BODY_SIZE` environment variable executing:
```shell
kubectl set -n prototype env deployment prototype QUARKUS_HTTP_LIMITS_MAX_BODY_SIZE="<new_value>"
```
