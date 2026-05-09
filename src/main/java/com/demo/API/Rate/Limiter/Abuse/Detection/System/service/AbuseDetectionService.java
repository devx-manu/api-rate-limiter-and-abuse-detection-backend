package com.demo.API.Rate.Limiter.Abuse.Detection.System.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.demo.API.Rate.Limiter.Abuse.Detection.System.entity.BlockedEntity;
import com.demo.API.Rate.Limiter.Abuse.Detection.System.repository.BlockedEntityRepository;

@Service
public class AbuseDetectionService {

    private final ConcurrentHashMap<String, Integer> burstCounter = new ConcurrentHashMap<>();
    private final BlockedEntityRepository blockedRepo;

    public AbuseDetectionService(BlockedEntityRepository blockedRepo) {
        this.blockedRepo = blockedRepo;
    }

    // 🚨 Track abuse
    public void recordRateLimitHit(String ip) {

        int count = burstCounter.merge(ip, 1, Integer::sum);

        if (count > 5 && count <= 10) {
            System.out.println("⚠️ Suspicious traffic from IP: " + ip);
        }

        if (count > 10) {
            blockByIp(ip, "Excessive burst traffic");
            burstCounter.remove(ip);
        }
    }

    // 🚫 BLOCK IP
    public void blockByIp(String ip, String reason) {

        Optional<BlockedEntity> existing = blockedRepo.findByIp(ip);

        if (existing.isPresent() &&
            existing.get().getBlockedUntil().isAfter(LocalDateTime.now())) {
            return;
        }

        BlockedEntity entity = new BlockedEntity();
        entity.setIp(ip);
        entity.setReason(reason);
        entity.setBlockedUntil(LocalDateTime.now().plusMinutes(3));

        blockedRepo.save(entity);
    }

    // 🚫 BLOCK USER (future ready)
    public void blockByUser(String userId, String reason) {

        Optional<BlockedEntity> existing = blockedRepo.findByUserId(userId);

        if (existing.isPresent() &&
            existing.get().getBlockedUntil().isAfter(LocalDateTime.now())) {
            return;
        }

        BlockedEntity entity = new BlockedEntity();
        entity.setUserId(userId);
        entity.setReason(reason);
        entity.setBlockedUntil(LocalDateTime.now().plusMinutes(3));

        blockedRepo.save(entity);
    }

    // ✅ CHECK BLOCK (WITH AUTO CLEAN)
    public boolean isBlocked(String ip) {

        Optional<BlockedEntity> entityOpt = blockedRepo.findByIp(ip);

        if (entityOpt.isPresent()) {
            BlockedEntity entity = entityOpt.get();

            if (entity.getBlockedUntil().isAfter(LocalDateTime.now())) {
                return true;
            } else {
                // ✅ AUTO DELETE EXPIRED BLOCK
                blockedRepo.delete(entity);
            }
        }

        return false;
    }

    public boolean isUserBlocked(String userId) {

        Optional<BlockedEntity> entityOpt = blockedRepo.findByUserId(userId);

        if (entityOpt.isPresent()) {
            BlockedEntity entity = entityOpt.get();

            if (entity.getBlockedUntil().isAfter(LocalDateTime.now())) {
                return true;
            } else {
                blockedRepo.delete(entity);
            }
        }

        return false;
    }

    // 🧹 CLEANUP JOB (every 1 min)
    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredBlocks() {
        blockedRepo.deleteAll(
            blockedRepo.findAll().stream()
                .filter(e -> e.getBlockedUntil().isBefore(LocalDateTime.now()))
                .toList()
        );
    }
}