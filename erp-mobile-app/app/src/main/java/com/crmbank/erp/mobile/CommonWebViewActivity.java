package com.crmbank.erp.mobile;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

/**
 * 🚀 공통 웹뷰 액티비티
 * 네이티브 화면이 없는 경우 서버의 웹 업무 페이지를 직접 로드합니다.
 */
public class CommonWebViewActivity extends BaseActivity {

    private WebView webView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common_web_view);

        String rawPgmid = getIntent().getStringExtra("PGMID");
        String title = getIntent().getStringExtra("TITLE");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(title != null ? title : "Web View");
        }

        // 🚀 웹 라우터 규격에 맞게 언더바 제거 (예: HSIO_010U -> HSIO010U)
        String pgmid = (rawPgmid != null) ? rawPgmid.replace("_", "") : "";

        webView = findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE); // 💡 개발 중이므로 캐시 끔

        webView.setWebViewClient(new WebViewClient());

        // 💡 웹 포트(80)를 사용하여 실제 웹 서비스로 접속
        String url = String.format("http://%s:%s/%s", Config.SERVER_IP, Config.WEB_PORT, pgmid);
        webView.loadUrl(url);
        
        Toast.makeText(this, "웹 화면으로 연결합니다: " + pgmid, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override protected String getProgramTitle() { return "Web View"; }
    @Override protected String getProgramId() { return "WEB_VIEW"; }
}
