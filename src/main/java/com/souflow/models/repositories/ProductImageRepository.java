package com.souflow.models.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.souflow.models.entities.ProductImage;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {
	
	@Modifying
    @Transactional
    @Query("UPDATE ProductImage pi SET pi.deleted = true WHERE pi.pk = :pk")
    void softDelete(@Param("pk") Long pk);
}
