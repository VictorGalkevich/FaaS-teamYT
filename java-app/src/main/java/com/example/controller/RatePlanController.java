package com.example.controller;

import com.example.domain.RatePlans;
import com.example.service.RatePlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/rateplans")
public class RatePlanController {

    private final RatePlanService ratePlanService;

    public RatePlanController(RatePlanService ratePlanService) {
        this.ratePlanService = ratePlanService;
    }

    @PostMapping
    public ResponseEntity<Void> saveRatePlans(RatePlans ratePlans) {
        ratePlanService.save(ratePlans);
        return ResponseEntity.noContent().build();
    }


}
