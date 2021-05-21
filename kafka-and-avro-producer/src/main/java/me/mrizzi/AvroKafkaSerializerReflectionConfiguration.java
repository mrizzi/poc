package me.mrizzi;

import io.apicurio.registry.serde.avro.AvroKafkaSerializer;
import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(targets= AvroKafkaSerializer.class)
public class AvroKafkaSerializerReflectionConfiguration {
}
