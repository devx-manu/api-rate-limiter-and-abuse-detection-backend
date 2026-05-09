package com.demo.API.Rate.Limiter.Abuse.Detection.System.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demo.API.Rate.Limiter.Abuse.Detection.System.entity.BlockedEntity;

public interface BlockedEntityRepository 
extends JpaRepository<BlockedEntity, Long> {

	Optional<BlockedEntity> findByIp(String ip);
	Optional<BlockedEntity> findByUserId(String userId);
}
