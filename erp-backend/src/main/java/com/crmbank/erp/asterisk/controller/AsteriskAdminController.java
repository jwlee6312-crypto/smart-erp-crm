package com.crmbank.erp.asterisk.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.crmbank.erp.asterisk.mapper.AsteriskMapper;
import com.crmbank.erp.asterisk.service.AsteriskAdminService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/crm/asterisk")
@RequiredArgsConstructor
public class AsteriskAdminController {

    private final AsteriskMapper asteriskMapper;
    private final AsteriskAdminService asteriskAdminService;

    @Value("${asterisk.sounds.path:/var/lib/asterisk/sounds/custom/}")
    private String uploadDir;

    @GetMapping("/extension/template")
    public List<Map<String, Object>> getIvrTemplate() {
        return convertListKeysToLowerCase(asteriskAdminService.getStandardIvrTemplate());
    }

    @PostMapping("/sound/upload")
    public Map<String, Object> uploadSound(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        try {
            Path directory = Paths.get(uploadDir);
            if (!Files.exists(directory)) Files.createDirectories(directory);
            Path targetPath = directory.resolve(file.getOriginalFilename());
            file.transferTo(targetPath);
            log.info("✅ 음원 업로드 성공: {}", targetPath);
            result.put("success", true);
        } catch (IOException e) {
            log.error("❌ 음원 업로드 실패: {}", e.getMessage());
            result.put("success", false);
        }
        return result;
    }

    @GetMapping("/pjsip/search")
    public List<Map<String, Object>> searchPjsip(@RequestParam Map<String, Object> params) {
        return convertListKeysToLowerCase(asteriskMapper.selectPjsipList(params));
    }

    @PostMapping("/pjsip/save")
    public void savePjsip(@RequestBody List<Map<String, Object>> list) {
        asteriskAdminService.savePjsip(list);
    }

    @GetMapping("/queue/search")
    public List<Map<String, Object>> searchQueues(@RequestParam Map<String, Object> params) {
        return convertListKeysToLowerCase(asteriskMapper.selectQueueList(params));
    }

    @GetMapping("/queue/member/search")
    public List<Map<String, Object>> searchQueueMembers(@RequestParam("queue_name") String queue_name) {
        return convertListKeysToLowerCase(asteriskMapper.selectQueueMemberList(queue_name));
    }

    @PostMapping("/queue/member/save")
    public void saveQueueMembers(@RequestBody Map<String, Object> params) {
        String queueName = String.valueOf(params.get("queue_name"));
        List<Map<String, Object>> members = (List<Map<String, Object>>) params.get("members");
        asteriskAdminService.saveQueueMembers(queueName, members);
    }

    @PostMapping("/queue/save")
    public void saveQueue(@RequestBody List<Map<String, Object>> list) {
        asteriskAdminService.saveQueues(list);
    }

    @GetMapping("/extension/search")
    public List<Map<String, Object>> searchExtensions(@RequestParam Map<String, Object> params) {
        return convertListKeysToLowerCase(asteriskMapper.selectExtensions(params));
    }

    @PostMapping("/extension/save")
    public void saveExtensions(@RequestBody List<Map<String, Object>> list) {
        asteriskAdminService.saveExtensions(list);
    }

    @GetMapping("/script/search")
    public List<Map<String, Object>> searchScripts(@RequestParam Map<String, Object> params) {
        List<Map<String, Object>> scripts = convertListKeysToLowerCase(asteriskMapper.selectArsScripts(params));
        scripts.forEach(script -> {
            try {
                String id = String.valueOf(script.get("id"));
                script.put("file_exists", Files.exists(Paths.get(uploadDir, id + ".wav")));
            } catch (Exception e) { script.put("file_exists", false); }
        });
        return scripts;
    }

    @PostMapping("/script/save")
    public void saveScripts(@RequestBody List<Map<String, Object>> list) {
        asteriskAdminService.saveArsScripts(list, "admin");
    }

    @GetMapping("/variable/search")
    public List<Map<String, Object>> searchVariables(@RequestParam Map<String, Object> params) {
        return convertListKeysToLowerCase(asteriskMapper.selectVariables(params));
    }

    @PostMapping("/variable/save")
    public void saveVariables(@RequestBody List<Map<String, Object>> list) {
        for (Map<String, Object> data : list) {
            asteriskMapper.upsertVariable(data);
        }
    }

    private List<Map<String, Object>> convertListKeysToLowerCase(List<Map<String, Object>> list) {
        if (list == null) return new ArrayList<>();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> map : list) {
            Map<String, Object> lowerMap = new HashMap<>();
            map.forEach((k, v) -> lowerMap.put(k.toLowerCase(), v));
            result.add(lowerMap);
        }
        return result;
    }
}
