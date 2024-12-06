package com.example.sitynetwalkerserver;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler extends Thread {
    private final Socket socket;
    private final Map<String, List<String>> chatHistory;
    private final Map<String, Integer> unresolvedCounts;
    private PrintWriter out;
    private BufferedReader in;

    public ClientHandler(Socket socket, Map<String, List<String>> chatHistory, Map<String, Integer> unresolvedCounts) {
        this.socket = socket;
        this.chatHistory = chatHistory;
        this.unresolvedCounts = unresolvedCounts;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String input;
            while ((input = in.readLine()) != null) {
                System.out.println("Received command: " + input);
                String[] parts = input.split("\\|");

                if (parts.length < 2) {
                    System.err.println("Invalid command format: " + input);
                    continue;
                }

                String command = parts[0];
                String issueType;

                switch (command) {
                    // 새로운 문제 신고
                    case "NEW_ISSUE":
                        issueType = parts[1];
                        unresolvedCounts.put(issueType, unresolvedCounts.getOrDefault(issueType, 0) + 1);
                        out.println("STATUS|UPDATED|" + issueType + "|" + unresolvedCounts.get(issueType));
                        System.out.println("New issue reported: " + issueType);
                        break;

                        // 문제 해결
                    case "RESOLVE_ISSUE":
                        issueType = parts[1];
                        unresolvedCounts.put(issueType, Math.max(unresolvedCounts.getOrDefault(issueType, 0) - 1, 0));
                        out.println("STATUS|UPDATED|" + issueType + "|" + unresolvedCounts.get(issueType));
                        System.out.println("Issue resolved: " + issueType);
                        break;

                        // 메세지 수신
                    case "CHAT":
                        issueType = parts[1];
                        String sender = parts[2];
                        String message = parts[3];
                        chatHistory.putIfAbsent(issueType, new ArrayList<>());
                        chatHistory.get(issueType).add(sender + ": " + message);
                        out.println("CHAT|RECEIVED|" + sender + ": " + message);
                        System.out.println("Chat message received for " + issueType + ": " + message);
                        break;

                    // 대화 기록 불러오기
                    case "GET_CHAT_HISTORY":
                        issueType = parts[1];
                        List<String> history = chatHistory.getOrDefault(issueType, new ArrayList<>());
                        out.println("CHAT_HISTORY|" + issueType + "|" + String.join(",", history));
                        System.out.println("Chat history sent for " + issueType);
                        break;

                        // 미해결 건수 불러오기
                    case "GET_UNRESOLVED_COUNT":
                        issueType = parts[1];
                        int unresolvedCount = unresolvedCounts.getOrDefault(issueType, 0);
                        out.println("UNRESOLVED_COUNT|" + issueType + "|" + unresolvedCount);
                        System.out.println("Unresolved count sent for " + issueType + ": " + unresolvedCount);
                        break;

                    default:
                        System.err.println("Unknown command: " + command);
                        break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
