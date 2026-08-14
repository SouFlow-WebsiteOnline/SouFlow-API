package com.souflow.models.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.souflow.models.entities.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
	@Query("""
        SELECT c
        FROM Category c
        WHERE
            (LOWER(c.nameEng) != 'custom' AND LOWER(c.nameVn) != 'custom')
            AND (:deleted IS NULL OR c.deleted = :deleted) 
            AND (
                :keyword IS NULL
                OR LOWER(c.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(c.nameVn) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(c.nameEng) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
    """)
    Page<Category> filterCategories(
            @Param("keyword") String keyword,
            @Param("deleted") Boolean deleted,
            Pageable pageable
    );
	
	@Modifying
    @Transactional
    @Query("UPDATE Category c SET c.deleted = true WHERE c.pk = :pk")
    void softDelete(@Param("pk") Long pk);

    @Query("SELECT c FROM Category c WHERE (LOWER(c.nameEng) != 'custom' AND LOWER(c.nameVn) != 'custom') AND (c.deleted = false OR c.deleted IS NULL)")
    java.util.List<Category> findAllActive();
}
