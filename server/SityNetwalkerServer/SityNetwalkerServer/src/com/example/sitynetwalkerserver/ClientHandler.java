package com.example.sitynetwalkerserver;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClientHandler extends Thread {
    private static final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>()); // 모든 클라이언트 관리
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

            synchronized (clients) {
                clients.add(this); // 클라이언트 추가
            }

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
                        //String newIssueMessage = parts.length > 2 ? parts[2] : "새로운 문제 발생";
                        synchronized (unresolvedCounts) {
                            unresolvedCounts.put(issueType, unresolvedCounts.getOrDefault(issueType, 0) + 1);
                        }
                        broadcast("STATUS|UPDATED|" + issueType + "|" + unresolvedCounts.get(issueType));
                        //broadcast("CHAT|" + issueType + "|관리자|" + newIssueMessage);
                        //saveChat(issueType, "관리자", newIssueMessage);
                        System.out.println("New issue reported: " + issueType);
                        break;

                    // 문제 해결
                    case "RESOLVE_ISSUE":
                        issueType = parts[1];
                        //String resolveMessage = parts.length > 2 ? parts[2] : "문제가 해결되었습니다.";
                        synchronized (unresolvedCounts) {
                            unresolvedCounts.put(issueType, Math.max(unresolvedCounts.getOrDefault(issueType, 0) - 1, 0));
                        }
                        broadcast("STATUS|UPDATED|" + issueType + "|" + unresolvedCounts.get(issueType));
                        //broadcast("CHAT|" + issueType + "|개발자|" + resolveMessage);
                        //saveChat(issueType, "개발자", resolveMessage);
                        System.out.println("Issue resolved: " + issueType);
                        break;

                    // 메세지 수신
                    case "CHAT":
                        issueType = parts[1];
                        String sender = parts[2];
                        String message = parts[3];
                        saveChat(issueType, sender, message);
                        broadcast("CHAT|RECEIVED|" + sender + ": " + message);
                        System.out.println("Chat message received for " + issueType + ": " + message);
                        break;

                    // 대화 기록 불러오기
                    case "GET_CHAT_HISTORY":
                        issueType = parts[1];
                        List<String> history;
                        synchronized (chatHistory) {
                            history = chatHistory.getOrDefault(issueType, new ArrayList<>());
                        }
                        out.println("CHAT_HISTORY|" + issueType + "|" + String.join(",", history));
                        System.out.println("Chat history sent for " + issueType);
                        break;

                    // 미해결 건수 불러오기
                    case "GET_UNRESOLVED_COUNT":
                        issueType = parts[1];
                        int unresolvedCount;
                        synchronized (unresolvedCounts) {
                            unresolvedCount = unresolvedCounts.getOrDefault(issueType, 0);
                        }
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
            closeConnection();
        }
    }

    // 모든 클라이언트에게 메시지 브로드캐스트
    private void broadcast(String message) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }
    }

    // 클라이언트에게 메시지 전송
    private void sendMessage(String message) {
        try {
            out.println(message);
        } catch (Exception e) {
            System.err.println("Failed to send message to client.");
        }
    }

    // 채팅 기록 저장
    private void saveChat(String issueType, String sender, String message) {
        synchronized (chatHistory) {
            chatHistory.putIfAbsent(issueType, new ArrayList<>());
            chatHistory.get(issueType).add(sender + ": " + message);
        }
    }

    // 연결 종료 및 클라이언트 제거
    private void closeConnection() {
        try {
            if (socket != null) {
                socket.close();
            }
            synchronized (clients) {
                clients.remove(this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}