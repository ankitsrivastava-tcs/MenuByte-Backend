package com.menubyte.repository;

import com.menubyte.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BusinessRepository extends JpaRepository<Business, Long> {
    List<Business> findByUserId(Long userId);
}
