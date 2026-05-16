package com.freshtrack.api.household;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HouseholdInviteRepository extends JpaRepository<HouseholdInvite, String> {
    List<HouseholdInvite> findAllByHouseholdId(Long householdId);
}

