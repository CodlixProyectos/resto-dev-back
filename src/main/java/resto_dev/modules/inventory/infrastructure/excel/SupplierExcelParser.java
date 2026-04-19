package resto_dev.modules.inventory.infrastructure.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Component
public class SupplierExcelParser {

    public record SupplierExcelRow(
            String name,
            String contactName,
            String phone,
            String email,
            String address
    ) {}

    public List<SupplierExcelRow> parse(MultipartFile file) throws Exception {
        List<SupplierExcelRow> rows = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            // Saltar cabecera
            if (rowIterator.hasNext()) {
                rowIterator.next();
            }

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                if (isRowEmpty(row)) continue;

                try {
                    String name = getCellValueAsString(row.getCell(0));
                    if (name == null || name.isBlank()) continue;

                    String contactName = getCellValueAsString(row.getCell(1));
                    String phone = getCellValueAsString(row.getCell(2));
                    String email = getCellValueAsString(row.getCell(3));
                    String address = getCellValueAsString(row.getCell(4));

                    rows.add(new SupplierExcelRow(name, contactName, phone, email, address));
                } catch (Exception e) {
                    log.warn("Error parseando fila de proveedor {}: {}", row.getRowNum(), e.getMessage());
                }
            }
        }

        return rows;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                }
                // Evitar notación científica para números largos (como teléfonos)
                yield String.format("%.0f", cell.getNumericCellValue());
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> null;
        };
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
