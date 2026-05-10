package com.freshtrack.api.stats.service;

import com.freshtrack.api.product.Product;
import com.freshtrack.api.product.ProductRepository;
import com.freshtrack.api.stats.Stats;
import com.freshtrack.api.stats.StatsRepository;
import com.freshtrack.api.stats.dto.StatsResponse;
import com.freshtrack.api.user.User;
import com.freshtrack.api.user.service.IUserService;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class StatsService implements IStatsService {
    private final ProductRepository productRepository;
    private final IUserService userService;
    private final StatsRepository statsRepository;

    public StatsService(ProductRepository productRepository,
                        IUserService userService,
                        StatsRepository statsRepository) {
        this.productRepository = productRepository;
        this.userService = userService;
        this.statsRepository = statsRepository;
    }

    @Override
    public StatsResponse getStats(String authToken) {
        User user = userService.getUserByEmail(authToken);
        return statsRepository.findByUserId(user.getId())
                .map(this::toResponse)
                .orElseGet(() -> createInitialStats(user));
    }

    private StatsResponse createInitialStats(User user) {
        Stats stats = calculateStats(user.getId());
        stats.setUser(user);
        Stats saved = statsRepository.save(stats);
        return toResponse(saved);
    }

    private Stats calculateStats(Long userId) {
        List<Product> products = productRepository.findAllByUserId(userId);
        LocalDate today = LocalDate.now();
        int expiringSoonDays = 2;
        LocalDate soonThreshold = today.plusDays(expiringSoonDays);

        int totalProducts = products.size();
        int expiringToday = (int) products.stream()
                .filter(product -> isSameDay(product.getExpiryDate(), today))
                .count();
        int expiringSoon = (int) products.stream()
                .filter(product -> isBetweenExclusive(product.getExpiryDate(), today, soonThreshold))
                .count();
        int foodSaved = (int) products.stream()
                .filter(product -> product.getExpiryDate() == null || !product.getExpiryDate().isBefore(today))
                .count();

        Set<LocalDate> receiptDates = products.stream()
                .map(Product::getPurchaseDate)
                .filter(date -> date != null)
                .collect(Collectors.toSet());
        int receiptsScanned = receiptDates.size();

        float wasteAvoided = 0.0f;

        return new Stats(
                null,
                totalProducts,
                expiringToday,
                expiringSoon,
                foodSaved,
                wasteAvoided,
                receiptsScanned,
                null
        );
    }

    private StatsResponse toResponse(Stats stats) {
        return new StatsResponse(
                stats.getId(),
                stats.getTotalProducts(),
                stats.getExpiringToday(),
                stats.getExpiringSoon(),
                stats.getFoodSaved(),
                stats.getWasteAvoided(),
                stats.getReceiptsScanned()
        );
    }

    private boolean isSameDay(LocalDate value, LocalDate target) {
        return value != null && value.isEqual(target);
    }

    private boolean isBetweenExclusive(LocalDate value, LocalDate start, LocalDate end) {
        return value != null && value.isAfter(start) && (value.isBefore(end) || value.isEqual(end));
    }
}
