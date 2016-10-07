/*
 * Copyright (C) by Courtanet, All Rights Reserved.
 */
package com.lesfurets.maven.partial.mocks;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;

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

    public ITHelper(File testProjectBaseDir, String pluginProjectBaseDir, String pluginProjectVersion) {
        this.pluginProjectBaseDir = pluginProjectBaseDir;
        this.pluginProjectVersion = pluginProjectVersion;
        this.testProjectBaseDir = testProjectBaseDir;
        this.testProjectPomPath = testProjectBaseDir.toPath().resolve("pom.xml");
        this.testProjectZip =  new File(pluginProjectBaseDir, IT_TEST_PROJECT_ZIP);
    }

    public void setupTest() throws IOException {
        unzipProject();
        copyTestPom();
        replaceTestPomVersion();
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
