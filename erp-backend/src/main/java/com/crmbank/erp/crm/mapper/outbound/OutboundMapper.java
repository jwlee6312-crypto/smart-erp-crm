package com.crmbank.erp.crm.mapper.outbound;

import com.crmbank.erp.crm.dto.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface OutboundMapper {
    // 1. 캠페인 관리
    String generateCampNo(Map<String, Object> params);
    List<Map<String, Object>> selectCampList(Map<String, Object> params);
    int insertCampMst(CampMstDto dto);
    int updateCampMst(CampMstDto dto);
    int deleteCampMst(Map<String, Object> params);

    // 2. 질문 및 답변 관리
    String generateSurvNo(Map<String, Object> params);
    List<Map<String, Object>> selectSurvMstList(Map<String, Object> params);

    List<Map<String, Object>> selectCampSurvMstList(Map<String, Object> params);
    List<Map<String, Object>> selectSurvDtlList(Map<String, Object> params);
    int insertSurvMst(SurvMstDto dto);
    int updateSurvMst(SurvMstDto dto);
    int deleteSurvMst(Map<String, Object> params);
    int insertSurvDtl(SurvDtlDto dto);
    int deleteSurvDtlAll(Map<String, Object> params);

    // 3. 설문 구성 및 코드
    List<Map<String, Object>> selectSurvTypeStats(Map<String, Object> params);
    List<Map<String, Object>> selectCrmCodeList(Map<String, Object> params);
    List<Map<String, Object>> selectMappedQuestions(Map<String, Object> params);
    int insertCampSurvey(Map<String, Object> params);
    int deleteCampSurveyAll(Map<String, Object> params);

    // 4. 콜 리스트 (대문자 Map 수용)
    List<Map<String, Object>> selectCampCallList(Map<String, Object> params);
    // 💡 [변경] 불안정한 배치 대신 단건 인서트를 반복 호출하는 방식으로 변경 (안정성 최우선)
    int insertCampCallList(Map<String, Object> params);
    int deleteCampCallListBatch(Map<String, Object> params);
    int updateCallListStatus(Map<String, Object> params);

    // 5. 속성 및 통계
    List<CampAttrMapperDto> selectAttrMapperList(Map<String, Object> params);
    int insertAttrMapper(CampAttrMapperDto dto);
    int deleteAttrMapperAll(Map<String, Object> params);
    Map<String, Object> selectCampCallStats(Map<String, Object> params);
    List<Map<String, Object>> selectCampDetailStats(Map<String, Object> params);
    List<Map<String, Object>> selectSurvFormList(Map<String, Object> params);

    // 6. 상담 결과 및 로그
    int insertTotalInteractionLog(Map<String, Object> params);
    List<Map<String, Object>> selectCampaignHistory(Map<String, Object> params);
    int insertCampaignRsltMst(CampRsltMstDto dto);
    int insertCampaignRsltDtl(CampRsltDtlDto dto);
    List<Map<String, Object>> selectCampaignRsltDtl(Map<String, Object> params);
    
    // 💡 답변 점수 조회
    BigDecimal selectAnsPoint(Map<String, Object> params);
}
