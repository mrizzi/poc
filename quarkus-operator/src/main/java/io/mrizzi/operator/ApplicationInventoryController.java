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

@Controller(namespaces = Controller.WATCH_CURRENT_NAMESPACE)
public class ApplicationInventoryController extends AbstractController implements ResourceController<ApplicationInventory> {

    private final Logger log = Logger.getLogger(getClass());
    @Inject
    KubernetesClient kubernetesClient;

    @Override
    public UpdateControl<ApplicationInventory> createOrUpdateResource(ApplicationInventory applicationInventory, Context<ApplicationInventory> context) {
        String namespace = applicationInventory.getMetadata().getNamespace();
        String name = metadataName(applicationInventory);
        log.infof("Execution createOrUpdateResource for '%s' in namespace '%s'", name, namespace);

        MixedOperation<PostgreSQL, KubernetesResourceList<PostgreSQL>, Resource<PostgreSQL>> postgreSQLClient = kubernetesClient.customResources(PostgreSQL.class);
        PostgreSQL postgreSQLApplicationInventory = postgreSQLClient.load(TackleController.class.getResourceAsStream("postgresql/application-inventory-postgresql.yaml")).get();

        MixedOperation<Rest, KubernetesResourceList<Rest>, Resource<Rest>> restClient = kubernetesClient.customResources(Rest.class);
        Rest applicationInventoryRest = restClient.load(TackleController.class.getResourceAsStream("rest/application-inventory-rest.yaml")).get();

        log.infof("Creating or updating PostgreSQL '%s' in namespace '%s'", postgreSQLApplicationInventory.getMetadata().getName(), namespace);
        postgreSQLClient.inNamespace(namespace).createOrReplace(postgreSQLApplicationInventory);

        log.infof("Creating or updating REST '%s' in namespace '%s'", applicationInventoryRest.getMetadata().getName(), namespace);
        restClient.inNamespace(namespace).createOrReplace(applicationInventoryRest);

        return UpdateControl.updateCustomResource(applicationInventory);
    }

    @Override
    public DeleteControl deleteResource(ApplicationInventory applicationInventory, Context<ApplicationInventory> context) {
        String namespace = applicationInventory.getMetadata().getNamespace();
        String name = metadataName(applicationInventory);

        return DeleteControl.DEFAULT_DELETE;
    }

}
