package com.example.core.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OperationLog implements Serializable {
    public enum Level {
        INFO,
        SUCCESS,
        WARNING,
        ERROR
    }

    private String id;
    private String operation;
    private String repository;
    private String details;
    private Level level;
    private long timestamp;
    private String formattedTime;

    public OperationLog() {}

    public OperationLog(String operation, String repository, String details, Level level) {
        this.id = String.valueOf(System.currentTimeMillis()) + "_" + (int)(Math.random() * 1000);
        this.operation = operation;
        this.repository = repository;
        this.details = details;
        this.level = level;
        this.timestamp = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault());
        this.formattedTime = sdf.format(new Date(this.timestamp));
    }

    public String getId() { return id != null ? id : ""; }
    public void setId(String id) { this.id = id; }

    public String getOperation() { return operation != null ? operation : "Git Operation"; }
    public void setOperation(String operation) { this.operation = operation; }

    public String getRepository() { return repository != null ? repository : ""; }
    public void setRepository(String repository) { this.repository = repository; }

    public String getDetails() { return details != null ? details : ""; }
    public void setDetails(String details) { this.details = details; }

    public Level getLevel() { return level != null ? level : Level.INFO; }
    public void setLevel(Level level) { this.level = level; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getFormattedTime() { return formattedTime != null ? formattedTime : ""; }
    public void setFormattedTime(String formattedTime) { this.formattedTime = formattedTime; }
}
