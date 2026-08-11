package com.souflow.models.repositories;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.souflow.models.entities.Discount;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {
	
    @Query("""
        SELECT d
        FROM Discount d
        WHERE
            (:deleted IS NULL OR d.deleted = :deleted)
            AND (:expired IS NULL OR d.expired = :expired)
            AND (
                :keyword IS NULL
                OR LOWER(d.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            AND (:fromDate IS NULL OR d.createdDate >= :fromDate)
            AND (:toDate IS NULL OR d.createdDate <= :toDate)
    """)
    Page<Discount> filterDiscounts(
            @Param("keyword") String keyword,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("expired") Boolean expired,
            @Param("deleted") Boolean deleted,
            Pageable pageable
    );
    
    @Modifying
    @Transactional
    @Query("""
            UPDATE Discount d 
            SET d.expired = true
            WHERE
                (:deleted IS NULL OR d.deleted = :deleted)
                AND (:expired IS NULL OR d.expired = :expired)
                AND (:keyword IS NULL OR LOWER(d.code) LIKE LOWER(CONCAT('%', :keyword, '%')))
                AND (:fromDate IS NULL OR d.createdDate >= :fromDate)
                AND (:toDate IS NULL OR d.createdDate <= :toDate)
                AND d.expiredDate <= CURRENT_TIMESTAMP
        """)
    int checkAndExpireBeforePagination(
    		@Param("keyword") String keyword,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("expired") Boolean expired,
            @Param("deleted") Boolean deleted
    );
    
    @Modifying
    @Transactional
    @Query("UPDATE Discount d SET d.expired = true WHERE d.pk = :pk AND d.expired = false AND d.expiredDate <= CURRENT_TIMESTAMP")
    int checkAndExpire(@Param("pk") Long pk);
    
    @Modifying
    @Transactional
    @Query("UPDATE Discount d SET d.deleted = true WHERE d.pk = :pk")
    int softDelete(@Param("pk") Long pk);

    Discount findByCode(String code);
}
