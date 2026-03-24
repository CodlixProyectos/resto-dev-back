package resto_dev.shared.storage.infrastructure.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import resto_dev.shared.responses.ApiResponse;
import resto_dev.shared.storage.application.port.output.StoragePort;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/storage")
@RequiredArgsConstructor
@Tag(name = "Storage", description = "Endpoints para carga de archivos y comprobantes")
public class StorageController {

    private final StoragePort storagePort;

    @PostMapping("/upload")
    @Operation(summary = "Subir archivo", description = "Carga un archivo (foto/comprobante) y retorna su URL pública.")
    public ResponseEntity<ApiResponse<String>> uploadFile(
            @RequestHeader("X-Organization-Id") UUID orgId,
            @RequestParam("file") MultipartFile file) throws IOException {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Archivo vacío"));
        }

        String url = storagePort.saveFile(orgId, file.getOriginalFilename(), file.getBytes());
        return ResponseEntity.ok(ApiResponse.ok(url, "Archivo cargado exitosamente"));
    }
}
