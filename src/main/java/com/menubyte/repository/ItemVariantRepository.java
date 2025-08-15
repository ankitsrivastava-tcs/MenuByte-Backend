package com.menubyte.repository;

import com.menubyte.entity.ItemVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ItemVariantRepository extends JpaRepository<ItemVariant, Long> {
    @Transactional
    void deleteByItemId(Long itemId);
}
