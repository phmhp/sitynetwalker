package com.example.sitynetwalkerserver;

import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    private ServerSocket serverSocket;
    private final Map<String, List<String>> chatHistory = new HashMap<>();
    private final Map<String, Integer> unresolvedCounts = new HashMap<>();

    public void start(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress());

            // 각 클라이언트 요청을 처리하는 핸들러 생성
            ClientHandler handler = new ClientHandler(clientSocket, chatHistory, unresolvedCounts);
            handler.start();
        }
    }

    public static void main(String[] args) throws IOException {
        ChatServer server = new ChatServer();
        server.start(8080);
    }
}
