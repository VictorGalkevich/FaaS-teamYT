package com.example.repository.interfaces;

import com.example.domain.RatePlans;

import java.util.List;

public interface RatePlansRepository {
    RatePlans getRatePlans();

    void saveRatePlans(RatePlans ratePlans);

    List<RatePlans> getAllRatePlans();
}
