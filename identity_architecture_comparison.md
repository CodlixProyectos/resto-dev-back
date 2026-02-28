# Arquitectura Multi-Tenant: ¿Usuarios Globales o Locales?

Este documento técnico explica y compara las dos estrategias principales para gestionar identidades (usuarios) en un sistema SaaS Multi-Tenant (Múltiples Inquilinos) como **Codlix Resto-Dev**, orientado a resolver la duda analítica de por qué **no** replicamos la tabla `users` en cada esquema de cliente.

---

## 🛑 Estrategia 1: Identidad Totalmente Aislada (Tabla `users` por Esquema)
*Es la idea de replicar la tabla completa de usuarios (`id`, `email`, `password`) dentro de cada `client_xxxxx`.*

### ¿Cómo funciona?
Cada restaurante ("Tenant") tiene su mini-universo. El Restaurante A ("La Pizzería") tiene una tabla `client_pizzeria.users` y el Restaurante B ("El Sushi") tiene su tabla `client_sushi.users`.

### Ventajas:
1. **Aislamiento absoluto:** Si borras el esquema de la pizzería, se borran todos sus usuarios al instante. Fácil de separar copias de seguridad por cliente.

### Desventajas Críticas (Por qué los SaaS modernos NO lo usan):
1. **El Problema del Multi-Local (Franquicias):**
   * Supongamos que "Jorge" es dueño de la Pizzería y compró el Sushi. Para acceder a tu aplicación deberá **registrarse dos veces** usando el mismo correo (una vez en cada restaurante).
   * Tendrá **dos contraseñas**. Si restablece su contraseña en la Pizzería, la del Sushi sigue igual, creando confusión.
   * **Incapacidad de Dashboard Consolidado:** Como el sistema ve a "Jorge Pizzería" y "Jorge Sushi" como dos filas totalmente distintas que no se conocen entre sí en la base de datos, construir un gráfico en el frontend que sume *"¿Cúanto ganaron todos los locales de Jorge hoy?"* requerirá queries lentos e ingeniería inversa muy compleja.

2. **La Facturación (SaaS Billing):**
   * Tu pasarela de pagos (Stripe, Paypal) necesita vincular el pago mensual a "Una Persona o Entidad" global. Si la persona existe repetida en 5 esquemas locales, el sistema principal no sabrá a quién cobrarle.

3. **Autenticación (JWT Limitado):**
   * Antes de hacer Login, tu Backend (Spring Boot) necesita saber si el usuario existe para darle el token. Pero si no sabes a qué restaurante pertenece Jorge porque no ha iniciado sesión, ¿en cuál de los 100 esquemas vas a buscar si su `email` existe para validar su contraseña?

---

## 🟢 Estrategia 2: Modelo Híbrido Estándar SaaS (Lo que estamos construyendo)
*Identidad Centralizada para los administradores + Operatividad local para los empleados físicos.*

### ¿Cómo funciona?

#### 1. Tabla Maestra (SaaS Identidad) -> `public.users`
*  Guarda a los Dueños, Contadores y Gerentes.
*  Tienen Email, Contraseña y se autentican en internet (Dashboard Web).
*  Se asocian a N restaurantes usando una tabla puente en el esquema global (`public.organization_members`).

#### 2. Tablas Locales (Staff Físico) -> `client_xxxx.employees`
*  Guarda a Meseros, Cajeros y Cocineros.
*  Tienen Nombres y PIN de 4 dígitos (ej: `1234`).
*  Se usan únicamente frente a la Tablet/POS fija localizada adentro de ese restaurante específico.

### Ventajas (Por qué es lo más escalable):

1. **Single Sign-On (SSO) Constante:** 
   Jorge (el Dueño) entra a `resto-dev.com`, ingresa su correo y contraseña una sola vez. Como interactuamos con el esquema `public`, el backend responde inmediatamente con el JWT.
2. **Selector de Negocios en el Header:** Al iniciar sesión, el sistema ve la tabla puente global e indica: *"El usuario Jorge tiene rol ADMIN en Pizzería y OWNER en Sushi"*. El frontend despliega un botón desplegable donde Jorge salta de una empresa a otra en menos de 1 segundo.
3. **Escalabilidad de Suscripciones:** El día que quieras cobrar "50$ por cada nuevo negocio", es fácil auditar cuántas organizaciones le pertenecen a qué usuario global.
4. **Respeto Operativo (Cero Basura Global):** Un cajero que gana el salario mínimo y solo usa la caja registradora no debería registrar su "Mail Personal" en nuestra plataforma B2B en la Nube. Codlix solo administra y factura *Suscripciones Empresariales*, no a los trabajadores de piso. Por eso a ellos los guardamos en su base local y entran con su PIN.

---

## Conclusión Ejecutiva

Replicar la tabla a todos los esquemas parece más sencillo al inicio porque "cada restaurante funciona por sí solo". Sin embargo, un SaaS B2B serio trata a sus clientes empresariales centralmente. 

Adoptar la **Identidad Global + Configuración por Organización** es el estándar de oro en GitHub, Notion, Slack, y Puntos de Venta (POS) como TouchBistro o Toast. Nos asegura que **Resto-Dev** soportará clientes que tengan 1 o 100 sucursales sin fragmentar la experiencia de usuario y haciendo que la facturación automatizada funcione perfectamente a futuro.
