package resto_dev.modules.menu.products.infrastructure.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
public class MenuExcelParser {

    public record CategoryExcelRow(
            String name,
            String description
    ) {}

    public record ProductExcelRow(
            String name,
            String description,
            BigDecimal price,
            String categoryName
    ) {}

    public List<CategoryExcelRow> parseCategories(MultipartFile file) throws Exception {
        List<CategoryExcelRow> rows = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip header
            if (rowIterator.hasNext()) rowIterator.next();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (isRowEmpty(row)) continue;

                try {
                    String name = getCellValueAsString(row.getCell(0));
                    if (name == null || name.isBlank()) continue;

                    String description = getCellValueAsString(row.getCell(1));

                    rows.add(new CategoryExcelRow(name, description));
                } catch (Exception e) {
                    log.warn("Error parsing category row {}: {}", row.getRowNum(), e.getMessage());
                }
            }
        }
        return rows;
    }

    public List<ProductExcelRow> parseProducts(MultipartFile file) throws Exception {
        List<ProductExcelRow> rows = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Skip header
            if (rowIterator.hasNext()) rowIterator.next();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (isRowEmpty(row)) continue;

                try {
                    String name = getCellValueAsString(row.getCell(0));
                    if (name == null || name.isBlank()) continue;

                    String description = getCellValueAsString(row.getCell(1));
                    BigDecimal price = getCellValueAsBigDecimal(row.getCell(2));
                    String categoryName = getCellValueAsString(row.getCell(3));

                    rows.add(new ProductExcelRow(name, description, price, categoryName));
                } catch (Exception e) {
                    log.warn("Error parsing product row {}: {}", row.getRowNum(), e.getMessage());
                }
            }
        }
        return rows;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double val = cell.getNumericCellValue();
                if (val == (long) val) {
                    yield String.valueOf((long) val);
                } else {
                    yield String.valueOf(val);
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private BigDecimal getCellValueAsBigDecimal(Cell cell) {
        if (cell == null) return BigDecimal.ZERO;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return BigDecimal.valueOf(cell.getNumericCellValue());
            } else if (cell.getCellType() == CellType.STRING) {
                String val = cell.getStringCellValue().trim();
                return val.isEmpty() ? BigDecimal.ZERO : new BigDecimal(val);
            }
        } catch (Exception e) {
            log.warn("Error parsing numeric cell: {}", e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }
}
