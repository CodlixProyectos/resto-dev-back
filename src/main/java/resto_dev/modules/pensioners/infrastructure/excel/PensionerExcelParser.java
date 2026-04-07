package resto_dev.modules.pensioners.infrastructure.excel;

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
public class PensionerExcelParser {

    public record PensionerExcelRow(
            String fullName,
            String dni,
            String email,
            String phoneNumber
    ) {}

    public List<PensionerExcelRow> parse(MultipartFile file) throws Exception {
        List<PensionerExcelRow> rows = new ArrayList<>();

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
                    String fullName = getCellValueAsString(row.getCell(0));
                    if (fullName == null || fullName.isBlank()) continue; // Nombre es obligatorio

                    String dni = getCellValueAsString(row.getCell(1));
                    String email = getCellValueAsString(row.getCell(2));
                    String phoneNumber = getCellValueAsString(row.getCell(3));

                    rows.add(new PensionerExcelRow(fullName, dni, email, phoneNumber));
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
