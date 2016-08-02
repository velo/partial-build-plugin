package com.vackosar.gitflowincrementalbuild.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.junit.Before;
import org.junit.Test;

public class DependencyUtilsTest {

    public static final String VERSION = "1.0";
    public static final String GROUP_ID = "com.test";

    MavenProject parent;
    MavenProject m1;
    MavenProject m2;
    MavenProject m3;
    MavenProject m4;
    MavenProject m5;

    List<MavenProject> allProjects;

    @Before
    public void setUp() {
        parent = new MavenProject();
        m1 = new MavenProject();
        m2 = new MavenProject();
        m3 = new MavenProject();
        m4 = new MavenProject();
        m5 = new MavenProject();

        parent.setArtifactId("parent");
        parent.setGroupId(GROUP_ID);
        parent.setVersion(VERSION);

        m1.setParent(parent);
        m1.setArtifactId("m1");
        m1.setGroupId(GROUP_ID);
        m1.setVersion(VERSION);
        Dependency d1 = new Dependency();
        d1.setArtifactId("m1");
        d1.setGroupId(GROUP_ID);
        d1.setVersion(VERSION);

        m2.setParent(parent);
        m2.setGroupId(GROUP_ID);
        m2.setArtifactId("m2");
        m2.setVersion(VERSION);
        Dependency d2 = new Dependency();
        d2.setArtifactId("m2");
        d2.setGroupId(GROUP_ID);
        d2.setVersion(VERSION);

        m3.setParent(parent);
        m3.setGroupId(GROUP_ID);
        m3.setArtifactId("m3");
        m3.setVersion(VERSION);
        Dependency d3 = new Dependency();
        d3.setArtifactId("m3");
        d3.setGroupId(GROUP_ID);
        d3.setVersion(VERSION);

        m4.setGroupId(GROUP_ID);
        m4.setArtifactId("m4");
        m4.setVersion(VERSION);
        Dependency d4 = new Dependency();
        d4.setArtifactId("m4");
        d4.setGroupId(GROUP_ID);
        d4.setVersion(VERSION);

        m5.setGroupId(GROUP_ID);
        m5.setArtifactId("m5");
        m5.setVersion(VERSION);
        Dependency d5 = new Dependency();
        d5.setArtifactId("m5");
        d5.setGroupId(GROUP_ID);
        d5.setVersion(VERSION);

        //        m5 -> m2
        m5.setDependencies(Arrays.asList(d2));
        //        m3 -> m2
        m3.setDependencies(Arrays.asList(d2));
        //        m1 -> m4
        //        m1 -> m3
        m1.setDependencies(Arrays.asList(d4, d3));

        parent.setDependencies(Arrays.asList(d1));

        allProjects = Arrays.asList(parent, m1, m2, m3, m4, m5);
    }

    @Test
    public void getAllDependencies() throws Exception {
        Set<MavenProject> dependencies = DependencyUtils.getAllDependencies(allProjects, m1);
        assertThat(dependencies).isEqualTo(Stream.of(m1, m3, m4, m2).collect(Collectors.toSet()));
    }

    @Test
    public void collectAllDependents() throws Exception {
        HashSet<MavenProject> dependents = new HashSet<>();
        DependencyUtils.collectAllDependents(allProjects, m1, dependents);
        assertThat(dependents).isEqualTo(Stream.of(m1, m2, m3, m5, parent).collect(Collectors.toSet()));
        dependents = new HashSet<>();
        DependencyUtils.collectAllDependents(allProjects, m5, dependents);
        assertThat(dependents).isEqualTo(Collections.emptySet());
    }

    @Test
    public void collectNoDependencies() throws Exception {
        HashSet<MavenProject> dependents = new HashSet<>();
        DependencyUtils.collectAllDependents(allProjects, m5, dependents);
        assertThat(dependents).isEqualTo(Collections.emptySet());
    }

    @Test
    public void collectParentDependencies() throws Exception {
        HashSet<MavenProject> dependents = new HashSet<>();
        DependencyUtils.collectAllDependents(Arrays.asList(m4, m1, parent), m4, dependents);
        assertThat(dependents).isEqualTo(Stream.of(m1, parent).collect(Collectors.toSet()));
    }
}