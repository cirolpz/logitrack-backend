# LogiTrack – Backend & AI Microservice

![Java](https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Python](https://img.shields.io/badge/Python_3.11-3776AB?style=for-the-badge&logo=python&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)

Sistema de gestión de envíos logísticos desarrollado como Trabajo Práctico para **Laboratorio de Construcción de Software (UNGS)**.

Permite registrar envíos, gestionar su ciclo de vida, calcular prioridades con IA, auditar operaciones y ejercer derechos ARCO (Ley 25.326).

---

## Arquitectura del sistema

El repositorio contiene **dos servicios independientes** deployados en Railway:

### 1. Backend API — Java / Spring Boot

| Tecnología | Versión |
|---|---|
| Java | 21 |
| Spring Boot | 3.4.4 |
| Spring Data JPA | — |
| PostgreSQL (Supabase) | — |
| Maven | — |
| Swagger / OpenAPI | — |

**Responsabilidades:**
- CRUD de envíos con validación de CP (rango 1000–9499)
- Gestión de estados logísticos con transiciones validadas
- Registro de historial de cambios de estado con usuario y timestamp
- Anonimización de datos personales (borrado lógico — Ley 25.326)
- Cálculo de probabilidad de retraso por envío
- Dashboard de auditoría con métricas de actividad
- Integración HTTP con el microservicio de IA

### 2. Microservicio IA — Python / Flask

| Tecnología | Uso |
|---|---|
| Flask | API REST |
| scikit-learn | RandomForestClassifier |
| pandas / numpy | Generación y manejo del dataset |
| Nominatim (OSM) | Geolocalización de CPs |

**Responsabilidades:**
- Predicción de prioridad (BAJA / MEDIA / ALTA)
- Cálculo de distancia entre CPs usando Haversine
- Entrenamiento con dataset sintético de 1200 registros
- Re-entrenamiento en caliente sin downtime (`POST /retrain`)

---

## Estructura del repositorio

```
logitrack-backend/
│
├── logitrack_IA/
│   ├── RandomForestIA.py      # Servicio Flask con endpoints /predict y /retrain
│   ├── generar_dataset.py     # Generador de dataset sintético (US-30)
│   ├── datasetIA.csv          # Dataset de entrenamiento (1200 registros)
│   ├── requirements.txt
│   ├── Dockerfile
│   └── railway.toml
│
├── src/
│   └── main/
│       ├── java/com/logitrack/logitrack_api/
│       │   ├── config/        # DatosSemillas, CorsConfig
│       │   ├── controller/    # EnvioController, DashboardController, HistorialController
│       │   ├── dto/           # EnvioRequestDTO, EnvioResponseDTO
│       │   ├── model/         # Envio, Usuario, HistorialEstado, EstadoEnvio
│       │   ├── repository/    # EnvioRepository, UsuarioRepository, HistorialEstadoRepository
│       │   └── service/       # EnvioService, UsuarioService
│       └── resources/
│           └── application.properties
│
├── tests/
├── README.md
├── CONTRIBUTING.md
└── pom.xml
```

---

## Variables de entorno (Railway)

| Variable | Descripción |
|---|---|
| `DATABASE_URL` | URL de conexión a Supabase (PostgreSQL) |
| `DATABASE_USER` | Usuario de la base de datos |
| `DATABASE_PASSWORD` | Contraseña de la base de datos |
| `IA_SERVICE_URL` | URL del microservicio IA (ej: `https://logitrack-ia-xxx.railway.app`) |
| `PORT` | Puerto dinámico asignado por Railway |

---

## Ciclo de vida de un envío

```
REGISTRADO → EN_TRANSITO → EN_SUCURSAL → ENTREGADO
```

El backend valida que no se puedan realizar saltos inválidos. Cada transición queda registrada en `HistorialEstado` con usuario y timestamp.

---

## Endpoints principales

### Envíos

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/envios` | Crear envío (llama a la IA para prioridad y distancia) |
| `GET` | `/api/envios` | Listar todos los envíos |
| `GET` | `/api/envios/{trackingId}` | Obtener envío por ID |
| `PUT` | `/api/envios/{trackingId}/estado` | Cambiar estado del envío |
| `GET` | `/api/envios/buscar?nombre=` | Buscar por nombre/apellido/trackingId |
| `GET` | `/api/envios/por-fecha?desde=&hasta=` | Filtrar por rango de fechas |
| `GET` | `/api/envios/{trackingId}/historial` | Historial de cambios de estado |
| `POST` | `/api/envios/{trackingId}/anonimizar` | Borrado lógico (Ley 25.326) |
| `GET` | `/api/envios/solicitudes-borrado` | Envíos anonimizados (solo Supervisor) |

### Dashboard y Auditoría

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/dashboard/resumen` | Métricas: totales, por estado, actividad reciente |
| `GET` | `/api/historial/buscar?usuario=&accion=` | Búsqueda de logs de auditoría |

### Microservicio IA

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/health` | Healthcheck |
| `POST` | `/predict` | Predecir prioridad y calcular distancia |
| `POST` | `/retrain` | Re-entrenar modelo sin downtime |

**Body de `/predict`:**
```json
{
  "cp_origen": "1663",
  "cp_destino": "7000",
  "peso": 15.0,
  "tipo_envio": "Medica"
}
```

**Respuesta:**
```json
{
  "prioridad": "ALTA",
  "distanciaKm": 385.2
}
```

---

## Dataset sintético (US-28 / US-30)

El modelo se entrena con **1200 registros sintéticos** generados por `generar_dataset.py`.

**Reglas de negocio aplicadas:**

| Condición | Prioridad |
|---|---|
| Tipo == Peligrosa | ALTA |
| Tipo == Médica y (peso > 5 ó dist > 100) | ALTA |
| Tipo Estándar/Frágil, peso > 15 y dist > 200 | ALTA |
| Tipo Estándar/Frágil, peso ≥ 5 ó dist ≥ 50 | MEDIA |
| Tipo Estándar/Frágil, peso < 5 y dist < 50 | BAJA |

Distribución: ~30% BAJA / ~50% MEDIA / ~20% ALTA

---

## Ejecutar en local

### 1. Microservicio IA

```bash
cd logitrack_IA
pip install -r requirements.txt
python RandomForestIA.py
# Disponible en http://localhost:5001
```

### 2. Backend

```bash
# Configurar variables de entorno o application.properties
mvn spring-boot:run
# Disponible en http://localhost:8080
```

### Swagger UI

```
http://localhost:8080/swagger-ui/index.html
```

---

## Datos semilla

Al iniciar con BD vacía se crean automáticamente:
- **2 usuarios**: `melina` (Operador, clave: `1234`) y `ciro` (Supervisor, clave: `admin`)
- **3 envíos de prueba** con distintos estados, prioridades y motivos

---

## Autores

Proyecto académico — **Grupo 6**
Ciro Martín López, Karin Pellegrini, Melina Scabini
Licenciatura en Sistemas — UNGS
