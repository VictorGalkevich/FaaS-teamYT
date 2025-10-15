package com.example.repository.interfaces;

import com.example.domain.FreeTierParams;

import java.util.List;

public interface FreeTierParamsRepository {
    FreeTierParams getFreeTierParams();

    void save(FreeTierParams freeTierParams);

    List<FreeTierParams> getAllRatePlans();
}
