package com.vackosar.gitflowincrementalbuild.mocks;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.vackosar.gitflowincrementalbuild.boundary.Configuration;
import com.vackosar.gitflowincrementalbuild.boundary.GuiceModule;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.logging.console.ConsoleLoggerManager;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.impl.StaticLoggerBinder;

import java.io.IOException;

public class ModuleMock extends AbstractModule {

    private final GuiceModule guiceModule;

    public static ModuleMock module() throws Exception {
        return new ModuleMock();
    }

    public static ModuleMock module(MavenSession session) throws Exception {
        return new ModuleMock(session);
    }

    private ModuleMock() throws Exception {
        this.guiceModule = new GuiceModule(new ConsoleLogger(), getMavenSessionMock());
    }

    private ModuleMock(MavenSession session) throws Exception {
        this.guiceModule = new GuiceModule(new ConsoleLogger(), session);
    }

    @Singleton
    @Provides
    public Logger provideLogger() {
        return new ConsoleLoggerManager().getLoggerForComponent("Test");
    }

    @Singleton @Provides public Git provideGit() throws IOException, GitAPIException {
        return guiceModule.provideGit(new StaticLoggerBinder(new ConsoleLoggerManager().getLoggerForComponent("Test")));
    }

    @Singleton @Provides public Configuration arguments() throws Exception {
        return new Configuration(guiceModule.provideMavenSession());
    }

    @Singleton @Provides public MavenSession provideMavenSession() {
        return guiceModule.provideMavenSession();
    }

    private MavenSession getMavenSessionMock() throws Exception {
        return MavenSessionMock.get();
    }

    @Override
    protected void configure() {}
}
