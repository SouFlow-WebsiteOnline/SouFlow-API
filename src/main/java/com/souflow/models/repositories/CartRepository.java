package com.souflow.models.repositories;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.souflow.models.entities.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {
	@Query("""
		SELECT ca
		FROM Cart ca
		WHERE (
				:keyword IS NULL
				OR LOWER(ca.account.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
				OR LOWER(ca.account.fullname) LIKE LOWER(CONCAT('%', :keyword, '%'))
				OR LOWER(ca.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
			)
			AND (:fromDate IS NULL OR ca.createdDate >= :fromDate)
			AND (:toDate IS NULL OR ca.createdDate <= :toDate)
			AND (:expired IS NULL OR ca.expired = :expired)
			AND (:deleted IS NULL OR ca.deleted = :deleted)
    """)
	Page<Cart> filterCarts(
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
			UPDATE Cart ca
			SET ca.expired = true
			WHERE ca.pk IN (
				SELECT c2.pk FROM Cart c2
				LEFT JOIN c2.account a
				WHERE (
					:keyword IS NULL
					OR LOWER(c2.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
					OR LOWER(a.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
					OR LOWER(a.fullname) LIKE LOWER(CONCAT('%', :keyword, '%'))
					OR LOWER(a.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
				)
				AND (:fromDate IS NULL OR c2.createdDate >= :fromDate)
				AND (:toDate IS NULL OR c2.createdDate <= :toDate)
				AND (:expired IS NULL OR c2.expired = :expired)
				AND (:deleted IS NULL OR c2.deleted = :deleted)
				AND c2.expiredDate <= CURRENT_TIMESTAMP
			)
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
    @Query("UPDATE Cart ca SET ca.expired = true WHERE ca.pk = :pk AND ca.expired = false AND ca.expiredDate <= CURRENT_TIMESTAMP")
    int checkAndExpire(@Param("pk") Long pk);
	
	@Modifying
    @Transactional
    @Query("UPDATE Cart ca SET ca.deleted = true WHERE ca.pk = :pk")
    int softDelete(@Param("pk") Long pk);
}
