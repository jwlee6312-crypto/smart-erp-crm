package com.crmbank.erp.hmet.service;

import com.crmbank.erp.hmet.mapper.HmetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HmetService {

    private final HmetMapper hmetMapper;

    public List<Map<String, Object>> getMeetingMaster(Map<String, Object> params) {
        return hmetMapper.selectMeetingMaster(params);
    }

    public List<Map<String, Object>> getMeetingAttendees(Map<String, Object> params) {
        return hmetMapper.selectMeetingAttendees(params);
    }

    public List<Map<String, Object>> getMeetingFiles(Map<String, Object> params) {
        return hmetMapper.selectMeetingFiles(params);
    }

    @Transactional
    public String saveMeeting(Map<String, Object> payload) {
        @SuppressWarnings("unchecked")
        Map<String, Object> master = (Map<String, Object>) payload.get("master");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> attendees = (List<Map<String, Object>>) payload.get("attendees");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> files = (List<Map<String, Object>>) payload.get("files");

        String cmpycd = String.valueOf(master.get("cmpycd"));
        String meetingid = String.valueOf(master.get("meetingid"));
        String meetingdt = String.valueOf(master.get("meetingdt")).replace("-", "");
        master.put("meetingdt", meetingdt);

        String actKind = String.valueOf(master.get("actkind")); // A: Add, U: Update

        if ("A".equals(actKind) || meetingid == null || meetingid.isEmpty() || "NEW".equals(meetingid)) {
            meetingid = hmetMapper.selectMaxMeetingId(cmpycd, meetingdt);
            master.put("meetingid", meetingid);
            hmetMapper.insertMeetingMaster(master);
        } else {
            hmetMapper.updateMeetingMaster(master);
        }

        // 참석자 갱신
        hmetMapper.deleteMeetingAttendees(cmpycd, meetingid);
        if (attendees != null) {
            int ser = 1;
            for (Map<String, Object> att : attendees) {
                att.put("cmpycd", cmpycd);
                att.put("meetingid", meetingid);
                att.put("ser", ser++);
                hmetMapper.insertMeetingAttendee(att);
            }
        }

        // 첨부파일 갱신
        if (files != null && !files.isEmpty()) {
            hmetMapper.deleteMeetingFiles(cmpycd, meetingid);
            int fileid = 1;
            for (Map<String, Object> file : files) {
                file.put("cmpycd", cmpycd);
                file.put("meetingid", meetingid);
                file.put("fileid", fileid++);
                hmetMapper.insertMeetingFile(file);
            }
        }

        return meetingid;
    }

    @Transactional
    public void deleteMeeting(Map<String, Object> params) {
        String cmpycd = String.valueOf(params.get("cmpycd"));
        String meetingid = String.valueOf(params.get("meetingid"));
        hmetMapper.deleteMeetingAttendees(cmpycd, meetingid);
        hmetMapper.deleteMeetingFiles(cmpycd, meetingid);
        hmetMapper.deleteMeetingMaster(params);
    }
}
