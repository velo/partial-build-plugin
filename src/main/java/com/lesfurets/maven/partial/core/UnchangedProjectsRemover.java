package com.lesfurets.maven.partial.core;

import static com.lesfurets.maven.partial.utils.PluginUtils.joinProjectIds;
import static com.lesfurets.maven.partial.utils.PluginUtils.writeChangedProjectsToFile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class UnchangedProjectsRemover {

    public static final String CHANGED_PROJECTS = "changed.projects";

    @Inject
    private Configuration configuration;
    @Inject
    private Logger logger;
    @Inject
    private ChangedProjects changedProjects;
    @Inject
    private ImpactedProjects impactedProjects;
    @Inject
    private RebuildProjects rebuildProjects;
    @Inject
    private MavenSession mavenSession;

    public void act() throws GitAPIException, IOException {

        final Set<MavenProject> changed = changedProjects.get();
        if (!changed.isEmpty()) {
            printDelimiter();
            logProjects(changed, "Changed Projects:");
        }

        final Set<MavenProject> ignored = configuration.ignoredProjects;
        if (!ignored.isEmpty()) {
            printDelimiter();
            logProjects(ignored, "Excluded Projects:");
        }

        final List<MavenProject> impactedChanges = impactedProjects.get(changed);
        writeChangedProjects(impactedChanges);
        rebuildProjects.setUpSession(impactedChanges);
    }

    private void writeChangedProjects(Collection<MavenProject> sortedChanged) {
        mavenSession.getProjects().forEach(m -> m.getProperties()
                        .setProperty(CHANGED_PROJECTS,
                                        joinProjectIds(sortedChanged, new StringJoiner(",")).toString()));

        if (configuration.writeChanged) {
            Path defaultPath = Modules.getPath(mavenSession.getTopLevelProject()).resolve(CHANGED_PROJECTS);
            Path outputFilePath = configuration.outputFile.orElse(defaultPath);
            writeChangedProjectsToFile(sortedChanged, outputFilePath.toFile());
        }
    }

    private void logProjects(Collection<MavenProject> projects, String title) {
        logger.info(title);
        logger.info("");
        projects.stream().map(MavenProject::getName).forEach(logger::info);
        logger.info("");
    }

    private void printDelimiter() {
        logger.info("------------------------------------------------------------------------");
    }

}
