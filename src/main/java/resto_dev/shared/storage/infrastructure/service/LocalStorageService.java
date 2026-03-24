package resto_dev.shared.storage.infrastructure.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import resto_dev.shared.storage.application.port.output.StoragePort;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class LocalStorageService implements StoragePort {

    @Value("${app.storage.local-dir:uploads}")
    private String uploadDir;

    @Value("${app.storage.base-url:http://localhost:8080}")
    private String baseUrl;

    @Override
    public String saveFile(UUID organizationId, String originalFilename, byte[] content) throws IOException {
        String extension = getExtension(originalFilename);
        String filename = UUID.randomUUID().toString() + extension;
        
        Path targetDir = Paths.get(uploadDir, organizationId.toString(), "payments");
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }

        Path targetPath = targetDir.resolve(filename);
        Files.write(targetPath, content);
        
        log.info("File saved to: {}", targetPath);
        
        // Return public URL
        return String.format("%s/uploads/%s/payments/%s", baseUrl, organizationId, filename);
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }
}
