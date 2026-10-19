package com.crmbank.erp.comm.service;

import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.Margin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class PlaywrightPdfService {

    /**
     * 🚀 [추가] HTML 소스를 직접 PDF로 변환
     */
    public byte[] generatePdfFromHtml(String htmlContent) {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();
            
            // HTML 내용 주입
            page.setContent(htmlContent);
            page.waitForLoadState(LoadState.NETWORKIDLE);

            byte[] pdfBytes = page.pdf(new Page.PdfOptions()
                .setFormat("A4")
                .setPrintBackground(true)
                .setMargin(new Margin().setTop("10mm").setBottom("10mm").setLeft("10mm").setRight("10mm"))
            );
            browser.close();
            return pdfBytes;
        } catch (Exception e) {
            log.error("❌ [PDF] HTML 변환 실패: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public byte[] generatePdfFromUrl(String url, Map<String, String> cookies) {
        String domain = "";
        String scheme = "http";

        try {
            URI uri = new URI(url);
            domain = uri.getHost();
            scheme = uri.getScheme();
        } catch (URISyntaxException e) {
            domain = "127.0.0.1";
            log.warn("URL 파싱 실패, 기본 127.0.0.1 사용: {}", url);
        }

        try (Playwright playwright = Playwright.create()) {
            log.info("[Playwright] 브라우저 실행 시도: {}", url);
            
            Browser browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions()
                    .setHeadless(true)
                    .setArgs(java.util.Arrays.asList(
                        "--no-sandbox",
                        "--disable-dev-shm-usage",
                        "--ignore-certificate-errors",
                        "--disable-gpu",
                        "--disable-web-security",       // 👈 추가: 도메인/포트 간 보안 제한 해제
                        "--single-process"
                    ))
            );

            // 뷰포트 크기 명시 (렌더링 안정화)
            BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(1280, 1024));

            // ✅ 세션 쿠키 주입 로직 수정
            if (cookies != null && !cookies.isEmpty()) {
                List<Cookie> cookieList = new ArrayList<>();

                // 👈 수정: IP 주소(127.0.0.1)일 때는 앞에 점(.)을 붙이면 브라우저가 거부함
                String cookieDomain = domain; 
                
                // 👈 수정: HTTP 환경에서 Secure가 true면 브라우저가 쿠키를 무시함
                boolean isSecure = "https".equalsIgnoreCase(scheme);

                for (Map.Entry<String, String> entry : cookies.entrySet()) {
                    cookieList.add(new Cookie(entry.getKey(), entry.getValue())
                        .setDomain(cookieDomain)
                        .setPath("/")
                        .setSecure(isSecure) 
                        .setHttpOnly(false) // 👈 수정: Vue 앱 호환성을 위해 false 권장
                    );
                }

                context.addCookies(cookieList);
                log.info("[Playwright] 쿠키 주입 완료 - 도메인: {}, Secure: {}, 개수: {}", 
                         cookieDomain, isSecure, cookieList.size());
            }

            Page page = context.newPage();
            Response response = page.navigate(url);

            // 리다이렉트 여부 확인
            if (response != null && response.url().contains("/login")) {
                log.error("⚠️ 인증 실패: 로그인 화면으로 리다이렉트됨. 주입된 도메인을 확인하세요.");
            }

            if (response == null || !response.ok()) {
                log.error("페이지 로드 실패, URL: [{}]", url);
                throw new IllegalArgumentException("PDF 생성용 페이지 로드에 실패했습니다.");
            }

            // ✅ 데이터 바인딩 대기 시간 강화
            page.waitForLoadState(LoadState.NETWORKIDLE); // 네트워크 통신 중단 대기
            page.waitForTimeout(1000); // 👈 추가: Vue 데이터 바인딩을 위한 여유 시간

            byte[] pdfBytes = page.pdf(new Page.PdfOptions()
                .setFormat("A4")
                .setPrintBackground(true)
                .setMargin(new Margin()
                    .setTop("12.7mm").setBottom("12.7mm")
                    .setLeft("12.7mm").setRight("12.7mm")
                )
            );

            browser.close();
            return pdfBytes;
        } catch (Exception e) {
            log.error("PDF 생성 실패 상세: {}", e.toString());
            throw new RuntimeException(e);
        }
    }
}