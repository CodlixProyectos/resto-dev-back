# Tenant Database Schema Design

This document outlines the proposed database schema for a **single tenant** (e.g., `client_a1b2`). Since the application uses a multi-tenant architecture with separate schemas per restaurant, these tables will be replicated inside each tenant's specific schema in PostgreSQL.

## Entity Relationship Diagram (ERD)

```mermaid
erDiagram
    %% Core Catalog
    CATEGORY {
        uuid id PK
        string name
        boolean is_active
        timestamp created_at
    }
    
    PRODUCT {
        uuid id PK
        uuid category_id FK
        string name
        string description
        decimal price
        boolean is_available
        timestamp created_at
    }

    %% Restaurant Layout
    AREA {
        uuid id PK
        string name "e.g., Terrace, Main Hall"
    }

    TABLE {
        uuid id PK
        uuid area_id FK
        string name "e.g., T-1, T-2"
        int capacity
        string status "AVAILABLE, OCCUPIED, RESERVED"
    }

    %% Staff / Users (Local to Tenant)
    WAITER {
        uuid id PK
        uuid global_user_id "Nullable if managed locally, or linked to public.users"
        string name
        string pin_code "For fast POS login"
        boolean is_active
    }

    %% Orders & Transactions
    ORDER {
        uuid id PK
        uuid table_id FK
        uuid waiter_id FK
        string status "OPEN, PREPARING, READY, SERVED, CLOSED, CANCELED"
        decimal total_amount
        timestamp opened_at
        timestamp closed_at
    }

    ORDER_ITEM {
        uuid id PK
        uuid order_id FK
        uuid product_id FK
        int quantity
        decimal unit_price
        string notes "e.g., No onions"
    }

    %% Payments & Invoicing
    PAYMENT {
        uuid id PK
        uuid order_id FK
        decimal amount
        string method "CASH, CARD, TRANSFER"
        timestamp paid_at
    }

    INVOICE {
        uuid id PK
        uuid payment_id FK
        string invoice_number "e.g., F001-492"
        string tax_id "RUC/CUIT/NIT"
        string customer_name
        string status "PENDING, ISSUED, FAILED"
        timestamp issued_at
    }

    %% Audit & Security
    AUDIT_LOG {
        uuid id PK
        string entity_name "e.g., ORDER, PAYMENT"
        uuid entity_id
        string action "CREATE, UPDATE, DELETE"
        string changes_json "JSON with old_value -> new_value"
        uuid user_id FK "Who did this?"
        timestamp created_at
    }

    %% Inventory (New Module)
    INGREDIENT {
        uuid id PK
        string name "e.g., Tomate, Carne Molida"
        string unit_of_measure "kg, gr, litros, unidades"
        decimal current_stock
        decimal minimum_stock "Alert threshold"
    }

    RECIPE_ITEM {
        uuid id PK
        uuid product_id FK "The dish"
        uuid ingredient_id FK "The raw material"
        decimal quantity_required "Amount needed per dish"
    }

    INVENTORY_TRANSACTION {
        uuid id PK
        uuid ingredient_id FK
        decimal quantity_changed "+50 or -2.5"
        string type "IN, OUT, ADJUSTMENT, SPOILAGE"
        string reference "e.g., Order #123, Purchase #45"
        timestamp created_at
    }

    %% Relationships
    CATEGORY ||--o{ PRODUCT : contains
    AREA ||--o{ TABLE : contains
    TABLE ||--o{ ORDER : has
    WAITER ||--o{ ORDER : manages
    WAITER ||--o{ AUDIT_LOG : "performs actions"
    ORDER ||--|{ ORDER_ITEM : contains
    PRODUCT ||--o{ ORDER_ITEM : ordered_as
    ORDER ||--o{ PAYMENT : paid_via
    PAYMENT ||--o| INVOICE : generates

    %% Inventory Relationships
    PRODUCT ||--o{ RECIPE_ITEM : "requires"
    INGREDIENT ||--o{ RECIPE_ITEM : "used_in"
    INGREDIENT ||--o{ INVENTORY_TRANSACTION : "tracks"
```

### ¿Qué agregamos a la Base de Datos para soportar el futuro crecimiento?

1. **Tabla `AUDIT_LOG` (Seguridad y Trazabilidad)**: 
   Tal como lo discutimos, esta tabla es sagrada. Si un mesero o administrador borra una orden (`action: "DELETE"`), o cambia un pago de S/100 a S/10 (`action: "UPDATE"`), todo queda registrado en formato JSON (`changes_json`) vinculando la acción al `user_id` exacto. Al ser una tabla que solo crece y nunca se actualiza, es ultra rápida para insertar datos.

2. **Tabla `INVOICE` (Integración con Facturación Electrónica / Colas)**:
   Cuando el cliente pide factura, no deberíamos bloquear el hilo de Java esperando que la SUNAT responda. Se crea un registro en `INVOICE` con estado `PENDING`. El worker de RabbitMQ manda el XML y si la SUNAT devuelve "OK", el worker actualiza esta tabla a estado `ISSUED` y guarda el `invoice_number` oficial.

3. **Modificaciones Menores que podrías considerar (Dependiendo de la madurez):**
   *   **Control de Inventario**: Una tabla extra `INVENTORY_TRANSACTION` si quieres descontar ingredientes exactos (ej: restar 150g de carne por cada hamburguesa vendida).
   *   **Caja / Cierre de Turno**: Una tabla `CASH_REGISTER_SESSION` para que el mesero "abra" su caja con S/50 en la mañana y rinda cuentas en la noche. 

## How this solves your requirements:

1. **Gestión de Órdenes y Pedidos (`ORDER`, `ORDER_ITEM`)**: 
   Cada orden está anclada a una Mesa (`TABLE`) y contiene múltiples productos. El estado (`status`) permite llevar el flujo: desde que se abre, se prepara en cocina, se sirve y se cobra.

2. **Gestión de Meseros (`WAITER`)**:
   Las órdenes están vinculadas al mesero que las atendió (`waiter_id`). Esto permite saber exactamente quién tomó qué pedido. Además, los meseros tienen un `pin_code` para que puedan loguearse rápidamente en la tablet del restaurante sin escribir un email/password largo.

3. **Estadísticas para los Dueños (Analytics)**:
   Gracias a esta estructura, el dueño puede obtener métricas súper valiosas haciendo *queries* directas en su esquema. Ejemplos de **Métricas que se pueden sacar automáticamente**:
   - **Ventas totales por día/mes:** Sumando los montos en la tabla `PAYMENT`.
   - **Mesero estrella:** Agrupando `ORDER` por `waiter_id` y sumando el `total_amount` generado por cada uno.
   - **Productos más vendidos:** Sumando la cantidad (`quantity`) en `ORDER_ITEM` agrupado por `product_id`.
   - **Rotación de mesas:** Calculando la diferencia de tiempo entre `opened_at` y `closed_at` en `ORDER` para saber qué tan rápido se desocupan las mesas.
   - **Hora pico:** Contando cuántas órdenes se abren por hora en `opened_at`.
