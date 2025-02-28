package com.menubyte.repository;

import com.menubyte.entity.Item;
import com.menubyte.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByMenu(Menu menu);
}
