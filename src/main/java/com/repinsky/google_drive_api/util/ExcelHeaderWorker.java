package com.repinsky.google_drive_api.util;

import com.repinsky.google_drive_api.exception.ExcelException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import java.util.*;
import java.util.function.Function;

public class ExcelHeaderWorker {
    public static <K, V> Map<K, V> getFilteredHeaders(Row headerRow, Function<Cell, K> keyMapper, Function<Cell, V> valueMapper, String...
            unExpectedHeaders) {
        Set<String> unExpectedHeaderSet = new HashSet<>(Arrays.asList(unExpectedHeaders));
        Map<K, V> filteredHeaders = new HashMap<>();
        for (Cell cell : headerRow) {
            if (cell.getCellType() == CellType.BLANK) {
                continue;
            }
            if (cell.getCellType() == CellType.NUMERIC) {
                continue;
            }
            if (cell.getCellType() == CellType.FORMULA){
                continue;
            }
            String header = cell.getStringCellValue();
            if (unExpectedHeaderSet.contains(header) || header.isEmpty()) {
                continue;
            }
            filteredHeaders.put(keyMapper.apply(cell), valueMapper.apply(cell));
        }
        return filteredHeaders;
    }

    public static String getCellValueFromSheet(Row row, int pointer) {
        Cell cell = row.getCell(pointer);
        if (cell == null) {
            return "";
        } else if (cell.getCellType() == CellType.NUMERIC) {
            if (cell.getCellStyle().getDataFormatString().contains("%")) {
                return String.valueOf(cell.getNumericCellValue() * 100);
            } else {
                return String.valueOf(cell.getNumericCellValue());
            }
        } else if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.BLANK) {
            return "";
        } else if (cell.getCellType() == CellType.FORMULA) {
            return "";
        } else {
            throw new ExcelException("Unexpected cell type: " + cell.getCellType());
        }
    }
}
