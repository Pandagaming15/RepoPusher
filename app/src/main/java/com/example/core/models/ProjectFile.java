package com.example.core.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ProjectFile implements Serializable {
    private String name;
    private String path;
    private String relativePath;
    private boolean isDirectory;
    private long sizeBytes;
    private long lastModified;
    private String extension;
    private boolean isIgnored;

    public ProjectFile() {}

    public ProjectFile(String name, String path, String relativePath, boolean isDirectory, long sizeBytes, long lastModified) {
        this.name = name;
        this.path = path;
        this.relativePath = relativePath;
        this.isDirectory = isDirectory;
        this.sizeBytes = sizeBytes;
        this.lastModified = lastModified;
        if (!isDirectory && name.contains(".")) {
            this.extension = name.substring(name.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        } else {
            this.extension = isDirectory ? "folder" : "";
        }
    }

    public String getName() { return name != null ? name : ""; }
    public void setName(String name) { this.name = name; }

    public String getPath() { return path != null ? path : ""; }
    public void setPath(String path) { this.path = path; }

    public String getRelativePath() { return relativePath != null ? relativePath : ""; }
    public void setRelativePath(String relativePath) { this.relativePath = relativePath; }

    public boolean isDirectory() { return isDirectory; }
    public void setDirectory(boolean directory) { isDirectory = directory; }

    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }

    public long getLastModified() { return lastModified; }
    public void setLastModified(long lastModified) { this.lastModified = lastModified; }

    public String getExtension() { return extension != null ? extension : ""; }
    public void setExtension(String extension) { this.extension = extension; }

    public boolean isIgnored() { return isIgnored; }
    public void setIgnored(boolean ignored) { isIgnored = ignored; }

    public String getFormattedSize() {
        if (isDirectory) return "--";
        if (sizeBytes < 1024) return sizeBytes + " B";
        if (sizeBytes < 1024 * 1024) return String.format(Locale.US, "%.1f KB", sizeBytes / 1024.0);
        return String.format(Locale.US, "%.2f MB", sizeBytes / (1024.0 * 1024.0));
    }

    public String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        return sdf.format(new Date(lastModified));
    }
}
