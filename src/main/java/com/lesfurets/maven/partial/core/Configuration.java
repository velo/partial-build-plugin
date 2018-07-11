package com.lesfurets.maven.partial.core;

import static com.lesfurets.maven.partial.utils.PluginUtils.extractPluginConfigValue;
import static com.lesfurets.maven.partial.utils.PluginUtils.separatePattern;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.artifact.filter.PatternIncludesArtifactFilter;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class Configuration {

    private static final String PLUGIN_KEY = "com.lesfurets:partial-build-plugin";

    private static final String MAKE_UPSTREAM = "make-upstream";

    public final boolean enabled;
    public final Path key;
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
    public final String ignoreChangedPattern;
    public final boolean buildSnapshotDependencies;
    public final Set<MavenProject> ignoredProjects;
    public final boolean impacted;
    public final boolean ignoreAllReactorProjects;
    public final boolean useNativeGit;
    public final String rootDirectory;

    @Inject
    public Configuration(MavenSession session) throws IOException {

        try {
            makeUpstream = MAKE_UPSTREAM.equals(session.getRequest().getMakeBehavior());
            Plugin plugin = session.getTopLevelProject().getPlugin(PLUGIN_KEY);
            // check properties
            checkPluginConfiguration(plugin);
            checkProperties(session.getTopLevelProject().getProperties());
            checkProperties(System.getProperties());
            checkProperties(session.getUserProperties());
            // parse into configuration
            enabled = Boolean.valueOf(Property.enabled.getValue());
            key = parseKey(session, Property.repositorySshKey.getValue());
            referenceBranch = Property.referenceBranch.getValue();
            baseBranch = Property.baseBranch.getValue();
            uncommited = Boolean.valueOf(Property.uncommited.getValue());
            untracked = Boolean.valueOf(Property.untracked.getValue());
            skipTestsForNotImpactedModules = Boolean.valueOf(Property.skipTestsForNotImpactedModules.getValue());
            buildAll = Boolean.valueOf(Property.buildAll.getValue());
            compareToMergeBase = Boolean.valueOf(Property.compareToMergeBase.getValue());
            fetchReferenceBranch = Boolean.valueOf(Property.fetchReferenceBranch.getValue());
            fetchBaseBranch = Boolean.valueOf(Property.fetchBaseBranch.getValue());
            outputFile = parseOutputFile(session, Property.outputFile.getValue());
            writeChanged = Boolean.valueOf(Property.writeChanged.getValue());
            buildSnapshotDependencies = Boolean.valueOf(Property.buildSnapshotDependencies.getValue());
            impacted = Boolean.valueOf(Property.impacted.getValue());
            ignoreAllReactorProjects = Boolean.valueOf(Property.ignoreAllReactorProjects.getValue());
            ignoreChangedPattern = Property.ignoreChanged.getValue();
            ignoredProjects = getIgnoredProjects(session, ignoreChangedPattern);
            useNativeGit = Boolean.valueOf(Property.useNativeGit.getValue());
            rootDirectory = session.getExecutionRootDirectory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private Path parseKey(MavenSession session, String keyOptionValue) throws IOException {
        Path pomDir = session.getTopLevelProject().getBasedir().toPath();
        if (keyOptionValue != null && !keyOptionValue.isEmpty()) {
            return pomDir.resolve(keyOptionValue).toAbsolutePath().toRealPath().normalize();
        }
        return null;
    }

    private Set<MavenProject> getIgnoredProjects(MavenSession session, String ignoreChangedPattern) {
        if (Strings.isNullOrEmpty(ignoreChangedPattern)) {
            return session.getProjects().stream()
                            .filter(this::isProjectIgnored)
                            .collect(Collectors.toSet());
        }
        List<String> patterns = separatePattern(ignoreChangedPattern);
        final PatternIncludesArtifactFilter filter = new PatternIncludesArtifactFilter(patterns);
        return session.getProjects().stream()
                        .filter(p -> filter.include(p.getArtifact()) || isProjectIgnored(p))
                        .collect(Collectors.toSet());
    }

    private boolean isProjectIgnored(MavenProject p) {
        return this.ignoreAllReactorProjects && "pom".equals(p.getPackaging()) && !p.getModules().isEmpty();
    }

    private Optional<Path> parseOutputFile(MavenSession session, String outputFileValue) throws IOException {
        Path pomDir = session.getTopLevelProject().getBasedir().toPath();
        if (outputFileValue != null && !outputFileValue.isEmpty()) {
            return Optional.of(pomDir.resolve(outputFileValue).toAbsolutePath().normalize());
        }
        return Optional.empty();
    }

    private void checkPluginConfiguration(Plugin plugin) {
        if (null != plugin) {
            Arrays.stream(Property.values())
                            .forEach(p -> p.setValue(extractPluginConfigValue(p.name(), plugin)));
        }
    }

    private void checkProperties(Properties properties) throws MavenExecutionException {
        try {
            properties.stringPropertyNames().stream()
                            .filter(s -> s.startsWith(Property.PREFIX))
                            .map(s -> s.replaceFirst(Property.PREFIX, ""))
                            .map(Property::valueOf)
                            .forEach(p -> p.setValue(properties.getProperty(p.fullName())));
        } catch (IllegalArgumentException e) {
            throw new MavenExecutionException("Invalid invalid GIB property found. Allowed properties: \n"
                            + Property.exemplifyAll(), e);
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                        .append("enable", enabled)
                        .append("key", key)
                        .append("referenceBranch", referenceBranch)
                        .append("baseBranch", baseBranch)
                        .append("uncommited", uncommited)
                        .append("untracked", untracked)
                        .append("makeUpstream", makeUpstream)
                        .append("skipTestsForNotImpactedModules", skipTestsForNotImpactedModules)
                        .append("buildAll", buildAll)
                        .append("compareToMergeBase", compareToMergeBase)
                        .append("fetchBaseBranch", fetchBaseBranch)
                        .append("fetchReferenceBranch", fetchReferenceBranch)
                        .append("outputFile", outputFile)
                        .append("writeChanged", writeChanged)
                        .append("ignoreChangedPattern", ignoreChangedPattern)
                        .append("buildSnapshotDependencies", buildSnapshotDependencies)
                        .append("impacted", impacted)
                        .append("ignoreAllReactorProjects", ignoreAllReactorProjects)
                        .append("useNativeGit", useNativeGit)
                        .toString();
    }
}
