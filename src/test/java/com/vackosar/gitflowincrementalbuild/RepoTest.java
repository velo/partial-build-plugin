package com.vackosar.gitflowincrementalbuild;

import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.net.URISyntaxException;

public class RepoTest {

    private RepoMock repoMock;

    @Before
    public void before() throws IOException, URISyntaxException {
        repoMock = new RepoMock();
        System.setProperty("user.dir", RepoMock.WORK_DIR);
    }

    @After
    public void after() throws Exception {
        repoMock.close();
    }
}
