package com.crmbank.erp.crm.mapper.inbound;

import com.crmbank.erp.crm.dto.*;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;
import java.util.Map;

@Mapper
public interface InboundMapper {
    // [1] 고객 및 이력 조회
    Map<String, Object> findCustomerByPhoneMap(Map<String, Object> params);
    Map<String, Object> findCustomerByCustCd(Map<String, Object> params);
    List<Map<String, Object>> selectItemList(Map<String, Object> params);
    List<Map<String, Object>> selectCallHistory(Map<String, Object> params);
    List<Map<String, Object>> selectServiceHistory(Map<String, Object> params);
    List<Map<String, Object>> selectSettleHistory(Map<String, Object> params);

    // 인바운드 상담현황 조회 (HGIA020S)
    List<Map<String, Object>> selectStatusList(Map<String, Object> params);

    // [콜백 관리] HGOA110U 조회 및 저장
    List<Map<String, Object>> selectCallbackList(Map<String, Object> params);
    int updateCallbackResult(Map<String, Object> params);

    // [2] 통합 인터랙션 로그 (MSSQL MERGE 사용)
    int insertTotalInteractionLog(TotalCallLogDto dto);
    int updateCallAnswer(Map<String, Object> params);
    int updateCallHangup(Map<String, Object> params);

    // [3] 기준 정보 및 체크
    String checkWorkMode(Map<String, Object> params);
    String checkHoliday(Map<String, Object> params);
    Map<String, Object> checkAgentStatus(Map<String, Object> params);
    Map<String, Object> getDutyAgent(Map<String, Object> params);
    String checkInternum(Map<String, Object> params);

    // [4] 상담 관리
    String generateSvcNo(Map<String, Object> params);
    int insertCallMst(CallMstDto dto);
    int updateCallMst(CallMstDto dto);
    int updateCallMstAiResult(CallMstDto dto); // 🚀 [추가] AI 요약 결과만 비동기 업데이트
    int deleteCallMst(Map<String, Object> params);
    CallMstDto selectCallMst(Map<String, Object> params);
    int insertEscalation(CtiEscalationDto dto);

    // [5] 녹취 파일 관리
    int insertCallMonitor(CallMonitorDto dto);
    int deleteCallMonitor(Map<String, Object> params);

    // [6] 이관 데이터 관리
    Map<String, Object> selectTransferData(String linkedid);
    int upsertTransferData(Map<String, Object> params);
}