package com.example.core;

import android.util.Log;
import com.example.core.models.GitCommitInfo;
import com.example.core.models.GitFileStatus;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.Status;
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
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

public class GitEngine {
    private static final String TAG = "GitEngine";
    private android.content.Context context;

    public GitEngine() {
    }

    public GitEngine(android.content.Context context) {
        this.context = context;
    }

    public boolean isGitRepository(File directory) {
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return false;
        }
        File gitDir = new File(directory, ".git");
        return gitDir.exists();
    }

    public synchronized void initRepository(File directory) throws Exception {
        if (!directory.exists()) {
            directory.mkdirs();
        }
        try (Git git = Git.init().setDirectory(directory).call()) {
            StoredConfig config = git.getRepository().getConfig();
            config.setString("core", null, "autocrlf", "input");
            config.setString("init", null, "defaultBranch", "main");
            config.save();
        }
    }

    public synchronized List<GitFileStatus> getStatus(File directory) throws Exception {
        if (!isGitRepository(directory)) {
            return Collections.emptyList();
        }
        List<GitFileStatus> fileStatuses = new ArrayList<>();
        try (Git git = Git.open(directory)) {
            Status status = git.status().call();

            for (String path : status.getAdded()) {
                fileStatuses.add(new GitFileStatus(path, GitFileStatus.ChangeType.ADDED, true, getFileSize(directory, path)));
            }
            for (String path : status.getChanged()) {
                fileStatuses.add(new GitFileStatus(path, GitFileStatus.ChangeType.MODIFIED, true, getFileSize(directory, path)));
            }
            for (String path : status.getRemoved()) {
                fileStatuses.add(new GitFileStatus(path, GitFileStatus.ChangeType.DELETED, true, 0L));
            }
            for (String path : status.getModified()) {
                fileStatuses.add(new GitFileStatus(path, GitFileStatus.ChangeType.MODIFIED, false, getFileSize(directory, path)));
            }
            for (String path : status.getMissing()) {
                fileStatuses.add(new GitFileStatus(path, GitFileStatus.ChangeType.MISSING, false, 0L));
            }
            for (String path : status.getUntracked()) {
                fileStatuses.add(new GitFileStatus(path, GitFileStatus.ChangeType.UNTRACKED, false, getFileSize(directory, path)));
            }
            for (String path : status.getConflicting()) {
                fileStatuses.add(new GitFileStatus(path, GitFileStatus.ChangeType.CONFLICT, false, getFileSize(directory, path)));
            }
        }
        return fileStatuses;
    }

    private long getFileSize(File baseDir, String relativePath) {
        File f = new File(baseDir, relativePath);
        return (f.exists() && f.isFile()) ? f.length() : 0L;
    }

    public synchronized void stageFile(File directory, String filePattern) throws Exception {
        try (Git git = Git.open(directory)) {
            git.add().addFilepattern(filePattern).call();
        }
    }

    public synchronized void stageAll(File directory) throws Exception {
        try (Git git = Git.open(directory)) {
            git.add().addFilepattern(".").call();
            try {
                git.add().setUpdate(true).addFilepattern(".").call();
            } catch (Exception ignored) {
            }
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
            String author = (authorName != null && !authorName.trim().isEmpty()) ? authorName.trim() : "RepoStudio User";
            String email = (authorEmail != null && !authorEmail.trim().isEmpty()) ? authorEmail.trim() : "user@repostudio.local";
            PersonIdent ident = new PersonIdent(author, email);

            RevCommit commit = git.commit()
                    .setMessage(message)
                    .setAuthor(ident)
                    .setCommitter(ident)
                    .setAllowEmpty(true)
                    .call();

            String fullId = commit.getId().getName();
            String shortId = fullId.length() >= 7 ? fullId.substring(0, 7) : fullId;
            long timestamp = ((long) commit.getCommitTime()) * 1000L;
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            return new GitCommitInfo(fullId, shortId, commit.getShortMessage(), author, email, timestamp, sdf.format(new Date(timestamp)));
        }
    }

    public synchronized List<GitCommitInfo> getCommitHistory(File directory, int maxCount) throws Exception {
        if (!isGitRepository(directory)) {
            return Collections.emptyList();
        }
        List<GitCommitInfo> history = new ArrayList<>();
        try (Git git = Git.open(directory)) {
            Repository repo = git.getRepository();
            Ref head = repo.exactRef("HEAD");
            if (head == null || head.getObjectId() == null) {
                return Collections.emptyList();
            }
            Iterable<RevCommit> commits = git.log().setMaxCount(maxCount).call();
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            for (RevCommit commit : commits) {
                String fullId = commit.getId().getName();
                String shortId = fullId.length() >= 7 ? fullId.substring(0, 7) : fullId;
                long timestamp = ((long) commit.getCommitTime()) * 1000L;
                String author = commit.getAuthorIdent() != null ? commit.getAuthorIdent().getName() : "Unknown";
                String email = commit.getAuthorIdent() != null ? commit.getAuthorIdent().getEmailAddress() : "";
                history.add(new GitCommitInfo(fullId, shortId, commit.getShortMessage(), author, email, timestamp, sdf.format(new Date(timestamp))));
            }
        }
        return history;
    }

    public synchronized String getCurrentBranch(File directory) {
        if (!isGitRepository(directory)) {
            return "main";
        }
        try (Git git = Git.open(directory)) {
            String branch = git.getRepository().getBranch();
            return branch != null ? branch : "main";
        } catch (Exception e) {
            return "main";
        }
    }

    public synchronized List<String> getBranches(File directory) {
        if (!isGitRepository(directory)) {
            return Collections.singletonList("main");
        }
        List<String> branchList = new ArrayList<>();
        try (Git git = Git.open(directory)) {
            List<Ref> refs = git.branchList().setListMode(ListBranchCommand.ListMode.ALL).call();
            for (Ref ref : refs) {
                String name = Repository.shortenRefName(ref.getName());
                if (!branchList.contains(name)) {
                    branchList.add(name);
                }
            }
        } catch (Exception ignored) {
        }
        if (branchList.isEmpty()) {
            branchList.add("main");
        }
        return branchList;
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
        if (!isGitRepository(directory)) {
            return null;
        }
        try (Git git = Git.open(directory)) {
            return git.getRepository().getConfig().getString("remote", remoteName, "url");
        } catch (Exception e) {
            return null;
        }
    }

    private CredentialsProvider createCredentialsProvider(String token) {
        if (token != null && !token.trim().isEmpty()) {
            return new UsernamePasswordCredentialsProvider(token.trim(), "");
        }
        return null;
    }

    public synchronized String push(File directory, String remoteName, String branchName, String token) throws Exception {
        try (Git git = Git.open(directory)) {
            PushCommand push = git.push().setRemote(remoteName).setCredentialsProvider(createCredentialsProvider(token));
            if (branchName != null && !branchName.isEmpty()) {
                push.setRefSpecs(new RefSpec(Constants.R_HEADS + branchName + ":refs/heads/" + branchName));
            }
            Iterable<PushResult> results = push.call();
            StringBuilder sb = new StringBuilder();
            for (PushResult result : results) {
                for (RemoteRefUpdate update : result.getRemoteUpdates()) {
                    RemoteRefUpdate.Status s = update.getStatus();
                    sb.append(update.getRemoteName()).append(": ").append(s.name()).append("\n");
                    if (s != RemoteRefUpdate.Status.OK && s != RemoteRefUpdate.Status.UP_TO_DATE) {
                        if (s == RemoteRefUpdate.Status.REJECTED_NONFASTFORWARD) {
                            throw new Exception("Push rejected: Non-fast-forward. Remote contains work that you do not have locally. Please pull first.");
                        }
                        if (s == RemoteRefUpdate.Status.REJECTED_NODELETE || s == RemoteRefUpdate.Status.REJECTED_OTHER_REASON) {
                            throw new Exception("Push rejected: " + update.getMessage());
                        }
                    }
                }
            }
            return sb.length() > 0 ? sb.toString().trim() : "Push completed successfully.";
        } catch (Exception ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "";
            if (msg.contains("not authorized") || msg.contains("401") || msg.contains("Authentication")) {
                throw new Exception("GitHub push failed: Authentication error. Please verify your token in Settings.");
            }
            if (msg.contains("not found") || msg.contains("404")) {
                throw new Exception("GitHub push failed: Remote repository not found.");
            }
            throw ex;
        }
    }

    public synchronized String pull(File directory, String remoteName, String branchName, String token) throws Exception {
        try (Git git = Git.open(directory)) {
            PullResult pullResult = git.pull()
                    .setRemote(remoteName)
                    .setRemoteBranchName(branchName)
                    .setCredentialsProvider(createCredentialsProvider(token))
                    .call();

            if (!pullResult.isSuccessful()) {
                if (pullResult.getMergeResult() != null && pullResult.getMergeResult().getConflicts() != null) {
                    throw new Exception("Pull resulted in merge conflicts. Resolve conflicts before committing.");
                }
                throw new Exception("Pull did not complete successfully.");
            }
            return "Pulled changes successfully: " + (pullResult.getMergeResult() != null ? pullResult.getMergeResult().getMergeStatus().name() : "OK");
        } catch (Exception ex) {
            String msg = ex.getMessage() != null ? ex.getMessage() : "";
            if (msg.contains("not authorized") || msg.contains("401")) {
                throw new Exception("GitHub pull failed: Authentication error. Please verify your token in Settings.");
            }
            throw ex;
        }
    }

    public synchronized String fetch(File directory, String remoteName, String token) throws Exception {
        try (Git git = Git.open(directory)) {
            git.fetch().setRemote(remoteName).setCredentialsProvider(createCredentialsProvider(token)).call();
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
        if (!isGitRepository(directory)) {
            return "";
        }
        try (Git git = Git.open(directory)) {
            Repository repo = git.getRepository();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (DiffFormatter formatter = new DiffFormatter(out)) {
                formatter.setRepository(repo);
                formatter.setDiffComparator(RawTextComparator.DEFAULT);
                formatter.setDetectRenames(true);

                ObjectId headId = repo.resolve("HEAD^{tree}");
                if (headId == null) {
                    File f = new File(directory, relativePath);
                    if (f.exists() && f.isFile()) {
                        return "New file: " + relativePath + "\n\n" + readFileSnippet(f, 2000);
                    }
                    return "New file: " + relativePath;
                }

                CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
                try (ObjectReader reader = repo.newObjectReader()) {
                    oldTreeParser.reset(reader, headId);
                    List<DiffEntry> diffs = formatter.scan(oldTreeParser, new CanonicalTreeParser());
                    for (DiffEntry entry : diffs) {
                        if (entry.getNewPath().equals(relativePath) || entry.getOldPath().equals(relativePath)) {
                            formatter.format(entry);
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
            }
        } catch (Exception e) {
            Log.e(TAG, "Error generating diff: " + e.getMessage());
            return "Unable to compute diff: " + e.getMessage();
        }
    }

    private String readFileSnippet(File file, int maxChars) {
        try {
            byte[] bytes = Files.readAllBytes(file.toPath());
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
