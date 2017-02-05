package com.lesfurets.maven.partial.mocks;

import java.io.*;
import java.net.URISyntaxException;

import org.codehaus.plexus.util.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.*;

public class LocalRepoMock extends RepoMock {

    private static final File REPO = RepoTest.LOCAL_DIR.toFile();
    private final Git git;
    private RemoteRepoMock remoteRepo;

    public LocalRepoMock(boolean remote) throws Exception {
        try {
            delete(REPO);
        } catch (Exception ignored) {
        }
        boolean mkdirs = REPO.mkdirs();
        if (!mkdirs) {
            throw new Exception("Cannot create directory for git repository : " + REPO.toString());
        }
        InputStream zip = LocalRepoMock.class.getResourceAsStream(RepoTest.TEMPLATE_ZIP);
        new UnZiper().act(zip, REPO);
        git = new Git(new FileRepository(new File(REPO + "/.git")));
        if (remote) {
            remoteRepo = new RemoteRepoMock(false);
            configureRemote(remoteRepo.repoUrl);
            git.fetch().call();
        }
    }

    private void resetGitRepo(Git git) throws GitAPIException, IOException {
        File gitignore = new File(REPO, ".gitignore");
        gitignore.createNewFile();
        git.commit().setAll(true).setMessage("git ignore").call();
        git.branchCreate().setName("develop").call();
        git.branchCreate().setName("features/1").call();
        git.branchCreate().setName("features/2").call();
        File projects = new File(this.getClass().getResource("/projects").getFile());
        FileUtils.copyDirectoryStructure(projects, REPO);
        RevCommit struct = git.commit().setAll(true).setMessage("project structure").call();

        Ref master = git.getRepository().findRef("master");
        git.checkout().setName("develop").call();
        git.reset().setRef(master.getName()).call();
        git.checkout().setName("features/2").call();
        git.reset().setRef(master.getName()).call();
        git.checkout().setName("features/1").call();
        git.reset().setRef(master.getName()).call();

        File file2 = REPO.toPath().resolve("child2/subChild1/src/resources/file2").toFile();
        FileUtils.fileWrite(file2, "changed File");
        File file1 = REPO.toPath().resolve("child3/src/resources/file1").toFile();
        FileUtils.fileWrite(file1, "changed line");
        File child4 = REPO.toPath().resolve("child4/pom.xml").toFile();
        FileUtils.fileAppend(child4.getName(), "<!-- -->");
        git.commit().setAll(true).setMessage("feature1 changes").call();

    }

    public void configureRemote(String repoUrl) throws URISyntaxException, IOException, GitAPIException {
        StoredConfig config = git.getRepository().getConfig();
        config.clear();
        config.setString("remote", "origin", "fetch", "+refs/heads/*:refs/remotes/origin/*");
        config.setString("remote", "origin", "push", "+refs/heads/*:refs/remotes/origin/*");
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
    public File getRepoDir() {
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
