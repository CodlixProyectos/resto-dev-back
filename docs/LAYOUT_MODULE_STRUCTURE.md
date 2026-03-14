# Estructura del Módulo Layout - Mejores Prácticas

## ✅ Arquitectura Limpia Implementada

El módulo de layout (zonas y mesas) sigue **Clean Architecture / Arquitectura Hexagonal**:

```
modules/layout/
├── zones/
│   ├── domain/
│   │   └── model/          # Entidades de dominio (Zone)
│   ├── application/
│   │   ├── port/
│   │   │   ├── input/      # Use Cases (CreateZoneUseCase, ListZonesUseCase)
│   │   │   └── output/     # Repository Ports (ZoneRepositoryPort)
│   │   ├── service/        # Implementación de Use Cases
│   │   ├── command/        # DTOs de entrada (CreateZoneCommand)
│   │   └── query/          # DTOs de consulta (SearchZonesQuery)
│   └── infrastructure/
│       ├── web/
│       │   ├── controller/ # REST Controllers
│       │   ├── dto/        # Request/Response DTOs
│       │   └── mapper/     # Mappers Web ↔ Domain
│       └── persistence/
│           ├── entity/     # JPA Entities
│           ├── repository/ # Spring Data Repositories
│           ├── adapter/    # Implementación de Repository Ports
│           └── mapper/     # Mappers JPA ↔ Domain
└── tables/
    └── (misma estructura)
```

## ✅ Uso Correcto de Shared Components

### 1. **Manejo de Errores Centralizado**

**Excepciones disponibles en `shared/errors/`:**

```java
// Para recursos no encontrados (404)
throw new ResourceNotFoundException("Zona", zoneId);

// Para duplicados (409 CONFLICT)
throw new DuplicateResourceException("zona", "nombre", zoneName);

// Para validaciones de negocio (400)
throw new BusinessValidationException("Mensaje personalizado");
throw BusinessValidationException.invalidReference("Zona", zoneId);
```

**GlobalExceptionHandler captura automáticamente:**
- `ApiException` y subclases → Respuesta con status HTTP específico
- `IllegalArgumentException` → 400 BAD REQUEST
- `MethodArgumentNotValidException` → 400 con detalles de validación
- `AccessDeniedException` → 403 FORBIDDEN
- `Exception` genérica → 500 INTERNAL SERVER ERROR

### 2. **Respuestas Estandarizadas**

**ApiResponse `shared/responses/ApiResponse.java`:**

```java
// Éxito con datos
return ResponseEntity.ok(ApiResponse.ok(data));

// Éxito con mensaje
return ResponseEntity.ok(ApiResponse.ok(data, "Operación exitosa"));

// Creación exitosa
return ResponseEntity.status(CREATED)
    .body(ApiResponse.created(data, "Recurso creado"));

// Error manual (raramente necesario)
return ResponseEntity.badRequest()
    .body(ApiResponse.error("Error personalizado"));
```

**Estructura JSON de respuesta:**
```json
{
  "success": true,
  "message": "Zona creada exitosamente",
  "data": { ... },
  "timestamp": "2026-03-11T12:00:00"
}
```

**PaginatedResponse `shared/responses/PaginatedResponse.java`:**

```java
PaginatedResponse<ZoneResponse> pageResponse = PaginatedResponse.<ZoneResponse>builder()
    .data(responseList)
    .page(pageModel.page() + 1)  // 1-indexed para frontend
    .size(pageModel.size())
    .totalElements(pageModel.totalElements())
    .totalPages(pageModel.totalPages())
    .hasNext(pageModel.page() < pageModel.totalPages() - 1)
    .hasPrevious(pageModel.page() > 0)
    .build();
```

### 3. **Base Entity para Auditoría**

**Todas las entidades JPA extienden `BaseEntity`:**

```java
@Entity
@Table(name = "zone")
public class ZoneJpaEntity extends BaseEntity {
    // id, createdAt, updatedAt automáticos
    private String name;
    private boolean active;
}
```

**BaseEntity provee automáticamente:**
- `UUID id` con `@UuidGenerator`
- `LocalDateTime createdAt` (auto en @PrePersist)
- `LocalDateTime updatedAt` (auto en @PreUpdate)

### 4. **Búsqueda Dinámica con Specifications**

**GenericSpecificationBuilder `shared/search/`:**

```java
// En Repository Adapters:
Specification<ZoneJpaEntity> spec = Specification.where(
    (root, query, cb) -> cb.conjunction()
);

if (query.search() != null) {
    spec = spec.and(GenericSpecificationBuilder.searchInFields(
        query.search(), "name", "description"
    ));
}

if (query.isActive() != null) {
    spec = spec.and(GenericSpecificationBuilder.isEntityActive(
        query.isActive()
    ));
}
```

