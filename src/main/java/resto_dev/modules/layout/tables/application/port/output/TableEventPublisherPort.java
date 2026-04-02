package resto_dev.modules.layout.tables.application.port.output;

import resto_dev.modules.layout.tables.domain.model.Table;
import java.util.UUID;

public interface TableEventPublisherPort {
    /**
     * Notifica un cambio en el estado o datos de una mesa a todos los interesados de la organización.
     * @param organizationId ID de la organización
     * @param table Objeto mesa actualizado
     * @param eventType Tipo de evento (TABLE_UPDATED, TABLE_CREATED, etc)
     */
    void publishTableEvent(UUID organizationId, Table table, String eventType);
}
