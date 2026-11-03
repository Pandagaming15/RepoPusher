package com.example.core.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import okhttp3.internal.ws.RealWebSocket;
import org.eclipse.jgit.lib.BranchConfig;

/* JADX INFO: loaded from: classes6.dex */
public class ProjectFile implements Serializable {
    private String extension;
    private boolean isDirectory;
    private boolean isIgnored;
    private long lastModified;
    private String name;
    private String path;
    private String relativePath;
    private long sizeBytes;

    public ProjectFile() {
    }

    public ProjectFile(String name, String path, String relativePath, boolean isDirectory, long sizeBytes, long lastModified) {
        this.name = name;
        this.path = path;
        this.relativePath = relativePath;
        this.isDirectory = isDirectory;
        this.sizeBytes = sizeBytes;
        this.lastModified = lastModified;
        if (!isDirectory && name.contains(BranchConfig.LOCAL_REPOSITORY)) {
            this.extension = name.substring(name.lastIndexOf(46) + 1).toLowerCase(Locale.ROOT);
        } else {
            this.extension = isDirectory ? "folder" : "";
        }
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

    public String getRelativePath() {
        return this.relativePath != null ? this.relativePath : "";
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public boolean isDirectory() {
        return this.isDirectory;
    }

    public void setDirectory(boolean directory) {
        this.isDirectory = directory;
    }

    public long getSizeBytes() {
        return this.sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public long getLastModified() {
        return this.lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public String getExtension() {
        return this.extension != null ? this.extension : "";
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public boolean isIgnored() {
        return this.isIgnored;
    }

    public void setIgnored(boolean ignored) {
        this.isIgnored = ignored;
    }

    public String getFormattedSize() {
        if (this.isDirectory) {
            return "--";
        }
        if (this.sizeBytes < RealWebSocket.DEFAULT_MINIMUM_DEFLATE_SIZE) {
            return this.sizeBytes + " B";
        }
        return this.sizeBytes < 1048576 ? String.format(Locale.US, "%.1f KB", Double.valueOf(this.sizeBytes / 1024.0d)) : String.format(Locale.US, "%.2f MB", Double.valueOf(this.sizeBytes / 1048576.0d));
    }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(this.lastModified));
    }
}
