package resto_dev.modules.layout.tables.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import resto_dev.modules.layout.tables.application.command.CreateTableCommand;
import resto_dev.modules.layout.tables.application.command.UpdateTableCommand;
import resto_dev.modules.layout.tables.application.port.input.CreateTableUseCase;
import resto_dev.modules.layout.tables.application.port.input.DeleteTableUseCase;
import resto_dev.modules.layout.tables.application.port.input.ListTablesUseCase;
import resto_dev.modules.layout.tables.application.port.input.UpdateTableUseCase;
import resto_dev.modules.layout.tables.application.port.output.TableRepositoryPort;
import resto_dev.modules.layout.tables.application.query.SearchTablesQuery;
import resto_dev.modules.layout.tables.domain.model.Table;
import resto_dev.modules.layout.tables.domain.model.TableStatus;
import resto_dev.modules.layout.zones.application.port.output.ZoneRepositoryPort;
import resto_dev.shared.common.pagination.PageModel;
import resto_dev.shared.errors.BusinessValidationException;
import resto_dev.shared.errors.DuplicateResourceException;
import resto_dev.shared.errors.ResourceNotFoundException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TableApplicationService implements
        CreateTableUseCase,
        ListTablesUseCase,
        UpdateTableUseCase,
        DeleteTableUseCase {

    private final TableRepositoryPort tableRepository;
    private final ZoneRepositoryPort zoneRepository;

    @Override
    public Table execute(CreateTableCommand command) {
        if (zoneRepository.findById(command.zoneId()).isEmpty()) {
            throw BusinessValidationException.invalidReference("Zona", command.zoneId());
        }

        if (tableRepository.existsByTableNumberAndZoneId(command.tableNumber(), command.zoneId())) {
            throw new DuplicateResourceException("Ya existe una mesa con ese número en esta zona.");
        }

        TableStatus status = command.status() != null ? command.status() : TableStatus.FREE;

        Table newTable = Table.builder()
                .zoneId(command.zoneId())
                .tableNumber(command.tableNumber())
                .capacity(command.capacity())
                .status(status)
                .active(true)
                .build();

        return tableRepository.save(newTable);
    }

    @Override
    public PageModel<Table> execute(SearchTablesQuery query) {
        return tableRepository.searchTables(query);
    }

    @Override
    public Table execute(UUID id, UpdateTableCommand command) {
        Table table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mesa", id));

        if (command.zoneId() != null && !table.getZoneId().equals(command.zoneId())) {
            if (zoneRepository.findById(command.zoneId()).isEmpty()) {
                throw BusinessValidationException.invalidReference("Zona", command.zoneId());
            }
        }

        UUID finalZoneId = command.zoneId() != null ? command.zoneId() : table.getZoneId();

        if (!table.getTableNumber().equalsIgnoreCase(command.tableNumber()) || !table.getZoneId().equals(finalZoneId)) {
            if (tableRepository.existsByTableNumberAndZoneId(command.tableNumber(), finalZoneId)) {
                throw new DuplicateResourceException("Ya existe otra mesa con ese número en esta zona.");
            }
        }

        table.setZoneId(finalZoneId);
        table.setTableNumber(command.tableNumber());
        table.setCapacity(command.capacity());
        table.setStatus(command.status());
        table.setActive(command.active());

        return tableRepository.save(table);
    }

    @Override
    public void execute(UUID id) {
        if (tableRepository.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Mesa", id);
        }
        tableRepository.deleteById(id);
    }
}
