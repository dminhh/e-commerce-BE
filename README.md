# E-Commerce Backend - Microservices Architecture

Hệ thống backend E-Commerce sử dụng kiến trúc microservices với Spring Boot, Eureka, API Gateway.

## Tech Stack

- **Java**: 17
- **Framework**: Spring Boot 3.3.4
- **Service Discovery**: Eureka Server
- **API Gateway**: Spring Cloud Gateway
- **Database**: MySQL
- **Cache**: Redis
- **Message Queue**: Kafka + Zookeeper
- **Search Engine**: Elasticsearch
- **Documentation**: Swagger/OpenAPI

## Cấu trúc dự án

```
e-commerce-BE/
├── common/              # Module chứa code dùng chung (configs, utils)
├── eureka-server/       # Service Discovery (port 8761)
├── api-gateway/         # API Gateway (port 8080)
├── identify/            # Authentication Service (port 7071)
├── information/         # User Information Service (port 6066)
├── product/             # Product Service
├── payment/             # Payment Service
├── notification/        # Notification Service
├── logger/              # Logging Service
├── docker-compose.yml   # Docker services config
├── start-services.sh    # Script khởi động tất cả services
└── stop-services.sh     # Script dừng tất cả services
```

## Yêu cầu hệ thống

- Java 17 hoặc cao hơn
- Maven 3.6+
- MySQL 8.0+
- Docker & Docker Compose
- Redis (optional - chạy qua Docker)


### 3. Khởi động Docker Services

Khởi động Kafka, Zookeeper, Elasticsearch:

```bash
docker-compose up -d
```

Kiểm tra services đang chạy:

```bash
docker ps
```

### 4. Build project

```bash
mvn clean install
```

## Chạy ứng dụng

### Cách 1: Sử dụng script tự động (Recommended)

```bash
./start-services.sh
```

Script sẽ tự động khởi động các services theo đúng thứ tự:
1. Eureka Server (port 8761)
2. API Gateway (port 8080)
3. Information Service (port 6066)
4. Identify Service (port 7071)

Để dừng tất cả services:

```bash
./stop-services.sh
```

### Cách 2: Chạy thủ công từng service

**Bước 1: Build common module**
```bash
cd common
mvn clean install
```

**Bước 2: Chạy Eureka Server**
```bash
cd eureka-server
mvn spring-boot:run
```

**Bước 3: Chạy API Gateway** (mở terminal mới)
```bash
cd api-gateway
mvn spring-boot:run
```

**Bước 4: Chạy Information Service** (mở terminal mới)
```bash
cd information
mvn spring-boot:run
```

**Bước 5: Chạy Identify Service** (mở terminal mới)
```bash
cd identify
mvn spring-boot:run
```

## Truy cập ứng dụng

### Dashboards & UI
- **Eureka Dashboard**: http://localhost:8761
- **API Gateway**: http://localhost:8080

### Swagger API Documentation
- **Identify Service**: http://localhost:7071/swagger-ui.html
- **Information Service**: http://localhost:6066/swagger-ui.html

### API Docs (JSON)
- **Identify Service**: http://localhost:7071/v3/api-docs
- **Information Service**: http://localhost:6066/v3/api-docs

## Ports

| Service       | Port |
|---------------|------|
| Eureka        | 8761 |
| API Gateway   | 8080 |
| Identify      | 7071 |
| Information   | 6066 |
| Zookeeper     | 2181 |
| Kafka         | 9092 |
| Elasticsearch | 9200, 9300 |


## Logs

Khi sử dụng `start-services.sh`, logs được lưu tại:
- Eureka: `/tmp/eureka.log`
- API Gateway: `/tmp/api-gateway.log`
- Information: `/tmp/information.log`
- Identify: `/tmp/identify.log`

Xem logs:
```bash
tail -f /tmp/identify.log
```
## Contributors

- Hồ Đức Minh
