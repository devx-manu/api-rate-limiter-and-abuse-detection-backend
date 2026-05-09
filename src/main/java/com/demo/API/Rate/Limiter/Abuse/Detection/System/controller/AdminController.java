package com.demo.API.Rate.Limiter.Abuse.Detection.System.controller;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.API.Rate.Limiter.Abuse.Detection.System.entity.ApiRequestLog;
import com.demo.API.Rate.Limiter.Abuse.Detection.System.entity.BlockedEntity;
import com.demo.API.Rate.Limiter.Abuse.Detection.System.repository.ApiRequestLogRepository;
import com.demo.API.Rate.Limiter.Abuse.Detection.System.repository.BlockedEntityRepository;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ApiRequestLogRepository logRepo;
    private final BlockedEntityRepository blockedRepo;

    public AdminController(ApiRequestLogRepository logRepo,
                           BlockedEntityRepository blockedRepo) {
        this.logRepo = logRepo;
        this.blockedRepo = blockedRepo;
    }

    @GetMapping("/logs")
    public Page<ApiRequestLog> logs(Pageable pageable) {
        Pageable sorted = PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            Sort.by("timestamp").descending()
        );

        return logRepo.findAll(sorted);
    }

    @GetMapping("/blocked")
    public List<BlockedEntity> blocked() {
        return blockedRepo.findAll();
    }

    @PostMapping("/unblock/{id}")
    public void unblock(@PathVariable Long id) {
        blockedRepo.deleteById(id);
    }
}