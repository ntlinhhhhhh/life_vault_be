package com.moon.auth.repository;

import com.moon.auth.entity.UserRole;
import com.moon.auth.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {

    List<UserRole> findByIdUserCode(String userCode);
}

