package main

import (
	"context"
	"fmt"
	"strings"

	"github.com/prometheus/common/expfmt"
)

// QueueProxyMetrics содержит метрики queue-proxy
type QueueProxyMetrics struct {
	RequestCount int64   `json:"request_count"`
	TotalTimeMs  float64 `json:"total_time"`
}

// GetQueueProxyMetrics получает метрики queue-proxy для указанного пода
func (s *Service) GetQueueProxyMetrics(ctx context.Context, podName string, namespace string) (*QueueProxyMetrics, error) {
	rest := s.k8sClient.CoreV1().RESTClient()

	// Получаем сырые метрики из пода
	// Формат: <pod-name>:<port> где порт обычно 9091 для метрик queue-proxy
	result := rest.Get().Namespace(namespace).Resource("pods").Name(podName + ":9091").SubResource("proxy").Suffix("metrics")
	bytes, err := result.DoRaw(ctx)
	if err != nil {
		return nil, fmt.Errorf("failed to get metrics from pod %s: %v", podName, err)
	}

	// Парсим метрики Prometheus
	metrics, err := parsePrometheusMetrics(string(bytes))
	if err != nil {
		return nil, fmt.Errorf("failed to parse metrics: %v", err)
	}

	// Извлекаем нужные метрики
	metricsResult := &QueueProxyMetrics{}

	// Получаем общее количество запросов из метрики queue_proxy_request_count
	requestCountMetrics := getMetricsByName(metrics, "revision_app_request_count")
	for _, metric := range requestCountMetrics {
		// Суммируем значения всех метрик с разными лейблами
		metricsResult.RequestCount += int64(metric.Value)
	}

	// Получаем общее время запросов из метрики queue_proxy_request_duration_seconds_sum
	// или аналогичной метрики времени
	durationMetrics := getMetricsByName(metrics, "revision_app_request_latencies")
	for _, metric := range durationMetrics {
		metricsResult.TotalTimeMs += metric.Value
	}

	return metricsResult, nil
}

// parsePrometheusMetrics парсит сырые метрики Prometheus
func parsePrometheusMetrics(data string) ([]Metric, error) {
	var parser expfmt.TextParser
	metricFamilies, err := parser.TextToMetricFamilies(strings.NewReader(data))
	if err != nil {
		return nil, fmt.Errorf("error parsing metrics: %v", err)
	}

	var metrics []Metric

	for name, family := range metricFamilies {
		metricType := family.GetType().String()
		help := family.GetHelp()

		for _, m := range family.GetMetric() {
			metric := Metric{
				Name:  name,
				Type:  metricType,
				Help:  help,
				Value: 0,
			}

			// Извлекаем лейблы
			labels := make(map[string]string)
			for _, label := range m.GetLabel() {
				labels[label.GetName()] = label.GetValue()
			}
			metric.Labels = labels

			var value float64
			switch {
			case m.Gauge != nil:
				value = m.Gauge.GetValue()
			case m.Counter != nil:
				value = m.Counter.GetValue()
			case m.Untyped != nil:
				value = m.Untyped.GetValue()
			case m.Histogram != nil:
				value = m.Histogram.GetSampleSum()
			case m.Summary != nil:
				value = m.Summary.GetSampleSum()
			default:
				continue
			}
			metric.Value = value
			metrics = append(metrics, metric)
		}
	}

	return metrics, nil
}

// getMetricsByName возвращает все метрики с указанным именем
func getMetricsByName(metrics []Metric, name string) []Metric {
	var result []Metric
	for _, metric := range metrics {
		if metric.Name == name {
			result = append(result, metric)
		}
	}
	return result
}

// Metric представляет распарсенную метрику Prometheus
type Metric struct {
	Name   string
	Type   string
	Help   string
	Value  float64
	Labels map[string]string
}
