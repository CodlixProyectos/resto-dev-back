package resto_dev.modules.layout.zones.application.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import resto_dev.modules.layout.zones.application.command.CreateZoneCommand;
import resto_dev.modules.layout.zones.application.command.UpdateZoneCommand;
import resto_dev.modules.layout.zones.application.port.input.CreateZoneUseCase;
import resto_dev.modules.layout.zones.application.port.input.DeleteZoneUseCase;
import resto_dev.modules.layout.zones.application.port.input.ListZonesUseCase;
import resto_dev.modules.layout.zones.application.port.input.UpdateZoneUseCase;
import resto_dev.modules.layout.zones.application.port.output.ZoneRepositoryPort;
import resto_dev.modules.layout.zones.application.query.SearchZonesQuery;
import resto_dev.modules.layout.zones.domain.model.Zone;
import resto_dev.shared.common.pagination.PageModel;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ZoneApplicationService implements
        CreateZoneUseCase,
        ListZonesUseCase,
        UpdateZoneUseCase,
        DeleteZoneUseCase {

    private final ZoneRepositoryPort zoneRepository;

    @Override
    public Zone execute(CreateZoneCommand command) {
        if (zoneRepository.existsByName(command.name())) {
            throw new IllegalArgumentException("Ya existe una Zona con ese nombre.");
        }

        Zone newZone = Zone.builder()
                .name(command.name())
                .description(command.description())
                .active(true)
                .build();

        return zoneRepository.save(newZone);
    }

    @Override
    public PageModel<Zone> execute(SearchZonesQuery query) {
        return zoneRepository.searchZones(query);
    }

    @Override
    public Zone execute(UUID id, UpdateZoneCommand command) {
        Optional<Zone> existingZone = zoneRepository.findById(id);

        if (existingZone.isEmpty()) {
            throw new IllegalArgumentException("No se encontró la Zona con ID: " + id);
        }

        Zone zone = existingZone.get();

        if (!zone.getName().equalsIgnoreCase(command.name()) && zoneRepository.existsByName(command.name())) {
            throw new IllegalArgumentException("Ya existe otra Zona con ese nombre.");
        }

        zone.setName(command.name());
        zone.setDescription(command.description());
        zone.setActive(command.active());

        return zoneRepository.save(zone);
    }

    @Override
    public void execute(UUID id) {
        if (zoneRepository.findById(id).isEmpty()) {
            throw new IllegalArgumentException("No se encontró la Zona con ID: " + id);
        }
        zoneRepository.deleteById(id);
    }
}
