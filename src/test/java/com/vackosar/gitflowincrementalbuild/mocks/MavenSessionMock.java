package com.vackosar.gitflowincrementalbuild.mocks;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;

public class MavenSessionMock {

    public static MavenSession get() throws Exception {
        List<MavenProject> projects = Arrays.asList(
                LocalRepoMock.WORK_DIR.resolve("parent"),
                LocalRepoMock.WORK_DIR.resolve("parent/child1"),
                LocalRepoMock.WORK_DIR.resolve("parent/child2"),
                LocalRepoMock.WORK_DIR.resolve("parent/child2/subchild1"),
                LocalRepoMock.WORK_DIR.resolve("parent/child2/subchild2"),
                LocalRepoMock.WORK_DIR.resolve("parent/child3"),
                LocalRepoMock.WORK_DIR.resolve("parent/child4"),
                LocalRepoMock.WORK_DIR.resolve("parent/child4/subchild41"),
                LocalRepoMock.WORK_DIR.resolve("parent/child4/subchild42"),
                LocalRepoMock.WORK_DIR.resolve("parent/child5")
        ).stream().map(MavenSessionMock::createProject).collect(Collectors.toList());
        MavenSession mavenSession = mock(MavenSession.class);
        when(mavenSession.getCurrentProject()).thenReturn(projects.get(0));
        MavenExecutionRequest request = mock(MavenExecutionRequest.class);
        when(mavenSession.getRequest()).thenReturn(request);
        when(mavenSession.getProjects()).thenReturn(projects);
        when(mavenSession.getTopLevelProject()).thenReturn(projects.get(0));
        return mavenSession;
    }

    private static MavenProject createProject(Path path) {
        MavenProject project = new MavenProject();
        Model model = new Model();
        model.setProperties(new Properties());
        project.setModel(model);
        project.setArtifactId(path.getFileName().toString());
        project.setGroupId(path.getFileName().toString());
        project.setVersion("1");
        project.setFile(path.resolve("pom.xml").toFile());
        return project;
    }
}
