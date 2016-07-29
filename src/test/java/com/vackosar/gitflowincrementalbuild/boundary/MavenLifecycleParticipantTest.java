package com.vackosar.gitflowincrementalbuild.boundary;

import java.lang.reflect.Field;

import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.logging.Logger;
import org.junit.Test;
import org.mockito.Mockito;

import com.vackosar.gitflowincrementalbuild.control.Property;
import com.vackosar.gitflowincrementalbuild.mocks.MavenSessionMock;

public class MavenLifecycleParticipantTest {

    @Test
    public void disabled() throws Exception {
        Property.enabled.setValue("false");
        MavenLifecycleParticipant participant = new MavenLifecycleParticipant();
        Field loggerField = participant.getClass().getDeclaredField("logger");
        loggerField.setAccessible(true);
        loggerField.set(participant, Mockito.mock(Logger.class));
        MavenSession mavenSession = MavenSessionMock.get();
        participant.afterProjectsRead(mavenSession);
    }


    @Test public void configured() throws Exception {
        MavenLifecycleParticipant participant = new MavenLifecycleParticipant();
        Field loggerField = participant.getClass().getDeclaredField("logger");
        loggerField.setAccessible(true);
        loggerField.set(participant, Mockito.mock(Logger.class));
        MavenSession mavenSession = MavenSessionMock.get();
        participant.afterProjectsRead(mavenSession);
    }



}
