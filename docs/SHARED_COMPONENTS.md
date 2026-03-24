# 📦 Shared Components - Guía Completa

Esta carpeta contiene todos los componentes reutilizables y transversales del sistema RestoDev.

## 📂 Estructura de `shared/`

```
shared/
├── audit/              # (Futuro) Sistema de auditoría
├── common/             # Clases base y utilidades comunes
├── config/             # Configuraciones globales de Spring
├── errors/             # Sistema centralizado de manejo de errores
├── responses/          # DTOs estándar para respuestas HTTP
├── search/             # Utilidades para búsquedas dinámicas (JPA Specifications)
├── security/           # JWT, permisos, autenticación
├── tenancy/            # Multi-tenancy (schema-per-tenant)
└── utils/              # Utilidades generales (StringUtils, etc)
```

---

## 🧩 1. Common (`shared/common/`)

### **BaseEntity.java**
Base abstracta para todas las entidades JPA. Provee auditoría automática.

**Uso:**
```java
@Entity
@Table(name = "products")
public class ProductJpaEntity extends BaseEntity {
    // id, createdAt, updatedAt se heredan automáticamente
    
    private String name;
    private BigDecimal price;
}
```

**Campos automáticos:**
- `UUID id` - Generado con `@UuidGenerator`
- `LocalDateTime createdAt` - Se establece en `@PrePersist`
- `LocalDateTime updatedAt` - Se actualiza en `@PreUpdate`

---

### **UseCase.java / UnitUseCase.java**
Interfaces funcionales para casos de uso.

**UseCase<I, O>** - Caso de uso que retorna datos:
```java
public interface CreateZoneUseCase extends UseCase<CreateZoneCommand, Zone> {
    @Override
    Zone execute(CreateZoneCommand command);
}
```

**UnitUseCase<I>** - Caso de uso que no retorna nada (void):
```java
public interface DeleteZoneUseCase extends UnitUseCase<UUID> {
    @Override
    void execute(UUID id);
}
```

---

### **PageModel.java**
Modelo de dominio para paginación (independiente de Spring Data).

**Uso en application layer:**
```java
public interface ZoneRepositoryPort {
    PageModel<Zone> searchZones(SearchZonesQuery query);
}
```

**Campos:**
- `List<T> content` - Elementos de la página actual
- `int page` - Número de página (0-indexed)
- `int size` - Tamaño de página
- `long totalElements` - Total de elementos
- `int totalPages` - Total de páginas

---

## 🚨 2. Errors (`shared/errors/`)

### **Jerarquía de Excepciones**

```
ApiException (base)
├── ResourceNotFoundException      → 404 NOT FOUND
├── DuplicateResourceException     → 409 CONFLICT
├── BusinessValidationException    → 400 BAD REQUEST
└── (otros métodos factory)
    ├── badRequest()               → 400
    ├── unauthorized()             → 401
    ├── forbidden()                → 403
    └── internal()                 → 500
```

### **ApiException.java**
Excepción base con código HTTP.

**Factory methods:**
```java
throw ApiException.badRequest("Datos inválidos");
throw ApiException.notFound("Recurso no encontrado");
throw ApiException.unauthorized("Token inválido");
throw ApiException.forbidden("Acceso denegado");
throw ApiException.conflict("Conflicto de recurso");
throw ApiException.internal("Error del servidor");
```

---

### **ResourceNotFoundException.java**
Para recursos que no existen (404).

**Uso:**
```java
// Constructor con tipo y ID
throw new ResourceNotFoundException("Zona", zoneId);
// → "Zona no encontrado con identificador: abc-123"

// Constructor con mensaje personalizado
throw new ResourceNotFoundException("Usuario no encontrado en la organización");
```

**Cuándo usar:**
- `findById()` retorna vacío
- Recurso requerido no existe en base de datos
- Relación no encontrada

---

### **DuplicateResourceException.java**
Para recursos duplicados (409 CONFLICT).

**Uso:**
```java
// Constructor con tipo, campo y valor
throw new DuplicateResourceException("zona", "nombre", "Salón Principal");
// → "Ya existe un zona con nombre: Salón Principal"

// Constructor con mensaje personalizado
throw new DuplicateResourceException("Ya existe una mesa con ese número en esta zona");
```

**Cuándo usar:**
- `existsByXXX()` retorna true
- Violación de constraint UNIQUE
- Lógica de negocio impide duplicados

---

### **BusinessValidationException.java**
Para reglas de negocio violadas (400 BAD REQUEST).

