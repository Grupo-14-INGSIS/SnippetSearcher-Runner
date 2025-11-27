# Resumen de la Sesión: Análisis del Repositorio SnippetSearcher Runner

Este documento resume el estado actual del proyecto `SnippetSearcher Runner` y define los próximos pasos para continuar con su desarrollo.

## 1. Análisis del Proyecto

Se ha realizado una evaluación completa del repositorio, incluyendo su estructura, dependencias, funcionalidades implementadas y requerimientos funcionales.

### Arquitectura y Tecnología

- **Proyecto:** `SnippetSearcher Runner`, un microservicio de Spring Boot escrito en Kotlin.
- **Stack Tecnológico:**
    - **Lenguaje:** Kotlin
    - **Framework:** Spring Boot (con Web, WebFlux, Data JPA, WebSocket)
    - **Base de Datos:** PostgreSQL
    - **Mensajería:** Spring Kafka (aunque la comunicación con Redis Streams se mencionó en el `AccessManager`)
    - **Contenerización:** Docker y Docker Compose.
    - **Monitoreo:** New Relic.
- **Rol en el Sistema:** Actúa como el servicio principal para la lógica de negocio de los snippets. Se comunica con otros servicios como `AccessManager` (para permisos) y un `formatter`. Los endpoints bajo `/internal` sugieren una API privada para la comunicación entre servicios.

## 2. Estado Actual de la Implementación

El proyecto está parcialmente implementado. Se identificaron las siguientes funcionalidades activas e inactivas.

### Funcionalidades Completadas y Activas

- **Gestión de Snippets (CRUD):** El `RunnerController` expone endpoints en `/internal/snippets` para crear, leer, actualizar y borrar snippets. Está diseñado para ser consumido por otro servicio (probablemente un API Gateway o BFF).
- **Configuración de Reglas de Formateo:** El `FormattingConfigController` (`/api/v1/formatting`) ofrece una API completa para que los usuarios gestionen sus reglas de formateo (consultar, habilitar/deshabilitar, etc.).
- **Gestión y Ejecución de Tests:** El `TestController` (`/api/v1/snippets/{snippetId}/tests`) permite crear, gestionar y **ejecutar** tests para snippets de forma síncrona.

### Funcionalidades Incompletas o Desactivadas

