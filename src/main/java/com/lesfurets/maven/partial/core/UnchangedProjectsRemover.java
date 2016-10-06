package com.lesfurets.maven.partial.core;

import static com.lesfurets.maven.partial.utils.DependencyUtils.collectDependenciesInSnapshot;
import static com.lesfurets.maven.partial.utils.DependencyUtils.collectDependents;
import static com.lesfurets.maven.partial.utils.DependencyUtils.getAllDependencies;
import static com.lesfurets.maven.partial.utils.PluginUtils.joinProjectIds;
import static com.lesfurets.maven.partial.utils.PluginUtils.writeChangedProjectsToFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;

import com.google.inject.Inject;
import com.google.inject.Singleton;

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

        final Set<MavenProject> ignored = configuration.ignoredProjects();
        if (!ignored.isEmpty()) {
            printDelimiter();
            logProjects(ignored, "Excluded Projects:");
        }

        // do not write ignored projects as changed
        changed.removeAll(ignored);

        collectDependentProjects(changed);

        List<MavenProject> sortedChanged = mavenSession.getProjects().stream()
                        .filter(changed::contains)
                        .collect(Collectors.toList());

        mavenSession.getProjects().forEach(m -> m.getProperties()
                        .setProperty(CHANGED_PROJECTS,
                                        joinProjectIds(sortedChanged, new StringJoiner(",")).toString()));

        if (configuration.writeChanged()) {
            Path defaultPath = Modules.getPath(mavenSession.getTopLevelProject()).resolve(CHANGED_PROJECTS);
            Path outputFilePath = configuration.outputFile().orElse(defaultPath);
            writeChangedProjectsToFile(sortedChanged, outputFilePath.toFile());
        }

        // add ignored projects to build
        changed.addAll(ignored);

        if (!configuration.buildAll()) {
            Set<MavenProject> rebuildProjects = getRebuildProjects(changed);
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
                            .filter(p -> !changed.contains(p))
                            .forEach(this::ifSkipDependenciesTest);
        }
    }

    public void collectDependentProjects(Set<MavenProject> changed) {
        mavenSession.getProjects().stream()
                        .filter(changed::contains)
                        .forEach(p -> collectDependents(mavenSession.getProjects(), p, changed));
        if (configuration.makeDependenciesInSnapshot()) {
            mavenSession.getProjects().stream()
                            .filter(changed::contains)
                            .forEach(p -> collectDependenciesInSnapshot(mavenSession.getProjects(), p, changed));
        }
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
