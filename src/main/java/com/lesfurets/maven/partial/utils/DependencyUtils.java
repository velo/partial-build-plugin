package com.lesfurets.maven.partial.utils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;

public class DependencyUtils {

    private static final String SNAPSHOT = "SNAPSHOT";

    public static Set<MavenProject> getAllDependencies(List<MavenProject> projects, MavenProject project) {
        Stream<MavenProject> projectDeps = project.getDependencies().stream()
                        .map(d -> convert(projects, d))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .flatMap(p -> getAllDependencies(projects, p).stream());
        Stream<MavenProject> pluginDeps = project.getBuildPlugins().stream()
                        .map(pl -> convert(projects, pl))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .flatMap(p -> getAllDependencies(projects, p).stream());
        Set<MavenProject> dependencies = Stream.concat(projectDeps, pluginDeps).collect(Collectors.toSet());
        dependencies.add(project);
        return dependencies;
    }

    public static void collectAllDependents(List<MavenProject> projects, MavenProject project,
                    Set<MavenProject> dependents) {
        projects.stream()
                        .filter(p -> project.equals(p.getParent()) || isDependentOf(p, project))
                        .filter(p -> !dependents.contains(p))
                        .forEach(p -> {
                            dependents.add(p);
                            if (!project.equals(p.getParent())) {
                                collectAllDependents(projects, p, dependents);
                            }
                        });
    }

    public static void collectAllDependenciesInSnapshot(List<MavenProject> projects, MavenProject project,
                    Set<MavenProject> dependents) {
        projects.stream()
                        .filter(p -> project.equals(p.getParent())
                                        || (isDependentOf(project, p) && p.getVersion().contains(SNAPSHOT)))
                        .filter(p -> !dependents.contains(p))
                        .forEach(p -> {
                            dependents.add(p);
                            if (!project.equals(p.getParent())) {
                                collectAllDependenciesInSnapshot(projects, p, dependents);
                            }
                        });
    }

    private static boolean isDependentOf(MavenProject possibleDependent, MavenProject project) {
        return possibleDependent.getDependencies().stream().anyMatch(d -> equals(project, d)) ||
                        possibleDependent.getBuildPlugins().stream().anyMatch(p -> equals(project, p));
    }

    private static Optional<MavenProject> convert(List<MavenProject> projects, Dependency dependency) {
        return projects.stream().filter(p -> equals(p, dependency)).findFirst();
    }

    private static Optional<MavenProject> convert(List<MavenProject> projects, Plugin plugin) {
        return projects.stream().filter(p -> equals(p, plugin)).findFirst();
    }

    private static boolean equals(MavenProject project, Dependency dependency) {
        return dependency.getArtifactId().equals(project.getArtifactId())
                        && dependency.getGroupId().equals(project.getGroupId())
                        && dependency.getVersion().equals(project.getVersion());
    }

    private static boolean equals(MavenProject project, Plugin plugin) {
        return plugin.getArtifactId().equals(project.getArtifactId())
                        && plugin.getGroupId().equals(project.getGroupId())
                        && plugin.getVersion().equals(project.getVersion());
    }

}
