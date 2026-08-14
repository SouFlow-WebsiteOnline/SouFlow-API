package com.souflow.models.repositories;

import java.time.LocalDate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.souflow.models.entities.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
	@Query("""
        SELECT co
        FROM Comment co
        WHERE
            (:deleted IS NULL OR co.deleted = :deleted)
            AND (
                :keyword IS NULL
                OR CAST(co.product.pk AS string) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(co.account.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            AND (:fromDate IS NULL OR co.createdDate >= :fromDate)
            AND (:toDate IS NULL OR co.createdDate <= :toDate)
    """)
    Page<Comment> filterComments(
            @Param("keyword") String keyword,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("deleted") Boolean deleted,
            Pageable pageable
    );
	
	@Modifying
    @Transactional
    @Query("UPDATE Comment c SET c.deleted = true WHERE c.pk = :pk")
    void softDelete(@Param("pk") Long pk);
}
