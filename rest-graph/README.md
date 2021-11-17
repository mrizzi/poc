# rest-graph Project

The Windup API component is a Kubernetes application meant to provide access to information created from Windup's rules execution during an analysis.  
The project is at an early stage so please check for updates soon.  

Features available:
* trigger the analysis of a compiled application posting the application archive (jar, war, ear)
* retrieve the hints identified for the application
* retrieve the hints for all the applications analyzed
* get analysis status updates while analysis is running

Features to be added: check the Issues and feel free to add your request for new features.  

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

The provided sample page is meant to ease the initial testing with the Windup API.  

### Test with the sample configuration

The sample page provides a sample analysis configuration immediately usable clicking on the button `Request Analysis with sample configuration`.  
The sample configuration will analyze the [jee-example-app-1.0.0.ear](./src/main/resources/META-INF/resources/samples/jee-example-app-1.0.0.ear) towards the Red Hat JBoss EAP 7, Quarkus, Cloud-readiness and Red Hat Runtimes targets.  

### Test with the custom configuration

The `Custom Configuration` form in the sample page let the user trigger an analysis with the desired values for the input parameters.  

## Customizations

### Max size for uploaded applications

Windup API default configuration allows the upload of applications up to 100 MB.  
This size can be changed applying a different value to the `QUARKUS_HTTP_LIMITS_MAX_BODY_SIZE` environment variable executing:  
```shell
kubectl set -n prototype env deployment prototype QUARKUS_HTTP_LIMITS_MAX_BODY_SIZE="<new_value>"
```
