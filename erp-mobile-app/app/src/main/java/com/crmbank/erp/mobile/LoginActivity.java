package com.crmbank.erp.mobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText etCompanyCode;
    private EditText etId;
    private EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // 🚀 성능 최적화를 위해 앱 시작 시 Retrofit 초기화
        RetrofitClient.init(this);
        
        setContentView(R.layout.activity_login);

        etCompanyCode = findViewById(R.id.etCompanyCode);
        etId = findViewById(R.id.etId);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);

        // 기본값 설정 (요청사항: coit)
        etCompanyCode.setText("coit");

        etPassword.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
                attemptLogin();
                return true;
            }
            return false;
        });

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String companyCode = etCompanyCode.getText().toString().trim();
        String id = etId.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (companyCode.isEmpty() || id.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequest loginRequest = new LoginRequest(companyCode, "KOR", id, password);

        ApiService apiService = RetrofitClient.getApiService();
        apiService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    Toast.makeText(LoginActivity.this, "Welcome, " + loginResponse.userName, Toast.LENGTH_SHORT).show();
                    saveUserInfo(loginResponse);
                    
                    Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Login Failed (" + response.code() + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Connection Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserInfo(LoginResponse response) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        if (response.userId != null) editor.putString("userId", response.userId);
        if (response.userName != null) editor.putString("userName", response.userName);
        if (response.cmpycd != null) editor.putString("cmpycd", response.cmpycd);
        if (response.deptcd != null) editor.putString("deptcd", response.deptcd);
        if (response.deptnm != null) editor.putString("deptnm", response.deptnm);
        if (response.usergrp != null) editor.putString("usergrp", response.usergrp);
        if (response.innerNo != null) editor.putString("innerNo", response.innerNo);
        if (response.hpno != null) editor.putString("hpno", response.hpno);
        if (response.email != null) editor.putString("email", response.email);
        
        // 🚀 Cache top menus for instant loading in MenuActivity
        if (response.headerMenus != null) {
            String menusJson = new Gson().toJson(response.headerMenus);
            editor.putString("cachedMenus", menusJson);
        }
        
        editor.apply();
    }
}
