package com.example.core.models;

import java.io.Serializable;

public class GitHubRepo implements Serializable {
    private long id;
    private String name;
    private String fullName;
    private String description;
    private boolean isPrivate;
    private String htmlUrl;
    private String cloneUrl;
    private String defaultBranch;
    private int stargazersCount;
    private int forksCount;
    private String language;
    private String updatedAt;

    public GitHubRepo() {}

    public GitHubRepo(String name, String fullName, String description, boolean isPrivate, String htmlUrl, String cloneUrl, String defaultBranch) {
        this.name = name;
        this.fullName = fullName;
        this.description = description;
        this.isPrivate = isPrivate;
        this.htmlUrl = htmlUrl;
        this.cloneUrl = cloneUrl;
        this.defaultBranch = defaultBranch != null ? defaultBranch : "main";
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name != null ? name : ""; }
    public void setName(String name) { this.name = name; }

    public String getFullName() { return fullName != null ? fullName : getName(); }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getDescription() { return description != null ? description : ""; }
    public void setDescription(String description) { this.description = description; }

    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean aPrivate) { isPrivate = aPrivate; }

    public String getHtmlUrl() { return htmlUrl != null ? htmlUrl : ""; }
    public void setHtmlUrl(String htmlUrl) { this.htmlUrl = htmlUrl; }

    public String getCloneUrl() { return cloneUrl != null ? cloneUrl : getHtmlUrl() + ".git"; }
    public void setCloneUrl(String cloneUrl) { this.cloneUrl = cloneUrl; }

    public String getDefaultBranch() { return defaultBranch != null && !defaultBranch.isEmpty() ? defaultBranch : "main"; }
    public void setDefaultBranch(String defaultBranch) { this.defaultBranch = defaultBranch; }

    public int getStargazersCount() { return stargazersCount; }
    public void setStargazersCount(int stargazersCount) { this.stargazersCount = stargazersCount; }

    public int getForksCount() { return forksCount; }
    public void setForksCount(int forksCount) { this.forksCount = forksCount; }

    public String getLanguage() { return language != null ? language : ""; }
    public void setLanguage(String language) { this.language = language; }

    public String getUpdatedAt() { return updatedAt != null ? updatedAt : ""; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
