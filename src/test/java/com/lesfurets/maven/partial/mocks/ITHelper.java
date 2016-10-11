/*
 * Copyright (C) by Courtanet, All Rights Reserved.
 */
package com.lesfurets.maven.partial.mocks;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.codehaus.plexus.logging.console.ConsoleLoggerManager;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.impl.StaticLoggerBinder;

public class ITHelper {

    public static final String IT_PATH = "src/it/";
    public static final String IT_PROJECTS = IT_PATH + "projects";
    public static final String IT_TEST_PROJECT_ZIP = IT_PATH + "project.zip";
    public static final String PROJECT_VERSION_PLACEHOLDER = "@project.version@";

    /**
     * Basedir of the partial-build-plugin : ${project.basedir}
     */
    private final String pluginProjectBaseDir;

    /**
     * Version of builded partial-build-plugin
     */
    private final String pluginProjectVersion;

    /**
     * Directory of the test project
     * {@see maven-invoker-plugin : basedir}
     */
    private final File testProjectBaseDir;

    private final Path testProjectPomPath;

    private final File testProjectZip;

    public StaticLoggerBinder staticLoggerBinder;

    public ITHelper(File testProjectBaseDir, String pluginProjectBaseDir, String pluginProjectVersion) {
        this.pluginProjectBaseDir = pluginProjectBaseDir;
        this.pluginProjectVersion = pluginProjectVersion;
        this.testProjectBaseDir = testProjectBaseDir;
        this.testProjectPomPath = testProjectBaseDir.toPath().resolve("pom.xml");
        this.testProjectZip =  new File(pluginProjectBaseDir, IT_TEST_PROJECT_ZIP);
        staticLoggerBinder = new StaticLoggerBinder(new ConsoleLoggerManager().getLoggerForComponent("Test"));

    }

    public void setupTest() throws IOException, URISyntaxException {
        copyGitRepo();
        copyTestPom();
        replaceTestPomVersion();
    }

    private void copyGitRepo() throws URISyntaxException, IOException {
        URL repo = LocalRepoMock.class.getResource("/repo");
        File repoDir = new File(repo.toURI());
        FileUtils.copyDirectoryStructure(repoDir, testProjectBaseDir);
        FileRepositoryBuilder fileRepositoryBuilder = new FileRepositoryBuilder();
        fileRepositoryBuilder.findGitDir(testProjectBaseDir);
        fileRepositoryBuilder.setWorkTree(testProjectBaseDir);
        Repository build = fileRepositoryBuilder.build();
        build.close();
    }

    public void copyTestPom() throws IOException {
        Files.copy(Paths.get(pluginProjectBaseDir, IT_PROJECTS, testProjectBaseDir.getName(), "pom.xml"), testProjectPomPath,
                        StandardCopyOption.REPLACE_EXISTING);
    }

    public void replaceTestPomVersion() throws IOException {
        String content = new String(Files.readAllBytes(testProjectPomPath), UTF_8);
        content = content.replaceAll(PROJECT_VERSION_PLACEHOLDER, pluginProjectVersion);
        Files.write(testProjectPomPath, content.getBytes(UTF_8));
    }

    public void unzipProject() {
        new UnZiper().act(testProjectZip, testProjectBaseDir);
    }
}
