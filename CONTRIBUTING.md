# Guía de Contribución — LogiTrack Backend

Para mantener el orden, la trazabilidad y la calidad del código, todos los miembros del Grupo 6 deben seguir estas directrices.

---

## Estrategia de ramas

El proyecto sigue **GitFlow simplificado**:

```
feature/us##-descripcion
        ↓
    develop
        ↓
      main
```

| Rama | Propósito |
|---|---|
| `main` | Producción. Se actualiza solo desde `develop` vía merge. |
| `develop` | Integración. Todas las features pasan por acá antes de main. |
| `feature/us##-*` | Una rama por User Story. Se crea desde `develop`. |
| `fix/descripcion` | Correcciones puntuales de bugs. |

**Ejemplos de nombres de rama:**
```
feature/us17-motivo-prioridad
feature/us24-probabilidad-retraso
feature/us28-us30-dataset-entrenamiento
fix/ia-timeout-distancia
fix/dockerfile-ia
```

---

## Flujo de trabajo paso a paso

```bash
# 1. Situarse en develop actualizado
git checkout develop
git pull origin develop

# 2. Crear rama de la US
git checkout -b feature/us##-descripcion

# 3. Desarrollar y commitear
git add <archivos>
git commit -m "feat: descripción del cambio"

# 4. Subir la rama
git push origin feature/us##-descripcion

# 5. Mergear a develop
git checkout develop
git merge feature/us##-descripcion --no-ff
git push origin develop

# 6. Mergear a main
git checkout main
git merge develop --no-ff
git push origin main

# 7. Eliminar rama feature
git branch -d feature/us##-descripcion
git push origin --delete feature/us##-descripcion
```

---

## Convención de commits

Se usa **Conventional Commits**:

```
tipo: descripción breve en presente
```

| Tipo | Cuándo usarlo |
|---|---|
| `feat` | Nueva funcionalidad (endpoint, componente, modelo) |
| `fix` | Corrección de bug |
| `refactor` | Mejora de código sin cambiar comportamiento |
| `docs` | README, CONTRIBUTING, comentarios |
| `test` | Tests unitarios o de integración |
| `chore` | Dependencias, configuración, Dockerfile |
| `debug` | Logs temporales de diagnóstico |

**Ejemplos reales del proyecto:**
```
feat: US-24 probabilidad de retraso calculada por reglas en EnvioResponseDTO
fix: trim() en iaServiceUrl para eliminar salto de línea en Railway
fix: agregar generar_dataset.py y datasetIA.csv al Dockerfile de la IA
fix: todos los endpoints de envíos devuelven EnvioResponseDTO con probabilidadRetraso
debug: logs detallados en consultarIA para diagnosticar fallo en Railway
```

---

## Estilo de código

### Java / Spring Boot

- **Java 21**
- Arquitectura en capas: `controller → service → repository → model`
- Siempre usar **DTOs** para requests y responses (nunca exponer la entidad directamente)
- Validaciones con **Bean Validation** (`@NotBlank`, `@NotNull`)
- Documentar endpoints con `@Operation` (Swagger)
- Variables de entorno via `@Value` y `application.properties`

### Python / Flask

- Seguir PEP 8
- Documentar funciones con docstrings
- Usar `timeout` en todas las llamadas HTTP externas
- El dataset y el generador deben mantenerse sincronizados

---

## Arquitectura de capas (Java)

```
controller/   ← recibe HTTP, delega al service, devuelve DTO
service/      ← lógica de negocio, llama al repository y a la IA
repository/   ← acceso a BD con Spring Data JPA
dto/          ← objetos de entrada (Request) y salida (Response)
model/        ← entidades JPA
config/       ← seeds, CORS, beans de configuración
```

---

## Variables de entorno

Nunca hardcodear credenciales. Usar variables de entorno en Railway y localmente en `application.properties` (no commitear ese archivo con datos reales).

```properties
ia.service.url=${IA_SERVICE_URL:http://localhost:5001}
spring.datasource.url=${DATABASE_URL}
```

---

## Buenas prácticas

- Commits pequeños y atómicos (un cambio lógico por commit)
- No subir credenciales, `.env` ni archivos de configuración con datos reales
- El fallback de la IA siempre debe devolver un valor razonable (no dejar campos `null`)
- Ante un bug en Railway, agregar logs con `System.out.println("[CONTEXTO] mensaje")` antes de debuggear a ciegas

---

## Autores

Proyecto académico — **Grupo 6**
Ciro Martín López, Karin Pellegrini, Melina Scabini
Licenciatura en Sistemas — UNGS
