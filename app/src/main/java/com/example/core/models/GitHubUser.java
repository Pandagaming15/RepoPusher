package com.example.core.models;

import java.io.Serializable;

/* JADX INFO: loaded from: classes6.dex */
public class GitHubUser implements Serializable {
    private String avatarUrl;
    private String bio;
    private String email;
    private int followers;
    private int following;
    private String htmlUrl;
    private long id;
    private String login;
    private String name;
    private int publicRepos;

    public GitHubUser() {
    }

    public GitHubUser(String login, String name, String avatarUrl, String email, String bio, int publicRepos, String htmlUrl) {
        this.login = login;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.email = email;
        this.bio = bio;
        this.publicRepos = publicRepos;
        this.htmlUrl = htmlUrl;
    }

    public String getLogin() {
        return this.login != null ? this.login : "";
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAvatarUrl() {
        return this.avatarUrl != null ? this.avatarUrl : "";
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getName() {
        return (this.name == null || this.name.isEmpty()) ? getLogin() : this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return this.email != null ? this.email : "";
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBio() {
        return this.bio != null ? this.bio : "";
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public int getPublicRepos() {
        return this.publicRepos;
    }

    public void setPublicRepos(int publicRepos) {
        this.publicRepos = publicRepos;
    }

    public int getFollowers() {
        return this.followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public int getFollowing() {
        return this.following;
    }

    public void setFollowing(int following) {
        this.following = following;
    }

    public String getHtmlUrl() {
        return this.htmlUrl != null ? this.htmlUrl : "https://github.com/" + getLogin();
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }
}
