package com.example.core;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.example.core.models.GitCommitInfo;
import com.example.core.models.GitFileStatus;
import com.example.core.models.GitHubRepo;
import com.example.core.models.GitHubUser;
import com.example.core.models.LocalRepository;
import com.example.core.models.OperationLog;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import org.eclipse.jgit.lib.Constants;

/* JADX INFO: loaded from: classes5.dex */
public class RepositoryService {
    private static final String TAG = "RepositoryService";
    private static RepositoryService instance;
    private LocalRepository activeRepository;
    private final Context context;
    private final GitEngine gitEngine;
    private final GitHubApiClient gitHubApiClient;
    private final KeystoreManager keystoreManager;
    private final List<OperationLog> operationLogs = new CopyOnWriteArrayList();
    private final SafFileManager safFileManager;

    private RepositoryService(Context context) {
        this.context = context.getApplicationContext();
        this.keystoreManager = new KeystoreManager(context);
        this.gitHubApiClient = new GitHubApiClient(this.keystoreManager);
        this.gitEngine = new GitEngine(context);
        this.safFileManager = new SafFileManager(context);
        logOperation("App Start", "System", "RepoStudio initialized successfully", OperationLog.Level.INFO);
        List<LocalRepository> discovered = this.safFileManager.getDiscoveredRepositories();
        if (!discovered.isEmpty()) {
            this.activeRepository = discovered.get(0);
            refreshActiveRepoInfo();
            return;
        }
        try {
            File defaultProj = new File(this.safFileManager.getBaseProjectsDir(), "MyFirstRepo");
            if (!defaultProj.exists()) {
                this.safFileManager.generateStarterProject(defaultProj, "java", "MyFirstRepo");
                this.gitEngine.initRepository(defaultProj);
                this.gitEngine.stageAll(defaultProj);
                this.gitEngine.commit(defaultProj, "Initial commit: RepoStudio setup", this.keystoreManager.getGitAuthorName(), this.keystoreManager.getGitAuthorEmail());
                logOperation("Repository Created", "MyFirstRepo", "Initialized starter Java repository with initial commit", OperationLog.Level.SUCCESS);
            }
            this.activeRepository = new LocalRepository("MyFirstRepo", defaultProj.getAbsolutePath(), "", "main", true);
        } catch (Exception e) {
            Log.w(TAG, "Creating default sample repo: " + e.getMessage());
        }
    }

    public static synchronized RepositoryService getInstance(Context context) {
        if (instance == null) {
            instance = new RepositoryService(context);
        }
        return instance;
    }

    public KeystoreManager getKeystoreManager() {
        return this.keystoreManager;
    }

    public GitHubApiClient getGitHubApiClient() {
        return this.gitHubApiClient;
    }

    public GitEngine getGitEngine() {
        return this.gitEngine;
    }

    public SafFileManager getSafFileManager() {
        return this.safFileManager;
    }

    public synchronized LocalRepository getActiveRepository() {
        return this.activeRepository;
    }

    public synchronized void setActiveRepository(LocalRepository repo) {
        this.activeRepository = repo;
        refreshActiveRepoInfo();
        if (repo != null) {
            logOperation("Select Repository", repo.getName(), "Switched active repository to " + repo.getPath(), OperationLog.Level.INFO);
        }
    }

    public synchronized void refreshActiveRepoInfo() {
        if (this.activeRepository != null && this.activeRepository.exists()) {
            File dir = new File(this.activeRepository.getPath());
            boolean isGit = this.gitEngine.isGitRepository(dir);
            this.activeRepository.setGitInitialized(isGit);
            if (isGit) {
                String branch = this.gitEngine.getCurrentBranch(dir);
                this.activeRepository.setCurrentBranch(branch);
                String remote = this.gitEngine.getRemoteUrl(dir, Constants.DEFAULT_REMOTE_NAME);
                this.activeRepository.setRemoteUrl(remote);
            }
        }
    }

