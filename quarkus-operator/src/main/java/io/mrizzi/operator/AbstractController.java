package io.mrizzi.operator;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.client.utils.Serialization;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractController {

    public static final String LABEL_NAME = "app.kubernetes.io/name"; 
    public static final String LABEL_INSTANCE = "app.kubernetes.io/instance"; 

    protected <R extends HasMetadata & Namespaced> void applyDefaultMetadata(R resource, String name, String namespace) {
        resource.getMetadata().setName(name);
        resource.getMetadata().setNamespace(namespace);
        resource.getMetadata().getLabels().put(LABEL_NAME, name);
        resource.getMetadata().getLabels().put(LABEL_INSTANCE, String.format("%s-%d", name, ThreadLocalRandom.current().nextInt(0, 101)));
    }

    protected <S, T> String metadataName(CustomResource<S, T> customResource) {
        return customResource.getMetadata().getName();
    }

    protected <S, T> String metadataName(CustomResource<S, T> customResource, String suffix) {
        return String.format("%s-%s", metadataName(customResource), suffix);
    }

    protected <T> T loadYaml(Class<T> clazz, String yaml) {
        try (InputStream is = getClass().getResourceAsStream(yaml)) {
            return Serialization.unmarshal(is, clazz);
        } catch (IOException ex) {
            throw new IllegalStateException("Cannot find yaml on classpath: " + yaml);
        }
    }

}
