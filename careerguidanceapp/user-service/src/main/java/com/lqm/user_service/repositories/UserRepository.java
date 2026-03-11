package com.lqm.user_service.repositories;

import com.lqm.user_service.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmailAndActiveTrue(String email);

    @Query("""
            SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END
            FROM User u WHERE u.email = :email
            AND (:excludeId IS NULL OR u.id <> :excludeId)
    """)
    boolean existsByEmailAndExcludeId(@Param("email") String email,
                                      @Param("excludeId") UUID excludeId);

    long countBy();

    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> countUserByRole();

    @Query("SELECT u.active, COUNT(u) FROM User u GROUP BY u.active")
    List<Object[]> countUserByStatus();

    @Query("SELECT YEAR(u.createdDate), COUNT(u) FROM User u " +
            "WHERE u.role = 'ROLE_STUDENT' " +
            "GROUP BY YEAR(u.createdDate) ORDER BY YEAR(u.createdDate) ASC")
    List<Object[]> countStudentGrowthByYear();

}