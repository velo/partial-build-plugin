package com.lesfurets.maven.partial.utils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;

public class DependencyUtils {

    public static Set<MavenProject> getAllDependencies(List<MavenProject> projects, MavenProject project) {
        Set<MavenProject> dependencies = project.getDependencies().stream()
                .map(d -> convert(projects, d))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .flatMap(p -> getAllDependencies(projects, p).stream())
                .collect(Collectors.toSet());
        dependencies.add(project);
        return dependencies;
    }

    public static void collectAllDependents(List<MavenProject> projects, MavenProject project,
            Set<MavenProject> dependents) {
        projects.stream()
                .filter(p -> isDependentOf(p, project) || project.equals(p.getParent()))
                .filter(p -> !dependents.contains(p))
                .forEach(p -> {
                    dependents.add(p);
                    if (!project.equals(p.getParent())) {
                        collectAllDependents(projects, p, dependents);
                    }
                });
    }

    private static boolean isDependentOf(MavenProject possibleDependent, MavenProject project) {
        return possibleDependent.getDependencies().stream().anyMatch(d -> equals(project, d));
    }

    private static Optional<MavenProject> convert(List<MavenProject> projects, Dependency dependency) {
        return projects.stream().filter(p -> equals(p, dependency)).findFirst();
    }

    private static boolean equals(MavenProject project, Dependency dependency) {
        return dependency.getArtifactId().equals(project.getArtifactId())
                && dependency.getGroupId().equals(project.getGroupId())
                && dependency.getVersion().equals(project.getVersion());
    }
}
