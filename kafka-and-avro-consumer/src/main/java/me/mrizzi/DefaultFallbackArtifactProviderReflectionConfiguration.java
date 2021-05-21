package me.mrizzi;

import io.apicurio.registry.serde.fallback.DefaultFallbackArtifactProvider;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(targets= DefaultFallbackArtifactProvider.class)
public class DefaultFallbackArtifactProviderReflectionConfiguration {
}
