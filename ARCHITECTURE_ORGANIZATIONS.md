# 🏛️ Arquitectura del Módulo de Organizaciones

Este módulo utiliza **Arquitectura Hexagonal** (también conocida como Puertos y Adaptadores) para separar la lógica de negocio de los detalles técnicos.

---

## 📂 1. Dominio (Domain)
*   **Ruta**: `src/main/java/resto_dev/modules/adminsaas/organizations/domain/`
*   **Contenido**: `Organization.java`
*   **Responsabilidad**: Define el modelo puro de datos. Es un objeto Java sin dependencias externas (sin anotaciones de JPA si es posible, aunque aquí se usan para simplificar). Es la verdad absoluta del negocio.

## 📂 2. Aplicación (Application)
Coordina las acciones del sistema sin saber *cómo* se guardan los datos o *quién* hace la petición.
*   **Ruta**: `src/main/java/resto_dev/modules/adminsaas/organizations/application/`
*   **Ports (Input)**: Interfaces que definen los casos de uso (ej: `CreateOrganizationUseCase`).
*   **Ports (Output)**: Interfaces que definen necesidades externas (ej: `OrganizationRepositoryPort`).
*   **Services**: Implementan los casos de uso. Contienen la lógica de coordinación (ej: validar, guardar, enviar mensajes a RabbitMQ).
*   **Command/Query**: Objetos que llevan datos desde el exterior hacia el servicio.

## 📂 3. Infraestructura (Infrastructure)
Contiene las implementaciones técnicas. Si cambiamos Postgres por MongoDB, solo se toca esta carpeta.
*   **Ruta**: `src/main/java/resto_dev/modules/adminsaas/organizations/infrastructure/`

### 🌐 Web (API REST)
*   **Controller**: Recibe el HTTP (`OrganizationController`).
*   **Web DTOs**: Estructura de datos que espera el Frontend.
*   **Web Mappers**: Convierten los DTOs en Commands de la capa de aplicación.

### 💾 Persistencia (Database)
*   **Entities**: Representación de las tablas de la DB (`OrganizationJpaEntity`).
*   **Repositories**: Interfaces de Spring Data.
*   **Adapters**: Implementan los puertos de salida (`OrganizationRepositoryAdapter`). Traducen el lenguaje del dominio al lenguaje de la base de datos.
*   **Persistence Mappers**: Convierten `Domain Model` <-> `JpaEntity`.

---

## 🔄 Flujo de una Petición (Ejemplo: Crear Organización)

1.  **Entrada**: `OrganizationController` recibe el JSON.
2.  **Mapeo**: Se convierte a `CreateOrganizationCommand`.
3.  **Ejecución**: El `CreateOrganizationApplicationService` se ejecuta:
    -   Genera el Slug.
    -   Guarda en la DB usando el puerto `OrganizationRepositoryPort`.
    -   **Envía mensaje a RabbitMQ** (o ejecuta el fallback síncrono si está caído).
4.  **Salida**: El adaptador de persistencia guarda en Postgres y se devuelve la respuesta al usuario.

---

## 🚀 RabbitMQ y Mensajería (Shared/Messaging)
Aunque no está dentro de la carpeta `organizations` (porque es compartido), los **Consumers** (`TenantProvisioningConsumer`) actúan como adaptadores de entrada que escuchan eventos y disparan lógica de negocio en segundo plano.
