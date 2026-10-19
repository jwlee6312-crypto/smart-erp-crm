package com.crmbank.erp.comm.service;

import com.crmbank.erp.comm.mapper.CommMapper;
import com.crmbank.erp.comm.dto.UserSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommService {

    private final CommMapper commMapper;

    @Transactional("erpTransactionManager")
    public UserSession login(String cmpycd, String userid, String passwd, String ip) throws Exception {
        log.info("🔍 [LOGIN DEBUG START] Input cmpycd: [{}], userid: [{}]", cmpycd, userid);

        // 🚀 [보안 정책] 'smart' 코드로 접근 시 내부적으로 'COIT'으로 강제 전환 처리 (회사명 비공개 원칙)
        String effectiveCmpycd = cmpycd.trim();
        if ("smart".equalsIgnoreCase(effectiveCmpycd)) {
            effectiveCmpycd = "COIT";
        }

        Map<String, Object> param = new HashMap<>();
        param.put("cmpycd", effectiveCmpycd);
        param.put("userid", userid.trim());

        log.info("🔍 [LOGIN DEBUG] Querying GET_COMPANY_INFO with param: {}", param);
        Map<String, Object> companyInfoRaw = commMapper.GET_COMPANY_INFO(param);
        log.info("🔍 [LOGIN DEBUG] companyInfoRaw result: {}", companyInfoRaw);

        if (companyInfoRaw == null) throw new Exception("등록되지 않은 회사아이디 입니다.");
        Map<String, Object> companyInfo = normalize(companyInfoRaw);
        
        log.info("🔍 [LOGIN DEBUG] Querying GET_USER_INFO with param: {}", param);
        Map<String, Object> userInfoRaw = commMapper.GET_USER_INFO(param);
        log.info("🔍 [LOGIN DEBUG] userInfoRaw result: {}", userInfoRaw);

        if (userInfoRaw == null) throw new Exception("사용자 정보를 찾을 수 없습니다.");
        Map<String, Object> userInfo = normalize(userInfoRaw);

        // 🚀 비밀번호 체크 (대소문자 무시 비교)
        String dbPw = String.valueOf(userInfo.getOrDefault("pw", "")).trim();
        if (dbPw.isEmpty()) dbPw = String.valueOf(userInfo.getOrDefault("passwd", "")).trim();
        
        String inputPwEnc = com.crmbank.erp.comm.util.SecurityUtil.encryptSha256(passwd);
        log.info("🔍 [LOGIN DEBUG] Input password SHA-256: [{}], DB password: [{}]", inputPwEnc, dbPw);

        if (!inputPwEnc.equalsIgnoreCase(dbPw)) {
            log.warn("❌ [비밀번호 불일치] ID: {}, Input: {}, DB: {}", userid, inputPwEnc, dbPw);
            throw new Exception("입력하신 비밀번호가 틀립니다.");
        }

        UserSession session = new UserSession();
        // 🚀 [뿌리 수술] 로그인 시점에 DB에서 가져온 모든 정보의 공백을 원천 제거하여 세션 오염 방지
        session.setCmpycd(String.valueOf(companyInfo.getOrDefault("cmpycd", cmpycd)).trim());
        session.setCmpynm(String.valueOf(companyInfo.getOrDefault("cmpynm", "")).trim());
        session.setUserid(String.valueOf(userInfo.getOrDefault("userid", userid)).trim());
        session.setUsernm(String.valueOf(userInfo.getOrDefault("usernm", "")).trim());
        session.setInner_no(String.valueOf(userInfo.getOrDefault("inner_no", "")).trim());
        session.setHpno(String.valueOf(userInfo.getOrDefault("hpno", "")).trim());
        session.setDeptcd(String.valueOf(userInfo.getOrDefault("deptcd", "")).trim());
        session.setDeptnm(String.valueOf(userInfo.getOrDefault("deptnm", "")).trim());
        session.setUsergrp(String.valueOf(userInfo.getOrDefault("usergrp", "")).trim());
        session.setEmail(String.valueOf(userInfo.getOrDefault("email", "")).trim());
        session.setStatus(String.valueOf(userInfo.getOrDefault("status", "10")).trim());
        session.setRouting_mode(String.valueOf(userInfo.getOrDefault("routing_mode", "20")).trim());
        session.setPhoto_path(String.valueOf(userInfo.getOrDefault("photo_path", userInfo.getOrDefault("photopath", userInfo.getOrDefault("photo", "")))).trim());

        param.put("iogbn", "I");
        param.put("ip", ip);
        commMapper.INSERT_LOGIN_HISTORY(param);

        return session;
    }

    private Map<String, Object> normalize(Map<String, Object> map) {
        Map<String, Object> res = new HashMap<>();
        if (map != null) {
            for (String key : map.keySet()) {
                String newKey = key.toLowerCase();
                // 🚀 'A.ITEMCD' 형태의 별칭 제거 (itemcd 로 정규화)
                int dotIdx = newKey.lastIndexOf('.');
                if (dotIdx != -1) newKey = newKey.substring(dotIdx + 1);
                res.put(newKey, map.get(key));
            }
        }
        return res;
    }

    public List<Map<String, Object>> getTopMenus() { return commMapper.GET_TOP_MENU_LIST(); }

    public List<Map<String, Object>> getTopMenusMobile() { return commMapper.GET_TOP_MENU_MOBILE_LIST(); }

    public List<Map<String, Object>> getLeftMenus(String cmpycd, String userid, String upmucd, String usergrp) {
        Map<String, Object> param = new HashMap<>();
        param.put("cmpycd", cmpycd); param.put("userid", userid);
        param.put("upmucd", upmucd); param.put("usergrp", usergrp);
        
        List<Map<String, Object>> rawList = commMapper.HA00_200S_STR(param);
        List<Map<String, Object>> resultList = new ArrayList<>();
        
        if (rawList != null) {
            for (Map<String, Object> row : rawList) {
                resultList.add(normalize(row));
            }
        }
        return resultList;
    }

    public List<Map<String, Object>> getProgramList(Map<String, Object> param) { return commMapper.GET_PROGRAM_LIST(param); }
}
