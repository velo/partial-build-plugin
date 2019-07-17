/*
 * Copyright (C) by Courtanet, All Rights Reserved.
 */
package com.lesfurets.maven.partial.core;

import static com.lesfurets.maven.partial.utils.DependencyUtils.getAllDependencies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
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
    private static final String SONAR_SKIP = "sonar.skip";

    @Inject
    private Configuration configuration;
    @Inject
    private Logger logger;
    @Inject
    private MavenSession mavenSession;

    public void setUpSession(Collection<MavenProject> changedProjects) {
        final Collection<MavenProject> changed = new ArrayList<>(changedProjects);
        changed.addAll(configuration.ignoredProjects);
        changed.addAll(configuration.buildAnywaysProjects);

        Comparator<MavenProject> comparator;
        if (configuration.buildChangedFirst)
        {
            logger.info("Sorting reactor so changed project are built first.");

            comparator = (project1, project2) -> {
                if (changedProjects.contains(project1)
                    && changedProjects.contains(project2))
                    // both projects are changed, keep order
                    return 0;

                if (changedProjects.contains(project1))
                    return -1;
                if (changedProjects.contains(project2))
                    return 1;

                return 0;
            };
        } else {
            // do not reshuffle projects
            comparator = (project1, project2) -> 0;
        }

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
                                .sorted(comparator)
                                .collect(Collectors.toList()));
            }
        } else {
            mavenSession.getProjects().stream()
                            .filter(p -> !changed.contains(p))
                            .forEach(p -> {
                                this.ifSkipDependenciesTest(p);
                                this.ifSkipDependenciesSonar(p);
                            });

            mavenSession.setProjects(mavenSession.getProjects().stream()
                .sorted(comparator)
                .collect(Collectors.toList()));
        }
    }

    private Stream<MavenProject> collectDependencies(Collection<MavenProject> changedProjects) {
        return changedProjects.stream()
                        .flatMap(this::ifMakeUpstreamGetDependencies)
                        .filter(p -> !changedProjects.contains(p))
                        .map(this::ifSkipDependenciesTest)
                        .map(this::ifSkipDependenciesSonar);
    }

    private MavenProject ifSkipDependenciesTest(MavenProject mavenProject) {
        if (configuration.skipTestsForNotImpactedModules) {
            mavenProject.getProperties().setProperty(MAVEN_TEST_SKIP, Boolean.TRUE.toString());
        }
        return mavenProject;
    }

    private MavenProject ifSkipDependenciesSonar(MavenProject mavenProject) {
        if (configuration.skipTestsForNotImpactedModules) {
            mavenProject.getProperties().setProperty(SONAR_SKIP, Boolean.TRUE.toString());
        }
        return mavenProject;
    }

    private Stream<MavenProject> ifMakeUpstreamGetDependencies(MavenProject mavenProject) {
        return getAllDependencies(mavenSession.getProjects(), mavenProject).stream();
    }
}
