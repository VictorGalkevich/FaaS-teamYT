package main

import (
	"context"
	"fmt"

	metav1 "k8s.io/apimachinery/pkg/apis/meta/v1"
	"k8s.io/client-go/kubernetes"
	"k8s.io/client-go/tools/clientcmd"
)

func main() {
	config, err := clientcmd.BuildConfigFromFlags("", "/home/rycbaryana/.kube/config")

	if err != nil {
		panic(err.Error())
	}

	client, err := kubernetes.NewForConfig(config)
	if err != nil {
		panic(err.Error())
	}

	deployments, err := client.AppsV1().Deployments("").List(context.Background(), metav1.ListOptions{
		LabelSelector: "serving.knative.dev/service",
	})

	if err != nil {
		panic(err.Error())
	}

	services := make([]string, 0)
	for _, deployment := range deployments.Items {
		if serviceName := deployment.Labels["serving.knative.dev/service"]; serviceName != "" {
			services = append(services, serviceName)
		}
	}
	// fmt.Printf("SERVICES: %v\n", services)


	ctx := context.Background()

	rest := client.CoreV1().RESTClient()

	result := rest.Get().Namespace("default").Resource("pods")
	bytes, err := result.DoRaw(ctx)
	if err != nil {
		panic(err.Error())
	}
	fmt.Printf("%s", string(bytes))

}
