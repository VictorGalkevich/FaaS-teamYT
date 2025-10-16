package com.example.service;

import com.example.domain.FunctionRequest;
import com.google.gson.Gson;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.util.Config;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class FunctionService {

    private final CustomObjectsApi customObjectsApi;

    public FunctionService() throws Exception {
        ApiClient client = Config.fromConfig("/app/.kube/config"); // путь из docker-compose
        Configuration.setDefaultApiClient(client);
        this.customObjectsApi = new CustomObjectsApi(client);
    }

    public String addFunction(FunctionRequest request) {
        Map<String, Object> service = buildKnativeService(request);

        try {
            Object result = customObjectsApi.createNamespacedCustomObject(
                    "serving.knative.dev",
                    "v1",
                    "default",
                    "services",
                    service,
                    null,
                    null,
                    null
            );
            return new Gson().toJson(result);
        } catch (ApiException e) {
            System.err.println("Kubernetes API error:");
            System.err.println("Code: " + e.getCode());
            System.err.println("Body: " + e.getResponseBody());
            System.err.println("Headers: " + e.getResponseHeaders());
            e.printStackTrace();
            return "error: " + e.getMessage();
        }

    }

    private Map<String, Object> buildKnativeService(FunctionRequest request) {
        Map<String, Object> service = new LinkedHashMap<>();
        service.put("apiVersion", "serving.knative.dev/v1");
        service.put("kind", "Service");

        Map<String, Object> metadata = Map.of("name", request.name(), "namespace", "default");
        service.put("metadata", metadata);

        Map<String, Object> annotations = Map.of(
                "autoscaling.knative.dev/minScale", String.valueOf(request.minScale()),
                "autoscaling.knative.dev/maxScale", String.valueOf(request.maxScale()),
                "autoscaling.knative.dev/target", String.valueOf(request.target()),
                "autoscaling.knative.dev/class", "kpa.autoscaling.knative.dev",
                "autoscaling.knative.dev/metric", request.metric(),
                "networking.knative.dev/ingress.class", "kourier.ingress.networking.knative.dev"
        );

        Map<String, Object> container = new LinkedHashMap<>();
        container.put("image", request.image());
        container.put("ports", List.of(Map.of("containerPort", request.port())));

        if (request.args() != null && !request.args().isEmpty()) {
            container.put("args", request.args());
        }

        if (request.env() != null && !request.env().isEmpty()) {
            List<Map<String, String>> envList = new ArrayList<>();
            request.env().forEach((key, value) -> envList.add(Map.of("name", key, "value", value)));
            container.put("env", envList);
        }

        Map<String, Object> specTemplate = Map.of(
                "metadata", Map.of("annotations", annotations),
                "spec", Map.of(
                        "containers", List.of(container),
                        "timeoutSeconds", request.timeoutSeconds()
                )
        );

        Map<String, Object> spec = Map.of("template", specTemplate);
        service.put("spec", spec);

        return service;
    }
}
