package com.example.sitynetwalker.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sitynetwalker.R;

public class RoleSelectionActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_role_selection);

        // 관리자 역할 선택 시
        findViewById(R.id.adminButton).setOnClickListener(view -> {
            Intent intent = new Intent(RoleSelectionActivity.this, AdminIssueSelectionActivity.class);
            intent.putExtra("ROLE", "관리자"); // 역할 전달
            startActivity(intent);
        });

        // 개발자 역할 선택 시
        findViewById(R.id.developerButton).setOnClickListener(view -> {
            Intent intent = new Intent(RoleSelectionActivity.this, DeveloperIssueStatusActivity.class);
            intent.putExtra("ROLE", "개발자"); // 역할 전달
            startActivity(intent);
        });
    }
}
