package com.vackosar.gitflowincrementalbuild.boundary;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import com.google.inject.Guice;
import com.google.inject.Injector;

@Component(role = AbstractMavenLifecycleParticipant.class)
public class MavenLifecycleParticipant extends AbstractMavenLifecycleParticipant {

    @Requirement
    private Logger logger;

    @Override
    public void afterProjectsRead(MavenSession session) throws MavenExecutionException {

        final Injector injector = Guice.createInjector(new GuiceModule(logger, session));
        final Configuration configuration = injector.getInstance(Configuration.class);

        logger.info(configuration.toString());

        try {
            if (configuration.enabled()) {
                logger.info("gitflow-incremental-builder starting...");
                injector.getInstance(UnchangedProjectsRemover.class).act();
                logger.info("gitflow-incremental-builder exiting...");
            } else {
                logger.info("gitflow-incremental-builder is disabled.");
            }
        } catch (Exception e) {
            throw new MavenExecutionException("Exception during gitflow-incremental-builder execution occured.", e);
        }
    }

}
