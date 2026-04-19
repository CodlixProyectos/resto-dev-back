package resto_dev.modules.inventory.infrastructure.excel;

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
public class InventoryExcelParser {

    public record InventoryExcelRow(
            String name,
            String categoryName,
            String description,
            String unit,
            BigDecimal initialStock,
            BigDecimal costPrice,
            BigDecimal minStock,
            BigDecimal maxStock
    ) {}

    public List<InventoryExcelRow> parse(MultipartFile file) throws Exception {
        List<InventoryExcelRow> rows = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0); // Primera hoja
            Iterator<Row> rowIterator = sheet.iterator();

            // Saltar cabecera si existe
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                
                // Ignorar filas vacías
                if (isRowEmpty(row)) continue;

                try {
                    String name = getCellValueAsString(row.getCell(0));
                    if (name == null || name.isBlank()) continue; // Nombre es obligatorio

                    String categoryName = getCellValueAsString(row.getCell(1));
                    String description = getCellValueAsString(row.getCell(2));
                    String unit = getCellValueAsString(row.getCell(3));
                    if (unit == null) unit = "un"; // Default unit

                    BigDecimal stock = getCellValueAsBigDecimal(row.getCell(4));
                    BigDecimal price = getCellValueAsBigDecimal(row.getCell(5));
                    BigDecimal minStock = getCellValueAsBigDecimal(row.getCell(6));
                    BigDecimal maxStock = getCellValueAsBigDecimal(row.getCell(7));

                    rows.add(new InventoryExcelRow(name, categoryName, description, unit, stock, price, minStock, maxStock));
                } catch (Exception e) {
                    log.warn("Error parseando fila {}: {}", row.getRowNum(), e.getMessage());
                }
            }
        }

        return rows;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
    }

    private BigDecimal getCellValueAsBigDecimal(Cell cell) {
        if (cell == null) return BigDecimal.ZERO;
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return new BigDecimal(cell.getStringCellValue());
            } catch (Exception e) {
                return BigDecimal.ZERO;
            }
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
