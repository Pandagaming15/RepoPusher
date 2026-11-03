package com.example.core.models;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/* JADX INFO: loaded from: classes6.dex */
public class OperationLog implements Serializable {
    private String details;
    private String formattedTime;
    private String id;
    private Level level;
    private String operation;
    private String repository;
    private long timestamp;

    public enum Level {
        INFO,
        SUCCESS,
        WARNING,
        ERROR
    }

    public OperationLog() {
    }

    public OperationLog(String operation, String repository, String details, Level level) {
        this.id = String.valueOf(System.currentTimeMillis()) + "_" + ((int) (Math.random() * 1000.0d));
        this.operation = operation;
        this.repository = repository;
        this.details = details;
        this.level = level;
        this.timestamp = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault());
        this.formattedTime = sdf.format(new Date(this.timestamp));
    }

    public String getId() {
        return this.id != null ? this.id : "";
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOperation() {
        return this.operation != null ? this.operation : "Git Operation";
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getRepository() {
        return this.repository != null ? this.repository : "";
    }

    public void setRepository(String repository) {
        this.repository = repository;
    }

    public String getDetails() {
        return this.details != null ? this.details : "";
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Level getLevel() {
        return this.level != null ? this.level : Level.INFO;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFormattedTime() {
        return this.formattedTime != null ? this.formattedTime : "";
    }

    public void setFormattedTime(String formattedTime) {
        this.formattedTime = formattedTime;
    }
}
