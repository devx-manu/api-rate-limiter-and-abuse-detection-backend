package com.demo.API.Rate.Limiter.Abuse.Detection.System.filter;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.demo.API.Rate.Limiter.Abuse.Detection.System.entity.ApiRequestLog;
import com.demo.API.Rate.Limiter.Abuse.Detection.System.repository.ApiRequestLogRepository;
import com.demo.API.Rate.Limiter.Abuse.Detection.System.service.AbuseDetectionService;
import com.demo.API.Rate.Limiter.Abuse.Detection.System.util.RateLimiterStore;
import com.demo.API.Rate.Limiter.Abuse.Detection.System.util.TokenBucket;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimiterFilter extends OncePerRequestFilter {

    private final RateLimiterStore store;
    private final AbuseDetectionService abuseService;
    private final ApiRequestLogRepository logRepo;

    public RateLimiterFilter(RateLimiterStore store,
                             AbuseDetectionService abuseService,
                             ApiRequestLogRepository logRepo) {
        this.store = store;
        this.abuseService = abuseService;
        this.logRepo = logRepo;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // ✅ CORS (DEV MODE)
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Max-Age", "3600");

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String ip = request.getRemoteAddr();

        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }

        String endpoint = request.getRequestURI();

        // ✅ Allow admin endpoints from localhost
        if (endpoint.startsWith("/api/admin") && ip.equals("127.0.0.1")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 🚫 BLOCK CHECK (HIGHEST PRIORITY)
        if (abuseService.isBlocked(ip)) {
            log(ip, endpoint, ApiRequestLog.Status.BLOCKED);

            response.setContentType("application/json");
            response.setStatus(429);
            response.getWriter().write("""
            {
              "error": "BLOCKED",
              "message": "You are temporarily blocked"
            }
            """);

            return;
        }

        // ⚡ RATE LIMIT CHECK
        TokenBucket bucket = store.getBucket(ip);

        if (!bucket.tryConsume()) {

            abuseService.recordRateLimitHit(ip);

            log(ip, endpoint, ApiRequestLog.Status.BLOCKED);

            response.setContentType("application/json");
            response.setStatus(429);
            response.getWriter().write("""
            {
              "error": "RATE_LIMIT",
              "message": "Too many requests"
            }
            """);

            return;
        }

        // ✅ SUCCESS
        log(ip, endpoint, ApiRequestLog.Status.ALLOWED);

        filterChain.doFilter(request, response);
    }

    private void log(String ip, String endpoint, ApiRequestLog.Status status) {
        ApiRequestLog log = new ApiRequestLog();
        log.setIp(ip);
        log.setEndpoint(endpoint);
        log.setTimestamp(LocalDateTime.now());
        log.setStatus(status);

        logRepo.save(log);
    }
}