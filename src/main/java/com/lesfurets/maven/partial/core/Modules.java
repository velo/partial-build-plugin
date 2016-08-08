package com.lesfurets.maven.partial.core;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class Modules {

    @Inject MavenSession session;

    public Map<Path, MavenProject> createPathMap() {
        return session.getProjects().stream()
                .collect(Collectors.toMap(Modules::getPath, project -> project));
    }

    public static Path getPath(MavenProject project) {
        try {
            return project.getBasedir().toPath().normalize().toAbsolutePath().toRealPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
