package resto_dev.modules.layout.tables.infrastructure.web.mapper;

import org.springframework.stereotype.Component;
import resto_dev.modules.layout.tables.domain.model.Table;
import resto_dev.modules.layout.tables.infrastructure.web.dto.output.TableEventResponse;

/**
 * Mapper especializado para convertir objetos de dominio en DTOs de eventos SSE.
 * Mantiene la separación de responsabilidades entre la API REST y el sistema de eventos.
 */
@Component
public class TableEventMapper {

    public TableEventResponse toEventResponse(Table table) {
        if (table == null) {
            return null;
        }

        return new TableEventResponse(
                table.getId(),
                table.getZoneId(),
                table.getTableNumber(),
                table.getCapacity(),
                table.getStatus(),
                table.isActive(),
                table.getPosX(),
                table.getPosY(),
                table.getWidth(),
                table.getHeight(),
                table.getRotation(),
                table.getShape());
    }
}
