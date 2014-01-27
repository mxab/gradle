/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.tooling.internal.publication;

import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.maven.MavenResolver;
import org.gradle.api.publish.Publication;
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.ivy.internal.publication.IvyPublicationInternal
import org.gradle.api.publish.ivy.internal.publisher.IvyPublicationIdentity;
import org.gradle.api.publish.maven.internal.publication.MavenPublicationInternal;
import org.gradle.api.publish.maven.internal.publisher.MavenProjectIdentity;
import org.gradle.api.publish.plugins.PublishingPlugin
import org.gradle.api.tasks.Upload;
import org.gradle.tooling.model.GradleModuleVersion;
import org.gradle.tooling.model.GradlePublication;

/**
 * Created with IntelliJ IDEA. User: bruchmann Date: 9/11/13 Time: 7:32 PM To change this template use File | Settings | File Templates.
 */
public class GradleProjectPublicationService {


    private Project project;

    public Collection<GradlePublication> getPublications() {


        collectionPublicationsFromPublishingExtension()


       Upload upload = project.getTasksByName("uploadArchives")

        upload.repositories.each {

        }


    }




    }

    private void collectionPublicationsFromPublishingExtension() {
        PublishingExtension p = (PublishingExtension) project.getExtensions().findByName(PublishingExtension.NAME)

        List<GradlePublication> publicationsFromPublishingExtension = p.publications.collect {


            GradlePublication gradlePublication
            if (publication instanceof MavenPublicationInternal) {
                MavenPublicationInternal mavenPublicationInternal = (MavenPublicationInternal) publication

                final MavenProjectIdentity identity = mavenPublicationInternal.mavenProjectIdentity

                GradleModuleVersion gradleModuleVersion = new GradleModuleVersion() {

                    public String getGroup() {
                        return identity.getGroupId()
                    }

                    public String getName() {
                        return identity.getArtifactId()
                    }

                    public String getVersion() {
                        return identity.getVersion()
                    }
                }
                gradlePublication = new GradlePublication(project.path, gradleModuleVersion)

            } else if (publication instanceof IvyPublicationInternal) {
                IvyPublicationInternal ivyPublicationInternal = publication
                IvyPublicationIdentity identity = ivyPublicationInternal.identity
                GradleModuleVersion gradleModuleVersion = new GradleModuleVersion() {

                    public String getGroup() {
                        return identity.getGroupId()
                    }

                    public String getName() {
                        return identity.getArtifactId()
                    }

                    public String getVersion() {
                        return identity.getVersion()
                    }
                }
                gradlePublication = new GradlePublication(project.path, gradleModuleVersion)
            }


            return gradlePublication

        }
    }
