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

    public RateLimiterFilter(
            RateLimiterStore store,
            AbuseDetectionService abuseService,
            ApiRequestLogRepository logRepo) {

        this.store = store;
        this.abuseService = abuseService;
        this.logRepo = logRepo;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        // =========================================
        // HANDLE OPTIONS
        // =========================================

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {

            response.setStatus(HttpServletResponse.SC_OK);

            return;
        }

        // =========================================
        // GET REAL CLIENT IP
        // =========================================

        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null ||
            ip.isBlank() ||
            "unknown".equalsIgnoreCase(ip)) {

            ip = request.getRemoteAddr();
        }

        // FIRST REAL IP

        if (ip.contains(",")) {

            ip = ip.split(",")[0].trim();
        }

        // LOCALHOST NORMALIZATION

        if ("0:0:0:0:0:0:0:1".equals(ip)) {

            ip = "127.0.0.1";
        }

        String endpoint = request.getRequestURI();

        // =========================================
        // ADMIN BYPASS
        // =========================================

        if (endpoint.startsWith("/api/admin")) {

            filterChain.doFilter(request, response);

            return;
        }

        // =========================================
        // HARD BLOCK CHECK
        // =========================================

        if (abuseService.isBlocked(ip)) {

            log(ip, endpoint, ApiRequestLog.Status.BLOCKED);

            sendJsonResponse(
                    response,
                    429,
                    "BLOCKED",
                    "IP temporarily blocked due to suspicious activity"
            );

            return;
        }

        // =========================================
        // TOKEN BUCKET CHECK
        // =========================================

        TokenBucket bucket = store.getBucket(ip);

        if (!bucket.tryConsume()) {

            abuseService.recordRateLimitHit(ip);

            log(ip, endpoint, ApiRequestLog.Status.RATE_LIMITED);

            sendJsonResponse(
                    response,
                    429,
                    "RATE_LIMIT",
                    "Too many requests. Token bucket exhausted."
            );

            return;
        }

        // =========================================
        // SUCCESS
        // =========================================

        log(ip, endpoint, ApiRequestLog.Status.ALLOWED);

        filterChain.doFilter(request, response);
    }

    // =========================================
    // JSON RESPONSE HELPER
    // =========================================

    private void sendJsonResponse(
            HttpServletResponse response,
            int status,
            String error,
            String message)
            throws IOException {

        response.setStatus(status);

        response.setCharacterEncoding("UTF-8");

        response.setContentType("application/json;charset=UTF-8");

        response.setHeader("Cache-Control", "no-cache");

        String jsonResponse = """
        {
          "error": "%s",
          "message": "%s"
        }
        """.formatted(error, message);

        response.getWriter().write(jsonResponse);

        response.getWriter().flush();
    }

    // =========================================
    // LOGGING
    // =========================================

    private void log(
            String ip,
            String endpoint,
            ApiRequestLog.Status status) {

        ApiRequestLog log = new ApiRequestLog();

        log.setIp(ip);

        log.setEndpoint(endpoint);

        log.setTimestamp(LocalDateTime.now());

        log.setStatus(status);

        logRepo.save(log);
    }
}