### 5. **Validación de DTOs**

**Usar anotaciones Jakarta Validation en Request DTOs:**

```java
public record CreateZoneRequest(
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "Máximo 100 caracteres")
    String name,
    
    @Size(max = 255)
    String description
) {}
```

**GlobalExceptionHandler convierte errores de validación automáticamente:**
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "name": "El nombre es obligatorio",
    "capacity": "Debe ser mayor a 0"
  }
}
```

## 🎯 Mejoras Implementadas en Layout

### Antes (❌):
```java
// Uso inconsistente de IllegalArgumentException
if (zone == null) {
    throw new IllegalArgumentException("Zona no encontrada");
}
// Siempre devuelve 400 BAD REQUEST

// Mensajes hardcodeados sin context
throw new IllegalArgumentException("Ya existe una Zona con ese nombre.");
```

### Después (✅):
```java
// Excepciones específicas con códigos HTTP correctos
Zone zone = zoneRepository.findById(id)
    .orElseThrow(() -> new ResourceNotFoundException("Zona", id));
// Devuelve 404 NOT FOUND

// Mensajes consistentes y con contexto
if (zoneRepository.existsByName(name)) {
    throw new DuplicateResourceException("zona", "nombre", name);
}
// Devuelve 409 CONFLICT con formato estandarizado
```

## 📋 Checklist para Nuevos Módulos

Al crear un nuevo módulo, asegúrate de:

- [ ] **Domain**: Modelos de negocio sin dependencias externas
- [ ] **Application Ports**: Interfaces para use cases e infraestructura
- [ ] **Application Service**: Lógica de negocio, usa excepciones de `shared/errors/`
- [ ] **Web DTOs**: Validaciones con Jakarta Validation
- [ ] **Web Mapper**: Convierte DTOs ↔ Commands/Domain
- [ ] **Controller**: Usa `ApiResponse` y `PaginatedResponse`
- [ ] **JPA Entity**: Extiende `BaseEntity`
- [ ] **Repository Adapter**: Usa `Specification` para búsquedas dinámicas
- [ ] **JPA Mapper**: Convierte JPA ↔ Domain

## 🔍 Patrones de Uso Común

### Crear Recurso:
```java
@Override
public Zone execute(CreateZoneCommand command) {
    // 1. Validar duplicados
    if (repository.existsByName(command.name())) {
        throw new DuplicateResourceException("zona", "nombre", command.name());
    }
    
    // 2. Crear dominio
    Zone zone = Zone.builder()
        .name(command.name())
        .active(true)
        .build();
    
    // 3. Persistir
    return repository.save(zone);
}
```

### Actualizar Recurso:
```java
@Override
public Zone execute(UUID id, UpdateZoneCommand command) {
    // 1. Buscar y validar existencia
    Zone zone = repository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Zona", id));
    
    // 2. Validar duplicados (excepto el mismo)
    if (!zone.getName().equals(command.name()) && 
        repository.existsByName(command.name())) {
        throw new DuplicateResourceException("zona", "nombre", command.name());
    }
    
    // 3. Actualizar
    zone.setName(command.name());
    zone.setActive(command.active());
    
    // 4. Persistir
    return repository.save(zone);
}
```

### Eliminar Recurso:
```java
@Override
public void execute(UUID id) {
    // Validar existencia
    if (repository.findById(id).isEmpty()) {
        throw new ResourceNotFoundException("Zona", id);
    }
    
    // Validar reglas de negocio
    if (hasRelatedTables(id)) {
        throw new BusinessValidationException(
            "No se puede eliminar una zona con mesas asociadas"
        );
    }
    
    repository.deleteById(id);
}
```

### Buscar con Paginación:
```java
@Override
public PageModel<Zone> execute(SearchZonesQuery query) {
    return repository.searchZones(query);
}
```

## 🚀 Beneficios de Esta Estructura

1. **Códigos HTTP Correctos**: 404 para not found, 409 para conflictos, 400 para validaciones
2. **Mensajes Consistentes**: Formato uniforme en todos los endpoints
3. **Separación de Concerns**: Domain sin dependencias de infraestructura
4. **Testeable**: Use cases sin dependencias de Spring/JPA
5. **Reutilizable**: Shared components evitan código duplicado
6. **Mantenible**: Cambios en formatos de respuesta en un solo lugar
7. **Documentable**: Estructura clara para OpenAPI/Swagger

## 📝 Notas Adicionales

- **No usar `IllegalArgumentException`** directamente en servicios de aplicación
- **No hardcodear mensajes HTTP** en controllers (usar ApiResponse factories)
- **Siempre extender BaseEntity** para auditoría automática
- **Validar en DTOs** con Jakarta Validation, validar negocio en services
- **Use Cases no conocen HTTP**, solo lanzan excepciones de dominio
