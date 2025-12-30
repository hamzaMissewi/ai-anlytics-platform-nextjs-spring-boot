# Quick Start Guide

## 🚀 Get Started in 5 Minutes

### Step 1: Start Infrastructure

```bash
# Start Kafka, PostgreSQL, and Zookeeper
docker-compose up -d
```

### Step 2: Build the Application

```bash
mvn clean package -DskipTests
```

### Step 3: Run the Application

```bash
java -jar target/ai-analytics-platform-1.0.0.jar
```

Or with Maven:

```bash
mvn spring-boot:run
```

### Step 4: Register a User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'
```

### Step 5: Login and Get Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

Save the `token` from the response.

### Step 6: Submit Analytics Event

```bash
curl -X POST http://localhost:8080/api/analytics/events \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{
    "eventType": "content",
    "data": {
      "content": "This is a test message for content moderation",
      "source": "web"
    },
    "userId": "user123",
    "source": "web-application"
  }'
```

### Step 7: Check Results

```bash
curl -X GET http://localhost:8080/api/analytics/results/my \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

## 🔑 Enable AI Features (Optional)

1. Get a free API key from https://huggingface.co
2. Set environment variable:
   ```bash
   export HUGGINGFACE_API_KEY=your_api_key_here
   ```
3. Restart the application

## 📊 Check Health

```bash
curl http://localhost:8080/actuator/health
```

## 🐳 Using Docker Compose (All-in-One)

```bash
# Build and start everything
docker-compose up --build

# Check logs
docker-compose logs -f app

# Stop everything
docker-compose down
```

## 🎯 Next Steps

- Read the full [README.md](README.md) for detailed documentation
- Explore the API endpoints
- Configure Kubernetes deployment
- Set up CI/CD pipeline
