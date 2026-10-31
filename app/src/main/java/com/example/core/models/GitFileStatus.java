package com.example.core.models;

import java.io.Serializable;

public class GitFileStatus implements Serializable {
    public enum ChangeType {
        MODIFIED,
        ADDED,
        UNTRACKED,
        DELETED,
        MISSING,
        CONFLICT
    }

    private String relativePath;
    private ChangeType changeType;
    private boolean isStaged;
    private long fileSize;

    public GitFileStatus() {}

    public GitFileStatus(String relativePath, ChangeType changeType, boolean isStaged, long fileSize) {
        this.relativePath = relativePath;
        this.changeType = changeType;
        this.isStaged = isStaged;
        this.fileSize = fileSize;
    }

    public String getRelativePath() { return relativePath != null ? relativePath : ""; }
    public void setRelativePath(String relativePath) { this.relativePath = relativePath; }

    public ChangeType getChangeType() { return changeType != null ? changeType : ChangeType.MODIFIED; }
    public void setChangeType(ChangeType changeType) { this.changeType = changeType; }

    public boolean isStaged() { return isStaged; }
    public void setStaged(boolean staged) { isStaged = staged; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getFileName() {
        if (relativePath == null || relativePath.isEmpty()) return "";
        int idx = relativePath.lastIndexOf('/');
        return idx >= 0 ? relativePath.substring(idx + 1) : relativePath;
    }
}
