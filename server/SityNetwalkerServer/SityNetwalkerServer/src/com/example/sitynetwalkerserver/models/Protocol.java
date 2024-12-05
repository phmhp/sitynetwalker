package com.example.sitynetwalkerserver.models;

public class Protocol {
    public static final String COMMAND_REPORT_ISSUE = "REPORT_ISSUE";
    public static final String COMMAND_FETCH_ISSUES = "FETCH_ISSUES";
    public static final String COMMAND_UPDATE_STATUS = "UPDATE_STATUS";

    public static String createReportIssueCommand(String type, String description) {
        return COMMAND_REPORT_ISSUE + "|" + type + "|" + description;
    }

    public static String createFetchIssuesCommand(String category) {
        return COMMAND_FETCH_ISSUES + "|" + category;
    }

    public static String createUpdateStatusCommand(String issueId, String newStatus) {
        return COMMAND_UPDATE_STATUS + "|" + issueId + "|" + newStatus;
    }
}
