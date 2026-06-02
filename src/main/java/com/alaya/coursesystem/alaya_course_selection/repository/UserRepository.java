// 包路径：com.alaya.coursesystem.alaya_course_selection.repository
package com.alaya.coursesystem.alaya_course_selection.repository;

import com.alaya.coursesystem.alaya_course_selection.entity.Role;
import com.alaya.coursesystem.alaya_course_selection.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE " +
        "(:keyword IS NULL OR u.username LIKE %:keyword% OR u.idCard LIKE %:keyword%) " +
        "AND (:role IS NULL OR u.role = :role)")
    Page<User> findByKeywordAndRole(@Param("keyword") String keyword, @Param("role") Role role, Pageable pageable);

    List<User> findByRole(Role role);
}
