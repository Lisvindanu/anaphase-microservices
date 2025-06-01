# 🚀 Anaphase Gateway

![Kotlin](https://img.shields.io/badge/kotlin-%230095D5.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Micronaut](https://img.shields.io/badge/micronaut-1E88E5?style=for-the-badge&logo=micronaut&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)

**API Gateway dengan Request Routing & Load Balancing** - Bagian dari Anaphase Microservices Framework

## 📋 Overview

Anaphase Gateway adalah API Gateway yang dibangun dengan **Kotlin + Micronaut** yang menyediakan request routing, load balancing, dan service discovery untuk arsitektur microservices. Gateway ini dirancang dengan pendekatan **Domain-Driven Design** dan **Event-Driven Architecture**.

### 🎯 **Phase 1 - Request Routing & Load Balancing** ✅ **COMPLETE**

- ✅ **Dynamic Request Routing** - Route berdasarkan path patterns
- ✅ **Multiple Load Balancing Strategies** - Round Robin, Weighted, Random
- ✅ **Service Discovery Integration** - Manual registry dengan pre-configured instances
- ✅ **Configuration-Driven Routes** - YAML-based route definitions
- ✅ **Health-Aware Routing** - Otomatis filter unhealthy instances
- ✅ **Debug & Monitoring Endpoints** - Real-time route dan service status

## 🏗️ Arsitektur

```
Client Request
      ↓
┌─────────────────────┐
│   Gateway Controller │ ← HTTP endpoints (/gateway/**)
└─────────────────────┘
      ↓
┌─────────────────────┐     ┌─────────────────────┐
│    Gateway Router   │ ←→  │   Service Registry  │
└─────────────────────┘     └─────────────────────┘
      ↓                              ↓
┌─────────────────────┐     ┌─────────────────────┐
│   Load Balancer     │     │ Service Instances   │
└─────────────────────┘     └─────────────────────┘
      ↓
┌─────────────────────┐
│   Target Service    │
└─────────────────────┘
```

## 🚀 Quick Start

### Prerequisites

```bash
# Check Java version
java --version  # Java 21+

# Check Gradle
./gradlew --version  # 8.13+

# Check directory structure
cd services/gateway
```

### Installation & Setup

```bash
# 1. Clone repository
git clone https://github.com/Lisvindanu/anaphase-microservices.git
cd anaphase-microservices/services/gateway

# 2. Build project
./gradlew clean build

# 3. Run Gateway
./gradlew run
```

**Gateway akan berjalan di:** `http://localhost:8080`

## 📁 Project Structure

```
services/gateway/src/main/kotlin/org/anaphase/gateway/
├── routing/
│   ├── ServiceInstance.kt          # Data model service instances
│   ├── LoadBalancingStrategy.kt    # Load balancing strategies  
│   └── RouteDefinition.kt          # Route configuration model
├── registry/
│   ├── ServiceRegistry.kt          # Service discovery interface
│   └── ManualServiceRegistry.kt    # Manual service registry impl
├── loadbalancer/
│   ├── LoadBalancer.kt             # Load balancer interface
│   ├── RoundRobinLoadBalancer.kt   # Round robin implementation
│   ├── WeightedRoundRobinLoadBalancer.kt
│   ├── RandomLoadBalancer.kt
│   └── LoadBalancerFactory.kt      # Factory pattern
├── router/
│   ├── GatewayRouter.kt            # Main routing logic
│   ├── GatewayRoutingController.kt # HTTP controllers
│   └── GatewayRoutesConfiguration.kt # YAML config binding
├── debug/
│   └── DebugController.kt          # Debug & monitoring endpoints
├── Application.kt                  # Main application
└── GatewayController.kt           # Basic endpoints
```

## ⚙️ Configuration

### Route Configuration (`application.yml`)

```yaml
gateway:
  routes:
    routes:
      - id: "discovery-route"
        path: "/gateway/api/discovery/**"
        serviceName: "discovery"
        stripPrefix: true
        retryAttempts: 3
        timeoutMs: 30000
        loadBalancingStrategy: "ROUND_ROBIN"
      
      - id: "catalog-route"
        path: "/gateway/catalog/**"
        serviceName: "catalog" 
        stripPrefix: true
        retryAttempts: 2
        timeoutMs: 20000
        loadBalancingStrategy: "WEIGHTED_ROUND_ROBIN"
```

### Service Registry

Gateway menggunakan **Manual Service Registry** dengan pre-configured instances:

- **Discovery Service**: `localhost:8081`
- **Catalog Service**: `localhost:8090`

## 🧪 Testing & Usage

### Basic Endpoints

```bash
# Gateway status
curl http://localhost:8080/status

# Gateway index
curl http://localhost:8080/

# Health check
curl http://localhost:8080/health
```

### Debug Endpoints

```bash
# Check configured routes
curl http://localhost:8080/debug/routes

# Check registered services  
curl http://localhost:8080/debug/services

# Check dependency injection status
curl http://localhost:8080/debug/startup

# Test discovery service routing
curl http://localhost:8080/debug/test/discovery
```

### Request Routing

```bash
# Route to discovery service (localhost:8081)
curl http://localhost:8080/gateway/discovery/
curl http://localhost:8080/gateway/api/discovery/

# Route to catalog service (localhost:8090)  
curl http://localhost:8080/gateway/catalog/
```

**Expected Responses:**

- ✅ **Service Running**: Successful routing dengan response dari target service
- ❌ **Service Down**: `"Request forwarding failed: Connection refused: localhost:8081"`
- ❌ **Route Not Found**: `"Route not found: /gateway/unknown/path"`

## 📊 Load Balancing Strategies

### 1. Round Robin (`ROUND_ROBIN`)
Distribusi request secara merata ke semua instances.

### 2. Weighted Round Robin (`WEIGHTED_ROUND_ROBIN`)
Distribusi berdasarkan weight yang dikonfigurasi per instance.

### 3. Random (`RANDOM`)
Pemilihan instance secara random.

### 4. Least Connections (`LEAST_CONNECTIONS`)
*Coming in Phase 2* - Pemilihan instance dengan koneksi paling sedikit.

## 🎛️ Monitoring & Observability

### Prometheus Metrics
Gateway menyediakan metrics di: `http://localhost:8080/prometheus`

### Swagger UI Documentation
API documentation tersedia di: `http://localhost:8080/swagger-ui`

### Debug Dashboard
Real-time monitoring: `http://localhost:8080/debug/startup`

## 🧩 Integration dengan Services Lain

### Discovery Service Integration

```bash
# Start Discovery Service (Terminal 1)
cd services/discovery
./gradlew run  # Runs on port 8081

# Test routing (Terminal 2)
curl http://localhost:8080/gateway/discovery/
# Should return successful response from discovery service
```

### Adding New Services

1. **Register Service Instance** (programmatically):
```kotlin
serviceRegistry.registerInstance(
    ServiceInstance(
        id = "new-service-1",
        name = "new-service", 
        host = "localhost",
        port = 9000
    )
)
```

2. **Add Route Configuration**:
```yaml
gateway:
  routes:
    routes:
      - id: "new-service-route"
        path: "/gateway/newservice/**"
        serviceName: "new-service"
        stripPrefix: true
        loadBalancingStrategy: "ROUND_ROBIN"
```

## 🔧 Development

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test
./gradlew test --tests="*GatewayIntegrationTest*"

# Test results
open build/reports/tests/test/index.html
```

### Building for Production

```bash
# Build JAR
./gradlew shadowJar

# Build native image (GraalVM)
./gradlew nativeCompile

# Build Docker image
./gradlew dockerBuild
```

## 🎯 Roadmap

### ✅ Phase 1 - Request Routing & Load Balancing (COMPLETE)
- [x] Dynamic request routing dengan pattern matching
- [x] Multiple load balancing strategies
- [x] Service discovery integration
- [x] Configuration-driven routes
- [x] Health-aware routing
- [x] Debug & monitoring endpoints

### 🔄 Phase 2 - Rate Limiting & Security (NEXT)
- [ ] Rate limiting per client/IP
- [ ] JWT authentication & validation
- [ ] Role-based access control (RBAC)
- [ ] API key management
- [ ] Request throttling

### 🔮 Phase 3 - Advanced Features (PLANNED)
- [ ] Circuit breaker pattern
- [ ] Request/response transformation
- [ ] Caching layer
- [ ] WebSocket support
- [ ] gRPC routing
- [ ] Service mesh integration

### 🌟 Phase 4 - Observability & Operations (PLANNED)
- [ ] Distributed tracing dengan Jaeger
- [ ] Advanced metrics & alerting
- [ ] Log aggregation
- [ ] Real-time dashboard
- [ ] Performance analytics

## 🤝 Contributing

Proyek ini adalah bagian dari tugas akhir **"Rancang Bangun Arsitektur Microservices Berbasis Kotlin yang Adaptif dengan Pendekatan Domain-Driven Design"**.

### Development Guidelines

1. **Follow DDD principles** - Bounded contexts dan ubiquitous language
2. **Maintain test coverage** - Unit tests untuk semua core functionality
3. **Document changes** - Update README untuk fitur baru
4. **Use conventional commits** - Format: `feat(routing): add circuit breaker pattern`

## 📞 Support & Contact

- **Developer**: [@Lisvindanu](https://github.com/Lisvindanu)
- **Repository**: [anaphase-microservices](https://github.com/Lisvindanu/anaphase-microservices)
- **Documentation**: [Wiki](https://github.com/Lisvindanu/anaphase-microservices/wiki)

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

<div align="center">

**🚀 Built with Kotlin + Micronaut + Domain-Driven Design**

*Phase 1: Request Routing & Load Balancing - Production Ready*

[![⭐ Star this project](https://img.shields.io/github/stars/Lisvindanu/anaphase-microservices.svg?style=social)](https://github.com/Lisvindanu/anaphase-microservices)

</div>