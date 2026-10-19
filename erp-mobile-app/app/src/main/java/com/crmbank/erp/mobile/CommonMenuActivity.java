package com.crmbank.erp.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class CommonMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_menu);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("공통 관리");
        }

        Button btnCustomerRegister = findViewById(R.id.btnCustomerRegister);
        Button btnEmployeeStatus = findViewById(R.id.btnEmployeeStatus);

        btnCustomerRegister.setOnClickListener(v -> {
            Intent intent = new Intent(CommonMenuActivity.this, com.crmbank.erp.mobile.hsba.MHSBA070U.class);
            startActivity(intent);
        });

        if (btnEmployeeStatus != null) {
            btnEmployeeStatus.setOnClickListener(v -> {
                Intent intent = new Intent(CommonMenuActivity.this, com.crmbank.erp.mobile.haba.MHABA920U.class);
                startActivity(intent);
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
