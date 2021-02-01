package io.tackle.controls.resources.hal;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.tackle.controls.resources.responses.Metadata;
import io.quarkus.rest.data.panache.runtime.hal.HalCollectionWrapper;

import java.util.Collection;

@JsonSerialize(using = HalCollectionEnrichedWrapperJacksonSerializer.class)
public class HalCollectionEnrichedWrapper extends HalCollectionWrapper {

    private final Metadata metadata;
    private final long totalCount;

    public HalCollectionEnrichedWrapper(Collection<Object> collection, Class<?> elementType, String collectionName, long totalCount) {
        super(collection, elementType, collectionName);
        metadata = Metadata.withCount(totalCount);
        this.totalCount = totalCount;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public long getTotalCount() {
        return totalCount;
    }

}