**Uso:**
```java
// Mensaje personalizado
throw new BusinessValidationException("No se puede eliminar una zona con mesas activas");

// Factory method para referencias inválidas
throw BusinessValidationException.invalidReference("Zona", zoneId);
// → "Zona referenciado no existe: abc-123"
```

**Cuándo usar:**
- Validaciones de lógica de negocio
- Referencias a recursos que no existen
- Estados inconsistentes
- Operaciones no permitidas por reglas de dominio

---

### **GlobalExceptionHandler.java**
Captura todas las excepciones y las convierte en `ApiResponse` uniforme.

**Excepciones manejadas automáticamente:**

| Excepción | HTTP Status | Respuesta |
|-----------|-------------|-----------|
| `ApiException` | Según `ex.getStatus()` | `{"success": false, "message": "..."}` |
| `MethodArgumentNotValidException` | 400 | `{"success": false, "message": "Validation failed", "errors": {...}}` |
| `IllegalArgumentException` | 400 | `{"success": false, "message": "..."}` |
| `AuthenticationException` | 401 | `{"success": false, "message": "Authentication failed"}` |
| `AccessDeniedException` | 403 | `{"success": false, "message": "Access denied"}` |
| `Exception` (genérica) | 500 | `{"success": false, "message": "Internal server error"}` |

**No necesitas manejar excepciones en controllers**, el handler las captura automáticamente.

---

## 📤 3. Responses (`shared/responses/`)

### **ApiResponse.java**
Wrapper estándar para todas las respuestas HTTP.

**Estructura JSON:**
```json
{
  "success": true,
  "message": "Operación exitosa",
  "data": { ... },
  "errors": null,
  "timestamp": "2026-03-11T12:00:00"
}
```

**Factory methods:**

```java
// ✅ Respuesta exitosa simple
return ResponseEntity.ok(ApiResponse.ok(data));
// → {"success": true, "data": {...}}

// ✅ Respuesta exitosa con mensaje
return ResponseEntity.ok(ApiResponse.ok(data, "Zona actualizada exitosamente"));

// ✅ Recurso creado (201)
return ResponseEntity.status(HttpStatus.CREATED)
    .body(ApiResponse.created(data, "Zona creada exitosamente"));

// ❌ Error manual (raramente necesario, usa excepciones)
return ResponseEntity.badRequest()
    .body(ApiResponse.error("Error personalizado"));

// ❌ Error con detalles
return ResponseEntity.badRequest()
    .body(ApiResponse.error("Validación fallida", validationErrors));
```

**Campos:**
- `boolean success` - Indica si la operación fue exitosa
- `String message` - Mensaje descriptivo (opcional)
- `T data` - Payload de la respuesta (opcional)
- `Object errors` - Detalles de errores de validación (opcional)
- `LocalDateTime timestamp` - Timestamp automático

---

### **PaginatedResponse.java**
Wrapper para respuestas paginadas.

**Uso:**
```java
PaginatedResponse<ZoneResponse> pageResponse = PaginatedResponse.<ZoneResponse>builder()
    .data(zoneResponseList)
    .page(pageModel.page() + 1)  // Convertir a 1-indexed para frontend
    .size(pageModel.size())
    .totalElements(pageModel.totalElements())
    .totalPages(pageModel.totalPages())
    .hasNext(pageModel.page() < pageModel.totalPages() - 1)
    .hasPrevious(pageModel.page() > 0)
    .build();

return ResponseEntity.ok(ApiResponse.ok(pageResponse));
```

**Estructura JSON:**
```json
{
  "success": true,
  "data": {
    "data": [ {...}, {...} ],
    "page": 1,
    "size": 10,
    "totalElements": 45,
    "totalPages": 5,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

**Factory method alternativo:**
```java
// Desde Spring Data Page directamente
Page<ZoneJpaEntity> page = repository.findAll(pageable);
PaginatedResponse<ZoneResponse> response = PaginatedResponse.of(
    page.map(mapper::toResponse)
);
```

---

## 🔍 4. Search (`shared/search/`)

### **GenericSpecificationBuilder.java**
Utilidades para construir JPA Specifications dinámicas.

**Métodos disponibles:**

#### `searchInFields(String search, String... fields)`
Búsqueda case-insensitive en múltiples campos.

```java
Specification<ZoneJpaEntity> spec = Specification.where(
    (root, query, cb) -> cb.conjunction()
);

