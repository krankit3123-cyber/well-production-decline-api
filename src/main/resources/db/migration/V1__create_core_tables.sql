-- ============================================================
-- V1: Core schema for Well Production Decline API
-- ============================================================

CREATE TABLE wells (
    id          BIGSERIAL PRIMARY KEY,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    version     BIGINT      NOT NULL DEFAULT 0,
    name        VARCHAR(200) NOT NULL,
    field_name  VARCHAR(200) NOT NULL,
    basin       VARCHAR(200),
    country     VARCHAR(100) NOT NULL,
    fluid_type  VARCHAR(20)  NOT NULL,   -- OIL, GAS, CONDENSATE
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE'  -- ACTIVE, SHUT_IN, ABANDONED
);

CREATE TABLE production_records (
    id              BIGSERIAL PRIMARY KEY,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    version         BIGINT      NOT NULL DEFAULT 0,
    well_id         BIGINT      NOT NULL REFERENCES wells(id),
    record_date     DATE        NOT NULL,
    production_rate NUMERIC(12, 4) NOT NULL,   -- bbl/day for oil, Mscf/day for gas
    CONSTRAINT uq_well_record_date UNIQUE (well_id, record_date)
);

CREATE INDEX idx_production_records_well_id_date
    ON production_records(well_id, record_date);

CREATE TABLE decline_forecasts (
    id                  BIGSERIAL PRIMARY KEY,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    version             BIGINT      NOT NULL DEFAULT 0,
    well_id             BIGINT      NOT NULL REFERENCES wells(id),
    decline_type        VARCHAR(20) NOT NULL,  -- EXPONENTIAL, HYPERBOLIC, HARMONIC
    initial_rate        NUMERIC(12, 4) NOT NULL,    -- qi  (bbl/day or Mscf/day)
    initial_decline_rate NUMERIC(10, 6) NOT NULL,   -- Di  (fraction/day)
    b_factor            NUMERIC(6, 4) NOT NULL DEFAULT 0,  -- b  (0 = exponential, 1 = harmonic)
    forecast_months     INT         NOT NULL,
    economic_limit      NUMERIC(12, 4),              -- abandonment rate
    eur                 NUMERIC(18, 4),              -- Estimated Ultimate Recovery
    computed_at         TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_decline_forecasts_well_id
    ON decline_forecasts(well_id);