    public List<LocalRepository> getAllLocalRepositories() {
        List<LocalRepository> list = new ArrayList<>();
        Set<String> seenPaths = new HashSet<>();
        List<LocalRepository> baseRepos = this.safFileManager.getDiscoveredRepositories();
        for (LocalRepository r : baseRepos) {
            if (!seenPaths.contains(r.getPath())) {
                seenPaths.add(r.getPath());
                list.add(r);
            }
        }
        Set<String> externalPaths = this.keystoreManager.getSavedExternalRepoPaths();
        for (String path : externalPaths) {
            if (!seenPaths.contains(path)) {
                File dir = new File(path);
                if (dir.exists() && dir.isDirectory()) {
                    seenPaths.add(path);
                    boolean isGit = this.gitEngine.isGitRepository(dir);
                    String branch = isGit ? this.gitEngine.getCurrentBranch(dir) : "main";
                    String remote = isGit ? this.gitEngine.getRemoteUrl(dir, Constants.DEFAULT_REMOTE_NAME) : "";
                    list.add(new LocalRepository(dir.getName(), dir.getAbsolutePath(), remote, branch, isGit));
                }
            }
        }
        for (LocalRepository r2 : list) {
            if (this.activeRepository != null && r2.getPath().equals(this.activeRepository.getPath())) {
                r2.setCurrentBranch(this.activeRepository.getCurrentBranch());
                r2.setRemoteUrl(this.activeRepository.getRemoteUrl());
                r2.setGitInitialized(this.activeRepository.isGitInitialized());
            } else if (r2.exists()) {
                File dir2 = new File(r2.getPath());
                if (this.gitEngine.isGitRepository(dir2)) {
                    r2.setCurrentBranch(this.gitEngine.getCurrentBranch(dir2));
                    r2.setRemoteUrl(this.gitEngine.getRemoteUrl(dir2, Constants.DEFAULT_REMOTE_NAME));
                }
            }
        }
        return list;
    }

    public LocalRepository openFolderFromSaf(Uri treeUri) throws Exception {
        SafFileManager.OpenFolderResult res = this.safFileManager.openFolder(treeUri);
        File dir = res.getDirectory();
        String folderName = res.getFolderName();
        this.keystoreManager.addSavedExternalRepoPath(dir.getAbsolutePath());
        boolean isGit = this.gitEngine.isGitRepository(dir);
        String branch = isGit ? this.gitEngine.getCurrentBranch(dir) : "main";
        String remote = isGit ? this.gitEngine.getRemoteUrl(dir, Constants.DEFAULT_REMOTE_NAME) : "";
        LocalRepository repo = new LocalRepository(folderName, dir.getAbsolutePath(), remote, branch, isGit);
        repo.setSafTreeUri(res.getTreeUriString());
        setActiveRepository(repo);
        if (res.isInPlace()) {
            logOperation("Open Folder", folderName, "Opened folder in-place: " + dir.getAbsolutePath() + (isGit ? " (Git detected)" : " (No Git yet)"), OperationLog.Level.SUCCESS);
        } else {
            logOperation("Open Folder", folderName, "Loaded " + res.getCopiedFileCount() + " files into: " + dir.getAbsolutePath() + (isGit ? " (Git detected)" : " (No Git yet)"), OperationLog.Level.SUCCESS);
        }
        return repo;
    }

    public void removeLocalRepository(LocalRepository repo) {
        if (repo == null) {
            return;
        }
        this.keystoreManager.removeSavedExternalRepoPath(repo.getPath());
        logOperation("Remove Repository", repo.getName(), "Removed from local list: " + repo.getPath(), OperationLog.Level.INFO);
        if (this.activeRepository != null && this.activeRepository.getPath().equals(repo.getPath())) {
            List<LocalRepository> remaining = getAllLocalRepositories();
            this.activeRepository = !remaining.isEmpty() ? remaining.get(0) : null;
            refreshActiveRepoInfo();
        }
    }

    public void logOperation(String operation, String repository, String details, OperationLog.Level level) {
        OperationLog log = new OperationLog(operation, repository, details, level);
        this.operationLogs.add(0, log);
        if (this.operationLogs.size() > 200) {
            this.operationLogs.remove(this.operationLogs.size() - 1);
        }
    }

    public List<OperationLog> getOperationLogs() {
        return new ArrayList(this.operationLogs);
    }

    public void clearLogs() {
        this.operationLogs.clear();
        logOperation("Logs Cleared", "System", "Audit history cleared by user", OperationLog.Level.INFO);
    }

    public void initGitInActiveRepo() throws Exception {
        if (this.activeRepository == null) {
            throw new IllegalStateException("No active repository selected");
        }
        File dir = new File(this.activeRepository.getPath());
        this.gitEngine.initRepository(dir);
        try {
            this.gitEngine.stageAll(dir);
            String authorName = this.keystoreManager.getGitAuthorName();
            String authorEmail = this.keystoreManager.getGitAuthorEmail();
            this.gitEngine.commit(dir, "Initial commit: " + this.activeRepository.getName(), authorName, authorEmail);
        } catch (Exception e) {
            Log.w(TAG, "Initial commit on git init: " + e.getMessage());
        }
        refreshActiveRepoInfo();
        logOperation("Git Init", this.activeRepository.getName(), "Initialized Git repository and committed files", OperationLog.Level.SUCCESS);
    }

