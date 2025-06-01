# Anaphase Microservices Framework

![Kotlin](https://img.shields.io/badge/kotlin-%230095D5.svg?style=for-the-badge&logo=kotlin&logoColor=white)
![Micronaut](https://img.shields.io/badge/micronaut-1E88E5?style=for-the-badge&logo=micronaut&logoColor=white)
![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white)



### 🎓 Judul Proyek & Deskripsi Singkat:
**Anaphase Microservices Framework** - "Rancang Bangun Arsitektur Microservices Berbasis Kotlin yang Adaptif dengan Pendekatan Domain-Driven Design untuk Aplikasi Enterprise."

### 💡 Motivasi/Latar Belakang Masalah:
Pengembangan aplikasi enterprise saat ini menghadapi tantangan dalam hal **skalabilitas** dan **maintainability**. Framework microservices yang ada didominasi oleh Java dengan startup time lambat dan memory usage tinggi. Proyek ini menghadirkan solusi berbasis Kotlin dengan pendekatan Domain-Driven Design untuk menciptakan arsitektur yang adaptif dan performant.

### 🛠️ Teknologi yang Digunakan:
- **Framework**: Kotlin, Micronaut 4.8.2, Gradle Kotlin DSL
- **Database**: PostgreSQL, Redis
- **Service Discovery**: Consul
- **Monitoring**: Prometheus, Jaeger
- **Testing**: JUnit 5, Testcontainers, AssertJ
- **Container**: Docker

### 📥 Instruksi Instalasi & Setup:
*(Penting jika orang lain ingin mencoba)*

#### Prerequisites:
```bash
java --version    # Java 21+
docker --version  # Docker 20.10+
./gradlew --version  # Gradle wrapper included
```

#### Quick Start:
```bash
# 1. Clone repository
git clone https://github.com/Lisvindanu/anaphase-microservices.git
cd anaphase-microservices

# 2. Start infrastructure (optional - services can run standalone)
docker-compose up -d consul postgres redis

# 3. Run Discovery Service (port 8081)
cd services/discovery
./gradlew run

# 4. Run Gateway Service (port 8080) - in new terminal
cd services/gateway  
./gradlew run

# 5. Verify services
curl http://localhost:8080/health    # Gateway health
curl http://localhost:8081/health    # Discovery health
```

### 🏗️ Arsitektur Sistem:
*(Berdasarkan services yang sudah ada)*

```
Client Request → Gateway (8080) → Discovery (8081)
                    ↓                    ↓
            [Authentication]      [Service Registry]
            [Rate Limiting]       [Health Monitoring]  
            [Load Balancing]      [Configuration]
```

**Current Services:**
- **🌐 Gateway Service** (port 8080): API Gateway dengan routing, security, monitoring
- **🔍 Discovery Service** (port 8081): Service registry dan health checking

**Planned Services:**
- **⚙️ Config Service** (port 8082): Centralized configuration management
- **🔐 Identity Service** (port 8083): Authentication & authorization
- **🏪 Domain Services**: Catalog, Orders, Payments (ports 8090+)

### 📱 Screenshot (Preview) Aplikasi:

#### Gateway - Swagger UI
```
http://localhost:8080/swagger-ui
```
![Gateway API](docs/screenshots/gateway-swagger.png)

#### Discovery Service Dashboard
```
http://localhost:8081/swagger-ui
```
![Discovery API](docs/screenshots/discovery-swagger.png)

#### Health Check Endpoints
```bash
# Gateway health
curl http://localhost:8080/health

# Discovery health  
curl http://localhost:8081/health
```

---

## 🚀 Features yang Sudah Implementasi

### ✅ **Gateway Service**
- ⚡ HTTP Server dengan Netty
- 🔐 Security dengan JWT
- 📊 Management endpoints
- 📖 OpenAPI documentation
- 🧪 Comprehensive testing setup

### ✅ **Discovery Service**
- 🔍 Service discovery dengan Consul
- 🗄️ Database integration (PostgreSQL, H2)
- ⚡ Redis caching support
- 📊 Health monitoring
- 🔄 Service registration & lookup

### ✅ **Infrastructure Ready**
- 🐳 Docker setup
- 📁 Organized project structure
- 🛠️ Gradle build configuration
- 📋 Comprehensive documentation structure

## 📂 Project Structure

```
anaphase/
├── services/
│   ├── gateway/          ✅ API Gateway (Ready)
│   └── discovery/        ✅ Service Discovery (Ready)
├── infrastructure/       📁 Config templates
├── shared/              📁 Common libraries (Planned)
├── tools/               📁 Development scripts (Planned)
└── docs/                📁 Documentation
```

## 🧪 Testing

```bash
# Test Gateway service
cd services/gateway
./gradlew test

# Test Discovery service  
cd services/discovery
./gradlew test

# Build all services
./gradlew build  # From root directory
```


### Current Phase: Infrastructure Foundation ✅
- [x] Service Discovery implementation
- [x] API Gateway setup
- [x] Basic security & monitoring
- [x] Project structure & documentation

### Next Phase: Core Services 🔄
- [ ] Configuration management service
- [ ] Identity & authentication service
- [ ] Event bus integration
- [ ] Domain services implementation

## 📞 Contact

**Developer**: [@Lisvindanu](https://github.com/Lisvindanu)  
**Repository**: [anaphase-microservices](https://github.com/Lisvindanu/anaphase-microservices)

---

<div align="center">

**🚀 Built with Kotlin + Micronaut + DDD**

*Currently: Infrastructure Foundation Complete*

[![⭐ Star this project](https://img.shields.io/github/stars/Lisvindanu/anaphase-microservices.svg?style=social)](https://github.com/Lisvindanu/anaphase-microservices)

</div>