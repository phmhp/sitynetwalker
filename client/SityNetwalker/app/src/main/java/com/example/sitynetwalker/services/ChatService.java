package com.example.sitynetwalker.services;

import java.io.*;
import java.net.*;

public class ChatService {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    // 연결 메서드
    public void connect(String serverIp, int port) throws IOException {
        socket = new Socket(serverIp, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

    public void sendNewIssue(String issueType) {
        if (out != null) {
            out.println("NEW_ISSUE|" + issueType);
        }
    }

    public void resolveIssue(String issueType) {
        if (out != null) {
            out.println("RESOLVE_ISSUE|" + issueType);
        }
    }

    public void sendMessage(String issueType, String role, String message) {
        if (out != null) {
            out.println("CHAT|" + issueType + "|" + role + "|" + message);
        }
    }

    public void requestChatHistory(String issueType) {
        if (out != null) {
            out.println("GET_CHAT_HISTORY|" + issueType);
        }
    }

    public void requestUnresolvedCount(String issueType) {
        if (out != null) {
            out.println("GET_UNRESOLVED_COUNT|" + issueType);
        }
    }

    public String receiveMessage() throws IOException {
        String response = in.readLine();
        if (response != null && response.startsWith("UNRESOLVED_COUNT")) {
            String[] parts = response.split("\\|");
            if (parts.length == 3) {
                String issueType = parts[1];
                String unresolvedCount = parts[2];
                return "STATUS|UPDATED|" + issueType + "|" + unresolvedCount;
            }
        }
        return response;
    }

    public void close() throws IOException {
        if (in != null) {
            in.close();
        }
        if (out != null) {
            out.close();
        }
        if (socket != null) {
            socket.close();
        }
    }
}

