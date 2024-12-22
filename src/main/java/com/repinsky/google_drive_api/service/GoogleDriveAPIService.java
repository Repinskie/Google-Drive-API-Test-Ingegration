package com.repinsky.google_drive_api.service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class GoogleDriveAPIService {
    private final ExcelParser excelParser;
    private final Drive drive;

    public File getFileInfo(String fileId) throws IOException {
        return drive.files().get(fileId).execute();
    }
    public String downloadAndParseFile(String fileId) {
        try {
            File file = getFileInfo(fileId);
            excelParser.setupFile(file);
            return "File processed successfully.";
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }
}