if (query.search() != null) {
    spec = spec.and(GenericSpecificationBuilder.searchInFields(
        query.search(), 
        "name", "description", "code"
    ));
}
// Busca "sala" en name OR description OR code (ILIKE)
```

#### `isEntityActive(Boolean active)`
Filtro por campo `active`.

```java
if (query.isActive() != null) {
    spec = spec.and(GenericSpecificationBuilder.isEntityActive(query.isActive()));
}
// WHERE active = true (o false)
```

#### Combinando especificaciones:

```java
Specification<TableJpaEntity> spec = Specification.where(
    (root, query, cb) -> cb.conjunction()  // Iniciar con condición vacía
);

// Búsqueda por texto
if (query.search() != null) {
    spec = spec.and(GenericSpecificationBuilder.searchInFields(
        query.search(), "tableNumber"
    ));
}

// Filtro por estado activo
if (query.isActive() != null) {
    spec = spec.and(GenericSpecificationBuilder.isEntityActive(query.isActive()));
}

// Filtro custom por zona
if (query.zoneId() != null) {
    spec = spec.and((root, cq, cb) -> 
        cb.equal(root.get("zone").get("id"), query.zoneId())
    );
}

// Filtro por status enum
if (query.status() != null) {
    spec = spec.and((root, cq, cb) -> 
        cb.equal(root.get("status"), query.status())
    );
}

Page<TableJpaEntity> result = repository.findAll(spec, pageable);
```

**⚠️ Importante:**
- **NO inicialices con `Specification.where(null)`** → Causará "Specification must not be null"
- **Usa siempre**: `Specification.where((root, query, cb) -> cb.conjunction())`

---

## 🔐 5. Security (`shared/security/`)

### **JWT (`security/jwt/`)**

#### **JwtProvider.java**
Generación y validación de tokens JWT.

**Métodos:**
```java
String token = jwtProvider.generateToken(userId);
UUID userId = jwtProvider.getUserIdFromToken(token);
boolean isValid = jwtProvider.validateToken(token);
```

#### **JwtAuthFilter.java**
Filtro que intercepta requests, valida JWT y establece SecurityContext.

**Flow:**
1. Extrae token del header `Authorization: Bearer <token>`
2. Valida token con `JwtProvider`
3. Extrae `userId`
4. Establece `SecurityContextHolder.getContext().setAuthentication()`

---

### **Permissions (`security/permissions/`)**

#### **PermissionEntity.java / RoleEntity.java**
Entidades para sistema RBAC.

**Estructura:**
- `PermissionEntity`: code, description, module
- `RoleEntity`: name, system, Set<PermissionEntity>

#### **PermissionDataLoader.java**
CommandLineRunner que crea permisos por defecto en el primer arranque.

**Permisos creados:**
- Layout: `VIEW_LAYOUT`, `MANAGE_LAYOUT`
- Menu: `VIEW_MENU`, `MANAGE_MENU`
- Orders: `VIEW_ORDERS`, `MANAGE_ORDERS`, `CREATE_ORDER`, `CANCEL_ORDER`
- Kitchen: `VIEW_KITCHEN`, `MANAGE_KITCHEN`
- Staff: `VIEW_STAFF`, `MANAGE_STAFF`
- Reports: `VIEW_REPORTS`, `EXPORT_REPORTS`
- Settings: `VIEW_SETTINGS`, `MANAGE_SETTINGS`
- Reservations: `VIEW_RESERVATIONS`, `MANAGE_RESERVATIONS`

#### **CustomPermissionEvaluator.java**
Evaluador custom para `@PreAuthorize("hasPermission()")`.

**Uso en controllers:**
```java
@PreAuthorize("hasPermission(#orgId, 'Organization', 'VIEW_LAYOUT')")
public ResponseEntity<ApiResponse<...>> listZones(@RequestHeader("X-Organization-Id") UUID orgId) {
    // ...
}
```

**Valida:**
1. Usuario autenticado es miembro de la organización
2. Usuario tiene el permiso requerido en su rol

---

## 🏢 6. Tenancy (`shared/tenancy/`)

Sistema multi-tenant con schema-per-tenant en PostgreSQL.

### **TenantContext.java**
ThreadLocal que almacena el schema actual.

```java
TenantContext.setCurrentTenant("client_abc123");
String schema = TenantContext.getCurrentTenant();  // → "client_abc123"
TenantContext.clear();  // Limpiar al final del request
```

---

### **TenantFilter.java**
Filtro que:
1. Lee header `X-Organization-Id`
2. Valida que el usuario sea miembro de la organización
3. Obtiene el schema de la organización
4. Llama `TenantContext.setCurrentTenant(schema)`
5. Retorna 403 si no es miembro

**Flow completo:**
```
Request → JwtAuthFilter → TenantFilter → Controller
                ↓              ↓
         Valida JWT    Valida membresía
         Establece     Establece schema
         SecurityContext  TenantContext
