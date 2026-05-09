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
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

       

        String ip = request.getHeader("X-Forwarded-For");

        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

       
        if (ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

       
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }

        String endpoint = request.getRequestURI();

        

        if (endpoint.startsWith("/api/admin")) {
            filterChain.doFilter(request, response);
            return;
        }

       

        if (abuseService.isBlocked(ip)) {

            log(ip, endpoint, ApiRequestLog.Status.BLOCKED);

            response.setContentType("application/json");
            response.setStatus(429);

            response.getWriter().write("""
            {
              "error": "BLOCKED",
              "message": "IP temporarily blocked due to suspicious activity"
            }
            """);

            return;
        }

       

        TokenBucket bucket = store.getBucket(ip);

        if (!bucket.tryConsume()) {

           
            abuseService.recordRateLimitHit(ip);

            log(ip, endpoint, ApiRequestLog.Status.BLOCKED);

            response.setContentType("application/json");
            response.setStatus(429);

            response.getWriter().write("""
            {
              "error": "RATE_LIMIT",
              "message": "Too many requests. Slow down."
            }
            """);

            return;
        }

       

        log(ip, endpoint, ApiRequestLog.Status.ALLOWED);

        filterChain.doFilter(request, response);
    }


    private void log(String ip,
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
