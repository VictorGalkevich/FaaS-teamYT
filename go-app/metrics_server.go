package main

import (
	"context"
	"fmt"

	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/metrics/pkg/apis/metrics/v1beta1"
)

type MetricsServiceStats struct {
	CPU    int64   // milli CPU
	Memory float64 // MiB
}

func (s *Service) GetPodMetrics(ctx context.Context, podName, namespace string) (*MetricsServiceStats, error) {
	podMetrics, err := s.metricsClient.MetricsV1beta1().PodMetricses(namespace).Get(ctx, podName, metav1.GetOptions{})
	if err != nil {
		return nil, fmt.Errorf("failed to get metrics for pod %s: %v", podName, err)
	}

	return s.parsePodMetrics(podMetrics), nil
}

func (s *Service) parsePodMetrics(podMetrics *v1beta1.PodMetrics) *MetricsServiceStats {
	var totalCPU, totalMemory int64

	for _, container := range podMetrics.Containers {
		if container.Name != "queue-proxy" {
			totalCPU += container.Usage.Cpu().MilliValue()
			totalMemory += container.Usage.Memory().Value()
		}
	}

	return &MetricsServiceStats{
		CPU:    totalCPU,
		Memory: float64(totalMemory) / (1024 * 1024),
	}
}
