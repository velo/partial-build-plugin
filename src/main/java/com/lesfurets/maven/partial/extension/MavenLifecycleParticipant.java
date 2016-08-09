package com.lesfurets.maven.partial.extension;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.lesfurets.maven.partial.core.*;

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
                logger.info("Starting Partial build...");
                injector.getInstance(UnchangedProjectsRemover.class).act();
            } else {
                logger.info("Partial build disabled...");
            }
        } catch (Exception e) {
            throw new MavenExecutionException("Exception during Partial Build execution: " + e.getMessage(), e);
        }
    }

}
