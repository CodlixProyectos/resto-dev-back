package resto_dev.modules.layout.tables.infrastructure.web.mapper;

import org.springframework.stereotype.Component;
import resto_dev.modules.layout.tables.application.command.CreateTableCommand;
import resto_dev.modules.layout.tables.application.command.UpdateTableCommand;
import resto_dev.modules.layout.tables.domain.model.Table;
import resto_dev.modules.layout.tables.infrastructure.web.dto.input.CreateTableRequest;
import resto_dev.modules.layout.tables.infrastructure.web.dto.input.UpdateTableRequest;
import resto_dev.modules.layout.tables.infrastructure.web.dto.output.TableResponse;

@Component
public class TableWebMapper {

    public CreateTableCommand toCommand(CreateTableRequest request) {
        return CreateTableCommand.builder()
                .zoneId(request.zoneId())
                .tableNumber(request.tableNumber())
                .capacity(request.capacity())
                .status(request.status())
                .posX(request.posX())
                .posY(request.posY())
                .width(request.width())
                .height(request.height())
                .rotation(request.rotation())
                .shape(request.shape())
                .build();
    }

    public UpdateTableCommand toCommand(UpdateTableRequest request) {
        return UpdateTableCommand.builder()
                .zoneId(request.zoneId())
                .tableNumber(request.tableNumber())
                .capacity(request.capacity())
                .status(request.status())
                .active(request.active())
                .posX(request.posX())
                .posY(request.posY())
                .width(request.width())
                .height(request.height())
                .rotation(request.rotation())
                .shape(request.shape())
                .build();
    }

    public TableResponse toResponse(Table table) {
        if (table == null) {
            return null;
        }

        return new TableResponse(
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
