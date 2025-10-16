#!/bin/bash

# Install Kubernetes with Knative on a clean VM

set -e

echo "[1/14] Updating system packages..."
sudo apt-get update -qq && sudo apt-get upgrade -y -qq

echo "[2/14] Installing required dependencies..."
sudo apt-get install -qq -y \
    curl \
    conntrack \
    socat \
    ebtables \
    apt-transport-https \
    ca-certificates \
    gnupg \
    lsb-release \
    software-properties-common

echo "[3/14] Installing Docker..."
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER
newgrp docker

echo "[4/14] Installing Minikube..."
curl -LO https://storage.googleapis.com/minikube/releases/latest/minikube-linux-amd64
sudo install minikube-linux-amd64 /usr/local/bin/minikube
rm minikube-linux-amd64

echo "[5/14] Starting Minikube with adequate resources..."
minikube start --driver=docker \
    --cpus=4 \
    --memory=8g \
    --disk-size=20g \
    --addons=ingress \
    --extra-config=kubelet.authentication-token-webhook=true

echo "[6/14] Installing kubectl..."
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
chmod +x kubectl
sudo mv kubectl /usr/local/bin/

echo "[7/14] Verify cluster is running..."
kubectl cluster-info

echo "[8/14] Install Knative Serving CRDs..."
kubectl apply -f https://github.com/knative/serving/releases/download/knative-v1.17.0/serving-crds.yaml

echo "[9/14] Install Knative Serving Core..."
kubectl apply -f https://github.com/knative/serving/releases/download/knative-v1.17.0/serving-core.yaml

echo "[10/14] Install Kourier Ingress..."
kubectl apply -f https://github.com/knative/net-kourier/releases/download/knative-v1.17.0/kourier.yaml

echo "[11/14] Configure Knative to use Kourier..."
kubectl patch configmap/config-network \
  --namespace knative-serving \
  --type merge \
  --patch '{"data":{"ingress-class":"kourier.ingress.networking.knative.dev"}}'

echo "[12/14] Configure domain..."
kubectl patch configmap/config-domain \
  --namespace knative-serving \
  --type merge \
  --patch '{"data":{"knative.local":""}}'

echo "[13/14] Deploy test echo service..."
cat <<EOF | kubectl apply -f -
apiVersion: serving.knative.dev/v1
kind: Service
metadata:
  name: echo
  namespace: default
spec:
  template:
    metadata:
      annotations:
        autoscaling.knative.dev/minScale: "1"
        autoscaling.knative.dev/maxScale: "5"
        autoscaling.knative.dev/target: "50"
        autoscaling.knative.dev/class: "kpa.autoscaling.knative.dev"
        autoscaling.knative.dev/metric: "rps"
        networking.knative.dev/ingress.class: "kourier.ingress.networking.knative.dev"
    spec:
      containers:
        - image: ealen/echo-server:latest
          ports:
            - containerPort: 80
          env:
            - name: EXAMPLE_ENV
              value: "value"
EOF

echo "[14/14] Setting up port forwarding..."
kubectl port-forward --namespace kourier-system service/kourier 8080:80 8443:443 &

echo "Waiting for services to be ready..."
sleep 30
kubectl wait --for=condition=Ready ksvc/echo --timeout=300s -n default

echo "Installation complete!"
echo "To test your Knative service, run:"
echo 'curl -H "Host: echo.default.knative.local" http://localhost:8080'
echo ""
echo "To access the Kubernetes dashboard, run:"
echo "minikube dashboard"
echo ""
echo "To get autoscaler metrics:"
echo "kubectl port-forward -n knative-serving deployment/autoscaler 9090:9090"
echo 'curl http://localhost:9090/metrics | grep autoscaler_stable_requests_per_second'
