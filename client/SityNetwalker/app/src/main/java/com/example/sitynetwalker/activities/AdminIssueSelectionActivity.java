package com.example.sitynetwalker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sitynetwalker.R;

public class AdminIssueSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_issue_selection);

        // 이전 화면에서 전달받은 ROLE
        String role = getIntent().getStringExtra("ROLE");

        // 문제 유형 버튼 초기화
        Button kioskButton = findViewById(R.id.button_kiosk);
        Button networkButton = findViewById(R.id.button_network);
        Button securityButton = findViewById(R.id.button_security);
        Button datamanageButton = findViewById(R.id.button_datamanage);
        Button customerButton = findViewById(R.id.button_customer);
        Button emergencyButton = findViewById(R.id.button_emergency);

        // 버튼 클릭 시 처리 로직
        // 선택한 문제 유형에 알맞은 채팅창으로 이동 (사용자의 역할 함께 전달)
        View.OnClickListener listener = view -> {
            Intent intent = new Intent(AdminIssueSelectionActivity.this, ChatActivity.class);
            intent.putExtra("ROLE", role); // 역할 정보 전달
            intent.putExtra("ISSUE_TYPE", ((Button) view).getText().toString()); // 문제 유형 전달
            startActivity(intent);
        };

        // 각 버튼에 클릭 리스너 설정
        kioskButton.setOnClickListener(listener);
        networkButton.setOnClickListener(listener);
        securityButton.setOnClickListener(listener);
        datamanageButton.setOnClickListener(listener);
        customerButton.setOnClickListener(listener);
        emergencyButton.setOnClickListener(listener);
    }
}
