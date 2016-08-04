package com.vackosar.gitflowincrementalbuild.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Function;

import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PluginUtils {

    private static Logger logger = LoggerFactory.getLogger(PluginUtils.class);

    private static Function<MavenProject, String> projectIdWriter = project ->
            project.getGroupId() + ":" + project.getArtifactId() + ":" + project.getVersion();

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

    public static void writeChangedProjectsToFile(Collection<MavenProject> projects, File outputFile,
            StringJoiner joiner) {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile)))) {
            writer.write(joinProjectIds(projects, joiner, projectIdWriter).toString());
        } catch (IOException e) {
            logger.warn("Error writing changed projects to file on path :" + outputFile.getPath(), e);
        }
    }

    public static void writeChangedProjectsToFile(Collection<MavenProject> projects, File outputFile) {
        writeChangedProjectsToFile(projects, outputFile, new StringJoiner("\n"));
    }

    public static StringJoiner joinProjectIds(Collection<MavenProject> projects, StringJoiner joiner) {
        for (MavenProject changedProject : projects) {
            joiner.add(changedProject.getGroupId() + ":" + changedProject.getArtifactId());
        }
        return joiner;
    }

    public static StringJoiner joinProjectIds(Collection<MavenProject> projects, StringJoiner joiner,
            Function<MavenProject, String> projectIdWriter) {
        for (MavenProject changedProject : projects) {
            joiner.add(projectIdWriter.apply(changedProject));
        }
        return joiner;
    }

    public static List<String> separatePattern(String patternString) {
        if (patternString.isEmpty()) {
            return Collections.emptyList();
        }
        patternString = StringUtils.deleteWhitespace(patternString);
        return Arrays.asList(patternString.split(","));
    }
}
