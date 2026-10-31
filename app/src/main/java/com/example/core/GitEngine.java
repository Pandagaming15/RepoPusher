package com.example.core;

import android.content.Context;
import android.util.Log;

import com.example.core.models.GitCommitInfo;
import com.example.core.models.GitFileStatus;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class GitEngine {
    private static final String TAG = "GitEngine";

    public GitEngine(Context context) {
        try {
            // Android environment configuration for JGit
            File filesDir = context.getFilesDir();
            System.setProperty("user.home", filesDir.getAbsolutePath());
            File configDir = new File(filesDir, ".config");
            if (!configDir.exists()) configDir.mkdirs();
            File gitConfig = new File(configDir, "gitconfig");
            System.setProperty("jgit.config", gitConfig.getAbsolutePath());
        } catch (Exception e) {
            Log.w(TAG, "Configuring JGit environment: " + e.getMessage());
        }
    }

    private CredentialsProvider createCredentialsProvider(String token) {
        if (token != null && !token.trim().isEmpty()) {
            // GitHub accepts personal access token / OAuth token with username "x-access-token"
            return new UsernamePasswordCredentialsProvider("x-access-token", token.trim());
        }
        return CredentialsProvider.getDefault();
    }

    public boolean isGitRepository(File directory) {
        if (directory == null || !directory.exists()) return false;
        File gitDir = new File(directory, ".git");
        return gitDir.exists() && gitDir.isDirectory();
    }

    public synchronized void initRepository(File directory) throws Exception {
        if (directory == null) throw new IllegalArgumentException("Directory cannot be null");
        if (!directory.exists()) directory.mkdirs();

        try (Git git = Git.init().setDirectory(directory).call()) {
            StoredConfig config = git.getRepository().getConfig();
            config.setString("core", null, "autocrlf", "input");
            config.save();
            Log.i(TAG, "Initialized Git repository at: " + directory.getAbsolutePath());
        }
    }

    public synchronized List<GitFileStatus> getStatus(File directory) throws Exception {
        if (!isGitRepository(directory)) {
            return Collections.emptyList();
        }

        List<GitFileStatus> fileStatuses = new ArrayList<>();
        try (Git git = Git.open(directory)) {
            Status status = git.status().call();

            // Untracked files
            Set<String> untracked = status.getUntracked();
            for (String path : untracked) {
                File f = new File(directory, path);
                fileStatuses.add(new GitFileStatus(path, GitFileStatus.ChangeType.UNTRACKED, false, f.length()));
            }

            // Modified files
            Set<String> modified = status.getModified();
            for (String path : modified) {
                File f = new File(directory, path);
                fileStatuses.add(new GitFileStatus(path, GitFileStatus.ChangeType.MODIFIED, false, f.length()));
            }

            // Changed / Staged modifications
            Set<String> changed = status.getChanged();
            for (String path : changed) {
                File f = new File(directory, path);
                fileStatuses.add(new GitFileStatus(path, GitFileStatus.ChangeType.MODIFIED, true, f.length()));
            }

            // Added (staged new files)
            Set<String> added = status.getAdded();
            for (String path : added) {
                File f = new File(directory, path);
                fileStatuses.add(new GitFileStatus(path, GitFileStatus.ChangeType.ADDED, true, f.length()));
            }

            // Removed / Missing
            Set<String> missing = status.getMissing();
            for (String path : missing) {
                fileStatuses.add(new GitFileStatus(path, GitFileStatus.ChangeType.MISSING, false, 0));
            }

            Set<String> removed = status.getRemoved();
            for (String path : removed) {
                fileStatuses.add(new GitFileStatus(path, GitFileStatus.ChangeType.DELETED, true, 0));
            }

            // Conflicting files
            Set<String> conflicting = status.getConflicting();
            for (String path : conflicting) {
                File f = new File(directory, path);
                fileStatuses.add(new GitFileStatus(path, GitFileStatus.ChangeType.CONFLICT, false, f.length()));
            }
        }
        return fileStatuses;
    }

    public synchronized void stageFile(File directory, String filePattern) throws Exception {
        try (Git git = Git.open(directory)) {
            git.add().addFilepattern(filePattern).call();
        }
    }

    public synchronized void stageAll(File directory) throws Exception {
        try (Git git = Git.open(directory)) {
            // Add new and modified files
            git.add().addFilepattern(".").call();
            // Also stage deleted files
            try {
                git.add().setUpdate(true).addFilepattern(".").call();
            } catch (Exception ignored) {}
        }
    }

    public synchronized void unstageFile(File directory, String filePattern) throws Exception {
        try (Git git = Git.open(directory)) {
            git.reset().addPath(filePattern).call();
        }
    }

    public synchronized void unstageAll(File directory) throws Exception {
        try (Git git = Git.open(directory)) {
            git.reset().call();
        }
    }

    public synchronized GitCommitInfo commit(File directory, String message, String authorName, String authorEmail) throws Exception {
        try (Git git = Git.open(directory)) {
            if (message == null || message.trim().isEmpty()) {
                message = "Update project files";
            }
            if (authorName == null || authorName.trim().isEmpty()) authorName = "RepoStudio User";
            if (authorEmail == null || authorEmail.trim().isEmpty()) authorEmail = "user@repostudio.local";

            RevCommit revCommit = git.commit()
                    .setMessage(message)
                    .setAuthor(authorName, authorEmail)
                    .setCommitter(authorName, authorEmail)
                    .call();

            String fullId = revCommit.getId().getName();
            String shortId = fullId.length() >= 7 ? fullId.substring(0, 7) : fullId;
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            String dateStr = sdf.format(new Date(revCommit.getCommitTime() * 1000L));

            return new GitCommitInfo(
                    fullId,
                    shortId,
                    revCommit.getShortMessage(),
                    revCommit.getAuthorIdent().getName(),
                    revCommit.getAuthorIdent().getEmailAddress(),
                    revCommit.getCommitTime() * 1000L,
                    dateStr
            );
        }
    }

    public synchronized List<GitCommitInfo> getCommitHistory(File directory, int maxCount) throws Exception {
        if (!isGitRepository(directory)) return Collections.emptyList();

        List<GitCommitInfo> history = new ArrayList<>();
        try (Git git = Git.open(directory)) {
            Repository repo = git.getRepository();
            Ref head = repo.exactRef(Constants.HEAD);
            if (head == null || head.getObjectId() == null) {
                // Empty repository with no commits yet
                return Collections.emptyList();
            }

            Iterable<RevCommit> commits = git.log().setMaxCount(maxCount).call();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

            for (RevCommit commit : commits) {
                String fullId = commit.getId().getName();
                String shortId = fullId.length() >= 7 ? fullId.substring(0, 7) : fullId;
                PersonIdent author = commit.getAuthorIdent();
                String dateStr = sdf.format(new Date(commit.getCommitTime() * 1000L));

                GitCommitInfo info = new GitCommitInfo(
                        fullId,
                        shortId,
                        commit.getFullMessage(),
                        author != null ? author.getName() : "Unknown",
                        author != null ? author.getEmailAddress() : "",
                        commit.getCommitTime() * 1000L,
                        dateStr
                );
                history.add(info);
            }
        }
        return history;
    }

    public synchronized String getCurrentBranch(File directory) {
        if (!isGitRepository(directory)) return "main";
        try (Git git = Git.open(directory)) {
            String branch = git.getRepository().getBranch();
            return branch != null ? branch : "main";
        } catch (Exception e) {
            return "main";
        }
    }

    public synchronized List<String> getBranches(File directory) {
        if (!isGitRepository(directory)) return Collections.singletonList("main");
        List<String> list = new ArrayList<>();
        try (Git git = Git.open(directory)) {
            List<Ref> refs = git.branchList().call();
            for (Ref ref : refs) {
                String name = ref.getName();
                if (name.startsWith(Constants.R_HEADS)) {
                    name = name.substring(Constants.R_HEADS.length());
                }
                list.add(name);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error listing branches: " + e.getMessage());
        }
        if (list.isEmpty()) list.add("main");
        return list;
    }

    public synchronized void createBranch(File directory, String branchName) throws Exception {
        try (Git git = Git.open(directory)) {
            git.branchCreate().setName(branchName).call();
        }
    }

    public synchronized void checkoutBranch(File directory, String branchName) throws Exception {
        try (Git git = Git.open(directory)) {
            git.checkout().setName(branchName).call();
        }
    }

    public synchronized void renameBranch(File directory, String oldName, String newName) throws Exception {
        try (Git git = Git.open(directory)) {
            git.branchRename().setOldName(oldName).setNewName(newName).call();
        }
    }

    public synchronized void setRemoteUrl(File directory, String remoteName, String url) throws Exception {
        try (Git git = Git.open(directory)) {
            StoredConfig config = git.getRepository().getConfig();
            config.setString("remote", remoteName, "url", url);
            config.setString("remote", remoteName, "fetch", "+refs/heads/*:refs/remotes/" + remoteName + "/*");
            config.save();
        }
    }

    public synchronized String getRemoteUrl(File directory, String remoteName) {
        if (!isGitRepository(directory)) return "";
        try (Git git = Git.open(directory)) {
            StoredConfig config = git.getRepository().getConfig();
            String url = config.getString("remote", remoteName, "url");
            return url != null ? url : "";
        } catch (Exception e) {
            return "";
        }
    }

    public synchronized String push(File directory, String remoteName, String branchName, String token) throws Exception {
        try (Git git = Git.open(directory)) {
            PushCommand push = git.push()
                    .setRemote(remoteName)
                    .setCredentialsProvider(createCredentialsProvider(token));

            if (branchName != null && !branchName.isEmpty()) {
                push.setRefSpecs(new org.eclipse.jgit.transport.RefSpec("refs/heads/" + branchName + ":refs/heads/" + branchName));
            }

            Iterable<PushResult> results;
            try {
                results = push.call();
            } catch (Exception ex) {
                String msg = ex.getMessage() != null ? ex.getMessage() : "";
                if (msg.contains("not authorized") || msg.contains("401") || msg.contains("Authentication")) {
                    throw new Exception("GitHub push failed: Authentication error. Please verify your token has 'repo' write permissions in Settings.");
                } else if (msg.contains("not found") || msg.contains("404")) {
                    throw new Exception("GitHub push failed: Remote repository not found. Verify repository exists on GitHub and that your token has access.");
                }
                throw ex;
            }

            StringBuilder sb = new StringBuilder();
            boolean hasSuccess = false;

            for (PushResult result : results) {
                for (RemoteRefUpdate update : result.getRemoteUpdates()) {
                    RemoteRefUpdate.Status s = update.getStatus();
                    sb.append(update.getRemoteName()).append(": ").append(s.name()).append("\n");
                    if (s == RemoteRefUpdate.Status.OK || s == RemoteRefUpdate.Status.UP_TO_DATE) {
                        hasSuccess = true;
                    } else if (s == RemoteRefUpdate.Status.REJECTED_NONFASTFORWARD) {
                        throw new Exception("Push rejected: Non-fast-forward. Remote contains work that you do not have locally. Please pull first.");
                    } else if (s == RemoteRefUpdate.Status.REJECTED_NODELETE || s == RemoteRefUpdate.Status.REJECTED_OTHER_REASON) {
                        throw new Exception("Push rejected: " + update.getMessage());
                    }
                }
            }

            if (sb.length() == 0) {
                return "Push completed successfully.";
            }
            return sb.toString().trim();
        }
    }

    public synchronized String pull(File directory, String remoteName, String branchName, String token) throws Exception {
        try (Git git = Git.open(directory)) {
            PullResult pullResult;
            try {
                pullResult = git.pull()
                        .setRemote(remoteName)
                        .setRemoteBranchName(branchName)
                        .setCredentialsProvider(createCredentialsProvider(token))
                        .call();
            } catch (Exception ex) {
                String msg = ex.getMessage() != null ? ex.getMessage() : "";
                if (msg.contains("not authorized") || msg.contains("401")) {
                    throw new Exception("GitHub pull failed: Authentication error. Please verify your token in Settings.");
                }
                throw ex;
            }

            if (!pullResult.isSuccessful()) {
                if (pullResult.getMergeResult() != null && pullResult.getMergeResult().getConflicts() != null) {
                    throw new Exception("Pull resulted in merge conflicts. Resolve conflicts before committing.");
                }
                throw new Exception("Pull did not complete successfully.");
            }
            return "Pulled changes successfully: " + pullResult.getMergeResult().getMergeStatus().name();
        }
    }

    public synchronized String fetch(File directory, String remoteName, String token) throws Exception {
        try (Git git = Git.open(directory)) {
            git.fetch()
                    .setRemote(remoteName)
                    .setCredentialsProvider(createCredentialsProvider(token))
                    .call();
            return "Fetch completed successfully from " + remoteName;
        }
    }

    public synchronized void cloneRepository(String cloneUrl, File targetDir, String token) throws Exception {
        if (targetDir.exists() && targetDir.listFiles() != null && targetDir.listFiles().length > 0) {
            throw new Exception("Target directory is not empty: " + targetDir.getName());
        }

        CloneCommand cloneCommand = Git.cloneRepository()
                .setURI(cloneUrl)
                .setDirectory(targetDir)
                .setCredentialsProvider(createCredentialsProvider(token));

        try (Git git = cloneCommand.call()) {
            Log.i(TAG, "Cloned repository successfully into: " + targetDir.getAbsolutePath());
        }
    }

    public synchronized String getFileDiff(File directory, String relativePath) {
        if (!isGitRepository(directory)) return "";
        try (Git git = Git.open(directory)) {
            Repository repo = git.getRepository();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (DiffFormatter formatter = new DiffFormatter(out)) {
                formatter.setRepository(repo);
                formatter.setDiffComparator(RawTextComparator.DEFAULT);
                formatter.setDetectRenames(true);

                ObjectId headId = repo.resolve("HEAD^{tree}");
                if (headId == null) {
                    // Initial commit state - read working tree file
                    File f = new File(directory, relativePath);
                    if (f.exists() && f.isFile()) {
                        return "New file: " + relativePath + "\n\n" + readFileSnippet(f, 2000);
                    }
                    return "New file: " + relativePath;
                }

                CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
                try (ObjectReader reader = repo.newObjectReader()) {
                    oldTreeParser.reset(reader, headId);
                }

                List<DiffEntry> diffs = formatter.scan(oldTreeParser, new CanonicalTreeParser());
                for (DiffEntry entry : diffs) {
                    if (entry.getNewPath().equals(relativePath) || entry.getOldPath().equals(relativePath)) {
                        formatter.format(entry);
                        break;
                    }
                }
            }
            String diffText = out.toString("UTF-8");
            if (diffText.isEmpty()) {
                File f = new File(directory, relativePath);
                if (f.exists() && f.isFile()) {
                    return "Untracked or pending change in " + relativePath + ":\n\n" + readFileSnippet(f, 2000);
                }
            }
            return diffText;
        } catch (Exception e) {
            Log.e(TAG, "Error generating diff: " + e.getMessage());
            return "Unable to compute diff: " + e.getMessage();
        }
    }

    private String readFileSnippet(File file, int maxChars) {
        try {
            byte[] bytes = java.nio.file.Files.readAllBytes(file.toPath());
            String str = new String(bytes, "UTF-8");
            if (str.length() > maxChars) {
                return str.substring(0, maxChars) + "\n... [truncated]";
            }
            return str;
        } catch (Exception e) {
            return "[Binary or unreadable file]";
        }
    }
}
