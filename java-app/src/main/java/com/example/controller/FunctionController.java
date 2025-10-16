package com.example.controller;

import com.example.domain.FunctionRequest;
import com.example.service.FunctionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/function")
public class FunctionController {
    private final FunctionService functionService;

    public FunctionController(FunctionService functionService) {
        this.functionService = functionService;
    }

    @PostMapping("/add")
    public ResponseEntity<String> addFunction(@RequestBody FunctionRequest request) {
        if (request.name() == null || request.name().isBlank() ||
            request.image() == null || request.image().isBlank() ||
            request.port() <= 0) {
            return ResponseEntity.badRequest().body("Ошибка: name, image и port являются обязательными.");
        }

        try {
            functionService.addFunction(request);
            return ResponseEntity.ok("Функция успешно добавлена");
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Ошибка при добавлении функции: " + ex.getMessage());
        }
    }
}

