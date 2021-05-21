package me.mrizzi;

import io.apicurio.registry.serde.headers.DefaultHeadersHandler;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(targets= DefaultHeadersHandler.class)
public class DefaultHeadersHandlerReflectionConfiguration {
}
