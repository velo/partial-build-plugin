package com.lesfurets.maven.partial.mocks;

import java.io.File;
import java.io.IOException;

import org.codehaus.plexus.util.FileUtils;
import org.eclipse.jgit.api.Git;

public abstract class RepoMock implements AutoCloseable {

    public abstract File getRepoDir();

    public abstract Git getGit();

    @Override
    public void close() throws Exception {
        getGit().getRepository().close();
        getGit().close();
        delete(getRepoDir());
    }

    protected void delete(File f) {
        try {
            if (f.isDirectory()) {
                FileUtils.deleteDirectory(f);
            }
            f.delete();
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + f);
        }
    }

}
