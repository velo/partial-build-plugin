package com.lesfurets.maven.partial.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.codehaus.plexus.logging.Logger;

import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DifferentFilesNative implements DifferentFiles {

    private static final String HEAD = "HEAD";
    private static final String REFS_REMOTES = "refs/remotes/";
    private static final String REFS_HEADS = "refs/heads/";
    private static long PROCESS_TIMEOUT = 10000;
    private Path parentPath;
    @Inject
    private Configuration configuration;
    @Inject
    private Logger logger;

    public Set<Path> get() throws IOException {
        fetch();
        checkout();
        String base = getBranchHead(configuration.baseBranch);
        final Set<Path> paths;
        String reference = resolveReference(base);
        final Path gitDir = getParentPath();
        paths = getDiff(base, reference, gitDir);
        if (configuration.uncommited) {
            paths.addAll(getUncommitedChanges(gitDir));
        }
        if (configuration.untracked) {
            paths.addAll(getUntrackedChanges(gitDir));
        }
        return paths;
    }

    private Path getParentPath() {
        if (parentPath == null) {
            File gitPath = new File(configuration.rootDirectory);
            if (gitPath.exists() && gitPath.isDirectory()) {
                while (gitPath.listFiles(pathname -> ".git".equals(pathname.getName())).length < 1) {
                    gitPath = gitPath.getParentFile();
                }
            }
            parentPath = gitPath.toPath();
        }
        return parentPath;
    }

    private void checkout() throws IOException {
        Process exec = executeGitCommand("symbolic-ref", HEAD);
        String fullBranch = checkOutput(exec);
        if (!HEAD.equals(configuration.baseBranch)
                && !fullBranch.equals(configuration.baseBranch)) {
            logger.info("Checking out base branch " + configuration.baseBranch + "...");
            Process checkout = executeGitCommand("checkout", configuration.baseBranch);
            checkOutput(checkout);
        }
    }

    private void fetch() throws IOException {
        if (configuration.fetchReferenceBranch) {
            fetch(configuration.referenceBranch);
        }
        if (configuration.fetchBaseBranch) {
            fetch(configuration.baseBranch);
        }
    }

    private void fetch(String branchName) throws IOException {
        logger.info("Fetching branch " + branchName);
        if (!branchName.startsWith(REFS_REMOTES)) {
            throw new IllegalArgumentException("Branch name '" + branchName + "' is not tracking branch name since it" +
                    " does not start " + REFS_REMOTES);
        }
        String remoteName = extractRemoteName(branchName);
        String shortName = extractShortName(remoteName, branchName);
        Process fetch = executeGitCommand("fetch", remoteName, REFS_HEADS + shortName + ":" + branchName);
        checkOutput(fetch);
    }

    private String extractRemoteName(String branchName) {
        return branchName.split("/")[2];
    }

    private String extractShortName(String remoteName, String branchName) {
        return branchName.replaceFirst(REFS_REMOTES + remoteName + "/", "");
    }

    private String getMergeBase(String baseCommit, String referenceHeadCommit) throws IOException {
        Process mergeBase = executeGitCommand("merge-base", baseCommit, referenceHeadCommit);
        String commit = checkOutput(mergeBase);
        logger.info("Using merge base of id: " + commit);
        return commit;
    }

    private Set<Path> getDiff(String base, String reference, Path gitDir) throws IOException {
        Process diff = executeGitCommand("diff", "--name-only", base, reference, gitDir.toString());
        String diffFiles = checkOutput(diff);
        return Stream.of(diffFiles.split("\n"))
                .map(File::new)
                .map(File::toPath)
                .map(gitDir::resolve)
                .map(Path::toAbsolutePath)
                .collect(Collectors.toSet());
    }

    private String getBranchHead(String branchName) throws IOException {
        Process revParse = executeGitCommand("rev-parse", branchName);
        String resolvedId = checkOutput(revParse);
        if (resolvedId == null) {
            throw new IllegalArgumentException("Git rev str '" + branchName + "' not found.");
        }
        logger.info("Head of branch " + branchName + " is commit of id: " + resolvedId);
        return resolvedId;
    }

    private Set<Path> getUncommitedChanges(Path gitDir) throws IOException {
        Process uncommitted = executeGitCommand("diff", "--name-only", gitDir.toString());
        String uncommittedFiles = checkOutput(uncommitted);
        return Stream.of(uncommittedFiles.split("\n"))
                .map(File::new)
                .map(File::toPath)
                .map(gitDir::resolve)
                .map(Path::toAbsolutePath)
                .collect(Collectors.toSet());
    }

    private Set<Path> getUntrackedChanges(Path gitDir) throws IOException {
        Process untracked = executeGitCommand("ls-files", "--others", "--exclude-standard",
                gitDir.toString());
        String uncommittedFiles = checkOutput(untracked);
        return Stream.of(uncommittedFiles.split("\n"))
                .map(File::new)
                .map(File::toPath)
                .map(gitDir::resolve)
                .map(Path::toAbsolutePath)
                .collect(Collectors.toSet());
    }

    private String resolveReference(String base) throws IOException {
        String refHead = getBranchHead(configuration.referenceBranch);
        if (configuration.compareToMergeBase) {
            return getMergeBase(base, refHead);
        } else {
            return refHead;
        }
    }

    private String getOutput(Process exec) throws IOException {
        return CharStreams.toString(new InputStreamReader(exec.getInputStream(), "UTF-8")).trim();
    }

    private String checkOutput(Process exec) throws IOException {
        String output = getOutput(exec);
        if (exec.exitValue() != 0) {
            logger.error("Executing " + output);
            throw new IOException("Process exited with " + exec.exitValue() + " : " + output);
        }
        return output;
    }

    private Process executeCommand(String... commands) throws IOException {
        return executeCommandOn(Arrays.asList(commands), getParentPath());
    }

    private Process executeGitCommand(String... commands) throws IOException {
        Stream<String> git = Stream.of("git");
        Stream<String> c = Stream.of(commands);
        return executeCommandOn(Stream.concat(git, c).collect(Collectors.toList()), getParentPath());
    }

    private Process executeCommandOn(List<String> commands, Path executionPath) throws IOException {
        Process exec =
                new ProcessBuilder(commands)
                        .directory(executionPath.toFile())
                        .redirectErrorStream(true)
                        .start();
        try {
            exec.waitFor(PROCESS_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new IOException(e.getMessage());
        }
        return exec;
    }
}
