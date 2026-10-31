package com.example.core.models;

import java.io.Serializable;

public class GitHubUser implements Serializable {
    private String login;
    private long id;
    private String avatarUrl;
    private String name;
    private String email;
    private String bio;
    private int publicRepos;
    private int followers;
    private int following;
    private String htmlUrl;

    public GitHubUser() {}

    public GitHubUser(String login, String name, String avatarUrl, String email, String bio, int publicRepos, String htmlUrl) {
        this.login = login;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.email = email;
        this.bio = bio;
        this.publicRepos = publicRepos;
        this.htmlUrl = htmlUrl;
    }

    public String getLogin() { return login != null ? login : ""; }
    public void setLogin(String login) { this.login = login; }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getAvatarUrl() { return avatarUrl != null ? avatarUrl : ""; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getName() { return name != null && !name.isEmpty() ? name : getLogin(); }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email != null ? email : ""; }
    public void setEmail(String email) { this.email = email; }

    public String getBio() { return bio != null ? bio : ""; }
    public void setBio(String bio) { this.bio = bio; }

    public int getPublicRepos() { return publicRepos; }
    public void setPublicRepos(int publicRepos) { this.publicRepos = publicRepos; }

    public int getFollowers() { return followers; }
    public void setFollowers(int followers) { this.followers = followers; }

    public int getFollowing() { return following; }
    public void setFollowing(int following) { this.following = following; }

    public String getHtmlUrl() { return htmlUrl != null ? htmlUrl : "https://github.com/" + getLogin(); }
    public void setHtmlUrl(String htmlUrl) { this.htmlUrl = htmlUrl; }
}
