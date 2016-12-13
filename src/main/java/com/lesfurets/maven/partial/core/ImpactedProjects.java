/*
 * Copyright (C) by Courtanet, All Rights Reserved.
 */
package com.lesfurets.maven.partial.core;

import static com.lesfurets.maven.partial.utils.DependencyUtils.collectDependenciesInSnapshot;
import static com.lesfurets.maven.partial.utils.DependencyUtils.collectDependents;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class ImpactedProjects {

    @Inject
    private Configuration configuration;
    @Inject
    private MavenSession mavenSession;

    public List<MavenProject> get(Collection<MavenProject> changedProjects) {
        HashSet<MavenProject> changed = new HashSet<>(changedProjects);
        changed.removeAll(configuration.ignoredProjects);
        mavenSession.getProjects().stream()
                        .filter(changed::contains)
                        .forEach(p -> collectDependents(mavenSession.getProjects(), p, changed));
        if (configuration.buildSnapshotDependencies) {
            mavenSession.getProjects().stream()
                            .filter(changed::contains)
                            .forEach(p -> collectDependenciesInSnapshot(mavenSession.getProjects(), p, changed));
        }
        return mavenSession.getProjects().stream().filter(changed::contains).collect(Collectors.toList());
    }
}
