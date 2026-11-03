package com.example.core.models;

import java.io.File;
import java.io.Serializable;

/* JADX INFO: loaded from: classes6.dex */
public class LocalRepository implements Serializable {
    private String currentBranch;
    private String description;
    private String id;
    private boolean isGitInitialized;
    private long lastActive;
    private String name;
    private String path;
    private String remoteUrl;
    private String safTreeUri = "";

    public LocalRepository() {
    }

    public LocalRepository(String name, String path, String remoteUrl, String currentBranch, boolean isGitInitialized) {
        this.id = path != null ? String.valueOf(path.hashCode()) : String.valueOf(System.currentTimeMillis());
        this.name = name;
        this.path = path;
        this.remoteUrl = remoteUrl != null ? remoteUrl : "";
        this.currentBranch = currentBranch != null ? currentBranch : "main";
        this.isGitInitialized = isGitInitialized;
        this.lastActive = System.currentTimeMillis();
    }

    public String getId() {
        if (this.id != null) {
            return this.id;
        }
        return this.path != null ? String.valueOf(this.path.hashCode()) : "";
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name != null ? this.name : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return this.path != null ? this.path : "";
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getRemoteUrl() {
        return this.remoteUrl != null ? this.remoteUrl : "";
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    public String getCurrentBranch() {
        return this.currentBranch != null ? this.currentBranch : "main";
    }

    public void setCurrentBranch(String currentBranch) {
        this.currentBranch = currentBranch;
    }

    public boolean isGitInitialized() {
        return this.isGitInitialized;
    }

    public void setGitInitialized(boolean gitInitialized) {
        this.isGitInitialized = gitInitialized;
    }

    public long getLastActive() {
        return this.lastActive;
    }

    public void setLastActive(long lastActive) {
        this.lastActive = lastActive;
    }

    public String getDescription() {
        return this.description != null ? this.description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSafTreeUri() {
        return this.safTreeUri != null ? this.safTreeUri : "";
    }

    public void setSafTreeUri(String safTreeUri) {
        this.safTreeUri = safTreeUri;
    }

    public boolean exists() {
        return this.path != null && new File(this.path).exists();
    }
}
