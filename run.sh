#!/bin/bash

# Проверка, что kubectl настроен на docker-desktop
echo "[Step 1] Checking Kubernetes context..."
CURRENT_CONTEXT=$(kubectl config current-context)
if [ "$CURRENT_CONTEXT" != "docker-desktop" ]; then
  echo "Current context is not docker-desktop. Switching..."
  kubectl config use-context docker-desktop
fi

# Установка Knative Serving
echo "[Step 2] Installing Knative Serving..."
kubectl apply -f https://github.com/knative/serving/releases/download/knative-v1.17.0/serving-crds.yaml
kubectl apply -f https://github.com/knative/serving/releases/download/knative-v1.17.0/serving-core.yaml

# Установка Kourier Ingress
echo "[Step 3] Installing Kourier Ingress..."
kubectl apply -f https://github.com/knative/net-kourier/releases/download/knative-v1.17.0/kourier.yaml
kubectl patch configmap/config-network \
  --namespace knative-serving \
  --type merge \
  --patch '{"data":{"ingress-class":"kourier.ingress.networking.knative.dev"}}'

# Настройка домена
echo "[Step 4] Configuring domain..."
kubectl patch configmap/config-domain \
  --namespace knative-serving \
  --type merge \
  --patch '{"data":{"knative.demo.com":""}}'

# Установка Metrics Server (если еще не установлен)
echo "[Step 5] Installing Metrics Server..."
if ! kubectl get deployment metrics-server -n kube-system &> /dev/null; then
  kubectl delete -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml 2>/dev/null || true
  kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml

  # Патчим Metrics Server
  echo "[Step 5.1] Patching Metrics Server..."
  kubectl patch deployment metrics-server -n kube-system --type='json' -p='[
  {
  "op": "add",
  "path": "/spec/template/spec/hostNetwork",
  "value": true
  },
  {
  "op": "replace",
  "path": "/spec/template/spec/containers/0/args",
  "value": [
  "--cert-dir=/tmp",
  "--secure-port=4443",
  "--kubelet-preferred-address-types=InternalIP,ExternalIP,Hostname",
  "--kubelet-use-node-status-port",
  "--metric-resolution=15s",
  "--kubelet-insecure-tls"
  ]
  },
  {
  "op": "replace",
  "path": "/spec/template/spec/containers/0/ports/0/containerPort",
  "value": 4443
  }
  ]'

  sleep 30
  kubectl -n kube-system get pods -l k8s-app=metrics-server
  kubectl get apiservices -l k8s-app=metrics-server
  kubectl top nodes
else
  echo "Metrics Server already installed, skipping..."
fi

# Развертывание тестового сервиса echo
echo "[Step 6] Deploying echo service..."
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

# Настройка порта для доступа к сервису
echo "[Step 7] Patch config-domain with domain knative.demo.com"
kubectl patch configmap/config-domain --namespace knative-serving --type merge --patch "{\"data\":{\"knative.demo.com\":\"\"}}"

# Проверка сервиса
echo "[Step 8] Testing the echo service..."
kubectl wait deployment --all --timeout=300s --for=condition=Available -n knative-serving
kubectl wait deployment --all --timeout=300s --for=condition=Available -n kourier-system
curl -H "Host: echo.default.knative.demo.com" 'http://localhost:80/api/v1/metrics?param=value'

# Запуск приложения через docker-compose
echo "[Step 9] Starting application with docker-compose..."
if [ -f "docker-compose.yaml" ]; then
  docker compose up -d
  echo "Application started with docker-compose"
else
  echo "docker-compose.yml not found, skipping docker-compose step"
fi

echo "Installation completed successfully!"
