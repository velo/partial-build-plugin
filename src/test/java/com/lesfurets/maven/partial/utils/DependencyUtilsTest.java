package com.lesfurets.maven.partial.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.model.*;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

public class DependencyUtilsTest {

    private static final String VERSION = "1.0";
    private static final String VERSION_NEXT = "1.1-SNAPSHOT";
    private static final String GROUP_ID = "com.test";

    private List<MavenProject> allProjects;

    /**
     * Maven project com.test:parent:1.0
     *
     * <pre>
     *     p --> m1
     * </pre>
     */
    private MavenProject parent;

    /**
     * Maven project com.test:m1:1.0
     *
     * <pre>
     *     m1 --> m3
     *     m1 --> m4
     *     p <-- m1
     * </pre>
     */
    private MavenProject m1;

    /**
     * Maven project com.test:m2:1.0
     *
     * <pre>
     *     p <-- m2
     * </pre>
     */
    private MavenProject m2;

    /**
     * Maven project com.test:m3:1.0
     *
     * <pre>
     *     m3 --> m2
     *     p <-- m3
     * </pre>
     */
    private MavenProject m3;

    /**
     * Maven project com.test:m4:1.0
     *
     * <pre>
     * </pre>
     */
    private MavenProject m4;

    /**
     * Maven project com.test:m5:1.0
     *
     * <pre>
     *     m5 --> m2
     * </pre>
     */
    private MavenProject m5;

    /**
     * Maven project com.test:m6:1.0
     *
     * <pre>
     *     m6 --> m7
     *     m6 --> m8
     *     p <-- m6
     * </pre>
     */
    private MavenProject m6;

    /**
     * Maven project com.test:m7:1.0
     *
     * <pre>
     *     p <-- m7
     * </pre>
     */
    private MavenProject m7;

    /**
     * Maven project com.test:m8:1.0-SNAPSHOT
     *
     * <pre>
     * </pre>
     */
    private MavenProject m8;

    @Before
    public void setUp() {
        parent = newMavenProject(GROUP_ID, "parent", VERSION, null);

        m1 = newMavenProject(GROUP_ID, "m1", VERSION, parent);
        m2 = newMavenProject(GROUP_ID, "m2", VERSION, parent);
        m3 = newMavenProject(GROUP_ID, "m3", VERSION, parent);
        m4 = newMavenProject(GROUP_ID, "m4", VERSION, null);
        m5 = newMavenProject(GROUP_ID, "m5", VERSION, null);
        m6 = newMavenProject(GROUP_ID, "m6", VERSION, parent);
        m7 = newMavenProject(GROUP_ID, "m7", VERSION, parent);
        m8 = newMavenProject(GROUP_ID, "m8", VERSION_NEXT, parent);

        parent.setDependencies(Arrays.asList(newDependency(m1)));
        m1.setDependencies(Arrays.asList(newDependency(m3), newDependency(m4)));
        m3.setDependencies(Arrays.asList(newDependency(m2)));
        m5.setDependencies(Arrays.asList(newDependency(m2)));
        m6.setDependencies(Arrays.asList(newDependency(m7), newDependency(m8)));

        allProjects = Arrays.asList(parent, m1, m2, m3, m4, m5, m6, m7, m8);

        Build build = new Build();
        Plugin plugin = new Plugin();
        plugin.setArtifactId(m7.getArtifactId());
        plugin.setGroupId(m7.getGroupId());
        plugin.setVersion(m7.getVersion());
        build.addPlugin(plugin);
        m5.setBuild(build);
    }

    @Test
    public void collectAllDependents() throws Exception {
        HashSet<MavenProject> dependents = new HashSet<>();
        DependencyUtils.collectAllDependents(allProjects, m2, dependents);
        assertThat(dependents).isEqualTo(Stream.of(parent, m1, m2, m3, m5, m6, m7, m8).collect(Collectors.toSet()));
    }

    @Test
    public void collectNoDependents() throws Exception {
        HashSet<MavenProject> dependents = new HashSet<>();
        DependencyUtils.collectAllDependents(allProjects, m5, dependents);
        assertThat(dependents).isEqualTo(Collections.emptySet());
    }

