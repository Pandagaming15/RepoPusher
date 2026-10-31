package com.example.core.models;

import java.io.File;
import java.io.Serializable;

public class LocalRepository implements Serializable {
    private String id;
    private String name;
    private String path;
    private String remoteUrl;
    private String currentBranch;
    private boolean isGitInitialized;
    private long lastActive;
    private String description;

    public LocalRepository() {}

    public LocalRepository(String name, String path, String remoteUrl, String currentBranch, boolean isGitInitialized) {
        this.id = path != null ? String.valueOf(path.hashCode()) : String.valueOf(System.currentTimeMillis());
        this.name = name;
        this.path = path;
        this.remoteUrl = remoteUrl != null ? remoteUrl : "";
        this.currentBranch = currentBranch != null ? currentBranch : "main";
        this.isGitInitialized = isGitInitialized;
        this.lastActive = System.currentTimeMillis();
    }

    public String getId() { return id != null ? id : (path != null ? String.valueOf(path.hashCode()) : ""); }
    public void setId(String id) { this.id = id; }

    public String getName() { return name != null ? name : ""; }
    public void setName(String name) { this.name = name; }

    public String getPath() { return path != null ? path : ""; }
    public void setPath(String path) { this.path = path; }

    public String getRemoteUrl() { return remoteUrl != null ? remoteUrl : ""; }
    public void setRemoteUrl(String remoteUrl) { this.remoteUrl = remoteUrl; }

    public String getCurrentBranch() { return currentBranch != null ? currentBranch : "main"; }
    public void setCurrentBranch(String currentBranch) { this.currentBranch = currentBranch; }

    public boolean isGitInitialized() { return isGitInitialized; }
    public void setGitInitialized(boolean gitInitialized) { isGitInitialized = gitInitialized; }

    public long getLastActive() { return lastActive; }
    public void setLastActive(long lastActive) { this.lastActive = lastActive; }

    public String getDescription() { return description != null ? description : ""; }
    public void setDescription(String description) { this.description = description; }

    public boolean exists() {
        return path != null && new File(path).exists();
    }
}
