# rest-graph Project

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
