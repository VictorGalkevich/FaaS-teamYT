package main

import (
	"context"
	"fmt"
	"time"
)

type MetricsUpdate struct {
	FunctionName      string
	RequestCountDelta int64
	TotalTimeMsDelta  float64
	CPU               int64   // milli CPU
	Memory            float64 // MiB
	PodsCount         int64
}

func getConnectionString() string {
	return "postgres://app:app@host.docker.internal:5432/db?sslmode=disable"
}

func main() {

	repository, err := NewRepository(getConnectionString())

	if err != nil {
		panic(err.Error())
	}

	service, err := NewService("/app/.kube/config")
	if err != nil {
		panic(err.Error())
	}

	ctx := context.Background()

	ticker := time.NewTicker(5 * time.Second)
	defer ticker.Stop()

	namespace := "default"

	prevMetrics := make(map[string]QueueProxyMetrics)

	for range ticker.C {
		functions, err := service.getFunctions(ctx, namespace)
		if err != nil {
			fmt.Printf("Error getting functions: %v\n", err)
		}
		for _, function := range functions {
			pods, err := service.getServicePods(ctx, function, namespace)
			if err != nil {
				fmt.Printf("Error getting pods for function %s: %+v\n", function, err)
			}

			metricsUpdate := MetricsUpdate{
				FunctionName: function,
			}

			curMetrics := make(map[string]QueueProxyMetrics)

			for _, pod := range pods {

				user, queueProxy := service.getContainers(pod)

				if user == nil || queueProxy == nil {
					fmt.Printf("containers are nil :(\n")
					continue
				}

				if !queueProxy.Ready {
					continue
				}

				containerId := user.ContainerID

				queueProxyMetrics, err := service.GetQueueProxyMetrics(ctx, pod.Name, namespace)
				if err != nil {
					fmt.Printf("Error getting queue proxy metrics for pod %s: %+v\n", pod.Name, err)
				} else {
					if prevMetric, ok := prevMetrics[containerId]; ok {
						metricsUpdate.RequestCountDelta += (queueProxyMetrics.RequestCount - prevMetric.RequestCount)
						metricsUpdate.TotalTimeMsDelta += (queueProxyMetrics.TotalTimeMs - prevMetric.TotalTimeMs)
					} else {
						metricsUpdate.RequestCountDelta += queueProxyMetrics.RequestCount
						metricsUpdate.TotalTimeMsDelta += queueProxyMetrics.TotalTimeMs
					}

					curMetrics[containerId] = *queueProxyMetrics

				}

				hardwareMetrics, err := service.GetPodMetrics(ctx, pod.Name, namespace)
				if err != nil {
					continue
				}
				metricsUpdate.CPU += hardwareMetrics.CPU
				metricsUpdate.Memory += hardwareMetrics.Memory

				metricsUpdate.PodsCount++
			}

			timestamp := time.Now().Format("2006-01-02 15:04:05")
			fmt.Printf("[%s] [%s] UPDATE METRICS: %+v\n", timestamp, function, metricsUpdate)

			prevMetrics = curMetrics

			repository.InsertMetric(ctx, metricsUpdate)
		}
	}
}
