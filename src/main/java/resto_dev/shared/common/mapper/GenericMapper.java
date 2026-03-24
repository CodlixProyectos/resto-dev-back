package resto_dev.shared.common.mapper;

/**
 * Generic interface for mapping between Domain Objects and Entities/DTOs.
 *
 * @param <D> Domain Object
 * @param <E> Entity or DTO
 */
public interface GenericMapper<D, E> {
    D toDomain(E entity);

    E toEntity(D domain);
}
