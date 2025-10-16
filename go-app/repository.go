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

	if err := createTableIfNotExists(db); err != nil {
		return nil, fmt.Errorf("failed to create table: %w", err)
	}

	return &Repository{db: db}, nil
}

func createTableIfNotExists(db *sql.DB) error {
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

func (r *Repository) Insert(ctx context.Context, metric MetricsUpdate) error {
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

func (r *Repository) Close() error {
	return r.db.Close()
}
