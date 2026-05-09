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

<<<<<<< HEAD
        // =========================================
        // HANDLE OPTIONS
        // =========================================

=======
        // OPTIONS
>>>>>>> 75e116c (Updated code)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {

            response.setStatus(HttpServletResponse.SC_OK);

            return;
        }

<<<<<<< HEAD
        // =========================================
        // GET REAL CLIENT IP
        // =========================================
=======
        // REAL IP
        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        if (ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
>>>>>>> 75e116c (Updated code)

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

<<<<<<< HEAD
        // =========================================
        // ADMIN BYPASS
        // =========================================

        if (endpoint.startsWith("/api/admin")) {

=======
        // ALLOW ADMIN
        if (endpoint.startsWith("/api/admin")) {
>>>>>>> 75e116c (Updated code)
            filterChain.doFilter(request, response);

            return;
        }

<<<<<<< HEAD
        // =========================================
        // HARD BLOCK CHECK
        // =========================================
=======
        // ==================================================
        // HARD BLOCK CHECK
        // ==================================================
>>>>>>> 75e116c (Updated code)

        if (abuseService.isBlocked(ip)) {

            log(ip, endpoint, ApiRequestLog.Status.BLOCKED);

<<<<<<< HEAD
            sendJsonResponse(
                    response,
                    429,
                    "BLOCKED",
                    "IP temporarily blocked due to suspicious activity"
            );
=======
            response.setContentType("application/json");
            response.setStatus(429);

            response.getWriter().write("""
            {
              "error": "BLOCKED",
              "message": "IP temporarily blocked due to suspicious activity"
            }
            """);
>>>>>>> 75e116c (Updated code)

            return;
        }

<<<<<<< HEAD
        // =========================================
        // TOKEN BUCKET CHECK
        // =========================================
=======
        // ==================================================
        // TOKEN BUCKET CHECK
        // ==================================================
>>>>>>> 75e116c (Updated code)

        TokenBucket bucket = store.getBucket(ip);

        if (!bucket.tryConsume()) {

            abuseService.recordRateLimitHit(ip);

            // IMPORTANT
            log(ip, endpoint, ApiRequestLog.Status.RATE_LIMITED);

<<<<<<< HEAD
            sendJsonResponse(
                    response,
                    429,
                    "RATE_LIMIT",
                    "Too many requests. Token bucket exhausted."
            );
=======
            response.setContentType("application/json");
            response.setStatus(429);

            response.getWriter().write("""
            {
              "error": "RATE_LIMIT",
              "message": "Token bucket exhausted. Wait for refill."
            }
            """);
>>>>>>> 75e116c (Updated code)

            return;
        }

<<<<<<< HEAD
        // =========================================
        // SUCCESS
        // =========================================
=======
        // ==================================================
        // SUCCESS
        // ==================================================
>>>>>>> 75e116c (Updated code)

        log(ip, endpoint, ApiRequestLog.Status.ALLOWED);

        filterChain.doFilter(request, response);
    }

<<<<<<< HEAD
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

        response.setContentType("application/json");

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
=======
    private void log(String ip,
                     String endpoint,
                     ApiRequestLog.Status status) {
>>>>>>> 75e116c (Updated code)

        ApiRequestLog log = new ApiRequestLog();

        log.setIp(ip);

        log.setEndpoint(endpoint);

        log.setTimestamp(LocalDateTime.now());

        log.setStatus(status);

        logRepo.save(log);
    }
}