    public List<GitFileStatus> getActiveRepoStatus() throws Exception {
        if (this.activeRepository == null || !this.activeRepository.exists()) {
            return Collections.emptyList();
        }
        File dir = new File(this.activeRepository.getPath());
        return this.gitEngine.getStatus(dir);
    }

    public void stageFile(String filePattern) throws Exception {
        if (this.activeRepository == null) {
            return;
        }
        this.gitEngine.stageFile(new File(this.activeRepository.getPath()), filePattern);
        logOperation("Stage File", this.activeRepository.getName(), "Staged: " + filePattern, OperationLog.Level.INFO);
    }

    public void stageAll() throws Exception {
        if (this.activeRepository == null) {
            return;
        }
        this.gitEngine.stageAll(new File(this.activeRepository.getPath()));
        logOperation("Stage All", this.activeRepository.getName(), "Staged all changes", OperationLog.Level.INFO);
    }

    public void unstageFile(String filePattern) throws Exception {
        if (this.activeRepository == null) {
            return;
        }
        this.gitEngine.unstageFile(new File(this.activeRepository.getPath()), filePattern);
        logOperation("Unstage File", this.activeRepository.getName(), "Unstaged: " + filePattern, OperationLog.Level.INFO);
    }

    public void unstageAll() throws Exception {
        if (this.activeRepository == null) {
            return;
        }
        this.gitEngine.unstageAll(new File(this.activeRepository.getPath()));
        logOperation("Unstage All", this.activeRepository.getName(), "Unstaged all changes", OperationLog.Level.INFO);
    }

    public GitCommitInfo commitChanges(String message) throws Exception {
        if (this.activeRepository == null) {
            throw new IllegalStateException("No active repository selected");
        }
        File dir = new File(this.activeRepository.getPath());
        String authorName = this.keystoreManager.getGitAuthorName();
        String authorEmail = this.keystoreManager.getGitAuthorEmail();
        GitCommitInfo info = this.gitEngine.commit(dir, message, authorName, authorEmail);
        logOperation("Commit", this.activeRepository.getName(), "[" + info.getShortId() + "] " + info.getMessage(), OperationLog.Level.SUCCESS);
        return info;
    }

    public String pushActiveRepo() throws Exception {
        if (this.activeRepository == null) {
            throw new IllegalStateException("No active repository selected");
        }
        File dir = new File(this.activeRepository.getPath());
        String token = this.keystoreManager.getGitHubToken();
        if (token == null || token.trim().isEmpty()) {
            throw new Exception("GitHub token is missing. Please sign in or set a Personal Access Token in Settings.");
        }
        String branch = this.activeRepository.getCurrentBranch();
        String res = this.gitEngine.push(dir, Constants.DEFAULT_REMOTE_NAME, branch, token);
        logOperation("Push", this.activeRepository.getName(), "Pushed branch " + branch + " to origin:\n" + res, OperationLog.Level.SUCCESS);
        return res;
    }

    public String pullActiveRepo() throws Exception {
        if (this.activeRepository == null) {
            throw new IllegalStateException("No active repository selected");
        }
        File dir = new File(this.activeRepository.getPath());
        String token = this.keystoreManager.getGitHubToken();
        if (token == null || token.trim().isEmpty()) {
            throw new Exception("GitHub token is missing. Please sign in or set a Personal Access Token in Settings.");
        }
        String branch = this.activeRepository.getCurrentBranch();
        String res = this.gitEngine.pull(dir, Constants.DEFAULT_REMOTE_NAME, branch, token);
        logOperation("Pull", this.activeRepository.getName(), "Pulled branch " + branch + " from origin: " + res, OperationLog.Level.SUCCESS);
        return res;
    }

    public String fetchActiveRepo() throws Exception {
        if (this.activeRepository == null) {
            throw new IllegalStateException("No active repository selected");
        }
        File dir = new File(this.activeRepository.getPath());
        String token = this.keystoreManager.getGitHubToken();
        String res = this.gitEngine.fetch(dir, Constants.DEFAULT_REMOTE_NAME, token);
        logOperation("Fetch", this.activeRepository.getName(), res, OperationLog.Level.INFO);
        return res;
    }

    public GitHubRepo createRemoteGitHubRepository(String name, String description, boolean isPrivate, boolean autoInit) throws Exception {
        String token = this.keystoreManager.getGitHubToken();
        if (token == null || token.trim().isEmpty()) {
            throw new Exception("GitHub token is not set. Please go to Settings and enter your Personal Access Token (with 'repo' scope).");
        }
        logOperation("Create GitHub Repo", name, "Creating repository on GitHub...", OperationLog.Level.INFO);
        try {
            GitHubRepo repo = this.gitHubApiClient.createRepository(name, description, isPrivate, autoInit);
            logOperation("Create GitHub Repo", name, "Successfully created: " + repo.getHtmlUrl(), OperationLog.Level.SUCCESS);
            return repo;
        } catch (Exception e) {
            logOperation("Create GitHub Repo Failed", name, e.getMessage(), OperationLog.Level.ERROR);
            throw e;
        }
    }

