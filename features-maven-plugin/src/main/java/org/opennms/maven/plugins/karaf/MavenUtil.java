package org.opennms.maven.plugins.karaf;

import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;

public class MavenUtil {

    /**
     * Convert a Maven <code>Artifact</code> into a PAX URL mvn format.
     *
     * @param artifact the Maven <code>Artifact</code>.
     * @return the corresponding PAX URL mvn format (mvn:<groupId>/<artifactId>/<version>/<type>/<classifier>)
     */
	public static String artifactToMvn(Artifact artifact) {
        return artifactToMvn(RepositoryUtils.toArtifact(artifact));
    }

    /**
     * Convert an Aether <code>org.sonatype.aether.artifact.Artifact</code> into a PAX URL mvn format.
     *
     * @param artifact the Aether <code>org.sonatype.aether.artifact.Artifact</code>.
     * @return the corresponding PAX URL mvn format (mvn:<groupId>/<artifactId>/<version>/<type>/<classifier>)
     */
	public static String artifactToMvn(org.sonatype.aether.artifact.Artifact artifact) {
        String bundleName;
        if (artifact.getExtension().equals("jar") && isEmpty(artifact.getClassifier())) {
            bundleName = String.format("mvn:%s/%s/%s", artifact.getGroupId(), artifact.getArtifactId(), artifact.getBaseVersion());
        } else {
            if (isEmpty(artifact.getClassifier())) {
                bundleName = String.format("mvn:%s/%s/%s/%s", artifact.getGroupId(), artifact.getArtifactId(), artifact.getBaseVersion(), artifact.getExtension());
            } else {
                bundleName = String.format("mvn:%s/%s/%s/%s/%s", artifact.getGroupId(), artifact.getArtifactId(), artifact.getBaseVersion(), artifact.getExtension(), artifact.getClassifier());
            }
        }
        return bundleName;
    }

	private static boolean isEmpty(final String classifier) {
        return classifier == null || classifier.length() == 0;
    }
}
