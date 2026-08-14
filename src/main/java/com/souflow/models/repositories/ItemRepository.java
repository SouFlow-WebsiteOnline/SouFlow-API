package com.souflow.models.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.souflow.models.entities.Item;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

}
