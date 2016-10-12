package com.lesfurets.maven.partial.mocks;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.codehaus.plexus.util.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

public abstract class RepoMock implements AutoCloseable {

    public static final String TEST_REPO_RESOURCE_PATH = "/repo";

    protected abstract File getRepoDir();

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

    public static void copyMockRepoTo(File targetDir) throws URISyntaxException, IOException {
        URL repo = LocalRepoMock.class.getResource(TEST_REPO_RESOURCE_PATH);
        File repoDir = new File(repo.toURI());
        FileUtils.copyDirectoryStructure(repoDir, targetDir);
    }

    public static Repository initRepositoryIn(File repositoryPath) throws IOException {
        FileRepositoryBuilder fileRepositoryBuilder = new FileRepositoryBuilder();
        fileRepositoryBuilder.findGitDir(repositoryPath);
        fileRepositoryBuilder.setWorkTree(repositoryPath);
        return fileRepositoryBuilder.build();
    }

}
