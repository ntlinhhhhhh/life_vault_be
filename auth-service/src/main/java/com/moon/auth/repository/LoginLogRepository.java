package com.moon.auth.repository;

import com.moon.auth.entity.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {

    List<LoginLog> findByUserCodeOrderByCreateDateDesc(String userCode);
}
