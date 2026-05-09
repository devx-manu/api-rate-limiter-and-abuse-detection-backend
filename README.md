# 🛡️ API Rate Limiter & Abuse Detection System — Backend

Enterprise-grade Spring Boot backend engineered to defend APIs against traffic abuse, request flooding, brute-force bursts, and suspicious client behavior.

Built with a scalable architecture inspired by modern gateway protection systems used in high-performance distributed environments.

---

# 🌌 Overview

This backend acts as an intelligent security layer between clients and protected APIs.

The system continuously monitors incoming traffic, applies token bucket rate limiting, detects abusive patterns, temporarily blocks malicious IPs, and logs all request activity for monitoring and analysis.

Designed to simulate real-world API gateway protection mechanisms.

---

# ⚡ Core Security Features

## 🚦 Token Bucket Rate Limiting

Implements a high-performance token bucket algorithm to control traffic flow.

### Capabilities

- Per-IP request limiting
- Burst traffic handling
- Automatic token refill
- Concurrent request protection
- Lightweight in-memory processing

### Behavior

When request limits are exceeded:

- HTTP `429 TOO MANY REQUESTS` is returned
- Abuse score increases
- Request gets logged as `RATE_LIMITED`

---

## 🚫 Intelligent Abuse Detection

The system continuously tracks suspicious traffic behavior.

Repeated rate-limit violations increase an abuse score associated with the client IP.

When the threshold is crossed:

- Client IP is temporarily blocked
- All future requests are denied
- Security events are logged
- `BLOCKED` response is returned

---

## 📊 Real-Time Request Monitoring

Every incoming request is captured and stored for auditing and analytics.

### Logged Information

- IP Address
- Endpoint Accessed
- Request Timestamp
- Request Status
- Abuse Events

### Request Status Types

```text
ALLOWED
RATE_LIMITED
BLOCKED
```

---

# 🧠 System Architecture

```text
Incoming Request
        │
        ▼
┌─────────────────────┐
│ RateLimiterFilter   │
└─────────────────────┘
        │
        ▼
┌─────────────────────┐
│ Token Bucket Check  │
└─────────────────────┘
        │
        ▼
┌─────────────────────┐
│ Abuse Detection     │
└─────────────────────┘
        │
        ▼
┌─────────────────────┐
│ Request Logging     │
└─────────────────────┘
        │
        ▼
   API Response
```

---

# 🏗️ Project Structure

```bash
src/main/java
│
├── config
│
├── controller
│
├── entity
│   └── ApiRequestLog.java
│
├── filter
│   └── RateLimiterFilter.java
│
├── repository
│   └── ApiRequestLogRepository.java
│
├── service
│   └── AbuseDetectionService.java
│
├── util
│   ├── TokenBucket.java
│   └── RateLimiterStore.java
│
└── ApiRateLimiterApplication.java
```

---

# 🛠️ Technology Stack

| Technology | Purpose |
|---|---|
| ☕ Java 21 | Core Language |
| 🚀 Spring Boot | Backend Framework |
| 🌐 Spring Web | REST APIs |
| 🗄️ Spring Data JPA | Database Layer |
| 🐬 MySQL | Persistent Storage |
| 📦 Maven | Dependency Management |
| 🐳 Docker | Containerization |
| 🔐 Jakarta Servlet API | Request Filtering |

---

# 📌 Sample API Responses

## ✅ Allowed Request

```json
{
  "message": "Request successful"
}
```

---

## ⚠️ Rate Limited

```json
{
  "error": "RATE_LIMIT",
  "message": "Too many requests. Token bucket exhausted."
}
```

---

## 🚫 Temporarily Blocked

```json
{
  "error": "BLOCKED",
  "message": "IP temporarily blocked due to suspicious activity"
}
```

---

# ⚙️ Environment Configuration

Create `application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/rate_limiter_db
spring.datasource.username=root
spring.datasource.password=your_password

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

server.port=8081
```

---

# ▶️ Running Locally

## 📥 Clone Repository

```bash
git clone https://github.com/yourusername/api-rate-limiter-backend.git
```

---

## 📂 Navigate to Project

```bash
cd api-rate-limiter-backend
```

---

## 📦 Build Project

```bash
./mvnw clean install
```

---

## ▶️ Start Application

```bash
./mvnw spring-boot:run
```

Backend runs on:

```bash
http://localhost:8081
```

---

# 🐳 Docker Support

## 🔨 Build Docker Image

```bash
docker build -t api-rate-limiter .
```

---

## ▶️ Run Docker Container

```bash
docker run -p 8081:8081 api-rate-limiter
```

---

# 🔐 Security Concepts Demonstrated

- API Rate Limiting
- Traffic Burst Protection
- Abuse Detection
- Temporary IP Blocking
- Concurrent Request Handling
- Request Monitoring
- Defensive API Architecture
- Backend Traffic Governance

---

# 📈 Future Enhancements

- ⚡ Redis Distributed Rate Limiting
- 🌐 API Gateway Integration
- 🔑 JWT Authentication
- 📊 Grafana + Prometheus Metrics
- 🔔 Real-Time WebSocket Monitoring
- ☁️ Kubernetes Deployment
- 🧠 AI-Based Threat Detection
- 📉 Live Traffic Analytics Dashboard
- 🔥 Dynamic Rule Engine
- 🌍 Multi-Node Distributed Protection

---

# 👨‍💻 Author

### Manu SH

Java Backend Developer | React Developer | DevOps Learner

---

# 📜 License

MIT License
