# 📚 TrendyThread Blog Application - Backend (Spring Boot)

A comprehensive RESTful API for a full-featured blog application built with Spring Boot, Spring Security, and JWT authentication.

## 🌟 Features

### Authentication & Authorization
- 🔐 JWT-based authentication with Bearer tokens
- 🔒 Secure password encryption using BCrypt
- 👤 User registration and login
- 🛡️ Role-based access control
- ⏰ Token expiration and refresh capabilities

### Blog Management
- ✏️ **Posts**: Create, read, update, and delete blog posts
- 📂 **Categories**: Organize posts by categories
- 💬 **Comments**: Add and manage comments on posts
- 👥 **Bloggers**: User profile management
- 🔍 **Search**: Search posts by keywords
- 📊 **Pagination**: Efficient data retrieval with pagination and sorting

### Security Features
- ✅ Ownership validation (users can only edit/delete their own content)
- 🚫 FORBIDDEN responses for unauthorized access attempts
- 🔓 Public READ access for posts and comments (no authentication required)
- 🔐 Protected CREATE/UPDATE/DELETE operations (authentication required)

### Data Auditing
- 📝 Automatic tracking of creation and update timestamps
- 👤 Automatic tracking of who created and updated records
- 🕒 BaseEntity with `createdAt`, `updatedAt`, `createdBy`, `updatedBy`

## 🛠️ Technology Stack

- **Framework**: Spring Boot 3.x
- **Security**: Spring Security 6.x with JWT
- **Database**: MySQL (JPA/Hibernate)
- **Caching**: Redis (Redis Stack)
- **Build Tool**: Gradle
- **Java Version**: 17+
- **Documentation**: Swagger/OpenAPI 3.0
- **Validation**: Jakarta Bean Validation
- **Logging**: SLF4J with Logback

## 📋 Prerequisites

- Java JDK 17 or higher ☕
- Gradle 8.14+ 🛠️ (or use the bundled `./gradlew`)
- MySQL 8.0+ 🗃️
- Docker (to run Redis Stack) 🐳
- Git 📦

## 🚀 Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/harshil-padasala/TrendyThread.git
cd trendy-thread
```

### 2. Configure Database

Update `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/trendy_thread_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

### 3. Start Redis Stack (Required)

The application uses Redis for caching (see `RedisConfig` and `IRedisConstant`), and **won't start** unless a Redis instance is reachable. Run Redis Stack via Docker:

```bash
docker run -d --name redis-stack -p 6379:6379 -p 8001:8001 redis/redis-stack:latest
```

- Redis server: `localhost:6379` (Spring Boot's default connection settings)
- RedisInsight UI (optional, for inspecting cached data): `http://localhost:8001`

If you're overriding the defaults, set the connection in `application.properties`/`application.yml`:

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

To stop the container later:

```bash
docker stop redis-stack && docker rm redis-stack
```

### 4. Build the Project

```bash
./gradlew clean build
```

### 5. Run the Application

```bash
./gradlew bootRun
```

The application will start on `http://localhost:8080`

### 6. Access API Documentation

Open your browser and navigate to:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- API Docs: `http://localhost:8080/v3/api-docs`

## 🐳 Running with Docker

The project uses [Jib](https://github.com/GoogleContainerTools/jib) to build the container image directly from Gradle — no Dockerfile needed.

### 1. Build the Image

```bash
./gradlew jibDockerBuild
```

This builds `trendythread:latest` using your local container daemon. The plugin is preconfigured to shell out to `podman` — if you use Docker instead, override the executable:

```bash
./gradlew jibDockerBuild -Djib.dockerClient.executable=docker
```

### 2. Run Redis Stack and the App on a Shared Network

The app container needs to reach Redis by hostname, so put both containers on the same Docker network:

```bash
docker network create trendythread-net

docker run -d --name redis-stack --network trendythread-net \
  -p 6379:6379 -p 8001:8001 redis/redis-stack:latest

docker run -d --name trendythread --network trendythread-net \
  -p 8080:8080 \
  -e SPRING_DATA_REDIS_HOST=redis-stack \
  -e SPRING_DATA_REDIS_PORT=6379 \
  -e LOGGING_FILE_NAME=/logs/trendy-thread.log \
  -v trendythread-logs:/logs \
  trendythread:latest
```

Adjust/add `-e SPRING_DATASOURCE_URL=...`, `-e SPRING_DATASOURCE_USERNAME=...`, `-e SPRING_DATASOURCE_PASSWORD=...` if your database isn't reachable via the defaults in `application.properties`.

The image ships a `/logs` directory writable by the container's non-root user, so `LOGGING_FILE_NAME` works out of the box. The `-v trendythread-logs:/logs` volume keeps `trendy-thread.log` around across container restarts; drop it if you're fine with logs disappearing on recreate (they still stream to `docker logs -f trendythread` either way).

### 3. Verify

```bash
docker logs -f trendythread
```

The app should be reachable at `http://localhost:8080` once it's up. Stop both containers with:

```bash
docker stop trendythread redis-stack && docker rm trendythread redis-stack
```

## 🛣️ API Endpoints

### Authentication (`/api/v1/auth`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| POST | `/auth/signup` | Register a new user | ❌ |
| POST | `/auth/login` | Login and get JWT token | ❌ |
| POST | `/auth/logout` | Logout current user | ✅ |

**Signup Request:**
```json
{
  "userName": "johndoe",
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "SecurePass123"
}
```

**Login Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer",
  "expiresIn": 3600,
  "username": "john@example.com",
  "userId": 1,
  "name": "John Doe",
  "roles": ["ROLE_USER"]
}
```

### Posts (`/api/v1/posts`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/posts` | Get all posts (paginated) | ❌ |
| GET | `/posts/latest?limit=6` | Get latest posts | ❌ |
| GET | `/posts/{postId}` | Get post by ID | ❌ |
| GET | `/posts/category/{categoryId}` | Get posts by category | ❌ |
| GET | `/posts/user/{userId}` | Get posts by user ID | ❌ |
| GET | `/posts/blogger` | Get authenticated user's posts | ✅ |
| GET | `/posts/search/{keyword}` | Search posts | ❌ |
| POST | `/posts/category/{categoryId}` | Create a new post | ✅ |
| PUT | `/posts/{postId}` | Update post (owner only) | ✅ |
| DELETE | `/posts/{postId}` | Delete post | ✅ |

**Create Post Request:**
```json
{
  "title": "My Awesome Blog Post",
  "description": "A brief description of the post",
  "content": "Full content of the blog post..."
}
```

### Comments (`/api/v1/posts/{postId}/comments`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/posts/{postId}/comments` | Get all comments for a post | ❌ |
| POST | `/posts/{postId}/comments` | Add a comment | ✅ |
| PUT | `/posts/{postId}/comments/{id}` | Update comment (owner only) | ✅ |
| DELETE | `/posts/{postId}/comments/{id}` | Delete comment | ✅ |

**Create Comment Request:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "content": "Great post! Very informative."
}
```

### Categories (`/api/v1/category`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/category` | Get all categories | ❌ |
| GET | `/category/{id}` | Get category by ID | ❌ |
| POST | `/category` | Create a new category | ✅ |
| PUT | `/category/{id}` | Update category | ✅ |
| DELETE | `/category/{id}` | Delete category | ✅ |