- **Ejecución Interactiva de Snippets (User Story #10):** Es la funcionalidad más crítica que falta. El `SnippetExecutionController`, responsable de ejecutar un snippet y devolver el output de forma interactiva (usando Server-Sent Events), está **completamente comentado**.
- **Gestión de Linting (User Story #14, #15):** El `LintingJobController` está **comentado**. Esto impide a los usuarios gestionar reglas de linteo y ver los resultados.
- **Jobs Asíncronos:** La presencia de `LintingJobService`, `FormattingJobService` y un `TestingController` comentado sugiere una arquitectura planificada para procesar tareas pesadas (formateo, linteo, testing de todo un proyecto) de forma asíncrona, pero no está implementada ni expuesta actualmente.

## 3. Contexto de Microservicios

- A partir del resumen de la sesión anterior, este servicio (`Runner`) es el `SnippetService` que debe interactuar con el `AccessManager`.
- Las llamadas a los endpoints de este servicio deberían estar protegidas, validando los permisos del usuario a través del `AccessManager`. Por ejemplo, antes de devolver un snippet, se debería consultar al `AccessManager` si el `userId` tiene permiso de lectura.

## 4. Próximos Pasos Sugeridos

El foco de las próximas sesiones será implementar las funcionalidades críticas que faltan.

1.  **Habilitar la Ejecución Interactiva de Snippets:**
    - Descomentar y finalizar la implementación del `SnippetExecutionController` y su `SnippetExecutionService`.
    - Esto es fundamental para cumplir con la User Story #10 y dar vida a la funcionalidad principal de la aplicación.

2.  **Implementar la Funcionalidad de Linting:**
    - Descomentar y completar el `LintingJobController` y los servicios asociados.
    - Implementar la lógica para que los usuarios puedan configurar reglas y ver los errores de linteo en sus snippets (User Stories #14 y #15).

3.  **Integrar con `AccessManager`:**
    - Añadir lógica a los endpoints existentes (ej: `RunnerController`, `TestController`) para que consulten al `AccessManager` antes de realizar operaciones.
    - Se debe asegurar que un usuario solo pueda ver, editar o testear los snippets para los que tiene permiso.

4.  **Finalizar los Jobs Asíncronos:**
    - Implementar completamente los servicios de jobs (`FormattingJobService`, `LintingJobService`, `TestingJobService`) para tareas masivas, mejorando la experiencia de usuario.

---

## Session Summary: Debugging Snippet Searcher Runner (2025-11-27)

During this session, we addressed several issues preventing the `Snippet Searcher Runner` application from starting correctly using `docker compose up`.

**1. Initial `hibernate-types` dependency conflict**
*   **Problem:** The application failed to start with an `org.hibernate.type.descriptor.java.spi.JdbcTypeRecommendationException: Could not determine recommended JdbcType for Java type 'java.util.Map<java.lang.String, java.lang.Object>'`. This was due to an incompatibility between the `com.vladmihalcea:hibernate-types-60` dependency (for Hibernate 6.0) and the project's Hibernate version (6.6). This library was interfering with the custom `MapJsonConverter`.
*   **Solution:** Removed the `implementation "com.vladmihalcea:hibernate-types-60:2.21.1"` dependency from `build.gradle`.

**2. `AssetServiceClient` bean not found error**
*   **Problem:** The `app-runner` service failed to start with `Parameter 2 of constructor in com.grupo14IngSis.snippetSearcherRunner.consumer.SnippetTaskConsumer required a bean of type 'com.grupo14IngSis.snippetSearcherApp.client.AssetServiceClient' that could not be found.`. This was a package name mismatch and a likely stale Docker image. Additionally, `AssetServiceClient` required a `RestTemplate` bean, but only `WebClient` was configured.
*   **Solution:**
    *   **Diagnosis:** Determined that the `SnippetTaskConsumer` source code correctly imported `com.grupo14IngSis.snippetSearcherRunner.client.AssetServiceClient`, indicating a stale Docker image was the primary cause of the package mismatch error.
    *   **RestTemplate:** Created `src/main/kotlin/com/grupo14IngSis/snippetSearcherRunner/config/RestTemplateConfig.kt` to provide a `RestTemplate` bean, as requested by the user.
    *   **Action for User:** Advised the user to run `docker compose up --build` to ensure the latest code and configuration were used.

**3. Database tables not found (`relation "formatting_rules" does not exist`)**
*   **Problem:** After resolving the `AssetServiceClient` issue, the application failed with `ERROR: relation "formatting_rules" does not exist`. This indicated the database initialization script (`db/init/01-init-snippet.sql`) was not being executed.
*   **Solution:**
    *   **Cause 1 (Incorrect Volume Mount):** Initially, the `docker-compose.yml` mapped `./db/init` to `/docker-entrypoint-initdb.d`, but the SQL script was in `db/`. The user then moved the script to `db/init/01-init-snippet.sql`.
    *   **Cause 2 (Stale Database Volume):** The PostgreSQL container only runs initialization scripts when its data directory is empty. The `runner-pgdata` named volume was persisting across restarts, preventing re-initialization.
    *   **Configuration:** Added `spring.jpa.hibernate.ddl-auto: update` to `application.yml` to instruct Hibernate to automatically create or update the schema based on entity classes.
    *   **Action for User:** Instructed the user to run `docker compose down -v` to remove the old data volume, followed by `docker compose up --build` to force a clean database setup and ensure the init script runs.

**4. `jsonb` column type mismatch (`column "config_rules" is of type jsonb but expression is of type character varying`)**
*   **Problem:** The application started, but then encountered an error trying to save data: `column "config_rules" is of type jsonb but expression is of type character varying`. This was because the custom `MapJsonConverter` was converting `Map` to a `String`, which Hibernate then tried to bind to a `jsonb` column, causing a type mismatch at the JDBC level.
*   **Solution:** Modified `FormattingRule.kt` and `LintingRule.kt` to replace the custom `@Convert(converter = MapJsonConverter::class)` annotation with `@JdbcTypeCode(SqlTypes.JSON)`. This leverages Hibernate's native JSON type handling, correctly mapping `MutableMap<String, Any>` to `jsonb` without needing a custom converter.
*   **Action for User:** Advised the user to run `docker compose up --build` to apply these entity changes.