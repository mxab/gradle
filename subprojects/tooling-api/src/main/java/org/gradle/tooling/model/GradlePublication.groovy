package org.gradle.tooling.model

import org.gradle.api.Incubating

/**
 * Created with IntelliJ IDEA.
 * User: bruchmann
 * Date: 9/4/13
 * Time: 9:48 PM
 * To change this template use File | Settings | File Templates.
 * @since 1.8
 */
@Incubating
class GradlePublication {

    private GradleModuleVersion id;
    private String path

    public GradlePublication(String path, GradleModuleVersion id){

        this.path = path
        this.id = id
    }
    public GradleModuleVersion getId() {
        return id;
    }

    String getPath() {
        return path
    }
}
