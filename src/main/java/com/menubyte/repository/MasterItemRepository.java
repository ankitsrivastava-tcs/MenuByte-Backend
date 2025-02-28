package com.menubyte.repository;

import com.menubyte.entity.MasterItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MasterItemRepository extends JpaRepository<MasterItem, Long> {
}
