/*
 * Copyright (C) by Courtanet, All Rights Reserved.
 */
package com.vackosar.gitflowincrementalbuild.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Set;
import java.util.StringJoiner;

import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

public class PluginUtils {

    public static String extractPluginConfigValue(String parameter, Plugin plugin) {
        String value = extractConfigValue(parameter, plugin.getConfiguration());
        for (int i = 0; i < plugin.getExecutions().size() && value == null; i++) {
            value = extractConfigValue(parameter, plugin.getExecutions().get(i).getConfiguration());
        }
        return value;
    }

    private static String extractConfigValue(String parameter, Object configuration) {
        try {
            return ((Xpp3Dom) configuration).getChild(parameter).getValue();
        } catch (Exception ex) {
        }
        return null;
    }

    public static void writeChangedProjectsToFile(Set<MavenProject> projects, File outputFile, StringJoiner joiner) {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile)))) {
            writer.write(joinProjectIds(projects, joiner).toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeChangedProjectsToFile(Set<MavenProject> projects, File outputFile) {
        writeChangedProjectsToFile(projects, outputFile, new StringJoiner("\n"));
    }

    public static StringJoiner joinProjectIds(Set<MavenProject> projects, StringJoiner joiner) {
        for (MavenProject changedProject : projects) {
            joiner.add(changedProject.getGroupId() + ":" + changedProject.getArtifactId());
        }
        return joiner;
    }
}
