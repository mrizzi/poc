package me.mrizzi;

import io.apicurio.registry.serde.avro.ReflectAvroDatumProvider;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(targets= ReflectAvroDatumProvider.class)
public class ReflectAvroDatumProviderReflectionConfiguration {
}
