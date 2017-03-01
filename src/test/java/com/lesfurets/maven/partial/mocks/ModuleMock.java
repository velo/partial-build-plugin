package com.lesfurets.maven.partial.mocks;

import java.io.IOException;

import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.logging.console.ConsoleLoggerManager;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.impl.StaticLoggerBinder;

import com.google.inject.*;
import com.lesfurets.maven.partial.core.Configuration;
import com.lesfurets.maven.partial.core.DifferentFiles;
import com.lesfurets.maven.partial.core.DifferentFilesNative;
import com.lesfurets.maven.partial.core.GuiceModule;

public class ModuleMock extends AbstractModule {

    private final GuiceModule guiceModule;

    public static ModuleMock module() throws Exception {
        return new ModuleMock();
    }

    public static ModuleMock module(String repoDir) throws Exception {
        return new ModuleMock(repoDir);
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

    private ModuleMock(String repoDir) throws Exception {
        this.guiceModule = new GuiceModule(new ConsoleLogger(), getMavenSessionMock(repoDir));
    }

    @Singleton
    @Provides
    public Logger provideLogger() {
        return new ConsoleLoggerManager().getLoggerForComponent("Test");
    }

    @Singleton
    @Provides
    public Git provideGit() throws IOException, GitAPIException {
        return guiceModule.provideGit(new StaticLoggerBinder(new ConsoleLoggerManager().getLoggerForComponent("Test")));
    }

    @Provides
    @Singleton
    public DifferentFiles provideDifferentFiles() {
        return new DifferentFilesNative();
    }

    @Singleton
    @Provides
    public Configuration arguments() throws Exception {
        return new Configuration(guiceModule.provideMavenSession());
    }

    @Singleton
    @Provides
    public MavenSession provideMavenSession() {
        return guiceModule.provideMavenSession();
    }

    private MavenSession getMavenSessionMock() throws Exception {
        return MavenSessionMock.get();
    }

    private MavenSession getMavenSessionMock(String repoDir) throws Exception {
        return MavenSessionMock.get(repoDir);
    }

    @Override
    protected void configure() {
    }

}
