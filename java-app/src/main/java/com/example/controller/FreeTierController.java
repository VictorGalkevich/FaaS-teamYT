package com.example.controller;

import com.example.domain.FreeTierParams;
import com.example.service.FreeTierService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/free-tier")
public class FreeTierController {

    private final FreeTierService freeTierService;

    public FreeTierController(FreeTierService freeTierService) {
        this.freeTierService = freeTierService;
    }

    @PostMapping
    public ResponseEntity<Void> save(@RequestBody FreeTierParams freeTierParams) {
        freeTierService.save(freeTierParams);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public List<FreeTierParams> getOne() {
        return freeTierService.get();
    }
}
