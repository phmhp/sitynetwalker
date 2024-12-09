package com.example.sitynetwalker.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sitynetwalker.R;
import com.example.sitynetwalker.services.ChatService;

import java.io.IOException;

public class ChatActivity extends AppCompatActivity {
    private ChatService chatService; // 서버와의 통신을 위한 ChatService 인스턴스
    private String role; // 관리자/개발자 역할
    private String issueType; // 문제 유형
    private LinearLayout chatContainer; // 채팅 메시지를 표시할 컨테이너

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // UI 요소 초기화
        chatContainer = findViewById(R.id.chat_container);
        EditText messageInput = findViewById(R.id.message_input);
        Button sendButton = findViewById(R.id.send_button);

        // 이전 화면에서 역할과 문제 유형 정보를 받아옴
        Intent intent = getIntent();
        role = intent.getStringExtra("ROLE");
        issueType = intent.getStringExtra("ISSUE_TYPE");

        chatService = new ChatService(); // ChatService 초기화

        // 서버에 연결
        new Thread(() -> {
            try {
                chatService.connect("192.168.45.50", 8080);
                if (chatService.isConnected()) {
                    System.out.println("Successfully connected to the server.");
                    requestChatHistory(); // 채팅 기록 요청
                    startListeningForMessages(); // 메시지 수신 대기 시작
                } else {
                    runOnUiThread(() -> {
                        // 연결 실패 시 알림 표시
                        showToast("Failed to connect to the server. Please check the server status.");
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // 전송 버튼 클릭 시
        sendButton.setOnClickListener(view -> {
            String message = messageInput.getText().toString();
            if (!message.isEmpty()) {
                // UI에 메시지 추가 (보낸 메시지 표시)
                addMessageToUI(role, message);

                // 역할에 따라 서버로 메시지 전송 및 상태 업데이트
                new Thread(() -> {
                    if (role.equals("관리자")) {
                        chatService.sendNewIssue(issueType); // NEW_ISSUE 요청 전송
                    } else if (role.equals("개발자") && message.equals("해결 완료")) {
                        chatService.resolveIssue(issueType); // RESOLVE_ISSUE 요청 전송
                    }
                    chatService.sendMessage(issueType, role, message); // 일반 채팅 메시지 전송
                    requestChatHistory(); // 채팅 기록 요청
                }).start();

                // 로컬 UI에 메시지 추가
                addMessageToUI(role, message);
                messageInput.setText(""); // 입력창 초기화
            }
        });
    }

    // 서버로부터 메시지 수신 대기
    private void startListeningForMessages() {
        new Thread(() -> {
            boolean running = true;
            while (running) {
                try {
                    String response = chatService.receiveMessage();
                    if (response != null) {
                        runOnUiThread(() -> handleServerResponse(response)); // UI에서 처리
                    } else {
                        running = false; // 연결 끊김 시 루프 종료
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    running = false; // 예외 발생 시 루프 종료
                }
            }
        }).start();
    }

    // 서버 응답 처리 (채팅 기록 업데이트)
    private void handleServerResponse(String response) {
        if (response.startsWith("CHAT_HISTORY")) {
            updateChatHistory(response);
        } else if (response.startsWith("CHAT|")) {
            processIncomingMessage(response);
        }
//        if (response.startsWith("CHAT_HISTORY")) {
//            runOnUiThread(() -> {
//                // 채팅 기록 UI 업데이트
//                String[] parts = response.split("\\|", 3);
//                if (parts.length == 3) {
//                    String[] messages = parts[2].split(",");
//                    chatContainer.removeAllViews(); // 기존 채팅 기록 제거
//                    for (String msg : messages) {
//                        String[] messageParts = msg.split(": ", 2);
//                        if (messageParts.length == 2) {
//                            addMessageToUI(messageParts[0], messageParts[1]); // 역할과 메시지 구분
//                        }
//                    }
//                }
//            });
//        }
    }

    // 채팅 기록 요청
    private void requestChatHistory() {
        new Thread(() -> {
            if (chatService.isConnected()) {
                chatService.requestChatHistory(issueType); // 현재 문제 유형의 채팅 기록 요청
            }
        }).start();
    }

    // 채팅 기록 UI 업데이트
    private void updateChatHistory(String response) {
        String[] parts = response.split("\\|", 3);
        if (parts.length == 3) {
            String[] messages = parts[2].split(",");
            chatContainer.removeAllViews(); // 기존 채팅 기록 제거
            for (String msg : messages) {
                String[] messageParts = msg.split(": ", 2);
                if (messageParts.length == 2) {
                    addMessageToUI(messageParts[0], messageParts[1]); // 역할과 메시지 구분
                }
            }
        }
    }

    // 실시간 메시지 UI에 추가
    private void processIncomingMessage(String response) {
        String[] parts = response.split("\\|", 3);
        if (parts.length == 3) {
            String[] messageParts = parts[2].split(": ", 2);
            if (messageParts.length == 2) {
                addMessageToUI(messageParts[0], messageParts[1]);
            }
        }
    }

    // 메시지를 UI에 추가 (글자 크기와 색상 설정 포함)
    private void addMessageToUI(String senderRole, String message) {
        TextView messageView = new TextView(ChatActivity.this);
        messageView.setText(senderRole + ": " + message);

        // 글자 크기 설정
        messageView.setTextSize(20);

        // 역할에 따라 메시지 색상 변경
        if (senderRole.equals("관리자")) {
            messageView.setTextColor(Color.RED); // 관리자 메시지는 빨간색
        } else if (senderRole.equals("개발자") && message.equals("해결 완료")) {
            messageView.setTextColor(Color.BLUE); // 개발자 메시지는 파란색
        } else {
            messageView.setTextColor(Color.BLACK); // 기본 메시지는 검은색
        }

        chatContainer.addView(messageView);
    }

    // 서버로부터 받은 메시지 처리
    private void showToast(String message) {
        // Custom toast implementation to display the message
        Toast.makeText(ChatActivity.this, message, Toast.LENGTH_LONG).show();
    }

//    // 채팅 기록 요청
//    private void requestChatHistory() {
//        chatService.requestChatHistory(issueType);
//    }

    // 액티비티 종료 시 서버와의 연결 닫기
    @Override
    protected void onDestroy() {
        super.onDestroy();
        new Thread(() -> {
            try {
                chatService.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}