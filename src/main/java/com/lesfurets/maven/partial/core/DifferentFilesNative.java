package com.lesfurets.maven.partial.core;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.of;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.codehaus.plexus.logging.Logger;

import com.google.common.io.CharStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DifferentFilesNative implements DifferentFiles {

    private static final String HEAD = "HEAD";
    private static final String REFS_REMOTES = "refs/remotes/";
    private static final String REFS_HEADS = "refs/heads/";
    private Path parentPath;
    @Inject
    private Configuration configuration;
    @Inject
    private Logger logger;
    private NativeProcessRunner runner;

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
        String fullBranch = runGitCommand("symbolic-ref", HEAD);
        if (!HEAD.equals(configuration.baseBranch) && !fullBranch.equals(configuration.baseBranch)) {
            logger.info("Checking out base branch " + configuration.baseBranch + "...");
            runGitCommand("checkout", configuration.baseBranch);
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
        runGitCommand("fetch", remoteName, REFS_HEADS + shortName + ":" + branchName);
    }

    private String extractRemoteName(String branchName) {
        return branchName.split("/")[2];
    }

    private String extractShortName(String remoteName, String branchName) {
        return branchName.replaceFirst(REFS_REMOTES + remoteName + "/", "");
    }

    private String getMergeBase(String baseCommit, String referenceHeadCommit) throws IOException {
        String commit = runGitCommand("merge-base", baseCommit, referenceHeadCommit);
        logger.info("Using merge base of id: " + commit);
        return commit;
    }

    private Set<Path> getDiff(String base, String reference, Path gitDir) throws IOException {
        String diffFiles = runGitCommand("diff", "--name-only", base, reference, gitDir.toString());
        return of(diffFiles.split("\n"))
                        .map(File::new)
                        .map(File::toPath)
                        .map(gitDir::resolve)
                        .map(Path::toAbsolutePath)
                        .collect(Collectors.toSet());
    }

    private String getBranchHead(String branchName) throws IOException {
        String resolvedId = runGitCommand("rev-parse", branchName);
        if (resolvedId == null) {
            throw new IllegalArgumentException("Git rev str '" + branchName + "' not found.");
        }
        logger.info("Head of branch " + branchName + " is commit of id: " + resolvedId);
        return resolvedId;
    }

    private Set<Path> getUncommitedChanges(Path gitDir) throws IOException {
        String uncommittedFiles = runGitCommand("diff", "--cached", "--name-only", "--diff-filter=ACDMRTU",
                        gitDir.toString());
        return of(uncommittedFiles.split("\n"))
                        .map(File::new)
                        .map(File::toPath)
                        .map(gitDir::resolve)
                        .map(Path::toAbsolutePath)
                        .collect(Collectors.toSet());
    }

    private Set<Path> getUntrackedChanges(Path gitDir) throws IOException {
        String uncommittedFiles = runGitCommand("ls-files", "--others", "--exclude-standard",
                        gitDir.toString());
        return of(uncommittedFiles.split("\n"))
                        .map(File::new)
                        .map(File::toPath)
                        .map(gitDir::resolve)
                        .map(Path::toAbsolutePath)
                        .collect(Collectors.toSet());
    }

    private String resolveReference(String base) throws IOException {
        final String refHead = getBranchHead(configuration.referenceBranch);
        if (configuration.compareToMergeBase) {
            return getMergeBase(base, refHead);
        } else {
            return refHead;
        }
    }

    private String runGitCommand(String... commands) throws IOException {
        final String gitPath = System.getenv("GIT_PATH");
        final String gitExec = (gitPath == null) ? "git" : gitPath;
        return getRunner().run(concat(of(gitExec), of(commands)).collect(toList()), getParentPath());
    }

    private NativeProcessRunner getRunner() {
        if (runner == null) {
            runner = new NativeProcessRunner();
        }
        return runner;
    }

    private class NativeProcessRunner {

        String run(List<String> commands, Path executionPath) throws IOException {
            final Process exec = new ProcessBuilder(commands)
                            .directory(executionPath.toFile())
                            .redirectErrorStream(true)
                            .start();
            try {
                exec.waitFor();
            } catch (InterruptedException e) {
                throw new IOException(e.getMessage(), e);
            }

            return checkOutput(exec);
        }

        private String checkOutput(Process exec) throws IOException {
            final String output = getOutput(exec);
            if (exec.exitValue() != 0) {
                throw new IOException("Process exited with " + exec.exitValue() + " : " + output);
            }
            return output;
        }

        private String getOutput(Process exec) throws IOException {
            return CharStreams.toString(new InputStreamReader(exec.getInputStream(), "UTF-8")).trim();
        }
    }

}
