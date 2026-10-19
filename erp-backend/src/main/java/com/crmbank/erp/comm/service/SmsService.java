package com.crmbank.erp.comm.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
public class SmsService {

    // 💡 [대행사 연동 정보] 추후 대행사에서 제공받은 정보를 여기에 입력하세요.
    private final String SMS_API_URL = "https://api.대행사주소.com/send"; // 예: aligo, ppurio 등
    private final String API_KEY = "YOUR_API_KEY";
    private final String SENDER_NO = "02-123-4567"; // SmartCore 발신 번호

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 실제로 문자를 발송하는 로직
     */
    public boolean sendSms(String to, String content) {
        log.info("--------------------------------------------------");
        log.info("[SMS 발송 요청] 수신: {}, 내용: {}", to, content);
        
        try {
            // 💡 [API 연동 샘플] 대부분의 대행사는 JSON 형태의 POST 요청을 사용합니다.
            /*
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("key", API_KEY);
            body.put("receiver", to);
            body.put("sender", SENDER_NO);
            body.put("msg", content);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(SMS_API_URL, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("SMS 발송 성공: {}", response.getBody());
                return true;
            }
            */
            
            log.info("현재는 개발 모드이므로 로그로만 출력합니다.");
            log.info("--------------------------------------------------");
            return true; 

        } catch (Exception e) {
            log.error("SMS 발송 중 오류 발생: {}", e.getMessage());
            return false;
        }
    }
}
