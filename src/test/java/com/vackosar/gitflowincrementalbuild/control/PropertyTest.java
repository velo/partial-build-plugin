package com.vackosar.gitflowincrementalbuild.control;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.apache.maven.execution.MavenSession;
import org.junit.Test;

import com.vackosar.gitflowincrementalbuild.boundary.Configuration;
import com.vackosar.gitflowincrementalbuild.mocks.MavenSessionMock;
import com.vackosar.gitflowincrementalbuild.mocks.ModuleMock;

public class PropertyTest {

    @Test
    public void exemplifyAll() {
        System.out.println(Property.exemplifyAll());
    }

    @Test
    public void systemProperties() throws Exception {
        System.setProperty("gib.referenceBranch", "refs/test/branch");
        ModuleMock module = ModuleMock.module();
        Configuration arguments = module.arguments();
        assertEquals("refs/test/branch", arguments.referenceBranch());
    }

    @Test
    public void userProperties() throws Exception {
        System.setProperty("gib.referenceBranch", "refs/test/otherBranch");
        MavenSession mavenSession = MavenSessionMock.get();
        Properties properties = new Properties();
        properties.setProperty("gib.referenceBranch", "refs/test/branch");
        when(mavenSession.getUserProperties()).thenReturn(properties);
        ModuleMock module = ModuleMock.module(mavenSession);
        Configuration arguments = module.arguments();
        assertEquals("refs/test/branch", arguments.referenceBranch());
    }

    @Test (expected = Exception.class)
    public void badProperty() throws Exception {
        System.setProperty("gib.badProperty", "refs/test/branch");
        ModuleMock module = ModuleMock.module();
        Configuration arguments = module.arguments();
    }
}
