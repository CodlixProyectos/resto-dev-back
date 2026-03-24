package resto_dev.shared.storage.application.port.output;

import java.io.IOException;
import java.util.UUID;

public interface StoragePort {
    /**
     * Saves a file for a specific organization.
     * @return the public URL of the saved file.
     */
    String saveFile(UUID organizationId, String originalFilename, byte[] content) throws IOException;
}
