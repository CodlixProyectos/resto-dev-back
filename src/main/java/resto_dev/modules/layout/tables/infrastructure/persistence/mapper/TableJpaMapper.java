package resto_dev.modules.layout.tables.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import resto_dev.modules.layout.tables.domain.model.Table;
import resto_dev.modules.layout.tables.infrastructure.persistence.entity.TableJpaEntity;
import resto_dev.modules.layout.zones.infrastructure.persistence.entity.ZoneJpaEntity;

@Component
public class TableJpaMapper {

    public Table toDomain(TableJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Table.builder()
                .id(entity.getId())
                .zoneId(entity.getZone() != null ? entity.getZone().getId() : null)
                .tableNumber(entity.getTableNumber())
                .capacity(entity.getCapacity())
                .status(entity.getStatus())
                .active(entity.getActive())
                .posX(entity.getPosX())
                .posY(entity.getPosY())
                .width(entity.getWidth())
                .height(entity.getHeight())
                .rotation(entity.getRotation())
                .shape(entity.getShape())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public TableJpaEntity toEntity(Table table) {
        if (table == null) {
            return null;
        }

        ZoneJpaEntity zone = null;
        if (table.getZoneId() != null) {
            zone = new ZoneJpaEntity();
            zone.setId(table.getZoneId());
        }

        TableJpaEntity entity = TableJpaEntity.builder()
                .zone(zone)
                .tableNumber(table.getTableNumber())
                .capacity(table.getCapacity())
                .status(table.getStatus())
                .active(table.isActive())
                .posX(table.getPosX())
                .posY(table.getPosY())
                .width(table.getWidth())
                .height(table.getHeight())
                .rotation(table.getRotation())
                .shape(table.getShape())
                .build();

        if (table.getId() != null) {
            entity.setId(table.getId());
        }

        entity.setCreatedAt(table.getCreatedAt());
        entity.setUpdatedAt(table.getUpdatedAt());

        return entity;
    }
}
