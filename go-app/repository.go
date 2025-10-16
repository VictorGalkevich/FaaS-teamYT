package main

import (
	"context"
	"database/sql"
	"fmt"
	"time"

	_ "github.com/jackc/pgx/v5/stdlib"
)

type Repository struct {
	db *sql.DB
}

func NewRepository(dsn string) (*Repository, error) {
	db, err := sql.Open("pgx", dsn)
	if err != nil {
		return nil, fmt.Errorf("failed to open database: %w", err)
	}

	db.SetMaxOpenConns(25)
	db.SetMaxIdleConns(25)
	db.SetConnMaxLifetime(5 * time.Minute)

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	if err := db.PingContext(ctx); err != nil {
		return nil, fmt.Errorf("failed to ping database: %w", err)
	}

	if err := createMetricsTableIfNotExists(db); err != nil {
		return nil, fmt.Errorf("failed to create table: %w", err)
	}

	if err := createContainerLastMetricsIfNotExists(db); err != nil {
		return nil, fmt.Errorf("failed to create table: %w", err)
	}

	return &Repository{db: db}, nil
}

func createMetricsTableIfNotExists(db *sql.DB) error {
	query := `
 CREATE TABLE IF NOT EXISTS execution_metrics (
  id SERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  request_count_delta BIGINT NOT NULL,
  total_time_ms_delta DOUBLE PRECISION NOT NULL,
  cpu_milli BIGINT NOT NULL,
  memory_mib DOUBLE PRECISION NOT NULL,
  pods_count BIGINT NOT NULL
 );
 
 CREATE INDEX IF NOT EXISTS idx_metrics_timestamp ON execution_metrics(timestamp);
 `
	_, err := db.Exec(query)
	return err
}

func createContainerLastMetricsIfNotExists(db *sql.DB) error {
	query := `
 CREATE TABLE IF NOT EXISTS container_last_metrics (
  container_id TEXT PRIMARY KEY,
  request_count_delta BIGINT NOT NULL,
  total_time_ms_delta DOUBLE PRECISION NOT NULL
 );
 `
	_, err := db.Exec(query)
	return err
}

func (r *Repository) InsertMetric(ctx context.Context, metric MetricsUpdate) error {
	query := `
 INSERT INTO execution_metrics (
  name,
  request_count_delta,
  total_time_ms_delta,
  cpu_milli,
  memory_mib,
  pods_count
 ) VALUES ($1, $2, $3, $4, $5, $6)
 `

	_, err := r.db.ExecContext(ctx, query,
		metric.FunctionName,
		metric.RequestCountDelta,
		metric.TotalTimeMsDelta,
		metric.CPU,
		metric.Memory,
		metric.PodsCount,
	)

	return err
}

func ReplaceAllContainerMetrics(db *sql.DB, metricsMap map[string]QueueProxyMetrics) error {
	tx, err := db.Begin()
	if err != nil {
		return fmt.Errorf("failed to begin transaction: %w", err)
	}
	defer tx.Rollback()

	_, err = tx.Exec("DELETE FROM container_last_metrics")
	if err != nil {
		return fmt.Errorf("failed to delete old records: %w", err)
	}

	stmt, err := tx.Prepare(`
		INSERT INTO container_last_metrics 
		(container_id, request_count_delta, total_time_ms_delta) 
		VALUES ($1, $2, $3)
	`)
	if err != nil {
		return fmt.Errorf("failed to prepare insert statement: %w", err)
	}
	defer stmt.Close()

	for containerID, metrics := range metricsMap {
		_, err = stmt.Exec(containerID, metrics.RequestCount, metrics.TotalTimeMs)
		if err != nil {
			return fmt.Errorf("failed to insert metrics for container %s: %w", containerID, err)
		}
	}

	err = tx.Commit()
	if err != nil {
		return fmt.Errorf("failed to commit transaction: %w", err)
	}

	return nil
}

func LoadContainerMetrics(db *sql.DB) (map[string]QueueProxyMetrics, error) {
	metricsMap := make(map[string]QueueProxyMetrics)

	rows, err := db.Query(`
		SELECT container_id, request_count_delta, total_time_ms_delta 
		FROM container_last_metrics
	`)
	if err != nil {
		return nil, fmt.Errorf("failed to query container metrics: %w", err)
	}
	defer rows.Close()

	for rows.Next() {
		var containerID string
		var metrics QueueProxyMetrics

		err := rows.Scan(&containerID, &metrics.RequestCount, &metrics.TotalTimeMs)
		if err != nil {
			return nil, fmt.Errorf("failed to scan row: %w", err)
		}

		metricsMap[containerID] = metrics
	}

	if err = rows.Err(); err != nil {
		return nil, fmt.Errorf("error iterating rows: %w", err)
	}

	return metricsMap, nil
}

func (r *Repository) Close() error {
	return r.db.Close()
}
