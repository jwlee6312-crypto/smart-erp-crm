package com.crmbank.erp.crm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.crmbank.erp.comm.config.AesConfig;
import com.crmbank.erp.comm.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OmniChatwootService {

    private final AesConfig aesConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${chatwoot.api.url:http://localhost:3000}")
    private String chatwootUrl;

    @Value("${chatwoot.account.id:3}")
    private String accountId;

    @Value("${chatwoot.inbox.id:4}") // 💡 Inbox ID 4로 변경 (Campaign API Channel)
    private String inboxId;

    @Value("${chatwoot.access.token}")
    private String adminToken;

    private String cachedAutoInboxId = null;

    public String syncAndGetPlainEmail(String emailData, String userName) {
        if (emailData == null || emailData.isEmpty()) return null;
        String plainEmail = emailData;
        if (!emailData.contains("@")) { 
            try { plainEmail = SecurityUtil.decryptAes(emailData, aesConfig.getAesKey()); } catch (Exception e) { }
        }
        return plainEmail;
    }

    public List<Map> getMessages(String email) {
        try {
            String contactId = getOrCreateContact(email, "", createHeaders());
            if (contactId == null) return new ArrayList<>();
            String conversationId = getOpenConversationId(contactId, createHeaders());
            if (conversationId == null) return new ArrayList<>();

            String url = String.format("%s/api/v1/accounts/%s/conversations/%s/messages", chatwootUrl, accountId, conversationId);
            ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(createHeaders()), Map.class);
            if (res.getBody() != null && res.getBody().containsKey("payload")) return (List<Map>) res.getBody().get("payload");
        } catch (Exception e) { log.error("❌ [OmniChat] 메시지 조회 실패: {}", e.getMessage()); }
        return new ArrayList<>();
    }

    public void replyToCustomer(String email, String content) {
        try {
            HttpHeaders headers = createHeaders();
            String contactId = getOrCreateContact(email, "", headers);
            String conversationId = getOrCreateConversation(contactId, headers);
            if (conversationId != null) sendMessage(conversationId, content, "outgoing", headers);
        } catch (Exception e) { log.error("❌ [OmniChat] 답장 실패"); }
    }

    public void sendInquiry(String email, String name, String content) {
        try {
            HttpHeaders headers = createHeaders();
            String contactId = getOrCreateContact(email, name, headers);
            String conversationId = getOrCreateConversation(contactId, headers);
            if (conversationId != null) sendMessage(conversationId, content, "incoming", headers);
        } catch (Exception e) { log.error("❌ [OmniChat] 문의 실패"); }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("api_access_token", adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private String getOrCreateContact(String email, String name, HttpHeaders headers) {
        try {
            String searchUrl = String.format("%s/api/v1/accounts/%s/contacts/search?q=%s", chatwootUrl, accountId, email);
            ResponseEntity<Map> res = restTemplate.exchange(searchUrl, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            if (res.getBody() != null && res.getBody().containsKey("payload")) {
                List<Map> payload = (List<Map>) res.getBody().get("payload");
                if (!payload.isEmpty()) return payload.get(0).get("id").toString();
            }
            Map<String, Object> body = new HashMap<>();
            body.put("email", email); if (name != null) body.put("name", name);
            ResponseEntity<Map> createRes = restTemplate.postForEntity(chatwootUrl + "/api/v1/accounts/" + accountId + "/contacts", new HttpEntity<>(body, headers), Map.class);
            if (createRes.getBody() != null) return ((Map)createRes.getBody().get("payload")).get("id").toString();
        } catch (Exception e) { }
        return null;
    }

    private String getOrCreateConversation(String contactId, HttpHeaders headers) {
        String existingId = getOpenConversationId(contactId, headers);
        if (existingId != null) return existingId;
        String targetInboxId = (cachedAutoInboxId != null) ? cachedAutoInboxId : inboxId;
        try {
            return createConversationWithId(contactId, targetInboxId, headers);
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                cachedAutoInboxId = findFirstValidInboxId(headers);
                if (cachedAutoInboxId != null) return createConversationWithId(contactId, cachedAutoInboxId, headers);
            }
        }
        return null;
    }

    private String createConversationWithId(String contactId, String id, HttpHeaders headers) {
        String url = String.format("%s/api/v1/accounts/%s/conversations", chatwootUrl, accountId);
        Map<String, Object> body = new HashMap<>();
        body.put("contact_id", contactId); body.put("inbox_id", id);
        ResponseEntity<Map> res = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), Map.class);
        Object data = res.getBody().get("payload") != null ? res.getBody().get("payload") : res.getBody();
        return ((Map)data).get("id").toString();
    }

    private String findFirstValidInboxId(HttpHeaders headers) {
        try {
            String url = String.format("%s/api/v1/accounts/%s/inboxes", chatwootUrl, accountId);
            ResponseEntity<Object> res = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Object.class);
            List<Map> inboxes = (res.getBody() instanceof List) ? (List<Map>) res.getBody() : (List<Map>) ((Map)res.getBody()).get("payload");
            if (inboxes != null && !inboxes.isEmpty()) return inboxes.get(0).get("id").toString();
        } catch (Exception e) { }
        return null;
    }

    private String getOpenConversationId(String contactId, HttpHeaders headers) {
        try {
            String url = String.format("%s/api/v1/accounts/%s/contacts/%s/conversations", chatwootUrl, accountId, contactId);
            ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            if (res.getBody() != null && res.getBody().containsKey("payload")) {
                List<Map> payload = (List<Map>) res.getBody().get("payload");
                for (Map conv : payload) if ("open".equals(conv.get("status"))) return conv.get("id").toString();
            }
        } catch (Exception e) { }
        return null;
    }

    private void sendMessage(String conversationId, String content, String type, HttpHeaders headers) {
        String url = String.format("%s/api/v1/accounts/%s/conversations/%s/messages", chatwootUrl, accountId, conversationId);
        Map<String, Object> body = new HashMap<>();
        body.put("content", content); body.put("message_type", type);
        restTemplate.postForEntity(url, new HttpEntity<>(body, headers), Map.class);
    }
}
