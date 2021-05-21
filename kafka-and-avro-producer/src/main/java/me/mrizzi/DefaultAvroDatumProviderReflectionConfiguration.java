package me.mrizzi;

import io.apicurio.registry.serde.avro.DefaultAvroDatumProvider;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(targets= DefaultAvroDatumProvider.class)
public class DefaultAvroDatumProviderReflectionConfiguration {
}
