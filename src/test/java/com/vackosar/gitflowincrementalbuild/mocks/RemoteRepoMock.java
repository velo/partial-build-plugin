package com.vackosar.gitflowincrementalbuild.mocks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.transport.Daemon;

public class RemoteRepoMock extends RepoMock {

    private static final File REPO = RepoTest.REMOTE_DIR.toFile();
    private final Git git;
    private static int port = 9418;
    public String repoUrl = null;
    private boolean bare;
    private Daemon server;
    private RepoResolver resolver;

    public RemoteRepoMock(boolean bare) throws IOException {
        this.bare = bare;
        InputStream zipStream = RemoteRepoMock.class.getResourceAsStream(RepoTest.TEMPLATE_ZIP);
        if (bare) {
            try {delete(REPO);} catch (Exception e) {}
            REPO.mkdir();
        } else {
            new UnZiper().act(zipStream, REPO);
        }
        repoUrl = "git://localhost:" + port + "/repo.git";
        start();
        port++;
        git = new Git(new FileRepository(new File(REPO + "/.git")));
    }

    private void start() {
        try {
            server = new Daemon(new InetSocketAddress(port));
            server.getService("git-receive-pack").setEnabled(true);
            resolver = new RepoResolver(REPO, bare);
            server.setRepositoryResolver(resolver);
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected File getRepoDir() {
        return REPO;
    }

    public Git getGit() {
        return git;
    }

    @Override
    public void close() throws Exception {
        server.stop();
        resolver.close();
        super.close();
    }
}
