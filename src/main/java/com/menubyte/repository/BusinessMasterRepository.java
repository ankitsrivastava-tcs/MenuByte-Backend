package com.menubyte.repository;

import com.menubyte.entity.BusinessMaster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BusinessMasterRepository extends JpaRepository<BusinessMaster, Long> {
    List<BusinessMaster> findByUserId(Long userId);
}
