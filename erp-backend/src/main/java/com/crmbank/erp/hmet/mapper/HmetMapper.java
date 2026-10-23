package com.crmbank.erp.hmet.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface HmetMapper {
    List<Map<String, Object>> selectMeetingMaster(Map<String, Object> params);
    List<Map<String, Object>> selectMeetingAttendees(Map<String, Object> params);
    List<Map<String, Object>> selectMeetingFiles(Map<String, Object> params);

    void insertMeetingMaster(Map<String, Object> params);
    void updateMeetingMaster(Map<String, Object> params);
    void deleteMeetingMaster(Map<String, Object> params);

    void insertMeetingAttendee(Map<String, Object> params);
    void deleteMeetingAttendees(@Param("cmpycd") String cmpycd, @Param("meetingid") String meetingid);

    void insertMeetingFile(Map<String, Object> params);
    void deleteMeetingFiles(@Param("cmpycd") String cmpycd, @Param("meetingid") String meetingid);

    String selectMaxMeetingId(@Param("cmpycd") String cmpycd, @Param("meetingdt") String meetingdt);
}
