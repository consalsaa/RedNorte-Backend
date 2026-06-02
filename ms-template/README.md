# 🏗️ ms-template - Arquetipo Maven Base para Microservicios y BFFs

Este proyecto es el **Arquetipo Base personalizado (no runtime)** para la **Plataforma RedNorte**. Su objetivo principal es actuar como una plantilla Maven reutilizable que permite al equipo de desarrollo generar nuevos microservicios y módulos BFF homogéneos en segundos, aplicando rigurosamente los estándares técnicos y de arquitectura de la institución.

---

## 🛠️ Características Tecnológicas del Andamiaje (Scaffolding)

El microservicio generado a partir de este arquetipo cuenta con la siguiente pila preconfigurada de fábrica:
*   **Java 21** y **Spring Boot 4.0.5** con soporte de **Spring Cloud 2025.1.1**.
*   **Spring Data JPA**: Configurado para acceso relacional a base de datos.
*   **Base de Datos Híbrida**: Mapea **H2 en memoria** por defecto para agilizar el desarrollo local y provee overrides de variables de entorno para integrarse con **PostgreSQL 15** en contenedores Docker de producción.
*   **Eureka Discovery Client**: Soporte de autodescubrimiento y registro automático en el servidor centralizado de Eureka Server.
*   **Resilience4j & Spring AOP**: Configuración base de Circuit Breaker para implementar de forma ágil e inmediata tolerancia a fallos ante caídas de servicios remotos.
*   **AppConfig (BFF Support)**: Bean `RestTemplate` anotado con `@LoadBalanced` pre-instanciado, permitiendo el enrutamiento y balanceo dinámico de llamadas HTTP utilizando nombres lógicos de Eureka.
*   **Dockerfile de producción**: Configuración de compilación multi-etapa (multi-stage) optimizada en Alpine Linux.

---

## 🚀 Guía de Instalación del Arquetipo

Sigue estos pasos para compilar e instalar el arquetipo en tu repositorio local Maven (`~/.m2`):

### Paso 1: Abrir la consola en el arquetipo
Abre una consola PowerShell o Bash en la ruta del arquetipo:
```bash
cd "C:\Users\Angel\Documents\Duoc\Fullstack III\Ev3\Rednorte\RedNorte-backend\ms-template"
```

### Paso 2: Ejecutar la instalación
Dado que no dispones de un comando `mvn` global en el PATH de la máquina de desarrollo, utiliza el Maven Wrapper disponible en los microservicios hermanos (el cual ya se encuentra copiado en este módulo para tu comodidad):
```powershell
# Instala el arquetipo en tu caché local .m2
.\mvnw.cmd clean install
```
*Una vez completado con éxito, verás el mensaje `BUILD SUCCESS`. El arquetipo ya estará registrado localmente bajo las coordenadas `com.rednorte.archetypes:rednorte-ms-archetype:1.0.0`.*

---

## 🏗️ Generar un Nuevo Proyecto con el Arquetipo

Para crear un nuevo microservicio o BFF utilizando el arquetipo instalado, abre una terminal en la carpeta donde deseas crear tu proyecto (ej: `RedNorte-backend/`) y ejecuta el siguiente comando:

```bash
mvn archetype:generate \
  -DarchetypeGroupId=com.rednorte.archetypes \
  -DarchetypeArtifactId=rednorte-ms-archetype \
  -DarchetypeVersion=1.0.0 \
  -DgroupId=com.rednorte \
  -DartifactId=ms-auditoria \
  -Dversion=1.0.0-SNAPSHOT \
  -Dpackage=com.rednorte.ms_auditoria \
  -DinteractiveMode=false
```

### 📋 Explicación de los Parámetros:
*   `archetypeGroupId`: Coordenadas del grupo del arquetipo (`com.rednorte.archetypes`).
*   `archetypeArtifactId`: Nombre del arquetipo base (`rednorte-ms-archetype`).
*   `archetypeVersion`: Versión del arquetipo (`1.0.0`).
*   `groupId`: El identificador de grupo para tu nuevo microservicio (ej: `com.rednorte`).
*   `artifactId`: El nombre del nuevo microservicio a generar (ej: `ms-auditoria`, `ms-estadisticas`, etc.).
*   `version`: Versión inicial del proyecto (ej: `1.0.0-SNAPSHOT`).
*   `package`: Nombre del paquete Java base (ej: `com.rednorte.ms_auditoria`). Las carpetas físicas del código fuente se estructurarán automáticamente siguiendo este paquete.
*   `interactiveMode=false`: Desactiva la confirmación interactiva para automatizar la creación inmediata.

---

## 💡 Consejos de Adaptación del Proyecto Generado

Una vez generado el proyecto a partir de la plantilla, puedes adaptarlo a tu caso de uso específico:

1.  **Si estás creando un BFF puro sin base de datos propia**:
    *   Puedes remover las dependencias de `spring-boot-starter-data-jpa`, `h2` y `postgresql` en el `pom.xml` generado si no requieres guardar datos relacionales locales.
    *   Mantén `AppConfig.java` y `Resilience4j` para gestionar el consumo orquestado y resiliente hacia los microservicios de backend.
2.  **Si estás creando un Microservicio de datos puro**:
    *   Mantén las dependencias de JPA y bases de datos relacionales.
    *   Crea tus entidades anotadas con `@Entity` dentro del paquete base. El `ddl-auto: update` de Hibernate se encargará de crear las tablas de forma automática.
    *   Si el microservicio no consumirá APIs externas de otros módulos, puedes eliminar la clase `AppConfig.java` de forma segura.
