package com.repinsky.google_drive_api.service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.repinsky.google_drive_api.exception.ExcelException;
import com.repinsky.google_drive_api.model.User;
import com.repinsky.google_drive_api.repository.UserRepository;
import com.repinsky.google_drive_api.util.ExcelHeaderWorker;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ExcelParser {
    private final ApplicationContext applicationContext;
    private final UserRepository userRepository;
    private final Drive drive;

    public void setupFile(File file) {

        String filePath = "downloaded_" + file.getName();
        downloadFile(file.getId(), filePath);

        try (FileInputStream fileInputStream = new FileInputStream(filePath);
             XSSFWorkbook workbook = new XSSFWorkbook(fileInputStream)) {

            Sheet sheet = workbook.getSheet("Лист1");
            if (sheet == null) {
                throw new ExcelException("Sheet 'Лист1' not found. Unable to refresh zones for UPS");
            }

            processingFile(sheet);

        } catch (Exception e) {
            log.error("Error processing file", e);
            throw new ExcelException(e.getMessage());
        }
    }

    private void downloadFile(String fileId, String destinationPath) {
        try (FileOutputStream outputStream = new FileOutputStream(destinationPath)) {
            drive.files().get(fileId).executeMediaAndDownloadTo(outputStream);
        } catch (IOException e) {
            log.error("Error download file", e);
            throw new ExcelException("Failed to download file from Google Drive");
        }
    }

    private void processingFile(Sheet sheet) {
        int numberOfRows = sheet.getPhysicalNumberOfRows();
        boolean headersFound = false;
        Map<String, Integer> filteredHeaderIndices = new HashMap<>();
        int headerRowNumber = 0;
        for (int i = 0; i < numberOfRows; i++) {
            if (sheet.getRow(i).getCell(0).getStringCellValue().equals("Name")) {
                headerRowNumber = i;
                headersFound = true;
                Row row = sheet.getRow(i);
                filteredHeaderIndices = ExcelHeaderWorker.getFilteredHeaders(
                        row, Cell::getStringCellValue, Cell::getColumnIndex);
                break;
            }
        }

        if (!headersFound) {
            throw new ExcelException("Header row for ups zones table not found");
        }

        fileParsing(sheet, filteredHeaderIndices, headerRowNumber, numberOfRows);
    }

    private void fileParsing(Sheet sheet, Map<String, Integer> filteredHeaderIndices, int headerRowNumber, int numberOfRows) {
        List<User> upsZones = new ArrayList<>();

        for (int rowIndex = headerRowNumber + 1; rowIndex < numberOfRows; rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                continue;
            }

            String firstName = ExcelHeaderWorker.getCellValueFromSheet(row, filteredHeaderIndices.get("Name"));

            if (firstName.isEmpty()) {
                continue;
            }

            String lastName = ExcelHeaderWorker.getCellValueFromSheet(row, filteredHeaderIndices.get("LastName"));

            upsZones.add(User.builder()
                    .firstName(firstName)
                    .lastName(lastName)
                    .build());
        }
        ExcelParser proxy = applicationContext.getBean(ExcelParser.class);
        proxy.saveUsersFromExcelFileToDb(upsZones);
    }

    @Transactional
    protected void saveUsersFromExcelFileToDb(List<User> userList) {
        userRepository.deleteAll();
        userRepository.saveAll(userList);
    }
}
