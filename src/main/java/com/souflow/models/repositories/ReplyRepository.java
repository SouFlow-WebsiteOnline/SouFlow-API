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

import com.souflow.models.entities.Reply;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Long> {

    @Query("""
        SELECT r
        FROM Reply r
        WHERE
            (:deleted IS NULL OR r.deleted = :deleted)
            AND (
                :keyword IS NULL
                OR CAST(r.comment.pk AS string) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(r.account.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(r.account.fullname) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(r.account.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
            )
            AND (:fromDate IS NULL OR r.createdDate >= :fromDate)
            AND (:toDate IS NULL OR r.createdDate <= :toDate)
    """)
    Page<Reply> filterReplies(
            @Param("keyword") String keyword,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("deleted") Boolean deleted,
            Pageable pageable
    );
    
    @Modifying
    @Transactional
    @Query("UPDATE Reply r SET r.deleted = true WHERE r.pk = :pk")
    void softDelete(@Param("pk") Long pk);
}
