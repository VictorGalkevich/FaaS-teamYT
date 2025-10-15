CREATE TABLE IF NOT EXISTS rate_plans (
    id               SERIAL PRIMARY KEY,
    per_call         NUMERIC(20,12) NOT NULL,
    exec_per_ms      NUMERIC(20,12) NOT NULL,
    mem_per_mb_ms    NUMERIC(20,12) NOT NULL,
    cpu_per_mcpu_ms  NUMERIC(20,12) NOT NULL,
    cold_start_fee   NUMERIC(20,12) NOT NULL
);

CREATE TABLE IF NOT EXISTS free_tier (
    id            SERIAL PRIMARY KEY,
    calls         NUMERIC(30,0)  NOT NULL,
    exec_ms       NUMERIC(30,0)  NOT NULL,
    mem_mb_ms     NUMERIC(30,0)  NOT NULL,
    cpu_mcpu_ms   NUMERIC(30,0)  NOT NULL
);

CREATE TABLE IF NOT EXISTS execution_metrics (
    id            SERIAL PRIMARY KEY,
    revision      VARCHAR(128)   NOT NULL UNIQUE,
    calls         NUMERIC(30,0)  NOT NULL,
    exec_ms       NUMERIC(30,0)  NOT NULL,
    mem_mb_ms     NUMERIC(30,0)  NOT NULL,
    cpu_mcpu_ms   NUMERIC(30,0)  NOT NULL,
    cold_start    NUMERIC(30,0)  NOT NULL,
    ts            timestamptz NOT NULL
);