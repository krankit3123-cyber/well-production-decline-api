# Well Production Decline API 🛢️

A production-grade **Spring Boot REST API** implementing **Arps Decline Curve Analysis** for oil & gas wells —
the industry-standard mathematical model used by petroleum engineers globally to forecast production and compute
Estimated Ultimate Recovery (EUR).

> Built by a Petroleum Engineering student at **IIT (ISM) Dhanbad** who also builds backend systems.

---

## The Math Behind It — Arps (1945)

The three decline models supported:

| Model | b-factor | Rate Equation | EUR Equation |
|-------|----------|---------------|--------------|
| **Exponential** | b = 0 | `q(t) = qi · exp(−Di · t)` | `EUR = (qi − q_lim) / Di` |
| **Hyperbolic** | 0 < b < 1 | `q(t) = qi / (1 + b·Di·t)^(1/b)` | `EUR = [qi^b / ((1−b)·Di)] · (qi^(1−b) − q_lim^(1−b))` |
| **Harmonic** | b = 1 | `q(t) = qi / (1 + Di·t)` | `EUR = (qi / Di) · ln(qi / q_lim)` |

Where:
- `qi` = initial production rate (bbl/day for oil, Mscf/day for gas)
- `Di` = initial nominal decline rate (fraction/day)
- `b` = Arps curvature factor
- `t` = elapsed time in days
- `q_lim` = economic limit rate (abandonment rate)

---

## Tech Stack

```
Language:     Java 17
Framework:    Spring Boot 3.3.5
Database:     PostgreSQL 16
Migrations:   Flyway
ORM:          Spring Data JPA / Hibernate
API Docs:     SpringDoc OpenAPI (Swagger UI)
Health:       Spring Actuator + Prometheus
Build:        Maven
Container:    Docker & Docker Compose
Tests:        JUnit 5 + AssertJ
```

---

## Project Structure

```
src/main/java/com/bp/decline/
├── WellProductionDeclineApplication.java
├── common/
│   ├── config/
│   │   ├── ClockConfig.java          # IST timezone clock bean
│   │   └── OpenApiConfig.java        # Swagger configuration
│   └── exception/
│       ├── GlobalExceptionHandler.java
│       └── ResourceNotFoundException.java
├── core/
│   ├── controller/
│   │   ├── HealthCheckController.java
│   │   ├── WellController.java
│   │   ├── ProductionRecordController.java
│   │   └── DeclineForecastController.java
│   ├── dto/
│   │   ├── well/         (WellRequest, WellResponse)
│   │   ├── production/   (ProductionRecordRequest, ProductionRecordResponse)
│   │   └── forecast/     (ForecastRequest, ForecastResponse, ForecastDataPoint)
│   ├── enums/
│   │   ├── DeclineType.java   (EXPONENTIAL, HYPERBOLIC, HARMONIC)
│   │   ├── FluidType.java     (OIL, GAS, CONDENSATE)
│   │   └── WellStatus.java    (ACTIVE, SHUT_IN, ABANDONED)
│   ├── logic/
│   │   └── ArpsDeclineEngine.java   ← Core petroleum engineering math
│   └── service/
│       ├── WellService.java
│       ├── ProductionRecordService.java
│       └── DeclineForecastService.java
└── persistence/
    ├── entity/
    │   ├── BaseEntity.java
    │   ├── WellEntity.java
    │   ├── ProductionRecordEntity.java
    │   └── DeclineForecastEntity.java
    └── repository/
        ├── WellRepository.java
        ├── ProductionRecordRepository.java
        └── DeclineForecastRepository.java

src/main/resources/
├── application.yml
└── db/migration/
    ├── V1__create_core_tables.sql    # Schema
    └── V2__seed_sample_data.sql      # Volve-inspired seed wells
```

---

## Running Locally

### Prerequisites
- Java 17+
- Docker & Docker Compose
- Maven 3.9+

### 1. Start PostgreSQL

```bash
docker-compose up -d
```

### 2. Run the Application

```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8083`

### 3. Explore via Swagger UI

```
http://localhost:8083/swagger-ui.html
```

---

## API Reference

### Health Check

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health-check` | Liveness ping → returns `"Pong"` |
| GET | `/actuator/health` | Detailed Spring health with DB status |

### Wells

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/wells` | Register a new well |
| GET | `/api/v1/wells` | List all wells (filter by `?status=ACTIVE`) |
| GET | `/api/v1/wells/{id}` | Get well by ID |
| PUT | `/api/v1/wells/{id}` | Update well details |

### Production Records

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/wells/{wellId}/production` | Add a production record |
| GET | `/api/v1/wells/{wellId}/production` | List all records for a well |
| GET | `/api/v1/wells/{wellId}/production/{recordId}` | Get a specific record |

### Decline Curve Forecasts ⭐

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/wells/{wellId}/forecasts` | Compute Arps forecast + EUR |
| GET | `/api/v1/wells/{wellId}/forecasts` | List all forecasts for a well |
| GET | `/api/v1/wells/{wellId}/forecasts/{forecastId}` | Get forecast with full time-series |

---

## Sample Postman Requests

### 1. Health Check
```
GET http://localhost:8083/health-check
```

### 2. List Seeded Wells
```
GET http://localhost:8083/api/v1/wells
```

### 3. Compute Hyperbolic Decline Forecast (Well ID 1 — Volve 15/9-F-5)
```json
POST http://localhost:8083/api/v1/wells/1/forecasts
Content-Type: application/json

{
  "declineType": "HYPERBOLIC",
  "initialRate": 3200.0,
  "initialDeclineRate": 0.002,
  "bFactor": 0.5,
  "forecastMonths": 120,
  "economicLimit": 50.0
}
```

**Expected response includes:**
- `eur` — Estimated Ultimate Recovery in bbl
- `timeSeries` — 120 monthly data points with rate + cumulative production
- Points marked `belowEconomicLimit: true` once rate drops below 50 bbl/day

### 4. Compute Exponential Decline (b must be 0)
```json
POST http://localhost:8083/api/v1/wells/4/forecasts
Content-Type: application/json

{
  "declineType": "EXPONENTIAL",
  "initialRate": 800.0,
  "initialDeclineRate": 0.001,
  "bFactor": 0.0,
  "forecastMonths": 60,
  "economicLimit": 20.0
}
```

---

## Running Tests

```bash
mvn test
```

10 unit tests covering:
- All three Arps decline models
- Monotonic rate decrease assertion
- Economic limit cutoff
- EUR analytical verification (Exponential EUR = (qi − q_lim) / Di)
- Time-series date sequencing

---

## Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| `b-factor` validated against `DeclineType` | In PE, b=0 must be EXPONENTIAL. The API enforces the physics. |
| Rates persisted, time-series recomputed | Avoids storing potentially millions of rows; EUR and params are what matter. |
| `DAYS_PER_MONTH = 30.4375` | Standard petroleum engineering convention (365.25/12). |
| Flyway seed data (Volve wells) | API is immediately usable in Postman without any setup. |
| `@Version` optimistic locking | Prevents race conditions on concurrent forecast writes. |

---

## Dataset Reference

Production seed data is inspired by the **Equinor Volve Oilfield Dataset** (North Sea, Norway) —
a real decommissioned oilfield dataset made publicly available by Equinor for research and education.

🔗 https://www.equinor.com/energy/volve-data-sharing
