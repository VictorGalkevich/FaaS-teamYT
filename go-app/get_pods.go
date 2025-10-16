package main

import (
	"context"
	"fmt"

	v1 "k8s.io/api/core/v1"
	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
)

func (s *Service) getFunctions(ctx context.Context, namespace string) ([]string, error) {
	services := []string{}

	deployments, err := s.k8sClient.AppsV1().Deployments(namespace).List(context.Background(), metav1.ListOptions{
		LabelSelector: "serving.knative.dev/service",
	})

	if err != nil {
		return nil, err
	}

	for _, deployment := range deployments.Items {
		if serviceName := deployment.Labels["serving.knative.dev/service"]; serviceName != "" {
			services = append(services, serviceName)
		}
	}

	return services, nil
}

func (s *Service) getServicePods(ctx context.Context, functionName string, namespace string) ([]v1.Pod, error) {
	pods, err := s.k8sClient.CoreV1().Pods(namespace).List(ctx, metav1.ListOptions{
		LabelSelector: fmt.Sprintf("serving.knative.dev/service=%s", functionName),
	})
	if err != nil {
		return nil, err
	}
	return pods.Items, nil
}

func (s *Service) getContainers(pod v1.Pod) (user *v1.ContainerStatus, queueProxy *v1.ContainerStatus) {
	for i := range pod.Status.ContainerStatuses {
		container := &pod.Status.ContainerStatuses[i]
		switch container.Name {
		case "user-container":
			user = container
		case "queue-proxy":
			queueProxy = container
		default:
			panic("мы балбесы")
		}
	}
	return
}
