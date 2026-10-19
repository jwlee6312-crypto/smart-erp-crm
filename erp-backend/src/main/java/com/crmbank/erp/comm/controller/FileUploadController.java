package com.crmbank.erp.comm.controller;

import com.crmbank.erp.comm.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/comm/upload")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @PostMapping("/profile")
    public ResponseEntity<?> uploadProfile(
            @RequestParam("file") MultipartFile file, 
            @RequestParam("userid") String userid,
            @RequestParam("cmpycd") String cmpycd) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("파일이 누락되었습니다.");
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            
            // 파일명 중복 방지
            String fileName = "profile_" + userid + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
            
            // 💡 정책 변경: storage/{cmpycd}/profile/
            String subPath = cmpycd + "/profile";
            fileStorageService.saveFileToPath(file, subPath, fileName);

            log.info("📸 [프로필 업로드 성공] 회사: {}, ID: {}, Path: {}", cmpycd, userid, fileName);
            return ResponseEntity.ok(Map.of("filepath", fileName));
        } catch (Exception e) {
            log.error("❌ [프로필 업로드 에러]: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }

    /**
     * 🚀 회사 로고 및 직인 업로드
     */
    @PostMapping("/company")
    public ResponseEntity<?> uploadCompanyFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("cmpycd") String cmpycd,
            @RequestParam("type") String type) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("파일이 누락되었습니다.");
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // 폴더 구조: storage/{cmpycd}/{type}/
            String subPath = cmpycd + "/" + type;
            String fileName = UUID.randomUUID().toString().substring(0, 8) + extension;

            fileStorageService.saveFileToPath(file, subPath, fileName);

            log.info("🏢 [회사 파일 업로드 성공] 회사: {}, 타입: {}, 파일: {}", cmpycd, type, fileName);
            return ResponseEntity.ok(Map.of("filename", fileName));
        } catch (Exception e) {
            log.error("❌ [회사 파일 업로드 에러]: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
