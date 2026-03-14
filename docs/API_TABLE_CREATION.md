# API de Creación de Mesas - Diseño y Automatización

## Endpoint
```
POST /api/v1/tables/create
```

## 📋 Resumen de Campos

| Campo | ¿Se envía desde frontend? | ¿Validado? | ¿Automático? | Descripción |
|-------|---------------------------|------------|--------------|-------------|
| **zoneId** | ✅ Sí (requerido) | ✅ @NotNull | ❌ | UUID de la zona donde se crea la mesa |
| **tableNumber** | ✅ Sí (requerido) | ✅ @NotBlank @Size(max=50) | ❌ | Identificador único (ej: "A1", "Mesa 5") |
| **capacity** | ✅ Sí (requerido) | ✅ @Positive | ❌ | Número de comensales (1-20) |
| **status** | ❌ No enviar | ❌ Opcional | ✅ Default: FREE | Estado inicial de la mesa |
| **id** | ❌ No enviar | - | ✅ UUID generado | Identificador único de la mesa |
| **active** | ❌ No enviar | - | ✅ Siempre true | Indica si la mesa está activa |

## 🔄 Flujo de Datos

### Frontend → Backend
```typescript
// CORRECTO ✅
const payload: CreateTableRequest = {
  zoneId: currentZone.id,
  tableNumber: "A1",
  capacity: 4
  // NO incluir: status, id, active
};
```

### Backend → Respuesta
```json
{
  "success": true,
  "message": "Mesa creada exitosamente",
  "data": {
    "id": "f47ac10b-58cc-4372-a567-0e02b2c3d479",    // ← Generado automáticamente
    "zoneId": "550e8400-e29b-41d4-a716-446655440000",
    "tableNumber": "A1",
    "capacity": 4,
    "status": "FREE",                                 // ← Valor por defecto
    "active": true                                    // ← Siempre true al crear
  }
}
```

## 🎯 ¿Por qué estos campos son automáticos?

### 1. **id** - Generado por la Base de Datos
```java
// En Table.java (Domain Model)
@Id
@GeneratedValue(generator = "UUID")
private UUID id;

// Razón: Garantiza unicidad global sin colisiones
```

### 2. **status** - Default: FREE
```java
// En TableApplicationService.java
TableStatus status = command.status() != null 
    ? command.status() 
    : TableStatus.FREE;  // ← Por defecto FREE

// Razón: Una mesa nueva SIEMPRE se crea disponible
// El frontend NO debe enviar este campo salvo casos especiales
```

### 3. **active** - Siempre true
```java
// En TableApplicationService.java
Table newTable = Table.builder()
    .active(true)  // ← Siempre true al crear
    .build();

// Razón: Una mesa recién creada está activa por definición
// Desactivar mesas es una operación separada (soft delete)
```

## 📊 Valores Permitidos para Status (Referencia)

| Valor | Significado | ¿Cuándo se usa? |
|-------|-------------|-----------------|
| `FREE` | Disponible | **Al crear** (default) / Al liberar mesa |
| `OCCUPIED` | Ocupada | Cuando hay comensales sentados |
| `DIRTY` | Sucia | Después de que se van los clientes |
| `OUT_OF_SERVICE` | Fuera de servicio | Mantenimiento o reparación |

## ✅ Validaciones del Backend

### CreateTableRequest
```java
@NotNull UUID zoneId           // ← Zona debe existir en BD
@NotBlank @Size(max=50) tableNumber  // ← No vacío, máx 50 caracteres
@Positive int capacity         // ← Mayor que 0
```

### Validaciones de Negocio
```java
// 1. Zona existe
if (zoneRepository.findById(zoneId).isEmpty()) {
    throw BusinessValidationException.invalidReference("Zona");
}

// 2. No duplicar número de mesa en la misma zona
if (tableRepository.existsByTableNumberAndZoneId(...)) {
    throw new DuplicateResourceException("Ya existe una mesa con ese número");
}
```

## 🔧 Implementación Frontend (Angular)

### Modelo TypeScript
```typescript
// src/app/shared/models/table.model.ts
export interface CreateTableRequest {
  zoneId: string;
  tableNumber: string;
  capacity: number;
  // status NO se incluye - backend lo asigna
}

export interface TableResponse {
  id: string;              // Auto-generado
  zoneId: string;
  tableNumber: string;
  capacity: number;
  status: TableStatus;     // Default: 'FREE'
  active: boolean;         // Siempre true
}
```

### Componente de Creación
```typescript
// add-table-modal.component.ts
onSubmit() {
  const request: CreateTableRequest = {
    zoneId: this.zoneId,
    tableNumber: this.tableNumber,
    capacity: this.capacity
    // NO enviar status ❌
  };
  
  this.tablesState.createTable(request, ...);
}
```

## 📖 Documentación OpenAPI (Swagger)

La documentación actualizada en Swagger muestra claramente:
- ✅ Campos requeridos con `requiredMode = REQUIRED`
- ⚠️ Campos opcionales con `requiredMode = NOT_REQUIRED`
- 📝 Descripciones detalladas de cada campo
- 🔍 Valores permitidos para enums (`allowableValues`)

Ejemplo en `CreateTableRequest.java`:
```java
@Schema(
    description = "Estado inicial (OPCIONAL - por defecto FREE)", 
    requiredMode = NOT_REQUIRED,
    allowableValues = {"FREE", "OCCUPIED", "DIRTY", "OUT_OF_SERVICE"}
)
TableStatus status
```

## 🎓 Mejores Prácticas

### ✅ HACER
- Enviar solo `zoneId`, `tableNumber`, `capacity`
- Validar capacidad en frontend (1-20 personas)
- Mostrar mensaje de error claro al usuario
- Permitir selección de zona antes de crear mesa

### ❌ NO HACER
- Enviar campo `status` desde frontend (salvo casos especiales)
- Enviar campo `id` (siempre generado por backend)
- Enviar campo `active` (siempre true al crear)
- Permitir crear mesa sin zona seleccionada

## 🚀 Casos Especiales

### ¿Cuándo SÍ enviar status?
```typescript
// Migración de datos o importación masiva
const specialRequest = {
  zoneId: "...",
  tableNumber: "A1",
  capacity: 4,
  status: "OUT_OF_SERVICE"  // ← Mesa en mantenimiento desde el inicio
};
```

Pero en operación normal del restaurante, **NO** enviar status.

## 📞 Códigos de Respuesta

| Código | Significado | Ejemplo |
|--------|-------------|---------|
| 201 | Mesa creada exitosamente | Retorna TableResponse completo |
| 400 | Datos inválidos | "tableNumber no puede estar vacío" |
| 404 | Zona no encontrada | "Zona con ID ... no existe" |
| 409 | Número de mesa duplicado | "Ya existe una mesa A1 en esta zona" |

## 🔗 Referencias

- **DTO Request**: `CreateTableRequest.java`
- **DTO Response**: `TableResponse.java`
- **Servicio**: `TableApplicationService.java`
- **Controlador**: `TableController.java`
- **Modelo Domain**: `Table.java`
- **Enum**: `TableStatus.java`

---
**Última actualización**: Enero 2025  
**Versión**: API v1.0
