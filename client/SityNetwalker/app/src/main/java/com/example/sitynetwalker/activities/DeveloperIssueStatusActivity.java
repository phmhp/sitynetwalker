package com.example.sitynetwalker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.sitynetwalker.R;
import com.example.sitynetwalker.services.ChatService;

import java.io.IOException;

public class DeveloperIssueStatusActivity extends AppCompatActivity {
    private ChatService chatService;
    private TextView kioskCount;
    private TextView networkCount;

    private boolean isRunning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer_issue_status);

        kioskCount = findViewById(R.id.count_kiosk);
        networkCount = findViewById(R.id.count_network);

        Button kioskButton = findViewById(R.id.button_kiosk);
        Button networkButton = findViewById(R.id.button_network);

        chatService = new ChatService();

        // 서버 연결 및 미해결 건수 요청
        new Thread(() -> {
            try {
                chatService.connect("192.168.45.50", 8080);
                if (chatService.isConnected()) {
                    runOnUiThread(this::updateCounts); // 초기화 시 미해결 건수 요청
                    startListeningForUpdates(); // 서버로부터 미해결 건수 업데이트 대기
                } else {
                    //runOnUiThread(() -> showToast("Failed to connect to the server. Please check the server status."));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        kioskButton.setOnClickListener(view -> navigateToChat("키오스크"));
        networkButton.setOnClickListener(view -> navigateToChat("네트워크"));
    }

    // 채팅 화면으로 이동
    private void navigateToChat(String issueType) {
        Intent intent = new Intent(DeveloperIssueStatusActivity.this, ChatActivity.class);
        intent.putExtra("ROLE", "개발자");
        intent.putExtra("ISSUE_TYPE", issueType);
        startActivity(intent);
    }

    // 미해결 건수 업데이트 요청
    private void updateCounts() {
        new Thread(() -> {
            if (chatService.isConnected()) {
                chatService.requestUnresolvedCount("키오스크");  // 키오스크 미해결 건수 요청
                chatService.requestUnresolvedCount("네트워크"); // 네트워크 미해결 건수 요청
            }
        }).start();
    }

    // 서버에서 미해결 건수 업데이트를 받으면 UI에 반영
    private void startListeningForUpdates() {
        new Thread(() -> {
            while (isRunning) {
                try {
                    String response = chatService.receiveMessage(); // 서버에서 메시지 수신
                    if (response != null && response.startsWith("STATUS|UPDATED")) {
                        String[] parts = response.split("\\|");
                        if (parts.length == 4) {
                            String issueType = parts[2];
                            int newCount = Integer.parseInt(parts[3]);

                            // UI 업데이트
                            runOnUiThread(() -> {
                                if (issueType.equals("키오스크")) {
                                    kioskCount.setText(String.valueOf(newCount));
                                } else if (issueType.equals("네트워크")) {
                                    networkCount.setText(String.valueOf(newCount));
                                }
                            });
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    isRunning = false; // 예외 발생 시 종료
                }
            }
        }).start();
    }

    // 화면으로 돌아올 때마다 미해결 건수를 요청하도록 설정
    @Override
    protected void onResume() {
        super.onResume();
        updateCounts(); // 화면에 돌아올 때마다 미해결 건수 업데이트
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false; // 쓰레드 종료
        new Thread(() -> {
            try {
                chatService.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}

