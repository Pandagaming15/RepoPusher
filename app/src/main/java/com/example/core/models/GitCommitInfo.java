package com.example.core.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GitCommitInfo implements Serializable {
    private String id;
    private String shortId;
    private String message;
    private String authorName;
    private String authorEmail;
    private long timestamp;
    private String formattedDate;
    private List<String> changedFiles = new ArrayList<>();

    public GitCommitInfo() {}

    public GitCommitInfo(String id, String shortId, String message, String authorName, String authorEmail, long timestamp, String formattedDate) {
        this.id = id;
        this.shortId = shortId;
        this.message = message;
        this.authorName = authorName;
        this.authorEmail = authorEmail;
        this.timestamp = timestamp;
        this.formattedDate = formattedDate;
    }

    public String getId() { return id != null ? id : ""; }
    public void setId(String id) { this.id = id; }

    public String getShortId() { return shortId != null ? shortId : ""; }
    public void setShortId(String shortId) { this.shortId = shortId; }

    public String getMessage() { return message != null ? message : ""; }
    public void setMessage(String message) { this.message = message; }

    public String getAuthorName() { return authorName != null ? authorName : "Unknown"; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorEmail() { return authorEmail != null ? authorEmail : ""; }
    public void setAuthorEmail(String authorEmail) { this.authorEmail = authorEmail; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getFormattedDate() { return formattedDate != null ? formattedDate : ""; }
    public void setFormattedDate(String formattedDate) { this.formattedDate = formattedDate; }

    public List<String> getChangedFiles() { return changedFiles; }
    public void setChangedFiles(List<String> changedFiles) { this.changedFiles = changedFiles != null ? changedFiles : new ArrayList<>(); }
}
