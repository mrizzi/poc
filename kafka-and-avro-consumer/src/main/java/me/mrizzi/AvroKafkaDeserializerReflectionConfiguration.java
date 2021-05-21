package me.mrizzi;

import io.apicurio.registry.serde.avro.AvroKafkaDeserializer;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(targets= AvroKafkaDeserializer.class)
public class AvroKafkaDeserializerReflectionConfiguration {
}
