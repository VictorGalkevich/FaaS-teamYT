package com.example.controller;

import com.example.domain.RatePlans;
import com.example.service.RatePlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/rateplans")
public class RatePlanController {

    private final RatePlanService ratePlanService;

    public RatePlanController(RatePlanService ratePlanService) {
        this.ratePlanService = ratePlanService;
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody RatePlans body) {
        ratePlanService.save(body);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public List<RatePlans> getOne() {
        return ratePlanService.get();
    }

}
