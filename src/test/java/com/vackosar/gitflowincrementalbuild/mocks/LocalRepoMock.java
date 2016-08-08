package com.vackosar.gitflowincrementalbuild.mocks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.codehaus.plexus.util.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;

public class LocalRepoMock extends RepoMock {

    private static final File REPO = RepoTest.LOCAL_DIR.toFile();
    private final Git git;
    private RemoteRepoMock remoteRepo;

    public LocalRepoMock(boolean remote) throws IOException, URISyntaxException, GitAPIException {
        try {delete(REPO);} catch (Exception e) {}
        InputStream zip = LocalRepoMock.class.getResourceAsStream(RepoTest.TEMPLATE_ZIP);
        new UnZiper().act(zip, REPO);
        git = new Git(new FileRepository(new File(RepoTest.LOCAL_DIR + "/.git")));
        if (remote) {
            remoteRepo = new RemoteRepoMock(false);
            configureRemote(remoteRepo.repoUrl);
            git.fetch().call();
        }
    }

    public void configureRemote(String repoUrl) throws URISyntaxException, IOException, GitAPIException {
        StoredConfig config = git.getRepository().getConfig();
        config.clear();
        config.setString("remote", "origin" ,"fetch", "+refs/heads/*:refs/remotes/origin/*");
        config.setString("remote", "origin" ,"push", "+refs/heads/*:refs/remotes/origin/*");
        config.setString("branch", "master", "remote", "origin");
        config.setString("baseBranch", "master", "merge", "refs/heads/master");
        config.setString("push", null, "default", "current");
        RemoteConfig remoteConfig = new RemoteConfig(config, "origin");
        URIish uri = new URIish(repoUrl);
        remoteConfig.addURI(uri);
        remoteConfig.addFetchRefSpec(new RefSpec("refs/heads/master:refs/heads/master"));
        remoteConfig.addPushRefSpec(new RefSpec("refs/heads/master:refs/heads/master"));
        remoteConfig.update(config);
        config.save();
        git.fetch().call();
    }

    public RemoteRepoMock getRemoteRepo() {
        return remoteRepo;
    }

    @Override
    protected File getRepoDir() {
        return REPO;
    }


    public Git getGit() {
        return git;
    }

    public void close() throws Exception {
        if (remoteRepo != null) {
            remoteRepo.close();
        }
        super.close();
    }

}
