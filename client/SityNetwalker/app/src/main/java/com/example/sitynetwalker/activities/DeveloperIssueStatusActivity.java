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
    private TextView securityCount;
    private TextView datamanageCount;
    private TextView customerCount;
    private TextView emergencyCount;

    private boolean isRunning = true; // 스레드 실행 상태를 나타내는 플래그

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer_issue_status);

        // UI 요소 초기화
        kioskCount = findViewById(R.id.count_kiosk);
        networkCount = findViewById(R.id.count_network);
        securityCount = findViewById(R.id.count_security);
        datamanageCount = findViewById(R.id.count_datamanage);
        customerCount = findViewById(R.id.count_customer);
        emergencyCount = findViewById(R.id.count_emergency);

        Button kioskButton = findViewById(R.id.button_kiosk);
        Button networkButton = findViewById(R.id.button_network);
        Button securityButton = findViewById(R.id.button_security);
        Button datamanageButton = findViewById(R.id.button_datamanage);
        Button customerButton = findViewById(R.id.button_customer);
        Button emergencyButton = findViewById(R.id.button_emergency);

        chatService = new ChatService(); // ChatService 객체 생성

        // 서버 연결 및 미해결 건수 요청
        new Thread(() -> {
            try {
                chatService.connect("192.168.45.50", 8080);
                if (chatService.isConnected()) {
                    runOnUiThread(this::updateCounts); // 초기화 시 미해결 건수 요청
                    startListeningForUpdates(); // 서버로부터 미해결 건수 업데이트 대기
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // 각 문제 유형 버튼 클릭 시 해당 채팅 화면으로 이동
        kioskButton.setOnClickListener(view -> navigateToChat("키오스크"));
        networkButton.setOnClickListener(view -> navigateToChat("네트워크"));
        securityButton.setOnClickListener(view -> navigateToChat("보안 및 유지 보수"));
        datamanageButton.setOnClickListener(view -> navigateToChat("데이터 품질 관리"));
        customerButton.setOnClickListener(view -> navigateToChat("고객 피드백"));
        emergencyButton.setOnClickListener(view -> navigateToChat("비상 상황"));
    }

    // 채팅 화면으로 이동
    private void navigateToChat(String issueType) {
        Intent intent = new Intent(DeveloperIssueStatusActivity.this, ChatActivity.class);
        intent.putExtra("ROLE", "개발자"); // 역할 정보 전달
        intent.putExtra("ISSUE_TYPE", issueType); // 문제 유형 정보 전달
        startActivity(intent); // ChatActivity 시작
    }

    // 서버로부터 미해결 건수 요청
    private void updateCounts() {
        new Thread(() -> {
            if (chatService.isConnected()) {
                chatService.requestUnresolvedCount("키오스크");  // 키오스크 미해결 건수 요청
                chatService.requestUnresolvedCount("네트워크"); // 네트워크 미해결 건수 요청
                chatService.requestUnresolvedCount("보안 유지 및 보수"); // 보안 유지 및 보수 미해결 건수 요청
                chatService.requestUnresolvedCount("데이터 품질 관리"); // 데이터 품질 관리 미해결 건수 요청
                chatService.requestUnresolvedCount("고객 피드백"); // 고객 피드백 미해결 건수 요청
                chatService.requestUnresolvedCount("비상 상황"); // 비상 상황 미해결 건수 요청
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
                                } else if (issueType.equals("보안 및 유지 보수")) {
                                    securityCount.setText(String.valueOf(newCount));
                                } else if (issueType.equals("데이터 품질 관리")) {
                                    datamanageCount.setText(String.valueOf(newCount));
                                } else if (issueType.equals("고객 피드백")) {
                                    customerCount.setText(String.valueOf(newCount));
                                } else if (issueType.equals("비상 상황")) {
                                    emergencyCount.setText(String.valueOf(newCount));
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

    // 액티비티 종료 시 쓰레드 및 서버 연결 종료
    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false; // 쓰레드 종료
        new Thread(() -> {
            try {
                chatService.close(); // 서버 연결 닫기
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}

