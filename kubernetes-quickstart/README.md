# kubernetes-quickstart project

This project has been created to test the addition of existing resources into the generated `minikube.json` file to understand if Quarkus 1.10.5 was affected by an issue.  
Steps to reproduce the issue:

1. start [minikube](https://kubernetes.io/docs/tasks/tools/#minikube) locally
1. add the Quarkus Minikube extension
   ```xml
    <dependency>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-minikube</artifactId>
    </dependency>
   ```
   to the [pom.xml](./pom.xml) file
1. execute `$ ./mvnw package -Dquarkus.container-image.build=true -Dquarkus.kubernetes.deploy=true` to package the application, create the application container and deploy it to the minikube instance
1. create [`src/main/kubernetes`](./src/main/kubernetes) folder
1. add the `minikube.json` file in this folder with the following content:
    ```json
    [
      {
        "apiVersion": "v1",
        "kind": "Service",
        "metadata": {
          "labels": {
            "app.kubernetes.io/name": "postgres",
            "app.kubernetes.io/version" : "10.6"
          },
          "name": "postgres"
        },
        "spec": {
          "ports": [
            {
              "name": "tcp",
              "port": 5432,
              "protocol": "TCP",
              "targetPort": 5432
            }
          ],
          "selector": {
            "app.kubernetes.io/name": "postgres",
            "app.kubernetes.io/version" : "10.6"
          },
          "sessionAffinity": "None",
          "type": "ClusterIP"
        }
      },
      {
        "apiVersion": "apps/v1",
        "kind": "Deployment",
        "metadata": {
          "labels": {
            "app.kubernetes.io/name": "postgres",
            "app.kubernetes.io/version" : "10.6"
          },
          "name": "postgres"
        },
        "spec": {
          "progressDeadlineSeconds": 600,
          "replicas": 1,
          "selector": {
            "matchLabels": {
              "app.kubernetes.io/name": "postgres",
              "app.kubernetes.io/version" : "10.6"
            }
          },
          "strategy": {
            "rollingUpdate": {
              "maxSurge": "25%",
              "maxUnavailable": "25%"
            },
            "type": "RollingUpdate"
          },
          "template": {
            "metadata": {
              "labels": {
                "app.kubernetes.io/name": "postgres",
                "app.kubernetes.io/version" : "10.6"
              }
            },
            "spec": {
              "containers": [
                {
                  "env": [
                    {
                      "name": "POSTGRES_USER",
                      "value": "user"
                    },
                    {
                      "name": "POSTGRES_PASSWORD",
                      "value": "password"
                    },
                    {
                      "name": "POSTGRES_DB",
                      "value": "quickstart"
                    }
                  ],
                  "image": "postgres:10.6",
                  "imagePullPolicy": "IfNotPresent",
                  "name": "postgres",
                  "ports": [
                    {
                      "containerPort": 5432,
                      "protocol": "TCP"
                    }
                  ],
                  "readinessProbe": {
                    "timeoutSeconds": 1,
                    "initialDelaySeconds": 5,
                    "exec": {
                      "command": [ "psql", "-U", "$(POSTGRES_USER)", "-d", "$(POSTGRES_DB)", "-c", "SELECT 1" ]
                    }
                  },
                  "livenessProbe": {
                    "timeoutSeconds": 10,
                    "initialDelaySeconds": 120,
                    "exec": {
                      "command": [ "psql", "-U", "$(POSTGRES_USER)", "-d", "$(POSTGRES_DB)", "-c", "SELECT 1" ]
                    }
                  },
                  "resources": {},
                  "securityContext": {
                    "privileged": false
                  },
                  "terminationMessagePath": "/dev/termination-log",
                  "terminationMessagePolicy": "File"
                }
              ],
              "dnsPolicy": "ClusterFirst",
              "restartPolicy": "Always",
              "schedulerName": "default-scheduler",
              "securityContext": {},
              "terminationGracePeriodSeconds": 30
            }
          }
        }
      }
    ]
   ```
   to add a `Service` and a `Deployment` for deploying a PostgreSQL instance together with the quickstart application
1. execute again `$ ./mvnw package -Dquarkus.container-image.build=true -Dquarkus.kubernetes.deploy=true` and the deployment into minikube will fail with the exception:
   ```java
   Build step io.quarkus.kubernetes.deployment.KubernetesDeployer#deploy threw an exception: io.dekorate.deps.kubernetes.client.KubernetesClientException: Failure executing: POST at: https://192.168.49.2:8443/apis/apps/v1/namespaces/default/deployments. Message: Deployment.apps "postgres" is invalid: spec.template.metadata.labels: Invalid value: map[string]string{"app.kubernetes.io/name":"postgres", "app.kubernetes.io/version":"10.6"}: `selector` does not match template `labels`. Received status: Status(apiVersion=v1, code=422, details=StatusDetails(causes=[StatusCause(field=spec.template.metadata.labels, message=Invalid value: map[string]string{"app.kubernetes.io/name":"postgres", "app.kubernetes.io/version":"10.6"}: `selector` does not match template `labels`, reason=FieldValueInvalid, additionalProperties={})], group=apps, kind=Deployment, name=postgres, retryAfterSeconds=null, uid=null, additionalProperties={}), kind=Status, message=Deployment.apps "postgres" is invalid: spec.template.metadata.labels: Invalid value: map[string]string{"app.kubernetes.io/name":"postgres", "app.kubernetes.io/version":"10.6"}: `selector` does not match template `labels`, metadata=ListMeta(_continue=null, remainingItemCount=null, resourceVersion=null, selfLink=null, additionalProperties={}), reason=Invalid, status=Failure, additionalProperties={}).
   at io.dekorate.deps.kubernetes.client.dsl.base.OperationSupport.requestFailure(OperationSupport.java:568)
   at io.dekorate.deps.kubernetes.client.dsl.base.OperationSupport.assertResponseCode(OperationSupport.java:507)
   at io.dekorate.deps.kubernetes.client.dsl.base.OperationSupport.handleResponse(OperationSupport.java:471)
   at io.dekorate.deps.kubernetes.client.dsl.base.OperationSupport.handleResponse(OperationSupport.java:430)
   at io.dekorate.deps.kubernetes.client.dsl.base.OperationSupport.handleCreate(OperationSupport.java:251)
   at io.dekorate.deps.kubernetes.client.dsl.base.BaseOperation.handleCreate(BaseOperation.java:815)
   at io.dekorate.deps.kubernetes.client.dsl.base.BaseOperation.create(BaseOperation.java:322)
   at io.dekorate.deps.kubernetes.client.handlers.DeploymentHandler.create(DeploymentHandler.java:53)
   at io.dekorate.deps.kubernetes.client.handlers.DeploymentHandler.create(DeploymentHandler.java:39)
   at io.dekorate.deps.kubernetes.client.dsl.internal.NamespaceVisitFromServerGetWatchDeleteRecreateWaitApplicableImpl.createOrReplace(NamespaceVisitFromServerGetWatchDeleteRecreateWaitApplicableImpl.java:137)
   at io.dekorate.deps.kubernetes.client.dsl.internal.NamespaceVisitFromServerGetWatchDeleteRecreateWaitApplicableImpl.createOrReplace(NamespaceVisitFromServerGetWatchDeleteRecreateWaitApplicableImpl.java:59)
   at io.quarkus.kubernetes.deployment.KubernetesDeployer.lambda$deploy$4(KubernetesDeployer.java:181)
   at java.base/java.util.ArrayList.forEach(ArrayList.java:1541)
   at io.quarkus.kubernetes.deployment.KubernetesDeployer.deploy(KubernetesDeployer.java:177)
   at io.quarkus.kubernetes.deployment.KubernetesDeployer.deploy(KubernetesDeployer.java:102)
   at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
   at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
   at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
   at java.base/java.lang.reflect.Method.invoke(Method.java:566)
   at io.quarkus.deployment.ExtensionLoader$2.execute(ExtensionLoader.java:972)
   at io.quarkus.builder.BuildContext.run(BuildContext.java:277)
   at org.jboss.threads.ContextClassLoaderSavingRunnable.run(ContextClassLoaderSavingRunnable.java:35)
   at org.jboss.threads.EnhancedQueueExecutor.safeRun(EnhancedQueueExecutor.java:2046)
   at org.jboss.threads.EnhancedQueueExecutor$ThreadBody.doRunTask(EnhancedQueueExecutor.java:1578)
   at org.jboss.threads.EnhancedQueueExecutor$ThreadBody.run(EnhancedQueueExecutor.java:1452)
   at java.base/java.lang.Thread.run(Thread.java:834)
   at org.jboss.threads.JBossThread.run(JBossThread.java:479)
   ```

The issue here is that `'selector' does not match template 'labels'`.  
Opening the generated `minikube.json` file (available in `target/kubernetes` folder) it has the `spec.selector` like

```json
"selector" : {
  "matchLabels" : {
    "app.kubernetes.io/name" : "kubernetes-quickstart",
    "app.kubernetes.io/version" : "1.0.0-SNAPSHOT"
  }
}
```

and the `spec.template.metadata.labels` like

```json
"labels" : {
  "app.kubernetes.io/name" : "postgres",
  "app.kubernetes.io/version" : "10.6"
}
```

so they are different because the `spec.selector` provided in the [src/main/kubernetes/minikube.json](src/main/kubernetes/minikube.json) 

```json
"selector" : {
  "matchLabels": {
    "app.kubernetes.io/name": "postgres",
    "app.kubernetes.io/version" : "10.6"
  }
}
```

has been overwritten during the build process with quickstart's name and version.
