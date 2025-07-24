package com.menubyte.repository;

import com.menubyte.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    /**
     * Finds a Menu by the ID of the Business it belongs to.
     * Since Business has a OneToOne relationship with Menu, this should return at most one.
     */
    Optional<Menu> findByBusinessId(Long businessId);
}
