package com.repinsky.google_drive_api.controller;

import com.google.api.services.drive.model.File;
import com.repinsky.google_drive_api.service.GoogleDriveAPIService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/api/google-drive/api-integration")
@RequiredArgsConstructor
public class GoogleDriveAPIIntegrationController {
    private final GoogleDriveAPIService googleDriveAPIService;

    @GetMapping("/process-file") public String processFile(@RequestParam String fileId) {
        return googleDriveAPIService.downloadAndParseFile(fileId);
    }

    @GetMapping("/file-info")
    public File getFileInfo(@RequestParam String fileId) {
        try {
            return googleDriveAPIService.getFileInfo(fileId);
        } catch (Exception e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }


}
