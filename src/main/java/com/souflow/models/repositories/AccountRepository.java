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

import com.souflow.models.entities.Account;
import com.souflow.models.enums.RoleCode;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
	
	Optional<Account> findByEmail(String email); 
		
	Optional<Account> findByUsername(String username);
	
	@Query("""
		SELECT a
		FROM Account a
		WHERE
			(:deleted IS NULL OR a.deleted = :deleted)
			AND (
				:keyword IS NULL
				OR LOWER(a.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
				OR LOWER(a.fullname) LIKE LOWER(CONCAT('%', :keyword, '%'))
				OR LOWER(a.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
			)
			AND (:fromDate IS NULL OR a.createdDate >= :fromDate)
			AND (:toDate IS NULL OR a.createdDate <= :toDate)
			AND (:disabled IS NULL OR a.disabled = :disabled)
			AND (:role IS NULL OR :role = 'ALL' OR a.role.code = :role)
	""")
	Page<Account> filterAccounts(
			@Param("deleted") Boolean deleted,
			@Param("keyword") String keyword,
			@Param("fromDate") LocalDateTime fromDate,
			@Param("toDate") LocalDateTime toDate,
			@Param("disabled") Boolean disabled,
			@Param("role") RoleCode role,
			Pageable pageable
	);
	
	@Modifying
	@Transactional
	@Query("""
		UPDATE Account a
		SET a.credentialExpired = true
		WHERE
			(:deleted is NULL OR a.deleted = :deleted)
			AND (
				:keyword IS NULL
				OR LOWER(a.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
				OR LOWER(a.fullname) LIKE LOWER(CONCAT('%', :keyword, '%'))
				OR LOWER(a.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
			)
			AND (:fromDate IS NULL OR a.createdDate >= :fromDate)
			AND (:toDate IS NULL OR a.createdDate <= :toDate)
			AND (:disabled IS NULL OR a.disabled = :disabled)
			AND (:role IS NULL OR a.role.code = :role)
			AND a.credentialExpiredDate <= CURRENT_TIMESTAMP
		""")
	void checkAndExpireCredentialBeforePagination(
			@Param("deleted") Boolean deleted,
			@Param("keyword") String keyword,
			@Param("fromDate") LocalDateTime fromDate,
			@Param("toDate") LocalDateTime toDate,
			@Param("disabled") Boolean disabled,
			@Param("role") RoleCode role
	);
	
	@Modifying
	@Transactional
	@Query("UPDATE Account a SET a.credentialExpired = true WHERE a.username = :username AND a.credentialExpiredDate <= CURRENT_TIMESTAMP")
	void checkAndExpireCredential(@Param("username") String username);
	
	@Modifying
    @Transactional
    @Query("UPDATE Account a SET a.deleted = true WHERE a.pk = :pk")
    void softDelete(@Param("pk") Long pk);

    @Query("SELECT COUNT(a) FROM Account a WHERE (a.deleted = false OR a.deleted IS NULL) AND (a.disabled = false OR a.disabled IS NULL)")
    long countActiveUsers();

    @Query("SELECT COUNT(a) FROM Account a WHERE a.createdDate >= :start AND a.createdDate < :end AND (a.deleted = false OR a.deleted IS NULL) AND (a.disabled = false OR a.disabled IS NULL)")
    long countUsersByMonthRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
