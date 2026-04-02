package resto_dev.modules.layout.tables.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import resto_dev.modules.layout.tables.application.command.CreateTableCommand;
import resto_dev.modules.layout.tables.application.command.UpdateTableCommand;
import resto_dev.modules.layout.tables.application.port.input.BulkCreateTableUseCase;
import resto_dev.modules.layout.tables.application.port.input.CreateTableUseCase;
import resto_dev.modules.layout.tables.application.port.input.DeleteTableUseCase;
import resto_dev.modules.layout.tables.application.port.input.ListTablesUseCase;
import resto_dev.modules.layout.tables.application.port.input.UpdateTableUseCase;
import resto_dev.modules.layout.tables.application.port.output.TableEventPublisherPort;
import resto_dev.modules.layout.tables.application.port.output.TableRepositoryPort;
import resto_dev.shared.tenancy.TenantContext;
import resto_dev.modules.layout.tables.application.query.SearchTablesQuery;
import resto_dev.modules.layout.tables.domain.model.Table;
import resto_dev.modules.layout.tables.domain.model.TableStatus;
import resto_dev.modules.layout.tables.infrastructure.web.dto.input.BulkCreateTableRequest;
import resto_dev.modules.layout.zones.application.port.output.ZoneRepositoryPort;
import resto_dev.shared.common.pagination.PageModel;
import resto_dev.shared.errors.BusinessValidationException;
import resto_dev.shared.errors.DuplicateResourceException;
import resto_dev.shared.errors.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TableApplicationService implements
        CreateTableUseCase,
        BulkCreateTableUseCase,
        ListTablesUseCase,
        UpdateTableUseCase,
        DeleteTableUseCase {

    private final TableRepositoryPort tableRepository;
    private final ZoneRepositoryPort zoneRepository;
    private final TableEventPublisherPort tableEventPublisher;

    @Override
    public Table execute(CreateTableCommand command) {
        if (zoneRepository.findById(command.zoneId()).isEmpty()) {
            throw BusinessValidationException.invalidReference("Zona", command.zoneId());
        }

        String tableNumber = command.tableNumber();
        if (tableNumber == null || tableNumber.isBlank() || tableRepository.existsByTableNumberAndZoneId(tableNumber, command.zoneId())) {
            long count = tableRepository.countActiveByZoneId(command.zoneId());
            long nextNum = count + 1;
            tableNumber = String.valueOf(nextNum);
            
            // Ensure uniqueness in this zone (collision resolution)
            while (tableRepository.existsByTableNumberAndZoneId(tableNumber, command.zoneId())) {
                nextNum++;
                tableNumber = String.valueOf(nextNum);
            }
        }

        TableStatus status = command.status() != null ? command.status() : TableStatus.FREE;

        Table newTable = Table.builder()
                .zoneId(command.zoneId())
                .tableNumber(tableNumber)
                .capacity(command.capacity())
                .status(status)
                .active(true)
                .posX(command.posX())
                .posY(command.posY())
                .width(command.width())
                .height(command.height())
                .rotation(command.rotation())
                .shape(command.shape())
                .build();

        Table savedTable = tableRepository.save(newTable);
        tableEventPublisher.publishTableEvent(TenantContext.getCurrentOrganizationId(), savedTable, "TABLE_CREATED");
        return savedTable;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public List<Table> execute(BulkCreateTableRequest request) {
        if (zoneRepository.findById(request.zoneId()).isEmpty()) {
            throw BusinessValidationException.invalidReference("Zona", request.zoneId());
        }

        List<Table> createdTables = new ArrayList<>();
        String baseNumber = request.baseTableNumber();
        int quantity = request.quantity();
        int capacity = request.capacity();

        // Check if the base number is numeric to allow intelligent increment
        boolean isNumeric = baseNumber.matches("\\d+");
        long startNum = isNumeric ? Long.parseLong(baseNumber) : -1;

        for (int i = 0; i < quantity; i++) {
            String currentTableNumber;
            if (isNumeric) {
                currentTableNumber = String.valueOf(startNum + i);
            } else {
                // If not numeric, just append the index if quantity > 1
                currentTableNumber = quantity > 1 ? baseNumber + "-" + (i + 1) : baseNumber;
            }

            CreateTableCommand command = CreateTableCommand.builder()
                    .zoneId(request.zoneId())
                    .tableNumber(currentTableNumber)
                    .capacity(capacity)
                    .status(TableStatus.FREE)
                    .build();

            // We call the existing execute logic to handle auto-generation if collision refers
            // Note: execute(command) already does "if exists -> count + 1"
            // To ensure 1, 2, 3... we should probably check existence here too or rely on the service logic.
            // The service logic will find the next available number if the one we want is taken.
            createdTables.add(this.execute(command));
        }

        return createdTables;
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
                throw new DuplicateResourceException("DEBUG: Colisión detectada al actualizar mesa " + command.tableNumber());
            }
        }

        table.setZoneId(finalZoneId);
        table.setTableNumber(command.tableNumber());
        table.setCapacity(command.capacity());
        table.setStatus(command.status());
        table.setActive(command.active());
        table.setPosX(command.posX());
        table.setPosY(command.posY());
        table.setWidth(command.width());
        table.setHeight(command.height());
        table.setRotation(command.rotation());
        table.setShape(command.shape());

        Table savedTable = tableRepository.save(table);
        tableEventPublisher.publishTableEvent(TenantContext.getCurrentOrganizationId(), savedTable, "TABLE_UPDATED");
        return savedTable;
    }

    @Override
    public void execute(UUID id) {
        Table table = tableRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mesa", id));
        tableRepository.deleteById(id);
        tableEventPublisher.publishTableEvent(TenantContext.getCurrentOrganizationId(), table, "TABLE_DELETED");
    }
}
