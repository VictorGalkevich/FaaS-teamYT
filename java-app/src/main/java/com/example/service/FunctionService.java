package com.example.service;

import com.example.domain.FunctionRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class FunctionService {

    public String addFunction(FunctionRequest request) {
        String template = loadTemplate("templates/function-template.yaml");

        String yaml = template
                .replace("{{name}}", request.name())
                .replace("{{image}}", request.image())
                .replace("{{port}}", String.valueOf(request.port()))
                .replace("{{minScale}}", String.valueOf(request.minScale()))
                .replace("{{maxScale}}", String.valueOf(request.maxScale()))
                .replace("{{target}}", String.valueOf(request.target()))
                .replace("{{metric}}", request.metric())
                .replace("{{timeoutSeconds}}", String.valueOf(request.timeoutSeconds()));

        StringBuilder argsBlock = new StringBuilder();
        if (request.args() != null && !request.args().isEmpty()) {
            argsBlock.append("args:\n");
            for (String arg : request.args()) {
                argsBlock.append("            - \"").append(arg).append("\"\n");
            }
        }

        StringBuilder envBlock = new StringBuilder();
        if (request.env() != null && !request.env().isEmpty()) {
            envBlock.append("          env:\n");
            for (Map.Entry<String, String> entry : request.env().entrySet()) {
                envBlock.append("            - name: ").append(entry.getKey()).append("\n");
                envBlock.append("              value: \"").append(entry.getValue()).append("\"");
            }
        }


        yaml = yaml.replace("{{argsBlock}}", argsBlock);
        yaml = yaml.replace("{{envBlock}}", envBlock);

        try {
            ProcessBuilder pb = new ProcessBuilder("kubectl", "apply", "-f", "-");
            Process process = pb.start();

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
                writer.write(yaml);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                reader.lines().forEach(System.out::println);
            }

            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
            return "error: " + e.getMessage();
        }

        return "ok";
    }

    private String loadTemplate(String path) {
        try (InputStream is = new ClassPathResource(path).getInputStream()) {
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Template not found: " + path);
        }
    }
}
