package com.lqm.repositories;

import com.lqm.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {

    Optional<User> findByEmail(String email);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
            "FROM User u WHERE u.email = :email " +
            "AND (:excludeId IS NULL OR u.id <> :excludeId)")
    boolean existsByEmailAndExcludeId(@Param("email") String email,
                                      @Param("excludeId") Integer excludeId);

    List<User> findByRoleIn(List<String> roles);
}
