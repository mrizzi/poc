package io.tackle.controls.resources.responses;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class Metadata {
    public long count;

    private Metadata() {}

    public static Metadata withCount(long count) {
        Metadata metadata = new Metadata();
        metadata.count = count;
        return metadata;
    }
}
