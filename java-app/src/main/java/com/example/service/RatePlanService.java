package com.example.service;

import com.example.domain.RatePlans;
import com.example.repository.interfaces.RatePlansRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RatePlanService {

    private final RatePlansRepository repository;

    public RatePlanService(RatePlansRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void save(RatePlans ratePlans) {
        repository.saveRatePlans(ratePlans);
    }

    public List<RatePlans> get() {
        return repository.getAllRatePlans();
    }
}