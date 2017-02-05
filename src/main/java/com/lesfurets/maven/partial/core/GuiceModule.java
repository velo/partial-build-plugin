package com.lesfurets.maven.partial.core;

import java.io.File;
import java.io.IOException;

import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.impl.StaticLoggerBinder;

import com.google.inject.*;

public class GuiceModule extends AbstractModule {

    private final Logger logger;
    private final MavenSession mavenSession;

    public GuiceModule(Logger logger, MavenSession mavenSession) {
        this.logger = logger;
        this.mavenSession = mavenSession;
    }

    @Provides
    @Singleton
    public Git provideGit(final StaticLoggerBinder staticLoggerBinder) throws IOException, GitAPIException {
        final FileRepositoryBuilder builder = new FileRepositoryBuilder();
        File pomDir = mavenSession.getCurrentProject().getBasedir().toPath().toFile();
        builder.findGitDir(pomDir);
        if (builder.getGitDir() == null) {
            throw new IllegalArgumentException("Git repository root directory not found ascending from current " +
                            "working directory:'" + pomDir + "'.");
        }
        logger.info("Git root is: " + String.valueOf(builder.getGitDir().getAbsolutePath()));
        return Git.wrap(builder.build());
    }

    @Provides
    @Singleton
    public MavenSession provideMavenSession() {
        return mavenSession;
    }

    @Provides
    @Singleton
    public DifferentFiles provideDifferentFiles(Injector injector) {
        final Configuration configuration = injector.getInstance(Configuration.class);
        if (configuration.useNativeGit) {
            return injector.getInstance(DifferentFilesNative.class);
        } else {
            return injector.getInstance(DifferentFilesJGit.class);
        }
    }

    @Provides
    @Singleton
    public Logger provideLogger() {
        return logger;
    }

    @Override
    protected void configure() {
    }

}
