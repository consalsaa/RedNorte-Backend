# Plataforma RedNorte — Backend 🏥

**RedNorte** es el backend de una plataforma inteligente para la gestión de listas de espera hospitalarias del Servicio Público de Salud RedNorte. Su propósito es optimizar el flujo de atención médica, automatizar reasignaciones ante cancelaciones, y proveer transparencia tanto al personal clínico como a los pacientes.

El sistema está implementado como una arquitectura de **microservicios desacoplados** usando **Spring Boot 3.x / 4.x**, **Java 21**, **Spring Cloud (Eureka + Gateway)**, **Redis**, **RabbitMQ**, **PostgreSQL** y con cobertura de pruebas garantizada mediante **JaCoCo (≥ 85%)**.

---

## 🏗️ Arquitectura del Sistema

```mermaid
graph TD
    Client[Cliente / Frontend React] -->|Peticiones HTTP| Gateway[API Gateway :8080]
    Gateway -->|Enrutamiento dinámico| Eureka[Servidor Eureka :8761]

    subgraph Microservicios de Negocio
        Gateway --> MS_Listas[ms-listas-espera :8081]
        Gateway --> MS_Portal[ms-portal-paciente :8083]
        Gateway --> MS_Reasignacion[ms-reasignacion :8082]
        Gateway --> MS_Usuarios[ms-usuarios :8084]
        Gateway --> MS_Notificaciones[ms-notificaciones :8085]
        Gateway --> MS_Auditoria[ms-auditoria :8086]
    end

    subgraph Infraestructura
        MS_Listas -.->|Caché @Cacheable/@CacheEvict| Redis[(Redis :6379)]
        MS_Reasignacion ==>|Publica evento JSON| Rabbit[(RabbitMQ :5672)]
        Rabbit ==>|@RabbitListener consume| MS_Notificaciones
        MS_Auditoria -.->|Stored Procedure| DB[(PostgreSQL :5432)]
    end
```

---

## 📦 Microservicios — Descripción de Propósito

### 1. 🔍 `eureka-server` (Puerto 8761)
**Propósito:** Servidor de descubrimiento de servicios. Todos los microservicios se registran aquí dinámicamente, permitiendo al API Gateway balancear carga sin conocer IPs físicas. Es el primer servicio en arrancar.

### 2. 🛡️ `api-gateway` (Puerto 8080)
**Propósito:** Punto de entrada único para toda la plataforma. Se encarga de:
- Validar tokens JWT mediante `JwtAuthFilter` (HMAC SHA-256, expiración de 1 hora).
- Enrutar peticiones al microservicio correcto vía Eureka.
- Permite acceso público solo a `/api/v1/auth/**` (login y registro) y a la documentación Swagger.

### 3. 📋 `ms-listas-espera` (Puerto 8081)
**Propósito:** Núcleo del negocio hospitalario. Gestiona el registro, priorización y estado de las atenciones médicas.
- **Patrón Factory Method:** Instancia polimórficamente `AtencionCirugia`, `AtencionConsulta` y `AtencionEmergencia`.
- **Caché Redis:** Cachea la lista de espera con `@Cacheable` e invalida con `@CacheEvict` ante cambios.
- **Patrón SAGA:** Expone endpoints de transacción compensatoria (`/saga/crear`, `/saga/confirmar`, `/saga/cancelar`).

### 4. 🔄 `ms-reasignacion` (Puerto 8082)
**Propósito:** Motor de contingencia. Cuando una cita se cancela, busca automáticamente al siguiente paciente prioritario, actualiza su estado y publica un evento en RabbitMQ.
- **Circuit Breaker (Resilience4j):** Protege contra fallos de `ms-listas-espera`. Si falla, guarda la reasignación como "FALLIDA" para reintentar.
- **RabbitMQ:** Publica mensajes al exchange `reasignacion.exchange`.

### 5. 🏥 `ms-portal-paciente` (Puerto 8083)
**Propósito:** BFF (Backend For Frontend). Actúa como intermediario entre la interfaz React y los microservicios de negocio, entregando vistas unificadas del historial del paciente. Incluye Circuit Breaker para degradar con gracia ante fallos.

### 6. 👤 `ms-usuarios` (Puerto 8084)
**Propósito:** Gestor de identidad y autenticación. Es el **único microservicio autorizado para emitir tokens JWT**. Gestiona el registro y login de usuarios con sus respectivos roles.
- **Endpoint `POST /api/v1/auth/login`:** Valida credenciales y devuelve un JWT.
- **Endpoint `POST /api/v1/auth/register`:** Registra un nuevo usuario en el sistema (rol por defecto: `ROLE_PACIENTE`).
- **`DataInitializer`:** Al arrancar, inyecta usuarios de prueba (`admin`, `medico`, `paciente`) si no existen.

