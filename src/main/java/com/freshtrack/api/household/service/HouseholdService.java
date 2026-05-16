package com.freshtrack.api.household.service;

import com.freshtrack.api.enums.NotificationType;
import com.freshtrack.api.household.Household;
import com.freshtrack.api.household.HouseholdInvite;
import com.freshtrack.api.household.HouseholdInviteRepository;
import com.freshtrack.api.household.HouseholdInviteStatus;
import com.freshtrack.api.household.HouseholdRepository;
import com.freshtrack.api.household.dto.FamilyInviteRequest;
import com.freshtrack.api.household.dto.FamilyInviteResponse;
import com.freshtrack.api.notification.Notification;
import com.freshtrack.api.notification.service.INotificationService;
import com.freshtrack.api.product.Product;
import com.freshtrack.api.product.ProductRepository;
import com.freshtrack.api.user.User;
import com.freshtrack.api.user.UserRepository;
import com.freshtrack.api.user.service.IUserService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HouseholdService implements IHouseholdService {
    private final HouseholdRepository householdRepository;
    private final HouseholdInviteRepository inviteRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final IUserService userService;
    private final INotificationService notificationService;

    public HouseholdService(HouseholdRepository householdRepository,
                            HouseholdInviteRepository inviteRepository,
                            UserRepository userRepository,
                            ProductRepository productRepository,
                            IUserService userService,
                            INotificationService notificationService) {
        this.householdRepository = householdRepository;
        this.inviteRepository = inviteRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public FamilyInviteResponse invite(String authToken, FamilyInviteRequest request) {
        User inviter = userService.getUserByEmail(authToken);
        String inviteeEmail = normalizeEmail(request.email());
        if (inviter.getEmail().equalsIgnoreCase(inviteeEmail)) {
            throw new IllegalArgumentException("You cannot invite yourself.");
        }

        Household household = resolveOrCreateHousehold(inviter, request.familyName());

        User existingInvitee = userRepository.findByEmail(inviteeEmail).orElse(null);
        if (existingInvitee != null && existingInvitee.getHousehold() != null) {
            if (Objects.equals(existingInvitee.getHousehold().getId(), household.getId())) {
                throw new IllegalArgumentException("User is already in your household.");
            }
            throw new IllegalArgumentException("User already belongs to another household.");
        }

        HouseholdInvite invite = new HouseholdInvite();
        invite.setId(UUID.randomUUID().toString());
        invite.setInviteeEmail(inviteeEmail);
        invite.setCreatedAt(OffsetDateTime.now());
        invite.setStatus(HouseholdInviteStatus.PENDING);
        invite.setHousehold(household);
        invite.setInviter(inviter);
        inviteRepository.save(invite);

        if (existingInvitee != null) {
            Notification notification = new Notification();
            notification.setTitle("Family invite");
            notification.setMessage(buildInviteMessage(inviter, household));
            notification.setTimestamp(OffsetDateTime.now().toString());
            notification.setRead(false);
            notification.setType(NotificationType.FAMILY_INVITE);
            notification.setInviterName(buildInviterName(inviter));
            notification.setInviterEmail(inviter.getEmail());
            notification.setFamilyName(household.getName());
            notification.setInviteId(invite.getId());
            notification.setUser(existingInvitee);
            notificationService.createNotification(notification);
        }

        return new FamilyInviteResponse(invite.getId(), household.getName(), inviteeEmail);
    }

    @Override
    @Transactional
    public void acceptInvite(String authToken, String inviteId) {
        User invitee = userService.getUserByEmail(authToken);
        HouseholdInvite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new IllegalArgumentException("Invite not found: " + inviteId));

        if (!invite.getInviteeEmail().equalsIgnoreCase(invitee.getEmail())) {
            throw new IllegalArgumentException("This invite is not for the current user.");
        }
        if (invite.getStatus() != HouseholdInviteStatus.PENDING) {
            throw new IllegalArgumentException("Invite has already been processed.");
        }

        invite.setStatus(HouseholdInviteStatus.ACCEPTED);
        inviteRepository.save(invite);

        invitee.setHousehold(invite.getHousehold());
        userRepository.save(invitee);
    }

    @Override
    @Transactional
    public void declineInvite(String authToken, String inviteId) {
        User invitee = userService.getUserByEmail(authToken);
        HouseholdInvite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new IllegalArgumentException("Invite not found: " + inviteId));

        if (!invite.getInviteeEmail().equalsIgnoreCase(invitee.getEmail())) {
            throw new IllegalArgumentException("This invite is not for the current user.");
        }
        if (invite.getStatus() != HouseholdInviteStatus.PENDING) {
            throw new IllegalArgumentException("Invite has already been processed.");
        }

        invite.setStatus(HouseholdInviteStatus.DECLINED);
        inviteRepository.save(invite);
    }

    @Override
    public List<User> getMembers(String authToken) {
        User user = userService.getUserByEmail(authToken);
        if (user.getHousehold() == null) {
            return List.of(user);
        }
        return userRepository.findAllByHouseholdId(user.getHousehold().getId());
    }

    @Override
    public List<Product> getHouseholdProducts(String authToken) {
        User user = userService.getUserByEmail(authToken);
        if (user.getHousehold() == null) {
            return productRepository.findAllByUserId(user.getId());
        }
        List<Long> memberIds = userRepository.findAllByHouseholdId(user.getHousehold().getId())
                .stream()
                .map(User::getId)
                .toList();
        return productRepository.findAllByUserIdIn(memberIds);
    }

    private Household resolveOrCreateHousehold(User inviter, String requestedName) {
        if (inviter.getHousehold() != null) {
            return inviter.getHousehold();
        }

        Household household = new Household();
        household.setName(normalizeFamilyName(inviter, requestedName));
        household.setOwner(inviter);
        Household saved = householdRepository.save(household);

        inviter.setHousehold(saved);
        userRepository.save(inviter);
        return saved;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private String normalizeFamilyName(User inviter, String requestedName) {
        String name = requestedName == null ? "" : requestedName.trim();
        if (!name.isBlank()) {
            return name;
        }
        String base = buildInviterName(inviter);
        return base.isBlank() ? "Family" : base + " Family";
    }

    private String buildInviterName(User inviter) {
        String first = inviter.getFirstName() == null ? "" : inviter.getFirstName().trim();
        String last = inviter.getLastName() == null ? "" : inviter.getLastName().trim();
        String combined = (first + " " + last).trim();
        return combined.isBlank() ? inviter.getEmail() : combined;
    }

    private String buildInviteMessage(User inviter, Household household) {
        String name = buildInviterName(inviter);
        return name + " invited you to join the " + household.getName() + " household.";
    }
}