    public void quickPublish(File projectDir, String repoName, String description, boolean isPrivate, String branchName, String commitMsg) throws Exception {
        String str;
        String token = this.keystoreManager.getGitHubToken();
        if (token == null || token.trim().isEmpty()) {
            throw new Exception("GitHub authentication required: Please add a Personal Access Token with 'repo' scope in Settings.");
        }
        String cleanRepoName = repoName.trim().replaceAll("\\s+", "-").replaceAll("[^a-zA-Z0-9_.-]", "-");
        if (cleanRepoName.isEmpty()) {
            cleanRepoName = "My-Repo";
        }
        logOperation("Publish Step 1/4", cleanRepoName, "Creating remote repository on GitHub...", OperationLog.Level.INFO);
        GitHubRepo remoteRepo;
        try {
            remoteRepo = this.gitHubApiClient.createRepository(cleanRepoName, description, isPrivate, false);
            logOperation("Publish Step 1/4", cleanRepoName, "Created remote repository: " + remoteRepo.getHtmlUrl(), OperationLog.Level.SUCCESS);
        } catch (Exception e) {
            String errMsg = e.getMessage() != null ? e.getMessage() : "";
            if (!errMsg.contains("already exists") && !errMsg.contains("422")) {
                logOperation("Publish Failed", cleanRepoName, "Could not create repository on GitHub: " + errMsg, OperationLog.Level.ERROR);
                throw new Exception("Could not create repository on GitHub: " + errMsg);
            }
            String userLogin = "";
            try {
                GitHubUser u = this.gitHubApiClient.getCurrentUser();
                userLogin = u.getLogin();
            } catch (Exception ignored) {
            }
            if (userLogin.isEmpty()) {
                userLogin = this.keystoreManager.getGitAuthorName().replaceAll("\\s+", "");
            }
            String htmlUrl = "https://github.com/" + userLogin + "/" + cleanRepoName;
            remoteRepo = new GitHubRepo(cleanRepoName, userLogin + "/" + cleanRepoName, description, isPrivate, htmlUrl, "https://github.com/" + userLogin + "/" + cleanRepoName + ".git", branchName);
            logOperation("Publish Step 1/4", cleanRepoName, "Repository already exists on GitHub, using remote: " + htmlUrl, OperationLog.Level.WARNING);
        }
        if (!this.gitEngine.isGitRepository(projectDir)) {
            this.gitEngine.initRepository(projectDir);
            logOperation("Publish Step 2/4", cleanRepoName, "Initialized local Git repository", OperationLog.Level.INFO);
        }
        String cloneUrl = remoteRepo.getCloneUrl();
        this.gitEngine.setRemoteUrl(projectDir, Constants.DEFAULT_REMOTE_NAME, cloneUrl);
        this.gitEngine.stageAll(projectDir);
        String authorName = this.keystoreManager.getGitAuthorName();
        String authorEmail = this.keystoreManager.getGitAuthorEmail();
        GitCommitInfo commitInfo = this.gitEngine.commit(projectDir, commitMsg, authorName, authorEmail);
        logOperation("Publish Step 3/4", cleanRepoName, "Committed files: " + commitInfo.getShortId(), OperationLog.Level.INFO);
        if (branchName != null && !branchName.isEmpty()) {
            String current = this.gitEngine.getCurrentBranch(projectDir);
            if (!current.equals(branchName)) {
                try {
                    this.gitEngine.renameBranch(projectDir, current, branchName);
                } catch (Exception e5) {
                    try {
                        this.gitEngine.createBranch(projectDir, branchName);
                        this.gitEngine.checkoutBranch(projectDir, branchName);
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        logOperation("Publish Step 4/4", cleanRepoName, "Pushing local commits to GitHub...", OperationLog.Level.INFO);
        String pushResult = this.gitEngine.push(projectDir, Constants.DEFAULT_REMOTE_NAME, branchName, token);
        logOperation("Publish Complete", cleanRepoName, "Successfully published to " + cloneUrl + "\n" + pushResult, OperationLog.Level.SUCCESS);
        LocalRepository local = new LocalRepository(cleanRepoName, projectDir.getAbsolutePath(), cloneUrl, branchName, true);
        setActiveRepository(local);
    }
}
