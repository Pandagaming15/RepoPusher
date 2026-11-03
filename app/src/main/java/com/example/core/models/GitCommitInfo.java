package com.example.core.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/* JADX INFO: loaded from: classes6.dex */
public class GitCommitInfo implements Serializable {
    private String authorEmail;
    private String authorName;
    private List<String> changedFiles = new ArrayList();
    private String formattedDate;
    private String id;
    private String message;
    private String shortId;
    private long timestamp;

    public GitCommitInfo() {
    }

    public GitCommitInfo(String id, String shortId, String message, String authorName, String authorEmail, long timestamp, String formattedDate) {
        this.id = id;
        this.shortId = shortId;
        this.message = message;
        this.authorName = authorName;
        this.authorEmail = authorEmail;
        this.timestamp = timestamp;
        this.formattedDate = formattedDate;
    }

    public String getId() {
        return this.id != null ? this.id : "";
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShortId() {
        return this.shortId != null ? this.shortId : "";
    }

    public void setShortId(String shortId) {
        this.shortId = shortId;
    }

    public String getMessage() {
        return this.message != null ? this.message : "";
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAuthorName() {
        return this.authorName != null ? this.authorName : "Unknown";
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorEmail() {
        return this.authorEmail != null ? this.authorEmail : "";
    }

    public void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFormattedDate() {
        return this.formattedDate != null ? this.formattedDate : "";
    }

    public void setFormattedDate(String formattedDate) {
        this.formattedDate = formattedDate;
    }

    public List<String> getChangedFiles() {
        return this.changedFiles;
    }

    public void setChangedFiles(List<String> changedFiles) {
        this.changedFiles = changedFiles != null ? changedFiles : new ArrayList<>();
    }
}
