/*
 * Copyright (C) by Courtanet, All Rights Reserved.
 */
package com.lesfurets.maven.partial.core;

import static com.lesfurets.maven.partial.utils.DependencyUtils.getAllDependencies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class RebuildProjects {

    private static final String MAVEN_TEST_SKIP = "maven.test.skip";

    @Inject
    private Configuration configuration;
    @Inject
    private Logger logger;
    @Inject
    private MavenSession mavenSession;

    public void setUpSession(Collection<MavenProject> changedProjects) {
        final Collection<MavenProject> changed = new ArrayList<>(changedProjects);
        changed.addAll(configuration.ignoredProjects);
        if (!configuration.buildAll) {
            Collection<MavenProject> rebuildProjects = changed;
            if (configuration.makeUpstream) {
                rebuildProjects = Stream.concat(changed.stream(), collectDependencies(changed))
                                .collect(Collectors.toSet());
            }
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

    private Stream<MavenProject> collectDependencies(Collection<MavenProject> changedProjects) {
        return changedProjects.stream()
                        .flatMap(this::ifMakeUpstreamGetDependencies)
                        .filter(p -> !changedProjects.contains(p))
                        .map(this::ifSkipDependenciesTest);
    }

    private MavenProject ifSkipDependenciesTest(MavenProject mavenProject) {
        if (configuration.skipTestsForNotImpactedModules) {
            mavenProject.getProperties().setProperty(MAVEN_TEST_SKIP, Boolean.TRUE.toString());
        }
        return mavenProject;
    }

    private Stream<MavenProject> ifMakeUpstreamGetDependencies(MavenProject mavenProject) {
        return getAllDependencies(mavenSession.getProjects(), mavenProject).stream();
    }
}
