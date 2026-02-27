# Arquitectura del Sistema — resto-dev

Este documento sirve como el "mapa" oficial del proyecto para no perderse a medida que crezca. Define cómo fluyen los datos, la separación de responsabilidades en Arquitectura Hexagonal y la estrategia multitenant con PostgreSQL.

---

## 1. Estrategia Multitenant y Base de Datos

El sistema usa una base de datos única (`resto_dev`) pero separa los datos lógicamente usando **Schemas de PostgreSQL**.

| Schema | Tablas | Propósito | ¿Quién lo usa? |
|---|---|---|---|
| `public` | `users`, `organizations`, `roles`, `permissions`, `role_permissions`, `organization_members`, `subscription_plans`, `organization_subscriptions` | **Tablas Maestras / Globales**. Gestionan el acceso al SaaS y la facturación. | El panel de control global (Súper Admins) y el login de usuarios. |
| `client_{uuid}` | (Futuro) `tables`, `categories`, `products`, `orders`, `payments`, etc. | **Tablas Operativas**. Contienen la info diaria exclusiva de cada organización. | El punto de venta (POS) y la gestión local de cada cliente. |

> **Nota:** Cuando se crea una [Organization](file:///c:/Users/casve/Documents/2025-1-UNI/Codlix/resto-dev/resto-dev/src/main/java/resto_dev/modules/adminsaas/organizations/domain/Organization.java#13-30) mediante [OrganizationController](file:///c:/Users/casve/Documents/2025-1-UNI/Codlix/resto-dev/resto-dev/src/main/java/resto_dev/modules/adminsaas/organizations/adapters/web/OrganizationController.java#26-65), un servicio interno ([SchemaService](file:///c:/Users/casve/Documents/2025-1-UNI/Codlix/resto-dev/resto-dev/src/main/java/resto_dev/shared/tenancy/SchemaService.java#12-62)) genera automáticamente un nuevo schema tipo `client_e7f3a9b1c2d4` en la BD.

---

## 2. Estructura General de Módulos (Packages)

Todo el código fuente vive dentro de `src/main/java/resto_dev/`. 

```mermaid
graph TD
    subgraph shared["shared/ (Transversal)"]
        direction TB
        S1["common/ (BaseEntity, DataSeeder)"]
        S2["errors/ (Exceptions, Handlers)"]
        S3["responses/ (ApiResponse)"]
        S4["security/ (JWT, Filters, Guards)"]
        S5["tenancy/ (TenantContext, SchemaService)"]
    end

    subgraph modules["modules/ (Negocio por Dominio)"]
        direction TB
        subgraph as["adminsaas/ (schema: public)"]
            M1["users/"]
            M2["organizations/ (Tenants genéricos)"]
            M3["members/ (Pivote user↔org↔rol)"]
            M4["subscriptions/ (Planes SaaS)"]
        end
        subgraph client_modules["operativos/ (schema: client_{uuid}) - Futuros"]
            C1["menu/"]
            C2["orders/"]
            C3["payments/"]
            C4["restaurant_settings/"]
        end
    end

    shared -.->|Usado por todos| modules

    style shared fill:#FFF3E0,color:#333
    style as fill:#E3F2FD,color:#333
    style client_modules fill:#F1F8E9,color:#333
```

---

## 3. Arquitectura Hexagonal (Dentro de cada módulo)

Cada módulo (ej. `users`, `organizations`) sigue Arquitectura Hexagonal estricta ("Ports and Adapters"). 

```mermaid
graph TB
    subgraph ext["🌐 Mundo Exterior"]
        HTTP["HTTP Request (Controller)"]
        PG["🐘 PostgreSQL (JPA)"]
    end

    subgraph adapters["Adapters (Capa Externa)"]
        direction TB
        subgraph web["adapters/web/"]
            AC["Controller<br/>(Recibe HTTP, llama Port In)"]
            DTO["DTOs de Request/Response"]
        end
        subgraph mappers["adapters/mappers/"]
            MAP["Mapea DTO ↔ Domain"]
        end
    end

    subgraph ports["Ports (Contratos/Interfaces)"]
        direction TB
        subgraph pin["ports/in/"]
            UP["Caso de Uso Port<br/>(Ej. CreateOrganizationPort)"]
            CDTO["Command DTOs"]
        end
        subgraph pout["ports/out/"]
            RP["Repository Port<br/>(Interface para guardar/buscar)"]
        end
    end

    subgraph app["Application (Capa de Lógica)"]
        UC["Use Case (Servicio)<br/>(Ej. CreateOrganizationUseCase)"]
    end

    subgraph domain["Domain (Núcleo Puro)"]
        DOM["Entidades de Dominio (POJO)<br/>(Ej. Organization.java)"]
    end

    subgraph infra["Infrastructure (Implementación Técnica)"]
        subgraph jpa["persistence/jpa/"]
            JE["JPA Entity (@Entity)"]
            JR["Spring Data JpaRepository"]
            JM["Mapea JPA ↔ Domain"]
            RA["Repository Adapter<br/>(Implementa Port Out usando JPA)"]
        end
    end

    HTTP --> AC
    AC --> UP
    UP -.->|implementa| UC
    UC --> RP
    UC --> DOM
    RP -.->|implementa| RA
    RA --> JR & JM
    JR --> PG
    JM --> JE
    JM --> DOM

    style domain fill:#4CAF50,color:#fff
    style app fill:#2196F3,color:#fff
    style ports fill:#FF9800,color:#fff
    style adapters fill:#9C27B0,color:#fff
    style infra fill:#607D8B,color:#fff
    style ext fill:#E0E0E0,color:#333
```

### Reglas de Oro de esta Arquitectura:
1. **El Dominio manda:** `Domain` son POJOs puros de Java. No tienen anotaciones de JPA (`@Entity`), ni de Spring. No dependen de **nada**.
2. **Dependencias hacia adentro:** `Adapters` depende de `Ports`. `Application` depende de `Ports` y `Domain`. Nada depende de `Infrastructure` ni `Adapters` hacia afuera.
3. **Inversión de Control:** La capa de `Application` no inyecta un Repositorio JPA. Inyecta la interfaz `OrganizationRepositoryPort`. La implementación está en `Infrastructure` (`OrganizationRepositoryAdapter`), que conecta con Spring Data JPA. Esto permite cambiar de base de datos sin tocar la lógica de negocio.

---

## 4. Flujo de Control de Acceso (Seguridad)

El sistema usa JWT sin estado. Así funciona:

1. **Login:** `POST /api/v1/auth/login`. Valida email/pwd. Genera un token JWT que contiene el ID del usuario (`sub`) y si es súper admin (`superAdmin`).
2. **Autenticación (cada request):** `JwtAuthFilter` intercepta peticiones. Extrae el JWT, lo valida y setea el `SecurityContext` de Spring.
3. **Contexto Multi-tenant (Futuro):** Cuando un usuario de un restaurante en particular inicie su jornada, enviará un header `X-Tenant-ID` (u organizationId en el token temporal). El `TenantContext` interceptará esto para enrutar las consultas de Hibernate al respectivo schema `client_{uuid}`.
4. **Autorización granular (Restaurantes):** La tabla maestra `organization_members` (pivote) enlaza a un Usuario con una Organización y un Rol (que tiene Permisos). Cuando el usuario intente hacer algo en ese restaurante, se validarán sus permisos consultando esta tabla.
