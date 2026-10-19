package com.crmbank.erp.asterisk.mapper;

import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface AsteriskMapper {
    // 1. 내선번호 (PJSIP)
    List<Map<String, Object>> selectPjsipList(Map<String, Object> params);
    int insertPjsipAuth(Map<String, Object> params);
    int insertPjsipAor(Map<String, Object> params);
    int insertPjsipEndpoint(Map<String, Object> params);
    int deletePjsip(String id);

    // 2. 수신그룹 (Queue)
    List<Map<String, Object>> selectQueueList(Map<String, Object> params);
    int upsertQueue(Map<String, Object> params);
    int deleteQueue(String name);

    // 3. 수신그룹 멤버
    List<Map<String, Object>> selectQueueMemberList(String queue_name);
    int deleteQueueMembers(String queue_name);
    int insertQueueMember(Map<String, Object> params);

    // 4. 다이얼플랜 (Extensions)
    List<Map<String, Object>> selectExtensions(Map<String, Object> params);
    int insertExtension(Map<String, Object> params);
    int deleteExtensions(Map<String, Object> params);

    // 5. ARS 스크립트 관리
    List<Map<String, Object>> selectArsScripts(Map<String, Object> params);
    int upsertArsScript(Map<String, Object> params);

    // 💡 [추가] 전역 변수(업무모드) 관리
    List<Map<String, Object>> selectVariables(Map<String, Object> params);
    int upsertVariable(Map<String, Object> params);
}
