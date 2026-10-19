package com.crmbank.erp.crm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.crmbank.erp.comm.config.AesConfig;
import com.crmbank.erp.comm.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * [Chatwoot Service - Finalized]
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatwootService {

    private final AesConfig aesConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${chatwoot.api.url:http://localhost:3000}")
    private String chatwootUrl;
    @Value("${chatwoot.account.id:3}")
    private String accountId;
    @Value("${chatwoot.inbox.id:4}")
    private String inboxId;
    @Value("${chatwoot.access.token:FvDEbYNM16jAJGY9hZQ1ymvY}")
    private String adminToken;

    private final Map<String, String> contactIdCache = new ConcurrentHashMap<>();

    private String getPlainEmail(String email) {
        if (email == null) return null;
        String trimmed = email.trim().toLowerCase();
        if (trimmed.contains("@")) return trimmed;
        try { 
            String decrypted = SecurityUtil.decryptAes(trimmed, aesConfig.getAesKey());
            return (decrypted != null) ? decrypted.trim().toLowerCase() : trimmed;
        } catch (Exception e) { return trimmed; }
    }

    private String cleanId(Object id) {
        if (id == null) return null;
        String s = String.valueOf(id);
        return s.contains(".") ? s.split("\\.")[0] : s;
    }

    public List<Map> getMessages(String encryptedEmail) {
        String email = getPlainEmail(encryptedEmail);
        if (email == null || email.isEmpty()) return new ArrayList<>();
        try {
            HttpHeaders headers = createHeaders();
            String contactId = getOrCreateContact(email, "", headers);
            if (contactId == null) return new ArrayList<>();
            String conversationId = getLatestConversationId(contactId, headers);
            if (conversationId == null) return new ArrayList<>();

            String url = String.format("%s/api/v1/accounts/%s/conversations/%s/messages", chatwootUrl, accountId, conversationId);
            ResponseEntity<Object> res = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Object.class);
            
            // Log set to TRACE to stop console flooding
            log.trace("[Chatwoot History] Fetched messages for ConvID: {}", conversationId);
            return (List<Map>) sanitize(extractList(res.getBody()));
        } catch (Exception e) {
            log.error("[Chatwoot] getMessages Error: {}", e.getMessage());
        }
        return new ArrayList<>();
    }

    public void clearHistory(String encryptedEmail) {
        String email = getPlainEmail(encryptedEmail);
        log.info("[Chatwoot] >>> CLEARING HISTORY for: {}", email);
        try {
            HttpHeaders headers = createHeaders();
            String contactId = getOrCreateContact(email, "", headers);
            if (contactId == null) return;

            String url = String.format("%s/api/v1/accounts/%s/contacts/%s/conversations", chatwootUrl, accountId, contactId);
            ResponseEntity<Object> res = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Object.class);
            List<Map> list = extractList(res.getBody());

            if (list != null) {
                String tid = cleanId(inboxId);
                for (Map c : list) {
                    if (tid.equals(cleanId(c.get("inbox_id")))) {
                        String convId = cleanId(c.get("id"));
                        String deleteUrl = String.format("%s/api/v1/accounts/%s/conversations/%s", chatwootUrl, accountId, convId);
                        restTemplate.exchange(deleteUrl, HttpMethod.DELETE, new HttpEntity<>(headers), Map.class);
                        log.info("[Chatwoot] Deleted ConvID: {}", convId);
                    }
                }
            }
            contactIdCache.remove(email);
        } catch (Exception e) {
            log.warn("[Chatwoot] clearHistory notice: {}", e.getMessage());
        }
    }

    public void replyToCustomer(String encryptedEmail, String content) {
        String email = getPlainEmail(encryptedEmail);
        log.info("[Chatwoot] >>> SENDING AGENT REPLY to {}: {}", email, content);
        try {
            HttpHeaders headers = createHeaders();
            String contactId = getOrCreateContact(email, "", headers);
            if (contactId == null) return;
            ensureContactInboxLinked(contactId, headers);
            String conversationId = getOrCreateConversation(contactId, headers);
            if (conversationId != null) {
                updateConversationStatus(conversationId, "open", headers);
                sendMessage(conversationId, content, "outgoing", headers);
            }
        } catch (Exception e) { log.error("[Chatwoot] replyToCustomer failed: {}", e.getMessage()); }
    }

    public void sendInquiry(String encryptedEmail, String name, String content) {
        String email = getPlainEmail(encryptedEmail);
        log.info("[Chatwoot] >>> RECEIVING CUSTOMER INQUIRY from {}: {}", email, content);
        try {
            HttpHeaders headers = createHeaders();
            String contactId = getOrCreateContact(email, name, headers);
            if (contactId == null) return;
            ensureContactInboxLinked(contactId, headers);
            String conversationId = getOrCreateConversation(contactId, headers);
            if (conversationId != null) {
                updateConversationStatus(conversationId, "open", headers);
                sendMessage(conversationId, content, "incoming", headers);
            }
        } catch (Exception e) { log.error("[Chatwoot] sendInquiry failed: {}", e.getMessage()); }
    }

    public void resolveConversation(String encryptedEmail) {
        String email = getPlainEmail(encryptedEmail);
        try {
            HttpHeaders headers = createHeaders();
            String contactId = getOrCreateContact(email, "", headers);
            String conversationId = getLatestConversationId(contactId, headers);
            if (conversationId != null) updateConversationStatus(conversationId, "resolved", headers);
        } catch (Exception e) { }
    }

    private void updateConversationStatus(String conversationId, String status, HttpHeaders headers) {
        try {
            String url = String.format("%s/api/v1/accounts/%s/conversations/%s/toggle_status", chatwootUrl, accountId, conversationId);
            Map<String, String> body = new HashMap<>(); body.put("status", status);
            restTemplate.postForEntity(url, new HttpEntity<>(body, headers), Map.class);
        } catch (Exception e) { }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.set("api_access_token", adminToken);
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    private String getOrCreateContact(String email, String name, HttpHeaders headers) {
        if (contactIdCache.containsKey(email)) return contactIdCache.get(email);
        try {
            String filterUrl = String.format("%s/api/v1/accounts/%s/contacts/filter", chatwootUrl, accountId);
            Map<String, Object> filterBody = new HashMap<>();
            Map<String, Object> item = new HashMap<>();
            item.put("attribute_key", "email"); item.put("filter_operator", "equal_to"); item.put("values", Collections.singletonList(email));
            filterBody.put("payload", Collections.singletonList(item));
            ResponseEntity<Map> res = restTemplate.postForEntity(filterUrl, new HttpEntity<>(filterBody, headers), Map.class);
            List<Map> list = (List<Map>) res.getBody().get("payload");
            if (list != null && !list.isEmpty()) {
                String id = cleanId(list.getFirst().get("id"));
                contactIdCache.put(email, id);
                return id;
            }
            Map<String, Object> body = new HashMap<>();
            body.put("email", email); if (name != null) body.put("name", name); body.put("inbox_id", inboxId);
            ResponseEntity<Map> cRes = restTemplate.postForEntity(chatwootUrl + "/api/v1/accounts/" + accountId + "/contacts", new HttpEntity<>(body, headers), Map.class);
            String id = extractId(cRes.getBody());
            if (id != null) contactIdCache.put(email, id);
            return id;
        } catch (Exception e) { return findContactByListing(email, headers); }
    }

    private String findContactByListing(String email, HttpHeaders headers) {
        try {
            String url = String.format("%s/api/v1/accounts/%s/contacts", chatwootUrl, accountId);
            ResponseEntity<Object> res = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Object.class);
            List<Map> list = extractList(res.getBody());
            for (Map m : list) if (email.equalsIgnoreCase((String) m.get("email"))) return cleanId(m.get("id"));
        } catch (Exception e) { }
        return null;
    }

    private void ensureContactInboxLinked(String contactId, HttpHeaders headers) {
        try {
            String url = String.format("%s/api/v1/accounts/%s/contacts/%s/contact_inboxes", chatwootUrl, accountId, contactId);
            Map<String, Object> body = new HashMap<>(); body.put("inbox_id", inboxId);
            restTemplate.postForEntity(url, new HttpEntity<>(body, headers), Map.class);
        } catch (Exception e) { }
    }

    private String getOrCreateConversation(String contactId, HttpHeaders headers) {
        String existingId = getLatestConversationId(contactId, headers);
        if (existingId != null) return existingId;
        try {
            Map<String, Object> body = new HashMap<>(); body.put("contact_id", contactId); body.put("inbox_id", inboxId);
            ResponseEntity<Map> res = restTemplate.postForEntity(chatwootUrl + "/api/v1/accounts/" + accountId + "/conversations", new HttpEntity<>(body, headers), Map.class);
            return extractId(res.getBody());
        } catch (Exception e) { return null; }
    }

    private String getLatestConversationId(String contactId, HttpHeaders headers) {
        try {
            String url = String.format("%s/api/v1/accounts/%s/contacts/%s/conversations", chatwootUrl, accountId, contactId);
            ResponseEntity<Object> res = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), Object.class);
            List<Map> list = extractList(res.getBody());
            if (list != null && !list.isEmpty()) {
                String tid = cleanId(inboxId);
                list.sort((a, b) -> Long.compare(Long.parseLong(cleanId(b.get("id"))), Long.parseLong(cleanId(a.get("id")))));
                for (Map c : list) if (tid.equals(cleanId(c.get("inbox_id")))) return cleanId(c.get("id"));
            }
        } catch (Exception e) { }
        return null;
    }

    private void sendMessage(String conversationId, String content, String type, HttpHeaders headers) {
        String url = String.format("%s/api/v1/accounts/%s/conversations/%s/messages", chatwootUrl, accountId, conversationId);
        Map<String, Object> body = new HashMap<>(); body.put("content", content); body.put("message_type", type); body.put("private", false);
        log.info("[Chatwoot API CALL] URL: {}, Type: {}, Content: {}", url, type, content);
        restTemplate.postForEntity(url, new HttpEntity<>(body, headers), Map.class);
    }

    private String extractId(Map body) {
        if (body == null) return null;
        Object data = body.containsKey("payload") ? body.get("payload") : body;
        if (data instanceof Map) {
            Map m = (Map) data;
            if (m.containsKey("id")) return cleanId(m.get("id"));
            if (m.containsKey("contact") && m.get("contact") instanceof Map) return cleanId(((Map)m.get("contact")).get("id"));
        }
        return null;
    }

    private List<Map> extractList(Object body) {
        if (body instanceof List) return (List<Map>) body;
        if (body instanceof Map) {
            Map map = (Map) body;
            Object p = map.getOrDefault("payload", map.get("data"));
            if (p instanceof List) return (List<Map>) p;
        }
        return new ArrayList<>();
    }

    private Object sanitize(Object obj) {
        if (obj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) obj;
            Map<String, Object> newMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) newMap.put(entry.getKey(), sanitize(entry.getValue()));
            return newMap;
        } else if (obj instanceof List) {
            List<Object> list = (List<Object>) obj;
            List<Object> newList = new ArrayList<>();
            for (Object item : list) newList.add(sanitize(item));
            return newList;
        } else if (obj instanceof Double) {
            Double d = (Double) obj;
            if (d == d.longValue()) return d.longValue();
        }
        return obj;
    }
}
