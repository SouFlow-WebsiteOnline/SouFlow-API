package com.souflow.models.repositories;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.souflow.models.entities.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByCode(String code);

    List<Product> findTop12ByDeletedFalseAndCustomisedFalseOrderBySalesDesc();

	@Query("""
        SELECT p
        FROM Product p
        LEFT JOIN FETCH p.category
        WHERE
            (:deleted IS NULL OR p.deleted = :deleted)
            AND (:available IS NULL OR p.available = :available)
            AND (:customised IS NULL OR p.customised = :customised)
            AND (:categoryPk IS NULL OR p.category.pk = :categoryPk)
            AND (:minPrice IS NULL OR p.price >= :minPrice)
            AND (:maxPrice IS NULL OR p.price <= :maxPrice)
            AND (
                :keyword IS NULL
                OR p.code LIKE :keyword
                OR p.nameVn LIKE :keyword
                OR p.nameEng LIKE :keyword
            )
            AND (:fromDate IS NULL OR p.createdDate >= :fromDate)
            AND (:toDate IS NULL OR p.createdDate <= :toDate)
        ORDER BY
            p.deleted ASC,
            CASE WHEN p.quantity <= 0 THEN 1 ELSE 0 END ASC
    """)
    Page<Product> filterProducts(
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("categoryPk") Long categoryPk,
            @Param("keyword") String keyword,
            @Param("customised") Boolean customised,
            @Param("available") Boolean available,
            @Param("deleted") Boolean deleted,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable
    );
	
	@Modifying
    @Transactional
    @Query("UPDATE Product p SET p.deleted = true WHERE p.pk = :pk")
    void softDelete(@Param("pk") Long pk);

    @Modifying
	@Transactional
	@Query("UPDATE Product p SET p.quantity = :quantity WHERE p.pk = :pk")
	int updateQuantity(@Param("pk") Long pk, @Param("quantity") Integer quantity);

    @Modifying
	@Transactional
	@Query("""
		UPDATE Product p
		SET p.quantity = p.quantity - :amount,
		    p.available = CASE WHEN (p.quantity - :amount) = 0 THEN false ELSE p.available END
		WHERE p.pk = :pk
		AND p.quantity >= :amount
	""")
	int decreaseQuantity(@Param("pk") Long pk, @Param("amount") Integer amount); 

    @Modifying
	@Transactional
	@Query("""
		UPDATE Product p
		SET p.quantity = p.quantity + :amount,
		    p.available = CASE WHEN (p.quantity + :amount) > 0 THEN true ELSE p.available END
		WHERE p.pk = :pk
	""")
	int increaseQuantity(@Param("pk") Long pk, @Param("amount") Integer amount);

	@Modifying
	@Transactional
	@Query("UPDATE Product p SET p.sales = (CASE WHEN p.sales IS NULL THEN 0L ELSE p.sales END) + :amount WHERE p.pk = :pk")
	int increaseSales(@Param("pk") Long pk, @Param("amount") Integer amount);

    @Query("SELECT COUNT(p) FROM Product p WHERE (p.deleted = false OR p.deleted IS NULL)")
    long countActiveProducts();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.createdDate >= :start AND p.createdDate < :end AND (p.deleted = false OR p.deleted IS NULL)")
    long countProductsByMonthRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT p.code, p.nameVn, p.quantity FROM Product p WHERE (p.deleted = false OR p.deleted IS NULL) AND p.quantity <= :threshold")
    List<Object[]> getLowStockProducts(@Param("threshold") int threshold);
}
