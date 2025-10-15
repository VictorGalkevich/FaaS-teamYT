package main

import (
	"fmt"
	"os"
	"path/filepath"

	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/rest"
	"k8s.io/client-go/tools/clientcmd"
	"k8s.io/client-go/util/homedir"
	metricsv "k8s.io/metrics/pkg/client/clientset/versioned"
)

type Service struct {
	k8sClient     *kubernetes.Clientset
	metricsClient *metricsv.Clientset
}

func NewService(kubeconfigPath string) (*Service, error) {
	var config *rest.Config
	var err error

	if kubeconfigPath == "" {
		if home := homedir.HomeDir(); home != "" {
			kubeconfigPath = filepath.Join(home, ".kube", "config")
			if _, err := os.Stat(kubeconfigPath); os.IsNotExist(err) {
				kubeconfigPath = ""
			}
		}
	}

	if kubeconfigPath != "" {
		config, err = clientcmd.BuildConfigFromFlags("", kubeconfigPath)
		if err != nil {
			return nil, fmt.Errorf("failed to build config from kubeconfig: %v", err)
		}
	} else {
		config, err = rest.InClusterConfig()
		if err != nil {
			return nil, fmt.Errorf("failed to create config (tried kubeconfig and in-cluster): %v", err)
		}
	}

	k8sClient, err := kubernetes.NewForConfig(config)
	if err != nil {
		return nil, fmt.Errorf("failed to create kubernetes client: %v", err)
	}

	metricsClient, err := metricsv.NewForConfig(config)
	if err != nil {
		return nil, fmt.Errorf("failed to create metrics client: %v", err)
	}

	return &Service{
		k8sClient:     k8sClient,
		metricsClient: metricsClient,
	}, nil
}
