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
import com.example.core.models.ProjectFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class RepositoryService {
    private static final String TAG = "RepositoryService";
    private static RepositoryService instance;

    private final Context context;
    private final KeystoreManager keystoreManager;
    private final GitHubApiClient gitHubApiClient;
    private final GitEngine gitEngine;
    private final SafFileManager safFileManager;

    private LocalRepository activeRepository;
    private final List<OperationLog> operationLogs = new CopyOnWriteArrayList<>();

    private RepositoryService(Context context) {
        this.context = context.getApplicationContext();
        this.keystoreManager = new KeystoreManager(context);
        this.gitHubApiClient = new GitHubApiClient(keystoreManager);
        this.gitEngine = new GitEngine(context);
        this.safFileManager = new SafFileManager(context);

        // Add initial system log
        logOperation("App Start", "System", "RepoStudio initialized successfully", OperationLog.Level.INFO);

        // Discover initial repos
        List<LocalRepository> discovered = safFileManager.getDiscoveredRepositories();
        if (!discovered.isEmpty()) {
            activeRepository = discovered.get(0);
            refreshActiveRepoInfo();
        } else {
            // Create a default initial sample repo so the user immediately has something to explore
            try {
                File defaultProj = new File(safFileManager.getBaseProjectsDir(), "MyFirstRepo");
                if (!defaultProj.exists()) {
                    safFileManager.generateStarterProject(defaultProj, "java", "MyFirstRepo");
                    gitEngine.initRepository(defaultProj);
                    gitEngine.stageAll(defaultProj);
                    gitEngine.commit(defaultProj, "Initial commit: RepoStudio setup", keystoreManager.getGitAuthorName(), keystoreManager.getGitAuthorEmail());
                    logOperation("Repository Created", "MyFirstRepo", "Initialized starter Java repository with initial commit", OperationLog.Level.SUCCESS);
                }
                activeRepository = new LocalRepository("MyFirstRepo", defaultProj.getAbsolutePath(), "", "main", true);
            } catch (Exception e) {
                Log.w(TAG, "Creating default sample repo: " + e.getMessage());
            }
        }
    }

    public static synchronized RepositoryService getInstance(Context context) {
        if (instance == null) {
            instance = new RepositoryService(context);
        }
        return instance;
    }

    public KeystoreManager getKeystoreManager() { return keystoreManager; }
    public GitHubApiClient getGitHubApiClient() { return gitHubApiClient; }
    public GitEngine getGitEngine() { return gitEngine; }
    public SafFileManager getSafFileManager() { return safFileManager; }

    public synchronized LocalRepository getActiveRepository() {
        return activeRepository;
    }

    public synchronized void setActiveRepository(LocalRepository repo) {
        this.activeRepository = repo;
        refreshActiveRepoInfo();
        if (repo != null) {
            logOperation("Select Repository", repo.getName(), "Switched active repository to " + repo.getPath(), OperationLog.Level.INFO);
        }
    }

    public synchronized void refreshActiveRepoInfo() {
        if (activeRepository == null || !activeRepository.exists()) return;
        File dir = new File(activeRepository.getPath());
        boolean isGit = gitEngine.isGitRepository(dir);
        activeRepository.setGitInitialized(isGit);
        if (isGit) {
            String branch = gitEngine.getCurrentBranch(dir);
            activeRepository.setCurrentBranch(branch);
            String remote = gitEngine.getRemoteUrl(dir, "origin");
            activeRepository.setRemoteUrl(remote);
        }
    }

    public List<LocalRepository> getAllLocalRepositories() {
        List<LocalRepository> list = new ArrayList<>();
        Set<String> seenPaths = new HashSet<>();

        // 1. Projects in baseProjectsDir
        List<LocalRepository> baseRepos = safFileManager.getDiscoveredRepositories();
        for (LocalRepository r : baseRepos) {
            if (!seenPaths.contains(r.getPath())) {
                seenPaths.add(r.getPath());
                list.add(r);
            }
        }

        // 2. Saved external folders opened by user
        Set<String> externalPaths = keystoreManager.getSavedExternalRepoPaths();
        for (String path : externalPaths) {
            if (!seenPaths.contains(path)) {
                File dir = new File(path);
                if (dir.exists() && dir.isDirectory()) {
                    seenPaths.add(path);
                    boolean isGit = gitEngine.isGitRepository(dir);
                    String branch = isGit ? gitEngine.getCurrentBranch(dir) : "main";
                    String remote = isGit ? gitEngine.getRemoteUrl(dir, "origin") : "";
                    LocalRepository r = new LocalRepository(dir.getName(), dir.getAbsolutePath(), remote, branch, isGit);
                    list.add(r);
                }
            }
        }

        // Update active repository details if present
        for (LocalRepository r : list) {
            if (activeRepository != null && r.getPath().equals(activeRepository.getPath())) {
                r.setCurrentBranch(activeRepository.getCurrentBranch());
                r.setRemoteUrl(activeRepository.getRemoteUrl());
                r.setGitInitialized(activeRepository.isGitInitialized());
            } else if (r.exists()) {
                File dir = new File(r.getPath());
                if (gitEngine.isGitRepository(dir)) {
                    r.setCurrentBranch(gitEngine.getCurrentBranch(dir));
                    r.setRemoteUrl(gitEngine.getRemoteUrl(dir, "origin"));
                }
            }
        }
        return list;
    }

    public LocalRepository openFolderFromSaf(Uri treeUri) throws Exception {
        SafFileManager.OpenFolderResult res = safFileManager.openFolder(treeUri);
        File dir = res.getDirectory();
        String folderName = res.getFolderName();

        // Remember external directory path
        keystoreManager.addSavedExternalRepoPath(dir.getAbsolutePath());

        boolean isGit = gitEngine.isGitRepository(dir);
        String branch = isGit ? gitEngine.getCurrentBranch(dir) : "main";
        String remote = isGit ? gitEngine.getRemoteUrl(dir, "origin") : "";

        LocalRepository repo = new LocalRepository(folderName, dir.getAbsolutePath(), remote, branch, isGit);
        setActiveRepository(repo);

        if (res.isInPlace()) {
            logOperation("Open Folder", folderName, "Opened directory in-place: " + dir.getAbsolutePath() + (isGit ? " (Git detected)" : " (No Git yet)"), OperationLog.Level.SUCCESS);
        } else {
            logOperation("Import Folder", folderName, "Imported " + res.getCopiedFileCount() + " files into: " + dir.getAbsolutePath(), OperationLog.Level.SUCCESS);
        }

        return repo;
    }

    public void removeLocalRepository(LocalRepository repo) {
        if (repo == null) return;
        keystoreManager.removeSavedExternalRepoPath(repo.getPath());
        logOperation("Remove Repository", repo.getName(), "Removed from local list: " + repo.getPath(), OperationLog.Level.INFO);
        if (activeRepository != null && activeRepository.getPath().equals(repo.getPath())) {
            List<LocalRepository> remaining = getAllLocalRepositories();
            activeRepository = !remaining.isEmpty() ? remaining.get(0) : null;
            refreshActiveRepoInfo();
        }
    }

    public void logOperation(String operation, String repository, String details, OperationLog.Level level) {
        OperationLog log = new OperationLog(operation, repository, details, level);
        operationLogs.add(0, log);
        // Limit history to 200 entries
        if (operationLogs.size() > 200) {
            operationLogs.remove(operationLogs.size() - 1);
        }
    }

    public List<OperationLog> getOperationLogs() {
        return new ArrayList<>(operationLogs);
    }

    public void clearLogs() {
        operationLogs.clear();
        logOperation("Logs Cleared", "System", "Audit history cleared by user", OperationLog.Level.INFO);
    }

    public void initGitInActiveRepo() throws Exception {
        if (activeRepository == null) throw new IllegalStateException("No active repository selected");
        File dir = new File(activeRepository.getPath());
        gitEngine.initRepository(dir);
        refreshActiveRepoInfo();
        logOperation("Git Init", activeRepository.getName(), "Initialized Git repository", OperationLog.Level.SUCCESS);
    }

    public List<GitFileStatus> getActiveRepoStatus() throws Exception {
        if (activeRepository == null || !activeRepository.exists()) return Collections.emptyList();
        File dir = new File(activeRepository.getPath());
        return gitEngine.getStatus(dir);
    }

    public void stageFile(String filePattern) throws Exception {
        if (activeRepository == null) return;
        gitEngine.stageFile(new File(activeRepository.getPath()), filePattern);
        logOperation("Stage File", activeRepository.getName(), "Staged: " + filePattern, OperationLog.Level.INFO);
    }

    public void stageAll() throws Exception {
        if (activeRepository == null) return;
        gitEngine.stageAll(new File(activeRepository.getPath()));
        logOperation("Stage All", activeRepository.getName(), "Staged all changes", OperationLog.Level.INFO);
    }

    public void unstageFile(String filePattern) throws Exception {
        if (activeRepository == null) return;
        gitEngine.unstageFile(new File(activeRepository.getPath()), filePattern);
        logOperation("Unstage File", activeRepository.getName(), "Unstaged: " + filePattern, OperationLog.Level.INFO);
    }

    public void unstageAll() throws Exception {
        if (activeRepository == null) return;
        gitEngine.unstageAll(new File(activeRepository.getPath()));
        logOperation("Unstage All", activeRepository.getName(), "Unstaged all changes", OperationLog.Level.INFO);
    }

    public GitCommitInfo commitChanges(String message) throws Exception {
        if (activeRepository == null) throw new IllegalStateException("No active repository selected");
        File dir = new File(activeRepository.getPath());
        String authorName = keystoreManager.getGitAuthorName();
        String authorEmail = keystoreManager.getGitAuthorEmail();
        GitCommitInfo info = gitEngine.commit(dir, message, authorName, authorEmail);
        logOperation("Commit", activeRepository.getName(), "[" + info.getShortId() + "] " + info.getMessage(), OperationLog.Level.SUCCESS);
        return info;
    }

    public String pushActiveRepo() throws Exception {
        if (activeRepository == null) throw new IllegalStateException("No active repository selected");
        File dir = new File(activeRepository.getPath());
        String token = keystoreManager.getGitHubToken();
        if (token == null || token.trim().isEmpty()) {
            throw new Exception("GitHub token is missing. Please sign in or set a Personal Access Token in Settings.");
        }
        String branch = activeRepository.getCurrentBranch();
        String res = gitEngine.push(dir, "origin", branch, token);
        logOperation("Push", activeRepository.getName(), "Pushed branch " + branch + " to origin:\n" + res, OperationLog.Level.SUCCESS);
        return res;
    }

    public String pullActiveRepo() throws Exception {
        if (activeRepository == null) throw new IllegalStateException("No active repository selected");
        File dir = new File(activeRepository.getPath());
        String token = keystoreManager.getGitHubToken();
        if (token == null || token.trim().isEmpty()) {
            throw new Exception("GitHub token is missing. Please sign in or set a Personal Access Token in Settings.");
        }
        String branch = activeRepository.getCurrentBranch();
        String res = gitEngine.pull(dir, "origin", branch, token);
        logOperation("Pull", activeRepository.getName(), "Pulled branch " + branch + " from origin: " + res, OperationLog.Level.SUCCESS);
        return res;
    }

    public String fetchActiveRepo() throws Exception {
        if (activeRepository == null) throw new IllegalStateException("No active repository selected");
        File dir = new File(activeRepository.getPath());
        String token = keystoreManager.getGitHubToken();
        String res = gitEngine.fetch(dir, "origin", token);
        logOperation("Fetch", activeRepository.getName(), res, OperationLog.Level.INFO);
        return res;
    }

    public GitHubRepo createRemoteGitHubRepository(String name, String description, boolean isPrivate, boolean autoInit) throws Exception {
        String token = keystoreManager.getGitHubToken();
        if (token == null || token.trim().isEmpty()) {
            throw new Exception("GitHub token is not set. Please go to Settings and enter your Personal Access Token (with 'repo' scope).");
        }
        logOperation("Create GitHub Repo", name, "Creating repository on GitHub...", OperationLog.Level.INFO);
        try {
            GitHubRepo repo = gitHubApiClient.createRepository(name, description, isPrivate, autoInit);
            logOperation("Create GitHub Repo", name, "Successfully created: " + repo.getHtmlUrl(), OperationLog.Level.SUCCESS);
            return repo;
        } catch (Exception e) {
            logOperation("Create GitHub Repo Failed", name, e.getMessage(), OperationLog.Level.ERROR);
            throw e;
        }
    }

    public void quickPublish(File projectDir, String repoName, String description, boolean isPrivate, String branchName, String commitMsg) throws Exception {
        String token = keystoreManager.getGitHubToken();
        if (token == null || token.trim().isEmpty()) {
            throw new Exception("GitHub authentication required: Please add a Personal Access Token with 'repo' scope in Settings.");
        }

        // Sanitize name for GitHub API
        String cleanRepoName = repoName.trim().replaceAll("\\s+", "-").replaceAll("[^a-zA-Z0-9_.-]", "-");
        if (cleanRepoName.isEmpty()) cleanRepoName = "My-Repo";

        logOperation("Publish Step 1/4", cleanRepoName, "Creating remote repository on GitHub...", OperationLog.Level.INFO);

        // 1. Create or verify GitHub repository
        GitHubRepo remoteRepo;
        try {
            remoteRepo = gitHubApiClient.createRepository(cleanRepoName, description, isPrivate, false);
            logOperation("Publish Step 1/4", cleanRepoName, "Created remote repository: " + remoteRepo.getHtmlUrl(), OperationLog.Level.SUCCESS);
        } catch (Exception e) {
            String errMsg = e.getMessage() != null ? e.getMessage() : "";
            if (errMsg.contains("already exists") || errMsg.contains("422")) {
                // If it already exists on user's GitHub, locate clone URL with real login
                String userLogin = "";
                try {
                    GitHubUser u = gitHubApiClient.getCurrentUser();
                    userLogin = u.getLogin();
                } catch (Exception ignored) {}
                if (userLogin.isEmpty()) {
                    userLogin = keystoreManager.getGitAuthorName().replaceAll("\\s+", "");
                }
                String cloneUrl = "https://github.com/" + userLogin + "/" + cleanRepoName + ".git";
                String htmlUrl = "https://github.com/" + userLogin + "/" + cleanRepoName;
                remoteRepo = new GitHubRepo(cleanRepoName, userLogin + "/" + cleanRepoName, description, isPrivate, htmlUrl, cloneUrl, branchName);
                logOperation("Publish Step 1/4", cleanRepoName, "Repository already exists on GitHub, using remote: " + htmlUrl, OperationLog.Level.WARNING);
            } else {
                logOperation("Publish Failed", cleanRepoName, "Could not create repository on GitHub: " + errMsg, OperationLog.Level.ERROR);
                throw new Exception("Could not create repository on GitHub: " + errMsg);
            }
        }

        // 2. Initialize Git locally if needed
        if (!gitEngine.isGitRepository(projectDir)) {
            gitEngine.initRepository(projectDir);
            logOperation("Publish Step 2/4", cleanRepoName, "Initialized local Git repository", OperationLog.Level.INFO);
        }

        // 3. Configure Remote URL
        String cloneUrl = remoteRepo.getCloneUrl();
        gitEngine.setRemoteUrl(projectDir, "origin", cloneUrl);

        // 4. Stage and Commit
        gitEngine.stageAll(projectDir);
        String authorName = keystoreManager.getGitAuthorName();
        String authorEmail = keystoreManager.getGitAuthorEmail();
        GitCommitInfo commitInfo = gitEngine.commit(projectDir, commitMsg, authorName, authorEmail);
        logOperation("Publish Step 3/4", cleanRepoName, "Committed files: " + commitInfo.getShortId(), OperationLog.Level.INFO);

        // 5. Checkout or ensure branch name
        if (branchName != null && !branchName.isEmpty()) {
            String current = gitEngine.getCurrentBranch(projectDir);
            if (!current.equals(branchName)) {
                try {
                    gitEngine.renameBranch(projectDir, current, branchName);
                } catch (Exception ignored) {
                    try {
                        gitEngine.createBranch(projectDir, branchName);
                        gitEngine.checkoutBranch(projectDir, branchName);
                    } catch (Exception ignored2) {}
                }
            }
        }

        // 6. Push to remote
        logOperation("Publish Step 4/4", cleanRepoName, "Pushing local commits to GitHub...", OperationLog.Level.INFO);
        String pushResult = gitEngine.push(projectDir, "origin", branchName, token);
        logOperation("Publish Complete", cleanRepoName, "Successfully published to " + cloneUrl + "\n" + pushResult, OperationLog.Level.SUCCESS);

        // Update active repo
        LocalRepository local = new LocalRepository(cleanRepoName, projectDir.getAbsolutePath(), cloneUrl, branchName, true);
        setActiveRepository(local);
    }
}
