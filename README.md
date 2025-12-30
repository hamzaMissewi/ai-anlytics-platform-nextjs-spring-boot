# AI-Powered Content Moderation & Safety Platform

A comprehensive real-time analytics platform built with Spring Boot, Kafka, AI/ML, Docker, and Kubernetes. This platform provides AI-powered content moderation and safety analysis using free Hugging Face models.

## 🚀 Features

- **AI-Powered Content Analysis**: Uses Hugging Face's free inference API for hate speech detection and content moderation
- **Real-Time Processing**: Kafka-based event streaming for real-time analytics
- **User Authentication**: Secure JWT-based authentication with Spring Security
- **User Management**: Complete user registration and management system with database storage
- **RESTful API**: Comprehensive REST API for analytics and user management
- **Containerized**: Docker and Docker Compose support for easy deployment
- **Kubernetes Ready**: Full K8s manifests for production deployment
- **CI/CD Pipeline**: GitHub Actions workflow for automated build and deployment
- **Health Monitoring**: Spring Boot Actuator for health checks and metrics

## 🛠️ Tech Stack

- **Java 17** - Programming language
- **Spring Boot 3.2.0** - Application framework
- **Spring Security** - Authentication and authorization
- **Spring Kafka** - Event streaming
- **PostgreSQL** - Primary database
- **H2 Database** - Development database
- **Hugging Face API** - Free AI/ML models for content moderation
- **Docker** - Containerization
- **Kubernetes** - Orchestration
- **GitHub Actions** - CI/CD

## 📋 Prerequisites

- Java 17 or higher
- Maven 3.6+
- Docker and Docker Compose
- Kubernetes cluster (for K8s deployment)
- Hugging Face API key (optional, for AI features)

## 🔧 Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd ai-analytics-platform
```

### 2. Configure Environment Variables

Create a `.env` file or set environment variables:

```bash
# Database
DB_URL=jdbc:postgresql://localhost:5432/analyticsdb
DB_USERNAME=analytics
DB_PASSWORD=analytics123

# Kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092

# JWT
JWT_SECRET=YourSecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLong
JWT_EXPIRATION=86400000

# Hugging Face AI (Optional - get free API key from https://huggingface.co)
HUGGINGFACE_API_KEY=your_huggingface_api_key
HUGGINGFACE_API_URL=https://api-inference.huggingface.co/models
AI_MODEL_ENABLED=true
```

### 3. Local Development with Docker Compose

```bash
# Start all services (Kafka, PostgreSQL, Zookeeper)
docker-compose up -d

# Build and run the application
mvn clean package
java -jar target/ai-analytics-platform-1.0.0.jar
```

### 4. Run with Docker

```bash
# Build Docker image
docker build -t ai-analytics-platform .

# Run with docker-compose (includes all dependencies)
docker-compose up
```

### 5. Deploy to Kubernetes

```bash
# Apply all Kubernetes manifests
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/postgres-deployment.yaml
kubectl apply -f k8s/kafka-deployment.yaml
kubectl apply -f k8s/app-deployment.yaml

# Check deployment status
kubectl get pods -n ai-analytics
kubectl get services -n ai-analytics
```

## 📡 API Endpoints

### Authentication

- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login and get JWT token

### Analytics

- `POST /api/analytics/events` - Submit analytics event (requires authentication)
- `GET /api/analytics/results/{eventId}` - Get result by event ID
- `GET /api/analytics/results` - Get all results
- `GET /api/analytics/results/my` - Get current user's results
- `GET /api/analytics/results/prediction/{prediction}` - Get results by prediction
- `GET /api/analytics/health` - Health check

### Health & Monitoring

- `GET /actuator/health` - Application health
- `GET /actuator/metrics` - Application metrics

## 📝 Usage Examples

### 1. Register a User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "securepassword123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### 2. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "password": "securepassword123"
  }'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "john_doe",
  "email": "john@example.com",
  "roles": ["ROLE_USER"]
}
```

### 3. Submit Analytics Event

```bash
curl -X POST http://localhost:8080/api/analytics/events \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "eventType": "content",
    "data": {
      "content": "This is a sample text for content moderation",
      "source": "web"
    },
    "userId": "user123",
    "source": "web-application"
  }'
```

### 4. Get Results

```bash
curl -X GET http://localhost:8080/api/analytics/results/my \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 🤖 AI Model Integration

The platform uses Hugging Face's free inference API for content moderation:

- **Model**: `facebook/roberta-hate-speech-dynabench-r4-target`
- **Purpose**: Hate speech detection and content safety analysis
- **Free Tier**: Available with Hugging Face account

To enable AI features:
1. Create a free account at https://huggingface.co
2. Generate an API token
3. Set `HUGGINGFACE_API_KEY` environment variable
4. Set `AI_MODEL_ENABLED=true`

If AI is disabled, the platform uses fallback logic for content analysis.

## 🗄️ Database Schema

### Users Table
- User authentication and profile information
- Roles: ROLE_USER, ROLE_ADMIN, ROLE_MODERATOR

### Analytics Results Table
- Stores AI analysis results
- Linked to users for personalization

## 🔒 Security Features

- JWT-based authentication
- Password encryption with BCrypt
- Role-based access control (RBAC)
- CORS configuration
- Secure API endpoints

## 📊 Monitoring

- Spring Boot Actuator endpoints
- Health checks for all services
- Metrics collection
- Kubernetes liveness and readiness probes

## 🚀 CI/CD Pipeline

GitHub Actions workflow automatically:
1. Builds the application
2. Runs tests
3. Creates Docker image
4. Pushes to container registry
5. Deploys to Kubernetes (on main branch)

## 📦 Project Structure

```
ai-analytics-platform/
├── src/
│   ├── main/
│   │   ├── java/com/analytics/platform/
│   │   │   ├── controller/     # REST controllers
│   │   │   ├── service/        # Business logic
│   │   │   ├── repository/     # Data access
│   │   │   ├── entity/         # JPA entities
│   │   │   ├── model/          # DTOs and models
│   │   │   ├── security/       # Security configuration
│   │   │   ├── kafka/          # Kafka producers/consumers
│   │   │   └── dto/            # Data transfer objects
│   │   └── resources/
│   │       └── application.yml
│   └── test/
├── k8s/                        # Kubernetes manifests
├── .github/workflows/          # CI/CD pipelines
├── Dockerfile
├── docker-compose.yml
└── pom.xml
```

## 🧪 Testing

```bash
# Run all tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## 📄 License

This project is licensed under the MIT License.

## 🙏 Acknowledgments

- Hugging Face for free AI model APIs
- Spring Boot community
- Apache Kafka project

## 📞 Support

For issues and questions, please open an issue on GitHub.

---

**Built with ❤️ using Spring Boot, Kafka, AI, Docker, and Kubernetes**
