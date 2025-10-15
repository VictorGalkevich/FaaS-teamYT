package com.example.domain;

import java.util.List;
import java.util.Map;

public record FunctionRequest(
        String name,
        String image,
        int port,
        List<String> args,
        Map<String, String> env,
        Integer minScale,
        Integer maxScale,
        String metric,
        Integer target,
        Integer timeoutSeconds
) {}
