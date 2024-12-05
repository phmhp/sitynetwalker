package com.example.sitynetwalkerserver;

import java.util.*;

public class IssueService {
    private Map<String, Integer> unresolvedCounts = new HashMap<>();
    private Map<String, List<String>> chatHistory = new HashMap<>();

    public IssueService() {
        // 초기 문제 유형 및 미해결 건수 설정
        unresolvedCounts.put("키오스크", 0);
        unresolvedCounts.put("네트워크", 0);
    }

    public synchronized void incrementIssue(String issueType) {
        unresolvedCounts.put(issueType, unresolvedCounts.getOrDefault(issueType, 0) + 1);
    }

    public synchronized void decrementIssue(String issueType) {
        unresolvedCounts.put(issueType, Math.max(0, unresolvedCounts.getOrDefault(issueType, 0) - 1));
    }

    public synchronized int getUnresolvedCount(String issueType) {
        return unresolvedCounts.getOrDefault(issueType, 0);
    }

    public synchronized void addChatMessage(String issueType, String sender, String message) {
        chatHistory.putIfAbsent(issueType, new ArrayList<>());
        chatHistory.get(issueType).add(sender + ": " + message);
    }

    public synchronized List<String> getChatHistory(String issueType) {
        return chatHistory.getOrDefault(issueType, Collections.emptyList());
    }
}