```

---

### **TenantIdentifierResolver.java**
Implementa `CurrentTenantIdentifierResolver` de Hibernate.

Lee `TenantContext.getCurrentTenant()` y lo retorna a Hibernate.
Si es `null`, retorna `"admin"` (schema por defecto).

---

### **TenantConnectionProvider.java**
Implementa `MultiTenantConnectionProvider` de Hibernate.

**Cuando Hibernate pide una conexión:**
1. Obtiene conexión del pool
2. Ejecuta `SET SCHEMA '<schema_name>'`
3. Retorna conexión configurada

**Cuando se libera la conexión:**
1. Ejecuta `SET SCHEMA 'admin'`
2. Devuelve conexión al pool

---

### **SchemaService.java / FlywayMigrationService.java**
Servicios para crear schemas de tenant dinámicamente.

**Uso:**
```java
// Crear schema y ejecutar migraciones
schemaService.createSchema("client_abc123");
flywayMigrationService.migrateTenantSchema("client_abc123");
```

---

## 🛠️ 7. Config (`shared/config/`)

### **OpenApiConfig.java**
Configuración de Swagger/OpenAPI.

Expone documentación en: `http://localhost:8080/swagger-ui.html`

---

## 🧰 8. Utils (`shared/utils/`)

### **StringUtils.java**
Utilidades para strings (slugify, normalize, etc).

---

## 📋 Checklist de Uso de Shared Components

### ✅ Al crear una entidad JPA:
- [ ] Extender `BaseEntity` para auditoría automática
- [ ] NO especificar schema en entidades tenant (solo en admin)

### ✅ Al crear servicios de aplicación:
- [ ] Lanzar `ResourceNotFoundException` cuando no encuentras recursos
- [ ] Lanzar `DuplicateResourceException` para duplicados
- [ ] Lanzar `BusinessValidationException` para reglas de negocio
- [ ] NO usar `IllegalArgumentException` directamente

### ✅ Al crear controllers:
- [ ] Retornar `ApiResponse.ok(data)` para éxitos
- [ ] Retornar `ApiResponse.created(data, msg)` para creaciones
- [ ] Usar `PaginatedResponse` para listados paginados
- [ ] NO manejar excepciones manualmente (deja que GlobalExceptionHandler lo haga)

### ✅ Al crear repositorios con búsquedas:
- [ ] Extender `JpaSpecificationExecutor<T>`
- [ ] Usar `GenericSpecificationBuilder` para búsquedas comunes
- [ ] Inicializar specs con `Specification.where((r,q,c) -> c.conjunction())`
- [ ] NO inicializar con `Specification.where(null)`

### ✅ Al crear endpoints protegidos:
- [ ] Usar `@PreAuthorize("hasPermission(#orgId, 'Organization', 'PERMISSION_CODE')")`
- [ ] Validar header `X-Organization-Id`
- [ ] Dejar que `TenantFilter` maneje la validación de membresía

---

## 🎯 Ejemplo Completo: Crear un CRUD

