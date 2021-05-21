package me.mrizzi;

import io.apicurio.registry.serde.strategy.TopicIdStrategy;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(targets= TopicIdStrategy.class)
public class TopicIdStrategyReflectionConfiguration {
}