### 7. 🔔 `ms-notificaciones` (Puerto 8085)
**Propósito:** Cartero digital del sistema. No expone endpoints HTTP. Su única función es escuchar la cola `reasignacion.queue` en RabbitMQ mediante `@RabbitListener` y simular el envío de alertas al paciente (email/SMS) registrando la notificación en el log del sistema.

### 8. 📊 `ms-auditoria` (Puerto 8086)
**Propósito:** Módulo de reportabilidad hospitalaria. Ejecuta el Stored Procedure `sp_calcular_estadisticas_espera` de PostgreSQL y expone los resultados en el endpoint `GET /api/v1/auditoria/estadisticas`, retornando métricas del rendimiento de la lista de espera por tipo de prioridad.

---

## 🛠️ Stack Tecnológico

| Tecnología | Versión | Uso |
|---|---|---|
| Java | 21 | Lenguaje base de todos los microservicios |
| Spring Boot | 3.x / 4.x | Framework de aplicaciones |
| Spring Cloud Gateway | 2025.x | API Gateway y filtro de seguridad JWT |
| Spring Cloud Eureka | 2025.x | Service Discovery y balanceo de carga |
| Resilience4j | 2.2.0 | Circuit Breaker y Fallbacks |
| Spring Data JPA | — | Acceso a datos con patrón Repository |
| PostgreSQL | 15 | Base de datos de producción |
| H2 | — | Base de datos en memoria para desarrollo local |
| Redis | alpine | Caché de la lista de espera |
| RabbitMQ | 3-management | Mensajería asíncrona de notificaciones |
| JJWT | 0.12.6 | Generación y validación de tokens JWT |
| JaCoCo | 0.8.12 | Cobertura de pruebas ≥ 85% |
| Springdoc OpenAPI | 2.5.0 | Documentación Swagger automática |

---

## 🔑 Endpoints de Autenticación (`ms-usuarios`)

Todos los endpoints de autenticación son **públicos** (no requieren token JWT).

### `POST /api/v1/auth/login`
Inicia sesión y obtiene un token JWT.

**Request body:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```
**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "username": "admin",
  "role": "ROLE_ADMIN",
  "message": "Autenticación exitosa"
}
```

### `POST /api/v1/auth/register`
Registra un nuevo usuario. El rol asignado por defecto es `ROLE_PACIENTE`.

**Request body:**
```json
{
  "username": "juan.perez",
  "password": "miPassword123"
}
```
**Response (201 Created):**
```json
{
  "message": "Usuario registrado exitosamente",
  "username": "juan.perez",
  "role": "ROLE_PACIENTE"
}
```
**Response (409 Conflict) — Usuario ya existe:**
```json
{
  "message": "El nombre de usuario 'juan.perez' ya está en uso."
}
```

---

## 🚀 Instrucciones de Despliegue

### Requisitos Previos
- JDK 21
- Docker y Docker Compose

### Levantar con Docker Compose (Recomendado)
```bash
docker-compose up -d
```
Esto levanta todos los servicios en el orden correcto: PostgreSQL → Redis → RabbitMQ → Eureka → Gateway → Microservicios.

### Desarrollo Local (sin Docker)
```bash
# 1. Levantar infraestructura de soporte
docker-compose up -d postgres redis rabbitmq

# 2. Arrancar en orden: eureka-server, luego api-gateway, luego los microservicios
cd eureka-server && ./mvnw spring-boot:run
```

### Cobertura de Pruebas (JaCoCo)
```bash
cd ms-usuarios
./mvnw clean verify
# Reporte en: target/site/jacoco/index.html
```

---

## 📖 Documentación OpenAPI / Swagger

Con el sistema corriendo, accede a la documentación interactiva en:

🔗 **Swagger UI**: [http://localhost:8080/webjars/swagger-ui/index.html](http://localhost:8080/webjars/swagger-ui/index.html)

---

## 👥 Usuarios de Prueba (Pre-cargados)

| Username | Password | Rol |
|---|---|---|
| `admin` | `admin123` | ROLE_ADMIN |
| `medico` | `medico123` | ROLE_MEDICO |
| `paciente` | `paciente123` | ROLE_PACIENTE |
