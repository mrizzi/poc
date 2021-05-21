package me.mrizzi;

import io.apicurio.registry.serde.DefaultIdHandler;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(targets= DefaultIdHandler.class)
public class DefaultIdHandlerReflectionConfiguration {
}
