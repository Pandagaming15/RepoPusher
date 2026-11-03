package com.example.core.models;

import java.io.Serializable;

/* JADX INFO: loaded from: classes6.dex */
public class GitHubRepo implements Serializable {
    private String cloneUrl;
    private String defaultBranch;
    private String description;
    private int forksCount;
    private String fullName;
    private String htmlUrl;
    private long id;
    private boolean isPrivate;
    private String language;
    private String name;
    private int stargazersCount;
    private String updatedAt;

    public GitHubRepo() {
    }

    public GitHubRepo(String name, String fullName, String description, boolean isPrivate, String htmlUrl, String cloneUrl, String defaultBranch) {
        this.name = name;
        this.fullName = fullName;
        this.description = description;
        this.isPrivate = isPrivate;
        this.htmlUrl = htmlUrl;
        this.cloneUrl = cloneUrl;
        this.defaultBranch = defaultBranch != null ? defaultBranch : "main";
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return this.name != null ? this.name : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return this.fullName != null ? this.fullName : getName();
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDescription() {
        return this.description != null ? this.description : "";
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isPrivate() {
        return this.isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        this.isPrivate = aPrivate;
    }

    public String getHtmlUrl() {
        return this.htmlUrl != null ? this.htmlUrl : "";
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public String getCloneUrl() {
        return this.cloneUrl != null ? this.cloneUrl : getHtmlUrl() + ".git";
    }

    public void setCloneUrl(String cloneUrl) {
        this.cloneUrl = cloneUrl;
    }

    public String getDefaultBranch() {
        return (this.defaultBranch == null || this.defaultBranch.isEmpty()) ? "main" : this.defaultBranch;
    }

    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch;
    }

    public int getStargazersCount() {
        return this.stargazersCount;
    }

    public void setStargazersCount(int stargazersCount) {
        this.stargazersCount = stargazersCount;
    }

    public int getForksCount() {
        return this.forksCount;
    }

    public void setForksCount(int forksCount) {
        this.forksCount = forksCount;
    }

    public String getLanguage() {
        return this.language != null ? this.language : "";
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getUpdatedAt() {
        return this.updatedAt != null ? this.updatedAt : "";
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
