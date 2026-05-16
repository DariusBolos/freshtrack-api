package com.freshtrack.api.household;

import com.freshtrack.api.household.dto.FamilyInviteRequest;
import com.freshtrack.api.household.dto.FamilyInviteResponse;
import com.freshtrack.api.household.dto.HouseholdMemberResponse;
import com.freshtrack.api.household.service.IHouseholdService;
import com.freshtrack.api.product.Product;
import com.freshtrack.api.product.dto.ProductResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/household")
public class HouseholdController {
    private final IHouseholdService householdService;

    public HouseholdController(IHouseholdService householdService) {
        this.householdService = householdService;
    }

    @PostMapping("/invite")
    public ResponseEntity<FamilyInviteResponse> inviteToHousehold(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody FamilyInviteRequest request) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        FamilyInviteResponse response = householdService.invite(token, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/invites/{inviteId}/accept")
    public ResponseEntity<Void> acceptInvite(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String inviteId) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        householdService.acceptInvite(token, inviteId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/invites/{inviteId}/decline")
    public ResponseEntity<Void> declineInvite(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String inviteId) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        householdService.declineInvite(token, inviteId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/members")
    public ResponseEntity<List<HouseholdMemberResponse>> getMembers(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        List<HouseholdMemberResponse> members = householdService.getMembers(token)
                .stream()
                .map(user -> new HouseholdMemberResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName()))
                .toList();
        return ResponseEntity.ok(members);
    }

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getHouseholdProducts(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        List<ProductResponse> products = householdService.getHouseholdProducts(token)
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(products);
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getQuantity(),
                product.getUnit(),
                product.getPurchaseDate(),
                product.getExpiryDate(),
                product.getCategory()
        );
    }
}
