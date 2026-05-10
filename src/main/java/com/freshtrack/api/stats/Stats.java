package com.freshtrack.api.stats;

import com.freshtrack.api.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "stats")
public class Stats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Integer totalProducts;
    private Integer expiringToday;
    private Integer expiringSoon;
    private Integer foodSaved;
    private Float wasteAvoided;
    private Integer receiptsScanned;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Stats() {}

    public Stats(
            Long id,
            Integer totalProducts,
            Integer expiringToday,
            Integer expiringSoon,
            Integer foodSaved,
            Float wasteAvoided,
            Integer receiptsScanned,
            User user
    ) {
        this.id = id;
        this.totalProducts = totalProducts;
        this.expiringToday = expiringToday;
        this.expiringSoon = expiringSoon;
        this.foodSaved = foodSaved;
        this.wasteAvoided = wasteAvoided;
        this.receiptsScanned = receiptsScanned;
        this.user = user;
    }
}
