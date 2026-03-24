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
import resto_dev.shared.errors.DuplicateResourceException;
import resto_dev.shared.errors.ResourceNotFoundException;

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
        String name = command.name();
        if (name == null || name.isBlank() || zoneRepository.existsByName(name)) {
            long count = zoneRepository.countActive();
            String baseName = (name == null || name.isBlank()) ? "Nivel" : name;
            long nextVal = count + 1;
            name = baseName + " " + nextVal;
            
            // Ensure uniqueness
            while (zoneRepository.existsByName(name)) {
                nextVal++;
                name = baseName + " " + nextVal;
            }
        }

        Zone newZone = Zone.builder()
                .name(name)
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
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zona", id));

        if (!zone.getName().equalsIgnoreCase(command.name()) && zoneRepository.existsByName(command.name())) {
            throw new DuplicateResourceException("zona", "nombre", command.name());
        }

        zone.setName(command.name());
        zone.setDescription(command.description());
        zone.setActive(command.active());
        zone.setEntrancePosX(command.entrancePosX());
        zone.setEntrancePosY(command.entrancePosY());

        return zoneRepository.save(zone);
    }

    @Override
    public void execute(UUID id) {
        if (zoneRepository.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Zona", id);
        }
        zoneRepository.deleteById(id);
    }
}
