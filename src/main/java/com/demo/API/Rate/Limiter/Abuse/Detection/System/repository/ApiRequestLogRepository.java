package com.demo.API.Rate.Limiter.Abuse.Detection.System.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;

import com.demo.API.Rate.Limiter.Abuse.Detection.System.entity.ApiRequestLog;

public interface ApiRequestLogRepository extends JpaRepository<ApiRequestLog, Long> {

}