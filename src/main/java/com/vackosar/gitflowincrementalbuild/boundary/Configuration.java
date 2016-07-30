package com.vackosar.gitflowincrementalbuild.boundary;

import static com.vackosar.gitflowincrementalbuild.utils.PluginUtils.extractPluginConfigValue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.vackosar.gitflowincrementalbuild.control.Property;

@Singleton
public class Configuration {

    private static final String PLUGIN_KEY = "com.vackosar.gitflowincrementalbuilder:gitflow-incremental-builder";

    private static final String MAKE_UPSTREAM = "make-upstream";

    public final boolean enabled;
    public final Optional<Path> key;
    public final String referenceBranch;
    public final String baseBranch;
    public final boolean uncommited;
    public final boolean untracked;
    public final boolean makeUpstream;
    public final boolean skipTestsForNotImpactedModules;
    public final boolean buildAll;
    public final boolean compareToMergeBase;
    public final boolean fetchBaseBranch;
    public final boolean fetchReferenceBranch;
    public final Optional<Path> outputFile;
    public final boolean writeChanged;

    @Inject
    public Configuration(MavenSession session) throws IOException {

        Plugin plugin = session.getCurrentProject().getPlugin(PLUGIN_KEY);
        if (plugin != null) {
            enabled = Boolean.valueOf(getPluginConfigOrDefault(Property.enabled, plugin));
            key = parseKey(session, extractPluginConfigValue(Property.repositorySshKey.name(), plugin));
            referenceBranch = getPluginConfigOrDefault(Property.referenceBranch, plugin);
            baseBranch = getPluginConfigOrDefault(Property.baseBranch, plugin);
            uncommited = Boolean.valueOf(getPluginConfigOrDefault(Property.uncommited, plugin));
            untracked = Boolean.valueOf(getPluginConfigOrDefault(Property.untracked, plugin));
            makeUpstream = MAKE_UPSTREAM.equals(session.getRequest().getMakeBehavior());
            skipTestsForNotImpactedModules = Boolean.valueOf(getPluginConfigOrDefault(Property
                            .skipTestsForNotImpactedModules, plugin));
            buildAll = Boolean.valueOf(getPluginConfigOrDefault(Property.buildAll, plugin));
            compareToMergeBase = Boolean.valueOf(getPluginConfigOrDefault(Property.compareToMergeBase, plugin));
            fetchReferenceBranch = Boolean.valueOf(getPluginConfigOrDefault(Property.fetchReferenceBranch,
                            plugin));
            fetchBaseBranch = Boolean.valueOf(getPluginConfigOrDefault(Property.fetchBaseBranch, plugin));
            outputFile = parseOutputFile(session, getPluginConfigOrDefault(Property.outputFile, plugin));
            writeChanged = Boolean.valueOf(getPluginConfigOrDefault(Property.writeChanged, plugin));
        } else {
            try {
                mergeCurrentProjectProperties(session);
                checkProperties();
                enabled = Boolean.valueOf(Property.enabled.getValue());
                key = parseKey(session, Property.repositorySshKey.getValue());
                referenceBranch = Property.referenceBranch.getValue();
                baseBranch = Property.baseBranch.getValue();
                uncommited = Boolean.valueOf(Property.uncommited.getValue());
                untracked = Boolean.valueOf(Property.untracked.getValue());
                makeUpstream = MAKE_UPSTREAM.equals(session.getRequest().getMakeBehavior());
                skipTestsForNotImpactedModules = Boolean.valueOf(Property.skipTestsForNotImpactedModules.getValue());
                buildAll = Boolean.valueOf(Property.buildAll.getValue());
                compareToMergeBase = Boolean.valueOf(Property.compareToMergeBase.getValue());
                fetchReferenceBranch = Boolean.valueOf(Property.fetchReferenceBranch.getValue());
                fetchBaseBranch = Boolean.valueOf(Property.fetchBaseBranch.getValue());
                outputFile = parseOutputFile(session, Property.outputFile.getValue());
                writeChanged = Boolean.valueOf(Property.writeChanged.getValue());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Optional<Path> parseKey(MavenSession session, String keyOptionValue) throws IOException {
        Path pomDir = session.getCurrentProject().getBasedir().toPath();
        if (keyOptionValue != null && !keyOptionValue.isEmpty()) {
            return Optional.of(pomDir.resolve(keyOptionValue).toAbsolutePath().toRealPath().normalize());
        } else {
            return Optional.empty();
        }
    }

    private Optional<Path> parseOutputFile(MavenSession session, String outputFileValue) throws IOException {
        Path pomDir = session.getCurrentProject().getBasedir().toPath();
        if (outputFileValue != null && !outputFileValue.isEmpty()) {
            return Optional.of(pomDir.resolve(outputFileValue).toAbsolutePath().normalize());
        } else {
            return Optional.empty();
        }
    }

    private void mergeCurrentProjectProperties(MavenSession mavenSession) {
        mavenSession.getTopLevelProject().getProperties().entrySet().stream()
                        .filter(e -> e.getKey().toString().startsWith(Property.PREFIX))
                        .filter(e -> System.getProperty(e.getKey().toString()) == null)
                        .forEach(e -> System.setProperty(e.getKey().toString(), e.getValue().toString()));
    }

    private void checkProperties() throws MavenExecutionException {
        try {
            System.getProperties().entrySet().stream().map(Map.Entry::getKey)
                            .filter(o -> o instanceof String).map(o -> (String) o)
                            .filter(s -> s.startsWith(Property.PREFIX))
                            .map(s -> s.replaceFirst(Property.PREFIX, ""))
                            .forEach(Property::valueOf);
        } catch (IllegalArgumentException e) {
            throw new MavenExecutionException("Invalid invalid GIB property found. Allowed properties: \n" + Property
                            .exemplifyAll(), e);
        }
    }

    private String getPluginConfigOrDefault(Property property, Plugin plugin) {
        String value = extractPluginConfigValue(property.name(), plugin);
        return value == null ? property.defaultValue : value;
    }

    @Override
    public String toString() {
        return "Configuration{" +
                        "enabled=" + enabled +
                        ", key=" + key +
                        ", referenceBranch='" + referenceBranch + '\'' +
                        ", baseBranch='" + baseBranch + '\'' +
                        ", uncommited=" + uncommited +
                        ", makeUpstream=" + makeUpstream +
                        ", skipTestsForNotImpactedModules=" + skipTestsForNotImpactedModules +
                        ", buildAll=" + buildAll +
                        ", compareToMergeBase=" + compareToMergeBase +
                        ", fetchBaseBranch=" + fetchBaseBranch +
                        ", fetchReferenceBranch=" + fetchReferenceBranch +
                        ", outputFile='" + outputFile + '\'' +
                        ", writeChanged=" + writeChanged +
                        '}';
    }
}
