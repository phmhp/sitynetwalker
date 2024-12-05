package com.example.sitynetwalkerserver;

public class Issue {
    private String id;
    private String type;
    private String status; // e.g., "REPORTED", "RESOLVED"
    private String reporter;

    public Issue(String id, String type, String status, String reporter) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.reporter = reporter;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReporter() {
        return reporter;
    }
}
