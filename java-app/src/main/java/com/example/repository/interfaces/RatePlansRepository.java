package com.example.repository.interfaces;

import com.example.domain.RatePlans;

import java.util.List;

public interface RatePlansRepository {
    RatePlans getCurrentRatePlans();
    List<RatePlans> getAllRatePlans();
    void saveRatePlans(RatePlans ratePlans);
}
