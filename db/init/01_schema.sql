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
  id SERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  request_count_delta BIGINT NOT NULL,
  total_time_ms_delta DOUBLE PRECISION NOT NULL,
  cold_start_ms_delta DOUBLE PRECISION NOT NULL,
  cpu_milli BIGINT NOT NULL,
  memory_mib DOUBLE PRECISION NOT NULL,
  pods_count BIGINT NOT NULL
 );
 
 CREATE INDEX IF NOT EXISTS idx_metrics_timestamp ON execution_metrics(timestamp);

 CREATE TABLE IF NOT EXISTS container_last_metrics (
  container_id TEXT PRIMARY KEY,
  request_count_delta BIGINT NOT NULL,
  total_time_ms_delta DOUBLE PRECISION NOT NULL
 );