package com.menubyte.repository;

import com.menubyte.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<Menu, Long> {
    Optional<Menu> findByBusinessId(Long businessId);
}
