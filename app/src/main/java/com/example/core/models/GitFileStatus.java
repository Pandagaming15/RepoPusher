package com.example.core.models;

import java.io.Serializable;

/* JADX INFO: loaded from: classes6.dex */
public class GitFileStatus implements Serializable {
    private ChangeType changeType;
    private long fileSize;
    private boolean isStaged;
    private String relativePath;

    public enum ChangeType {
        MODIFIED,
        ADDED,
        UNTRACKED,
        DELETED,
        MISSING,
        CONFLICT
    }

    public GitFileStatus() {
    }

    public GitFileStatus(String relativePath, ChangeType changeType, boolean isStaged, long fileSize) {
        this.relativePath = relativePath;
        this.changeType = changeType;
        this.isStaged = isStaged;
        this.fileSize = fileSize;
    }

    public String getRelativePath() {
        return this.relativePath != null ? this.relativePath : "";
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public ChangeType getChangeType() {
        return this.changeType != null ? this.changeType : ChangeType.MODIFIED;
    }

    public void setChangeType(ChangeType changeType) {
        this.changeType = changeType;
    }

    public boolean isStaged() {
        return this.isStaged;
    }

    public void setStaged(boolean staged) {
        this.isStaged = staged;
    }

    public long getFileSize() {
        return this.fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileName() {
        if (this.relativePath == null || this.relativePath.isEmpty()) {
            return "";
        }
        int idx = this.relativePath.lastIndexOf(47);
        String str = this.relativePath;
        return idx >= 0 ? str.substring(idx + 1) : str;
    }
}
