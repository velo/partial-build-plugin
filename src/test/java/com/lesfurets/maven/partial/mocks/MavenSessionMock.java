package com.lesfurets.maven.partial.mocks;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;

public class MavenSessionMock {

    public static MavenSession get() throws Exception {
        return getMavenSession(null);
    }

    public static MavenSession get(String repoDir) throws Exception {
        return getMavenSession(repoDir);
    }

    private static MavenSession getMavenSession(String repoDir) {
        List<MavenProject> projects = Stream.of(
                        RepoTest.LOCAL_DIR.resolve("."),
                        RepoTest.LOCAL_DIR.resolve("./child1"),
                        RepoTest.LOCAL_DIR.resolve("./child2"),
                        RepoTest.LOCAL_DIR.resolve("./child2/subchild1"),
                        RepoTest.LOCAL_DIR.resolve("./child2/subchild2"),
                        RepoTest.LOCAL_DIR.resolve("./child3"),
                        RepoTest.LOCAL_DIR.resolve("./child4"),
                        RepoTest.LOCAL_DIR.resolve("./child4/subchild41"),
                        RepoTest.LOCAL_DIR.resolve("./child4/subchild42"),
                        RepoTest.LOCAL_DIR.resolve("./child5")
        ).map(MavenSessionMock::createProject).collect(Collectors.toList());
        MavenSession mavenSession = mock(MavenSession.class);
        when(mavenSession.getCurrentProject()).thenReturn(projects.get(0));
        MavenExecutionRequest request = mock(MavenExecutionRequest.class);
        when(mavenSession.getRequest()).thenReturn(request);
        when(mavenSession.getUserProperties()).thenReturn(new Properties());
        when(mavenSession.getProjects()).thenReturn(projects);
        when(mavenSession.getTopLevelProject()).thenReturn(projects.get(0));
        when(mavenSession.getExecutionRootDirectory()).thenReturn(repoDir);
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
