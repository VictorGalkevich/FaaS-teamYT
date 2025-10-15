package com.example.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/asd")
    public String hello() {
        return "Hello from Java (Spring Boot, separated)";
    }
}