package com.freshtrack.api.household.service;

import com.freshtrack.api.household.dto.FamilyInviteRequest;
import com.freshtrack.api.household.dto.FamilyInviteResponse;
import com.freshtrack.api.product.Product;
import com.freshtrack.api.user.User;
import java.util.List;

public interface IHouseholdService {
    FamilyInviteResponse invite(String authToken, FamilyInviteRequest request);
    void acceptInvite(String authToken, String inviteId);
    void declineInvite(String authToken, String inviteId);
    List<User> getMembers(String authToken);
    List<Product> getHouseholdProducts(String authToken);
}
