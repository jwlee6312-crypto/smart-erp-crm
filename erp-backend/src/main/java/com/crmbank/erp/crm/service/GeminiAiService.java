package com.crmbank.erp.crm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiAiService {

    @SuppressWarnings("unused")
    @Value("${google.ai.gemini.api-key}")
    private String apiKey;

    @SuppressWarnings("unused")
    @Value("${google.ai.gemini.model-name:gemini-1.5-flash}")
    private String modelName;

    @SuppressWarnings("unused")
    @Value("${asterisk.tts.script.path}")
    private String scriptBaseDir;

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unused")
    @PostConstruct
    public void init() {
        log.info("🚀 [AI] Gemini Service Ready - Model: {}, API Key: {}", modelName, (apiKey != null ? "****" : "Missing"));
        restTemplate.getMessageConverters().addFirst(new StringHttpMessageConverter(StandardCharsets.UTF_8));
    }

    public Map<String, String> analyze(String chatLog, String audioPath) {
        if (audioPath != null && !audioPath.trim().isEmpty()) return analyzeAudio(audioPath, null);
        Map<String, String> result = new HashMap<>();
        result.put("stt", chatLog != null ? chatLog : "");
        result.put("summary", summarizeText(chatLog));
        return result;
    }

    public String summarizeText(String chatLog) {
        if (chatLog == null || chatLog.trim().isEmpty()) return "내용 없음";
        String prompt = "상담 내역을 3줄 요약해줘. JSON 형식 {\"summary\": \"...\"}으로만 응답해:\n\n" + chatLog;
        String aiResponse = callGeminiApi(prompt, Collections.emptyMap());
        return parseField(aiResponse, "summary");
    }

    public Map<String, String> analyzeAudio(String filePath, String customerPhone) {
        Map<String, String> result = new HashMap<>();
        try {
            // 🚀 [해결 핵심] 로컬 Whisper가 물리적 채널을 이미 찢어서(Stereo) 가져옵니다.
            String jsonOutput = executeLocalSttRaw(filePath);
            Map<String, Object> sttResult = parseJson(jsonOutput);
            
            if (sttResult.isEmpty() || !sttResult.containsKey("trb_ment")) {
                throw new RuntimeException("물리적 채널 추출 실패");
            }

            String trb = String.valueOf(sttResult.get("trb_ment"));
            String ans = String.valueOf(sttResult.get("ans_ment"));

            // 1순위 데이터: 물리적으로 분리된 텍스트 보존
            result.put("trb_ment", trb);
            result.put("ans_ment", ans);
            result.put("stt", String.valueOf(sttResult.get("text")));

            // 2순위 데이터: 구글은 이제 '요약'만 담당 (추측 배제)
            try {
                String prompt = "다음 상담 내용을 3줄 요약해줘: " + result.get("stt");
                result.put("summary", summarizeText(prompt));
            } catch (Exception aiEx) {
                result.put("summary", "AI 요약 사용량 초과");
            }
            
        } catch (Exception e) {
            log.error("❌ 하이브리드 분석 실패: {}", e.getMessage());
            result.put("trb_ment", "분석 실패: " + e.getMessage());
            result.put("summary", "오류 발생");
        }
        return result;
    }

    private String executeLocalSttRaw(String filePath) throws Exception {
        String sttScript = scriptBaseDir.replace("generate_tts.py", "stt_whisper.py");
        String[] command = {"python3", sttScript, filePath};
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
            return reader.lines().collect(Collectors.joining());
        }
    }

    private String executeLocalStt(String filePath) throws Exception {
        if (scriptBaseDir == null) throw new RuntimeException("Script directory not configured");
        String sttScript = scriptBaseDir.replace("generate_tts.py", "stt_whisper.py");
        String[] command = {"python3", sttScript, filePath};
        
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        
        String output;
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
            output = reader.lines().collect(Collectors.joining());
        }
        
        Map<String, Object> json = parseJson(output);
        if (Boolean.TRUE.equals(json.get("success"))) {
            return String.valueOf(json.get("text"));
        }
        throw new RuntimeException(String.valueOf(json.get("error")));
    }

    private String callGeminiApi(String prompt, Map<String, Object> audioData) {
        try {
            String url = "https://generativelanguage.googleapis.com/v1/models/" + modelName.replace("models/", "") + ":generateContent?key=" + apiKey.trim();
            
            List<Map<String, Object>> parts = new ArrayList<>();
            if (audioData != null && !audioData.isEmpty()) parts.add(Map.of("inline_data", audioData));
            parts.add(Map.of("text", prompt));

            Map<String, Object> requestBody = Map.of("contents", List.of(Map.of("parts", parts)));
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(URI.create(url), HttpMethod.POST, new HttpEntity<>(requestBody, headers), new ParameterizedTypeReference<>() {});
            
            Map<String, Object> body = response.getBody();
            if (body != null && body.get("candidates") instanceof List<?> candidates && !candidates.isEmpty()) {
                if (candidates.getFirst() instanceof Map<?, ?> first && first.get("content") instanceof Map<?, ?> content) {
                    if (content.get("parts") instanceof List<?> resParts && !resParts.isEmpty() && resParts.getFirst() instanceof Map<?, ?> part) {
                        return String.valueOf(part.get("text"));
                    }
                }
            }
        } catch (Exception e) {
            log.error("❌ Gemini API Error: {}", e.getMessage());
        }
        return "{}";
    }

    private String parseField(String rawText, String field) {
        Map<String, Object> map = parseJson(rawText);
        return String.valueOf(map.getOrDefault(field, rawText));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String text) {
        try {
            int start = text.indexOf("{");
            int end = text.lastIndexOf("}");
            if (start != -1 && end != -1) {
                // 🚀 [해결] 명시적 제네릭 맵으로 파싱하여 Raw type 경고 제거
                return objectMapper.readValue(text.substring(start, end + 1), Map.class);
            }
        } catch (Exception e) {
            log.warn("🧩 JSON Parse Fail: {}", text);
        }
        return Collections.emptyMap();
    }
}
