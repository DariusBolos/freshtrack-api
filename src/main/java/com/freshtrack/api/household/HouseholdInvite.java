package com.freshtrack.api.household;

import com.freshtrack.api.user.User;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Data;

@Data
@Entity
@Table(name = "household_invites")
public class HouseholdInvite {
    @Id
    private String id;
    private String inviteeEmail;
    private OffsetDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private HouseholdInviteStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_id", nullable = false)
    private User inviter;

    public HouseholdInvite() {}

    public HouseholdInvite(String id, String inviteeEmail, OffsetDateTime createdAt, HouseholdInviteStatus status, Household household, User inviter) {
        this.id = id;
        this.inviteeEmail = inviteeEmail;
        this.createdAt = createdAt;
        this.status = status;
        this.household = household;
        this.inviter = inviter;
    }
}

