package com.example.service;

import com.example.domain.RatePlans;
import com.example.repository.interfaces.RatePlansRepository;
import org.springframework.stereotype.Service;

@Service
public class RatePlanService {

    private final RatePlansRepository ratePlansRepository;

    public RatePlanService(RatePlansRepository ratePlansRepository) {
        this.ratePlansRepository = ratePlansRepository;
    }

    public void save(RatePlans ratePlans) {
        ratePlansRepository.saveRatePlans(ratePlans);
    }
}
