package io.mrizzi.operator;

import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.javaoperatorsdk.operator.api.Context;
import io.javaoperatorsdk.operator.api.Controller;
import io.javaoperatorsdk.operator.api.DeleteControl;
import io.javaoperatorsdk.operator.api.ResourceController;
import io.javaoperatorsdk.operator.api.UpdateControl;
import org.jboss.logging.Logger;

import javax.inject.Inject;

@Controller
public class TackleController implements ResourceController<Tackle> {

    private final Logger log = Logger.getLogger(getClass());
    @Inject
    KubernetesClient kubernetesClient;

    @Override
    public DeleteControl deleteResource(Tackle tackle, Context<Tackle> context) {
        String namespace = tackle.getMetadata().getNamespace();
        kubernetesClient.customResources(PostgreSQL.class).inNamespace(namespace).delete(kubernetesClient.customResources(PostgreSQL.class).inNamespace(namespace).list().getItems());
        kubernetesClient.customResources(Keycloak.class).inNamespace(namespace).delete(kubernetesClient.customResources(Keycloak.class).inNamespace(namespace).list().getItems());
        kubernetesClient.customResources(Rest.class).inNamespace(namespace).delete(kubernetesClient.customResources(Rest.class).inNamespace(namespace).list().getItems());
        return DeleteControl.DEFAULT_DELETE;
    }

    @Override
    public UpdateControl<Tackle> createOrUpdateResource(Tackle tackle, Context<Tackle> context) {
        String namespace = tackle.getMetadata().getNamespace();

        // deploy all DB instances
        MixedOperation<PostgreSQL, KubernetesResourceList<PostgreSQL>, Resource<PostgreSQL>> postgreSQLClient = kubernetesClient.customResources(PostgreSQL.class);

        PostgreSQL postgreSQLKeycloak = postgreSQLClient.load(TackleController.class.getResourceAsStream("postgresql/keycloak-postgresql.yaml")).get();
        postgreSQLKeycloak.getMetadata().setNamespace(namespace);
        postgreSQLClient.inNamespace(namespace).createOrReplace(postgreSQLKeycloak);

        PostgreSQL postgreSQLControls = postgreSQLClient.load(TackleController.class.getResourceAsStream("postgresql/controls-postgresql.yaml")).get();
        postgreSQLControls.getMetadata().setNamespace(namespace);
        postgreSQLClient.inNamespace(namespace).createOrReplace(postgreSQLControls);

        PostgreSQL postgreSQLApplicationInventory = postgreSQLClient.load(TackleController.class.getResourceAsStream("postgresql/application-inventory-postgresql.yaml")).get();
        postgreSQLApplicationInventory.getMetadata().setNamespace(namespace);
        postgreSQLClient.inNamespace(namespace).createOrReplace(postgreSQLApplicationInventory);

        PostgreSQL postgreSQLPathfinder = postgreSQLClient.load(TackleController.class.getResourceAsStream("postgresql/pathfinder-postgresql.yaml")).get();
        postgreSQLPathfinder.getMetadata().setNamespace(namespace);
        postgreSQLClient.inNamespace(namespace).createOrReplace(postgreSQLPathfinder);

        // deploy Keycloak instance
        MixedOperation<Keycloak, KubernetesResourceList<Keycloak>, Resource<Keycloak>> keycloakClient = kubernetesClient.customResources(Keycloak.class);
        Keycloak keycloak = keycloakClient.load(TackleController.class.getResourceAsStream("keycloak/keycloak.yaml")).get();
        keycloakClient.inNamespace(namespace).createOrReplace(keycloak);

        // deploy REST services instance
        MixedOperation<Rest, KubernetesResourceList<Rest>, Resource<Rest>> restClient = kubernetesClient.customResources(Rest.class);
        Rest controls = restClient.load(TackleController.class.getResourceAsStream("rest/controls-rest.yaml")).get();
        restClient.inNamespace(namespace).createOrReplace(controls);
        Rest applicationInventory = restClient.load(TackleController.class.getResourceAsStream("rest/application-inventory-rest.yaml")).get();
        restClient.inNamespace(namespace).createOrReplace(applicationInventory);
        Rest pathfinder = restClient.load(TackleController.class.getResourceAsStream("rest/pathfinder-rest.yaml")).get();
        restClient.inNamespace(namespace).createOrReplace(pathfinder);

        BasicStatus status = new BasicStatus();
        tackle.setStatus(status);
        return UpdateControl.updateCustomResource(tackle);
    }

}
