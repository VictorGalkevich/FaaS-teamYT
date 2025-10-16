-- rate_plans (1 строка)
INSERT INTO rate_plans (per_call, exec_per_ms, mem_per_mb_ms, cpu_per_mcpu_ms, cold_start_fee)
VALUES (0.0005, 0.000001, 0.0000000065, 0.0000000012, 0.05);

-- free_tier (1 строка)
INSERT INTO free_tier (calls, exec_ms, mem_mb_ms, cpu_mcpu_ms)
VALUES (100000, 0, 0, 0);