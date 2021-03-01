# Proof of Concepts

Set of projects for proof of concepts (poc) code.

##  Projects

### Quarkus [kubernetes-quickstart](./kubernetes-quickstart)

Code based on [Quarkus - Kubernetes extension](https://quarkus.io/guides/deploying-to-kubernetes) guide.  
It has been created to test the addition of existing resources into the created `minikube.json` file to understand if Quarkus 1.10.5 was affected by an issue.  
More in the project's [README](./kubernetes-quickstart/README.md).

### [Controls POC](./controls)
Code for the Controls application POC covering the Business Service entity.
More in the project's [README](./controls/README.md).

### [GitHub to Jira issues synchronization](./sync-issues)
Sample (and working) scripts triggered by the [issues.yml](.github/workflows/issues.yml) GitHub workflow when an issue is opened, closed and reopended to, respectively, create, close and reopen a subtask in Jira.  
More in the project's [README](./sync-issues/README.md).

### [commons-rest](./commons-rest-parent)
REST library to support a consistent development of microservices.  
More in the project's [README](./commons-rest-parent/README.md).  
