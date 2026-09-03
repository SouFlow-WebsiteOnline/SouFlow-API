package com.souflow.models.repositories;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.souflow.models.entities.Order;
import com.souflow.models.enums.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
	
	Optional<Order> findByCode(String code);
	
	java.util.List<Order> findByStatusAndCreatedDateBefore(OrderStatus status, LocalDateTime time);
	
    @Query("""
        SELECT o
        FROM Order o
        LEFT JOIN FETCH o.account a
        WHERE
            (:accountPk IS NULL OR a.pk = :accountPk)
            AND (:deleted IS NULL OR o.deleted = :deleted)
            AND (:expired IS NULL OR o.expired = :expired)
            AND (
                :keyword IS NULL
                OR LOWER(o.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(a.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(o.fullname) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            AND (:fromDate IS NULL OR o.createdDate >= :fromDate)
            AND (:toDate IS NULL OR o.createdDate <= :toDate)
            AND (:status IS NULL OR o.status = :status)
    """)
    Page<Order> filterOrders(
            @Param("keyword") String keyword,
            @Param("accountPk") Long accountPk,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            @Param("status") OrderStatus status,
            @Param("expired") Boolean expired,
            @Param("deleted") Boolean deleted,
            Pageable pageable
    );
    
    @Query("""
        SELECT o
        FROM Order o
        LEFT JOIN FETCH o.account a
        WHERE
            (o.deleted = false OR o.deleted IS NULL)
            AND o.status NOT IN ('CANCELLED', 'DELIVERED')
            AND (
                :keyword IS NULL
                OR LOWER(o.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(a.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(o.fullname) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
    """)
    Page<Order> filterActiveOrders(
            @Param("keyword") String keyword,
            Pageable pageable
    );
    
    @Modifying
    @Transactional
    @Query("""
            UPDATE Order o 
            SET o.expired = true
            WHERE o.pk IN (
                SELECT o2.pk FROM Order o2
                LEFT JOIN o2.account a
                WHERE
                    (:deleted IS NULL OR o2.deleted = :deleted)
                    AND (:expired IS NULL OR o2.expired = :expired)
                    AND (
                        :keyword IS NULL
                        OR LOWER(o2.code) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        OR LOWER(a.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        OR LOWER(o2.fullname) LIKE LOWER(CONCAT('%', :keyword, '%'))
                        OR LOWER(a.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    )
                    AND (:fromDate IS NULL OR o2.createdDate >= :fromDate)
                    AND (:toDate IS NULL OR o2.createdDate <= :toDate)
                    AND o2.expiredDate <= CURRENT_TIMESTAMP
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
    @Query("UPDATE Order o SET o.expired = true WHERE o.pk = :pk AND o.expired = false AND o.expiredDate <= CURRENT_TIMESTAMP")
    int checkAndExpire(@Param("pk") Long pk);

    @Modifying
    @Transactional
    @Query("UPDATE Order o SET o.deleted = true WHERE o.pk = :pk")
    int softDelete(@Param("pk") Long pk);

    @Modifying
    @Transactional
    @Query(value = """
        UPDATE orders
        SET status = 'PAID'
        WHERE pk = :orderPk
        AND (
            SELECT COALESCE(SUM(p.amount), 0)
            FROM payments p
            WHERE p.order_pk = :orderPk
            AND p.paid = 1
        ) >= total
    """, nativeQuery = true)
    int markOrderAsPaidIfFullyPaid(@Param("orderPk") Long orderPk);

    @Query("SELECT SUM(o.total) FROM Order o WHERE o.status = :status AND o.createdDate >= :start AND o.createdDate < :end AND (o.deleted = false OR o.deleted IS NULL)")
    java.math.BigDecimal getRevenueByMonthRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("status") OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdDate >= :start AND o.createdDate < :end AND (o.deleted = false OR o.deleted IS NULL)")
    Long countOrdersByMonthRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT YEAR(o.createdDate), MONTH(o.createdDate), SUM(o.total) FROM Order o WHERE o.createdDate >= :startDate AND o.createdDate < :endDate AND o.status = :status AND (o.deleted = false OR o.deleted IS NULL) GROUP BY YEAR(o.createdDate), MONTH(o.createdDate) ORDER BY YEAR(o.createdDate) ASC, MONTH(o.createdDate) ASC")
    java.util.List<Object[]> getMonthlyRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("status") OrderStatus status);

    @Query("SELECT YEAR(o.createdDate), MONTH(o.createdDate), DAY(o.createdDate), SUM(o.total) FROM Order o WHERE o.createdDate >= :startDate AND o.createdDate < :endDate AND o.status = :status AND (o.deleted = false OR o.deleted IS NULL) GROUP BY YEAR(o.createdDate), MONTH(o.createdDate), DAY(o.createdDate) ORDER BY YEAR(o.createdDate) ASC, MONTH(o.createdDate) ASC, DAY(o.createdDate) ASC")
    java.util.List<Object[]> getDailyRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("status") OrderStatus status);

    @Query("SELECT HOUR(o.createdDate), SUM(o.total) FROM Order o WHERE o.createdDate >= :startDate AND o.createdDate < :endDate AND o.status = :status AND (o.deleted = false OR o.deleted IS NULL) GROUP BY HOUR(o.createdDate) ORDER BY HOUR(o.createdDate) ASC")
    java.util.List<Object[]> getHourlyRevenue(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("status") OrderStatus status);

    @Query("SELECT c.nameVn, SUM(od.subtotal) FROM OrderDetail od JOIN od.order o JOIN od.product p JOIN p.category c WHERE o.status = :status AND o.createdDate >= :startDate AND o.createdDate < :endDate AND (o.deleted = false OR o.deleted IS NULL) GROUP BY c.nameVn ORDER BY SUM(od.subtotal) DESC")
    java.util.List<Object[]> getRevenueByCategory(@Param("status") OrderStatus status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT p.code, p.nameVn, SUM(od.quantity), SUM(od.subtotal) FROM OrderDetail od JOIN od.order o JOIN od.product p WHERE o.status = :status AND o.createdDate >= :startDate AND o.createdDate < :endDate AND (o.deleted = false OR o.deleted IS NULL) GROUP BY p.code, p.nameVn ORDER BY SUM(od.quantity) DESC")
    java.util.List<Object[]> getTopSellingProducts(@Param("status") OrderStatus status, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);
}