### Users (`/api/v1/users`)

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/users` | Get all users | ✅ |
| GET | `/users/{userId}` | Get user by ID | ❌ |
| PUT | `/users/{userId}` | Update user profile | ✅ |
| DELETE | `/users/{userId}` | Delete user | ✅ |

## 🔐 Authentication

Include the JWT token in the Authorization header for protected endpoints:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

## 🏗️ Project Structure

```
src/main/java/com/trendythread/app/
├── audit/              # Auditing configuration (AuditAware)
├── config/             # Configuration classes
│   ├── security/       # Security & JWT configuration
│   └── logging/        # Logging configuration
├── constants/          # Application constants
├── controllers/        # REST controllers
├── dto/                # Data Transfer Objects
├── entities/           # JPA entities
├── exceptions/         # Custom exceptions & handlers
├── filter/             # JWT authentication filters
├── payloads/           # Response payloads
├── repositories/       # JPA repositories
├── services/           # Business logic
│   └── impl/           # Service implementations
└── util/               # Utility classes
```

## 🔒 Security Configuration

### Public Endpoints (No Authentication Required)
- Authentication endpoints (`/api/v1/auth/**`)
- GET requests for posts (`/api/v1/posts/**`)
- GET requests for comments
- GET requests for user profiles

### Protected Endpoints (Authentication Required)
- Creating posts, comments, categories
- Updating/deleting content
- Accessing personal data

### Ownership Validation
- Users can only **edit** or **delete** their own posts
- Users can only **edit** or **delete** their own comments
- Returns `403 FORBIDDEN` when attempting to modify others' content

## 📊 Database Schema

### Key Entities
- **Blogger**: User information and authentication
- **Post**: Blog posts with title, content, description
- **Comment**: Comments on posts
- **Category**: Post categories
- **BaseEntity**: Abstract entity with audit fields

### Relationships
- Blogger → Posts (One-to-Many)
- Category → Posts (One-to-Many)
- Post → Comments (One-to-Many)
- Blogger → Comments (One-to-Many)

## 🧪 Testing

Run tests with:
```bash
./gradlew test
```

## 📝 Sample Data

SQL files are provided for populating sample data:
- `bloggers_rows.sql` - Sample users
- `category_rows.sql` - Sample categories
- `post_rows.sql` - Sample posts
- `comment_rows.sql` - Sample comments

## 🐛 Error Handling

The API uses standard HTTP status codes:
- `200 OK` - Successful GET/PUT
- `201 Created` - Successful POST
- `400 Bad Request` - Validation errors
- `401 Unauthorized` - Missing or invalid token
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - Server error

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is open source and available under the MIT License.

## 👨‍💻 Author

**Harshil Padasala**
- GitHub: [@harshil-padasala](https://github.com/harshil-padasala)

## 🙏 Acknowledgments

- Spring Boot team for the amazing framework
- Spring Security for robust authentication
- All contributors and users of this project

---

**Happy Coding! 🚀**
