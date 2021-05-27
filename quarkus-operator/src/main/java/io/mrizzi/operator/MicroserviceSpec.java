package io.mrizzi.operator;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class MicroserviceSpec {

    private String restImage;
    private String postgreSQLImage;

    public String getPostgreSQLImage() {
        return postgreSQLImage;
    }

    public void setPostgreSQLImage(String postgreSQLImage) {
        this.postgreSQLImage = postgreSQLImage;
    }

    public String getRestImage() {
        return restImage;
    }

    public void setRestImage(String restImage) {
        this.restImage = restImage;
    }
}