```java
// 1. Domain Model
@Getter
@Setter
@Builder
public class Product {
    private UUID id;
    private String name;
    private BigDecimal price;
    private boolean active;
}

// 2. JPA Entity
@Entity
@Table(name = "products")
public class ProductJpaEntity extends BaseEntity {  // ← Extiende BaseEntity
    private String name;
    private BigDecimal price;
    private boolean active;
}

// 3. Repository
public interface ProductJpaRepository extends 
    JpaRepository<ProductJpaEntity, UUID>,
    JpaSpecificationExecutor<ProductJpaEntity> {  // ← Para búsquedas dinámicas
}

// 4. Application Service
@Service
@RequiredArgsConstructor
public class ProductApplicationService {
    
    private final ProductRepositoryPort repository;
    
    public Product create(CreateProductCommand cmd) {
        if (repository.existsByName(cmd.name())) {
            throw new DuplicateResourceException("producto", "nombre", cmd.name());
            // ↑ 409 CONFLICT automático
        }
        
        Product product = Product.builder()
            .name(cmd.name())
            .price(cmd.price())
            .active(true)
            .build();
            
        return repository.save(product);
    }
    
    public Product update(UUID id, UpdateProductCommand cmd) {
        Product product = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Producto", id));
            // ↑ 404 NOT FOUND automático
        
        product.setName(cmd.name());
        product.setPrice(cmd.price());
        
        return repository.save(product);
    }
    
    public PageModel<Product> search(SearchProductsQuery query) {
        return repository.searchProducts(query);
    }
}

// 5. Repository Adapter (con Specifications)
@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepositoryPort {
    
    private final ProductJpaRepository jpaRepository;
    
    @Override
    public PageModel<Product> searchProducts(SearchProductsQuery query) {
        // ✅ Inicializar correctamente
        Specification<ProductJpaEntity> spec = 
            Specification.where((root, q, cb) -> cb.conjunction());
        
        // Búsqueda por nombre
        if (query.search() != null) {
            spec = spec.and(GenericSpecificationBuilder.searchInFields(
                query.search(), "name"
            ));
        }
        
        // Filtro por activo
        if (query.isActive() != null) {
            spec = spec.and(GenericSpecificationBuilder.isEntityActive(
                query.isActive()
            ));
        }
        
        Pageable pageable = PageRequest.of(query.page(), query.size());
        Page<ProductJpaEntity> page = jpaRepository.findAll(spec, pageable);
        
        List<Product> products = page.getContent().stream()
            .map(mapper::toDomain)
            .toList();
        
        return new PageModel<>(
            products,
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages()
        );
    }
}

// 6. Controller
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final CreateProductUseCase createUseCase;
    private final ListProductsUseCase listUseCase;
    
    @PostMapping("/create")
    @PreAuthorize("hasPermission(#orgId, 'Organization', 'MANAGE_MENU')")
    public ResponseEntity<ApiResponse<ProductResponse>> create(
            @RequestHeader("X-Organization-Id") UUID orgId,
            @Valid @RequestBody CreateProductRequest request) {
        
        CreateProductCommand cmd = mapper.toCommand(request);
        Product product = createUseCase.execute(cmd);
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.created(
                mapper.toResponse(product), 
                "Producto creado exitosamente"
            ));
        // ↑ ApiResponse gestiona el formato JSON
    }
    
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<PaginatedResponse<ProductResponse>>> list(
            @RequestHeader("X-Organization-Id") UUID orgId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        SearchProductsQuery query = new SearchProductsQuery(search, page, size);
        PageModel<Product> pageModel = listUseCase.execute(query);
        
        PaginatedResponse<ProductResponse> pageResponse = 
            PaginatedResponse.<ProductResponse>builder()
                .data(pageModel.content().stream().map(mapper::toResponse).toList())
                .page(pageModel.page() + 1)
                .size(pageModel.size())
                .totalElements(pageModel.totalElements())
                .totalPages(pageModel.totalPages())
                .hasNext(pageModel.page() < pageModel.totalPages() - 1)
                .hasPrevious(pageModel.page() > 0)
                .build();
        
        return ResponseEntity.ok(ApiResponse.ok(pageResponse));
        // ↑ PaginatedResponse + ApiResponse = respuesta estándar
    }
}
```

---

## 🚀 Beneficios de Usar Shared Components

1. **Consistencia**: Todas las respuestas HTTP tienen el mismo formato
2. **Códigos HTTP Correctos**: 404 para not found, 409 para conflictos, etc.
3. **Menos Código**: No repites validaciones ni manejo de errores
4. **Testeable**: Componentes desacoplados fáciles de mockear
5. **Mantenible**: Cambios en un lugar afectan todo el sistema
6. **Auditabilidad**: Timestamps automáticos en todas las entidades
7. **Multi-tenancy Transparente**: Schema switching automático
8. **Seguridad Centralizada**: JWT y permisos gestionados uniformemente

---

## ⚠️ Errores Comunes a Evitar

### ❌ NO:
```java
// NO hardcodear respuestas HTTP
return ResponseEntity.ok(Map.of("success", true, "data", data));

// NO usar IllegalArgumentException
throw new IllegalArgumentException("No encontrado");

// NO inicializar Specifications con null
Specification<T> spec = Specification.where((Specification<T>) null);

// NO manejar excepciones en controllers
try {
    // ...
} catch (Exception e) {
    return ResponseEntity.badRequest().body("Error");
}
```

### ✅ SÍ:
```java
// SÍ usar ApiResponse
return ResponseEntity.ok(ApiResponse.ok(data));

// SÍ usar excepciones específicas
throw new ResourceNotFoundException("Producto", productId);

// SÍ inicializar Specifications correctamente
Specification<T> spec = Specification.where((r, q, c) -> c.conjunction());

// SÍ dejar que GlobalExceptionHandler maneje errores
// (no catch en controllers, lanza excepciones directamente)
```

---

## 📚 Ver También

- [LAYOUT_MODULE_STRUCTURE.md](./LAYOUT_MODULE_STRUCTURE.md) - Arquitectura de módulos
- `application.yml` - Configuración de tenancy y JWT
- OpenAPI Docs: `http://localhost:8080/swagger-ui.html`
