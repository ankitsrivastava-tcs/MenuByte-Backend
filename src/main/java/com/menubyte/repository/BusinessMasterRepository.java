package com.menubyte.repository;

import com.menubyte.entity.BusinessMaster;
import com.menubyte.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessMasterRepository extends JpaRepository<BusinessMaster, Long> {

    /**
     * Finds a BusinessMaster record by the User ID and Business ID.
     */
    Optional<BusinessMaster> findByUserIdAndBusinessId(Long userId, Long businessId);

    /**
     * Finds all BusinessMaster records for a specific User ID.
     */
    List<BusinessMaster> findByUserId(Long userId);

    /**
     * Finds all BusinessMaster records for a specific Business ID.
     */
    BusinessMaster findByBusinessId(Long businessId);

    /**
     * Finds all BusinessMaster records with a specific SubscriptionStatus.
     */
    List<BusinessMaster> findBySubscriptionStatus(SubscriptionStatus status);

    /**
     * Finds all BusinessMaster records that are active on a given date.
     */
    List<BusinessMaster> findByRegisterDateBeforeAndEndDateAfter(LocalDate dateBefore, LocalDate dateAfter);
}
