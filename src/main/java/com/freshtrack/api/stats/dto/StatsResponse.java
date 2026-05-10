package com.freshtrack.api.stats.dto;

import java.time.LocalDate;

public record StatsResponse(
        Long id,
        Integer totalProducts,
        Integer expiringToday,
        Integer expiringSoon,
        Integer foodSaved,
        Float wasteAvoided,
        Integer receiptsScanned
) {
}
