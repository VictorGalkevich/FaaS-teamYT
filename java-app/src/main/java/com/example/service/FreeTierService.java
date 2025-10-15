package com.example.service;

import com.example.domain.FreeTierParams;
import com.example.repository.interfaces.FreeTierParamsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FreeTierService {

    private final FreeTierParamsRepository paramRepository;

    public FreeTierService(FreeTierParamsRepository paramRepository) {
        this.paramRepository = paramRepository;
    }

    @Transactional
    public void save(FreeTierParams params) {
        paramRepository.save(params);
    }

    public List<FreeTierParams> get() {
        return paramRepository.getAllRatePlans();
    }
}
