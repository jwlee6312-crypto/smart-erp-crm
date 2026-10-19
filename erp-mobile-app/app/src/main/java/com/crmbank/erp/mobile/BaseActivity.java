package com.crmbank.erp.mobile;

import android.content.Intent;
import android.net.Uri;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * 모든 M+PGMID 액티비티의 공통 기반 클래스
 * - 표준 테마 및 브랜딩 적용
 * - 권한 체크 및 공통 네비게이션 관리
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // ActionBar 설정 (뒤로가기 버튼 활성화)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            setupActionBar();
        }
    }

    /**
     * 액션바 타이틀 및 브랜딩 설정 (자식 클래스에서 오버라이드 가능)
     */
    protected void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getProgramTitle());
        }
    }

    /**
     * 공통 기능: 전화 걸기
     */
    protected void makeCall(String phoneNo) {
        if (phoneNo == null || phoneNo.trim().isEmpty()) {
            Toast.makeText(this, "전화번호가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phoneNo.replace("-", "").trim()));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "전화 기능을 실행할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 공통 기능: 메일 보내기 (Gmail 우선)
     */
    protected void sendEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            Toast.makeText(this, "이메일 주소가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        String mailto = "mailto:" + email;
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse(mailto));
        intent.setPackage("com.google.android.gm");
        try {
            startActivity(intent);
        } catch (Exception ex) {
            Intent chooser = new Intent(Intent.ACTION_SENDTO);
            chooser.setData(Uri.parse(mailto));
            startActivity(Intent.createChooser(chooser, "메일 앱을 선택하세요"));
        }
    }

    /**
     * 프로그램 제목 반환 (추상 메서드)
     */
    protected abstract String getProgramTitle();

    /**
     * 프로그램 ID 반환 (M+PGMID)
     */
    protected abstract String getProgramId();

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // ?ㅻ줈媛湲?踰꾪듉 ?숈옉
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
