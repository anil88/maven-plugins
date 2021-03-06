package org.opennms.maven.plugins.karaf;

import org.apache.karaf.features.internal.model.Bundle;
import org.apache.karaf.features.internal.model.ConfigFile;
import org.apache.karaf.features.internal.model.Dependency;
import org.apache.karaf.features.internal.model.Feature;

public class FeatureBuilder {

    private org.opennms.maven.plugins.karaf.model.Feature m_feature;

    public FeatureBuilder(final String name) {
        m_feature = new org.opennms.maven.plugins.karaf.model.Feature(name);
    }

    public FeatureBuilder(final String name, final String version) {
        m_feature = new org.opennms.maven.plugins.karaf.model.Feature(name, version);
    }

    public Feature getFeature() {
        return m_feature;
    }

    public FeatureBuilder setVersion(final String version) {
        m_feature.setVersion(version);
        return this;
    }

    public FeatureBuilder setDescription(final String description) {
        m_feature.setDescription(description);
        return this;
    }

    public FeatureBuilder setDetails(final String details) {
        m_feature.setDetails(details);
        return this;
    }

    public FeatureBuilder addConfig(final String name, String contents) {
        final org.apache.karaf.features.internal.model.Config config = new org.apache.karaf.features.internal.model.Config();
        config.setName(name);
        config.setValue(contents);
        m_feature.addConfig(config);
        return this;
    }

    public FeatureBuilder addConfigFile(final String location, final String finalname) {
        return addConfigFile(location, finalname, null);
    }

    public FeatureBuilder addConfigFile(final String location, final String finalname, final Boolean override) {
        final ConfigFile file = new ConfigFile();
        file.setLocation(location);
        file.setFinalname(finalname);
        file.setOverride(override);
        m_feature.addConfigFile(file);
        return this;
    }

    public FeatureBuilder addFeature(final String name) {
        return addFeature(name, null);
    }

    public FeatureBuilder addFeature(final String name, final String version) {
        final Dependency dependency = new Dependency();
        dependency.setName(name);
        dependency.setVersion(version);
        m_feature.addDependency(dependency);
        return this;
    }

    public FeatureBuilder addBundle(final String location) {
        return addBundle(location, null, null, null);
    }

    public FeatureBuilder addBundle(final String location, final int startLevel) {
        return addBundle(location, startLevel, null, null);
    }

    private FeatureBuilder addBundle(final String location, final Integer startLevel, final Boolean start, final Boolean dependency) {
        final Bundle bundle = new Bundle();
        bundle.setLocation(location);
        bundle.setStart(start);
        bundle.setStartLevel(startLevel);
        bundle.setDependency(dependency);
        m_feature.addBundle(bundle);

        return this;
    }

    public boolean isEmpty() {
        return isEmpty(m_feature);
    }

    public static boolean isEmpty(final Feature feature) {
        return (
                feature.getConfigurations().isEmpty() &&
                feature.getConfigurationFiles().isEmpty() &&
                feature.getBundles().isEmpty() &&
                feature.getDependencies().isEmpty()
                );
    }
}
