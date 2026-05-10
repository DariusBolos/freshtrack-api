package com.freshtrack.api.stats.service;

import com.freshtrack.api.stats.dto.StatsResponse;

public interface IStatsService {
    StatsResponse getStats(String authToken);
}

