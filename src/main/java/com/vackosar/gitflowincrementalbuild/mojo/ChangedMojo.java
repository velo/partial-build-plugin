/*
 * Copyright (C) by Courtanet, All Rights Reserved.
 */
package com.vackosar.gitflowincrementalbuild.mojo;

import static com.vackosar.gitflowincrementalbuild.boundary.UnchangedProjectsRemover.CHANGED_PROJECTS;
import static com.vackosar.gitflowincrementalbuild.control.Property.PREFIX;
import static com.vackosar.gitflowincrementalbuild.utils.PluginUtils.joinProjectIds;
import static com.vackosar.gitflowincrementalbuild.utils.PluginUtils.writeChangedProjectsToFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.errors.GitAPIException;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.vackosar.gitflowincrementalbuild.boundary.Configuration;
import com.vackosar.gitflowincrementalbuild.boundary.GuiceModule;
import com.vackosar.gitflowincrementalbuild.boundary.UnchangedProjectsRemover;
import com.vackosar.gitflowincrementalbuild.control.ChangedProjects;
import com.vackosar.gitflowincrementalbuild.utils.MavenToPlexusLogAdapter;

@Mojo(name = "writeChanged", defaultPhase = LifecyclePhase.VALIDATE,
        threadSafe = true, inheritByDefault = false, aggregator = true)
public class ChangedMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}")
    private MavenProject project;

    @Parameter(defaultValue = "${session}")
    private MavenSession session;

    @Parameter(required = false, property = PREFIX + "enabled", defaultValue = "true")
    public boolean enabled;

    @Parameter(required = false, property = PREFIX + "key", defaultValue = "")
    public String key;

    @Parameter(required = false, property = PREFIX + "referenceBranch", defaultValue = "refs/remotes/origin/develop")
    public String referenceBranch;

    @Parameter(required = false, property = PREFIX + "baseBranch", defaultValue = "HEAD")
    public String baseBranch;

    @Parameter(required = false, property = PREFIX + "uncommited", defaultValue = "true")
    public boolean uncommited;

    @Parameter(required = false, property = PREFIX + "makeUpstream", defaultValue = "false")
    public boolean makeUpstream;

    @Parameter(required = false, property = PREFIX + "skipTestsForNotImpactedModules", defaultValue = "false")
    public boolean skipTestsForNotImpactedModules;

    @Parameter(required = false, property = PREFIX + "buildAll", defaultValue = "false")
    public boolean buildAll;

    @Parameter(required = false, property = PREFIX + "compareToMergeBase", defaultValue = "true")
    public boolean compareToMergeBase;

    @Parameter(required = false, property = PREFIX + "fetchBaseBranch", defaultValue = "false")
    public boolean fetchBaseBranch;

    @Parameter(required = false, property = PREFIX + "fetchReferenceBranch", defaultValue = "false")
    public boolean fetchReferenceBranch;

    @Parameter(required = false, property = PREFIX + "outputFile", defaultValue = "${project.basedir}/changed.projects")
    public String outputFile;

    @Parameter(required = false, property = PREFIX + "writeChanged", defaultValue = "false")
    public String writeChanged;

    @Parameter(required = false, property = PREFIX + "ignoreChanged")
    public String ignoreChanged;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        if (!project.isExecutionRoot()) {
            return;
        }

        Injector injector = Guice.createInjector(new GuiceModule(new MavenToPlexusLogAdapter(getLog()), session));
        UnchangedProjectsRemover projectsRemover = injector.getInstance(UnchangedProjectsRemover.class);
        ChangedProjects changedProjects = injector.getInstance(ChangedProjects.class);
        Configuration configuration = injector.getInstance(Configuration.class);

        getLog().info(configuration.toString());

        try {
            Set<MavenProject> changed = changedProjects.get();
            Set<MavenProject> allDependentProjects = projectsRemover.getAllDependentProjects(changed);

            final List<MavenProject> sortedChanged = session.getProjects().stream()
                    .filter(allDependentProjects::contains)
                    .collect(Collectors.toList());

            writeChangedProjectsToFile(sortedChanged, new File(outputFile));
            session.getProjects().forEach(m -> m.getProperties()
                    .setProperty(CHANGED_PROJECTS, joinProjectIds(sortedChanged, new StringJoiner(",")).toString()));
        } catch (GitAPIException | IOException e) {
            e.printStackTrace();
        }

    }

}
