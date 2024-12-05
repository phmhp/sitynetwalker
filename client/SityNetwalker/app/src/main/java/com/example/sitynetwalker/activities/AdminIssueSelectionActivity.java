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

        Button kioskButton = findViewById(R.id.button_kiosk);
        Button networkButton = findViewById(R.id.button_network);
        Button otherButton = findViewById(R.id.button_other);

        View.OnClickListener listener = view -> {
            Intent intent = new Intent(AdminIssueSelectionActivity.this, ChatActivity.class);
            intent.putExtra("ROLE", role); // 역할 정보 전달
            intent.putExtra("ISSUE_TYPE", ((Button) view).getText().toString()); // 문제 유형 전달
            startActivity(intent);
        };

        kioskButton.setOnClickListener(listener);
        networkButton.setOnClickListener(listener);
        otherButton.setOnClickListener(listener);
    }
}
