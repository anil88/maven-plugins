/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.opennms.maven.plugins.karaf;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.apache.karaf.tooling.features.CopyFileBasedDescriptor;
import org.apache.karaf.tooling.features.model.ArtifactRef;
import org.apache.karaf.tooling.features.model.Feature;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Add features to a repository directory
 *
 * @goal generate-features-maven-repo
 * @phase compile
 * @execute phase="compile"
 * @requiresDependencyResolution runtime
 * @inheritByDefault true
 * @description Add the features to the repository
 */
@SuppressWarnings("deprecation")
public class GenerateFeaturesMavenRepoMojo extends AbstractFeatureMojo {

    /**
     * @parameter default-value="${project.build.directory}/features-repo"
     */
    protected File repository;

    /**
     * If set to true the exported bundles will be directly copied into the repository dir.
     * If set to false the default maven repository layout will be used
     * @parameter
     */
    private boolean flatRepoLayout;

    /**
     * @parameter
     */
    protected List<CopyFileBasedDescriptor> copyFileBasedDescriptors;

    /**
     * @parameter default-value="${localRepository}"
     */
    private org.apache.maven.artifact.repository.ArtifactRepository localRepository;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (localRepository != null && localRepository.getBasedir() != null) {
            System.setProperty("Dorg.ops4j.pax.url.mvn.localRepository", localRepository.getBasedir());
        }

        final Set<Feature> featuresSet = resolveFeatures();

        for (final Artifact descriptor : descriptorArtifacts) {
            copy(descriptor, repository);
        }

        for (final Feature feature : featuresSet) {
            copyArtifactsToDestRepository(feature.getBundles());
            copyArtifactsToDestRepository(feature.getConfigFiles());
        }

        copyFileBasedDescriptorsToDestRepository();

    }

    private void copyArtifactsToDestRepository(final List<? extends ArtifactRef> artifactRefs) throws MojoExecutionException {
        for (final ArtifactRef artifactRef : artifactRefs) {
            final Artifact artifact = artifactRef.getArtifact();
            if (artifact != null) {
                copy(artifact, repository);
            }
        }
    }

    protected void copy(final Artifact artifact, final File destRepository) {
        try {
            getLog().info("Copying artifact: " + artifact);
            final File destFile = new File(destRepository, getRelativePath(artifact));
            copy(artifact.getFile(), destFile);
            if (!"pom".equals(artifact.getType())) {
                final Artifact pomArtifact = getPom(artifact);
                File destPom = new File(destRepository, getRelativePath(pomArtifact));
                copy(pomArtifact.getFile(), destPom);
            }
            if (this.flatRepoLayout) {
                getLog().info("Skipping metadata generation, flatRepoLayout = " + this.flatRepoLayout);
            } else {
                final String metadataFile = getMetadataFile(artifact);
                getLog().info("Generating metadata file: " + metadataFile);
                MavenUtil.generateMavenMetadata(artifact, new File(destRepository, metadataFile));
            }
        } catch (final Exception e) {
            getLog().warn("Error copying artifact " + artifact, e);
        }
    }

    Artifact getPom(final Artifact artifact) {
        final Artifact pomArtifact = factory.createArtifactWithClassifier(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), "pom", null);
        pomArtifact.setOptional(artifact.isOptional());
        pomArtifact.setRelease(artifact.isRelease());
        final File dir = artifact.getFile().getParentFile();
        pomArtifact.setFile(new File(dir, MavenUtil.getFileName(pomArtifact)));
        getLog().info("Artifact: " + artifact + " pom = " + pomArtifact);
        return pomArtifact;
    }
    /**
     * Get relative path for artifact
     * TODO consider DefaultRepositoryLayout
     * @param artifact
     * @return relative path of the given artifact in a default repo layout
     */
    private String getRelativePath(final Artifact artifact) {
        final String dir = (this.flatRepoLayout) ? "" : MavenUtil.getDir(artifact);
        final String name = MavenUtil.getFileName(artifact);
        return dir + name;
    }

    private void copyFileBasedDescriptorsToDestRepository() {
        if (copyFileBasedDescriptors != null) {
            for (final CopyFileBasedDescriptor fileBasedDescriptor : copyFileBasedDescriptors) {
                final File destDir = new File(repository, fileBasedDescriptor.getTargetDirectory());
                final File destFile = new File(destDir, fileBasedDescriptor.getTargetFileName());
                copy(fileBasedDescriptor.getSourceFile(), destFile);
            }
        }
    }

    private String getMetadataFile(final Artifact artifact) {
        final String dir = MavenUtil.getDir(artifact);
        return dir + File.separator + "maven-metadata.xml";
    }
}
