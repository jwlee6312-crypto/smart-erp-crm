package com.crmbank.erp.comm.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Service
public class FileStorageService {

    @Value("${STORAGE_PATH:/data/storage/common}")
    private String storageDirectory;

    @PostConstruct
    public void init() {
        File directory = new File(storageDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public void saveFile(MultipartFile fileToSave) throws IOException {
        saveFileWithName(fileToSave, fileToSave.getOriginalFilename());
    }

    public void saveFileWithName(MultipartFile fileToSave, String fileName) throws IOException {
        saveFileToPath(fileToSave, "", fileName);
    }

    public void saveFileToPath(MultipartFile fileToSave, String subPath, String fileName) throws IOException {
        if (fileToSave == null || fileName == null) {
            throw new NullPointerException("fileToSave or fileName is null");
        }

        Path root = Paths.get(storageDirectory);
        Path targetDir = root.resolve(subPath);
        
        if (!targetDir.toFile().exists()) {
            targetDir.toFile().mkdirs();
        }

        Path targetPath = targetDir.resolve(fileName);

        // 보안 체크: 상위 디렉토리 참조 방지
        if (!targetPath.normalize().startsWith(root.normalize())) {
            throw new SecurityException("Unsupported filename!");
        }

        Files.copy(fileToSave.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
    }

    public File getDownloadFile(String fileName) throws Exception {
        if (fileName == null) {
            throw new NullPointerException("fileName is null");
        }
        
        Path root = Paths.get(storageDirectory);
        Path filePath = root.resolve(fileName);

        if (!filePath.normalize().startsWith(root.normalize())) {
            throw new SecurityException("Unsupported filename!");
        }
        
        File fileToDownload = filePath.toFile();
        if (!fileToDownload.exists()) {
            throw new FileNotFoundException("No file named: " + fileName);
        }
        return fileToDownload;
    }
}
