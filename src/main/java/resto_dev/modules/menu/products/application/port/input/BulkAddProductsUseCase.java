package resto_dev.modules.menu.products.application.port.input;

import org.springframework.web.multipart.MultipartFile;
import resto_dev.modules.menu.products.domain.model.Product;

import java.util.List;

public interface BulkAddProductsUseCase {
    List<Product> importProducts(MultipartFile file);
}