    @Test
    public void collectDependentsTransitiveM1() throws Exception {
        HashSet<MavenProject> dependents = new HashSet<>();
        DependencyUtils.collectAllDependents(allProjects, m1, dependents);
        assertThat(dependents).isEqualTo(Stream.of(parent, m1, m2, m3, m6, m7, m8).collect(Collectors.toSet()));
    }

    @Test
    public void collectDependentsTransitiveM3() throws Exception {
        HashSet<MavenProject> dependents = new HashSet<>();
        DependencyUtils.collectAllDependents(allProjects, m3, dependents);
        assertThat(dependents).isEqualTo(Stream.of(parent, m1, m2, m3, m6, m7, m8).collect(Collectors.toSet()));
    }

    @Test
    public void collectTransitiveDependents() throws Exception {
        HashSet<MavenProject> dependents = new HashSet<>();
        DependencyUtils.collectAllDependents(allProjects, m4, dependents);
        assertThat(dependents).isEqualTo(Stream.of(parent, m1, m2, m3, m6, m7, m8).collect(Collectors.toSet()));
    }

    @Test
    public void collectTransitiveDependentsParent() {
        HashSet<MavenProject> dependents = new HashSet<>();
        DependencyUtils.collectAllDependents(allProjects, parent, dependents);
        assertThat(dependents).isEqualTo(Stream.of(m1, m2, m3, m6, m7, m8).collect(Collectors.toSet()));
    }

    @Test
    public void collectParentDependents() throws Exception {
        HashSet<MavenProject> dependents = new HashSet<>();
        DependencyUtils.collectAllDependents(Arrays.asList(m4, m1, parent), m4, dependents);
        assertThat(dependents).isEqualTo(Stream.of(m1, parent).collect(Collectors.toSet()));
    }

    @Test
    public void collectTransitiveDependentsDontFollowParent() throws Exception {
        HashSet<MavenProject> dependents = new HashSet<>();
        DependencyUtils.collectAllDependents(allProjects, m7, dependents);
        assertThat(dependents).isEqualTo(Stream.of(m6, m5).collect(Collectors.toSet()));
    }

    @Test
    public void getAllDependencies() throws Exception {
        Set<MavenProject> dependencies = DependencyUtils.getAllDependencies(allProjects, m1);
        assertThat(dependencies).isEqualTo(Stream.of(m1, m3, m4, m2).collect(Collectors.toSet()));
    }

    @Test
    public void getNoDependencies() throws Exception {
        Set<MavenProject> dependencies = DependencyUtils.getAllDependencies(allProjects, m2);
        assertThat(dependencies).isEqualTo(Stream.of(m2).collect(Collectors.toSet()));
    }

    @Test
    public void getPluginDependencies() throws Exception {
        Set<MavenProject> dependencies = DependencyUtils.getAllDependencies(allProjects, m5);
        assertThat(dependencies).isEqualTo(Stream.of(m2, m5, m7).collect(Collectors.toSet()));
    }

    @Test
    public void collectDependenciesInSnapshot() throws Exception {
        HashSet<MavenProject> dependencies = new HashSet<>();
        DependencyUtils.collectAllDependenciesInSnapshot(allProjects, m6, dependencies);
        assertThat(dependencies).isEqualTo(Stream.of(m8).collect(Collectors.toSet()));
    }

    private static MavenProject newMavenProject(String groupId, String artefactId, String version,
                    MavenProject parent) {
        MavenProject mavenProject = new MavenProject();
        mavenProject.setGroupId(groupId);
        mavenProject.setArtifactId(artefactId);
        mavenProject.setVersion(version);
        mavenProject.setParent(parent);
        return mavenProject;
    }

    private static Dependency newDependency(MavenProject mavenProject) {
        Dependency dependency = new Dependency();
        dependency.setGroupId(mavenProject.getGroupId());
        dependency.setArtifactId(mavenProject.getArtifactId());
        dependency.setVersion(mavenProject.getVersion());
        return dependency;
    }

}
