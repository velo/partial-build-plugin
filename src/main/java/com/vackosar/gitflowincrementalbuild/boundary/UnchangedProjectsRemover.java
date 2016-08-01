package com.vackosar.gitflowincrementalbuild.boundary;

import static com.vackosar.gitflowincrementalbuild.utils.DependencyUtils.getAllDependencies;
import static com.vackosar.gitflowincrementalbuild.utils.DependencyUtils.getAllDependents;
import static com.vackosar.gitflowincrementalbuild.utils.PluginUtils.joinProjectIds;
import static com.vackosar.gitflowincrementalbuild.utils.PluginUtils.separatePattern;
import static com.vackosar.gitflowincrementalbuild.utils.PluginUtils.writeChangedProjectsToFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.artifact.filter.PatternIncludesArtifactFilter;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.vackosar.gitflowincrementalbuild.control.ChangedProjects;
import com.vackosar.gitflowincrementalbuild.control.Modules;

@Singleton
public class UnchangedProjectsRemover {

    public static final String CHANGED_PROJECTS = "changed.projects";
    private static final String MAVEN_TEST_SKIP = "maven.test.skip";

    @Inject
    private Configuration configuration;
    @Inject
    private Logger logger;
    @Inject
    private ChangedProjects changedProjects;
    @Inject
    private MavenSession mavenSession;

    public void act() throws GitAPIException, IOException {
        Set<MavenProject> changed = changedProjects.get();
        printDelimiter();
        logProjects(changed, "Changed Projects:");

        final Set<MavenProject> ignored = getIgnoredProjects();
        if (!ignored.isEmpty()) {
            printDelimiter();
            logProjects(ignored, "Ignoring Changes in Projects:");
        }

        final Set<MavenProject> changedIgnored = changed.stream()
                .filter(p -> !ignored.contains(p))
                .collect(Collectors.toSet());

        final Set<MavenProject> changedProjects = getAllDependentProjects(changedIgnored);

        mavenSession.getAllProjects().forEach(m -> m.getProperties()
                .setProperty(CHANGED_PROJECTS, joinProjectIds(changedProjects, new StringJoiner(",")).toString()));

        if (configuration.writeChanged()) {
            Path defaultPath = Modules.getPath(mavenSession.getTopLevelProject()).resolve(CHANGED_PROJECTS);
            Path outputFilePath = configuration.outputFile().orElse(defaultPath);
            writeChangedProjectsToFile(changedProjects, outputFilePath.toFile());
        }

        if (!configuration.buildAll()) {
            Set<MavenProject> rebuildProjects = getRebuildProjects(changedIgnored);

            if (rebuildProjects.isEmpty()) {
                logger.info("No changed artifacts to build. Executing validate goal only.");
                mavenSession.getGoals().clear();
                mavenSession.getGoals().add("validate");
            } else {
                mavenSession.setProjects(mavenSession.getProjects().stream()
                        .filter(rebuildProjects::contains)
                        .collect(Collectors.toList()));
            }
        } else {
            mavenSession.getProjects().stream()
                    .filter(p -> !changedIgnored.contains(p))
                    .forEach(this::ifSkipDependenciesTest);
        }
    }

    public Set<MavenProject> getAllDependentProjects(Set<MavenProject> changed) {
        mavenSession.getProjects().stream()
                .filter(changed::contains)
                .forEach(p -> getAllDependents(mavenSession.getProjects(), p, changed));
        return changed;
    }

    private Set<MavenProject> getIgnoredProjects() {
        if (Strings.isNullOrEmpty(configuration.ignoreChanged())) {
            return Collections.emptySet();
        }
        List<String> patterns = separatePattern(configuration.ignoreChanged());
        final PatternIncludesArtifactFilter filter = new PatternIncludesArtifactFilter(patterns);
        return mavenSession.getProjects().stream()
                .filter(p -> filter.include(p.getArtifact()))
                .collect(Collectors.toSet());
    }

    private Set<MavenProject> getRebuildProjects(Set<MavenProject> changedProjects) {
        if (configuration.makeUpstream()) {
            return Stream.concat(changedProjects.stream(), collectDependencies(changedProjects))
                    .collect(Collectors.toSet());
        } else {
            return changedProjects;
        }
    }

    private Stream<MavenProject> collectDependencies(Set<MavenProject> changedProjects) {
        return changedProjects.stream()
                .flatMap(this::ifMakeUpstreamGetDependencies)
                .filter(p -> !changedProjects.contains(p))
                .map(this::ifSkipDependenciesTest);
    }

    private MavenProject ifSkipDependenciesTest(MavenProject mavenProject) {
        if (configuration.skipTestsForNotImpactedModules()) {
            mavenProject.getProperties().setProperty(MAVEN_TEST_SKIP, Boolean.TRUE.toString());
        }
        return mavenProject;
    }

    private Stream<MavenProject> ifMakeUpstreamGetDependencies(MavenProject mavenProject) {
        return getAllDependencies(mavenSession.getProjects(), mavenProject).stream();
    }

    private void logProjects(Collection<MavenProject> projects, String title) {
        logger.info(title);
        logger.info("");
        projects.stream().map(MavenProject::getName).forEach(logger::info);
        logger.info("");
    }

    private void printDelimiter() {
        logger.info("------------------------------------------------------------------------");
    }

}
