# Facebook Clone

A social media application with features similar to Facebook, built using microservices architecture.

## Technology Stack

### Backend
- Java Spring Boot 3.2.0
- Spring Security with JWT for authentication
- Hibernate/JPA for ORM
- PostgreSQL for database
- RESTful APIs for communication between services

### Frontend
- Angular 15
- TypeScript
- Bootstrap 5 for styling
- RxJS for reactive programming

## Features

- **User Authentication**: Register, login, JWT-based authentication
- **Post Management**: Create, edit, delete posts with tags
- **Comment System**: Add, edit, and delete comments on posts
- **Voting System**: Like/dislike posts and comments with scoring
- **User Scoring**: Users earn/lose points based on interactions
- **Moderator Capabilities**: Content moderation, user banning

## Microservices Architecture

The application is built with a microservices architecture, consisting of:

1. **Auth Service** (port 8081) - Handles user authentication and authorization
2. **User Service** (port 8082) - Manages user profiles and scoring
3. **Post Service** (port 8083) - Handles posts, tags, and filtering
4. **Comment Service** (port 8084) - Manages comments on posts
5. **Reaction Service** (port 8085) - Handles votes/likes/dislikes (not fully implemented)

## Setup Instructions

### Prerequisites
- Java 17+
- Node.js 16+
- Angular CLI
- Maven
- Either:
  - PostgreSQL 13+ (local installation)
  - Docker (for containerized PostgreSQL)

### Quick Start

1. Clone the repository:
```bash
git clone https://github.com/yourusername/facebook-clone.git
cd facebook-clone
```

2. Make the scripts executable:
```bash
chmod +x *.sh
```

3. Set up the database (choose one option):

   **Option A: Using Docker (recommended):**
   ```bash
   ./docker-postgres-start.sh
   ```

   **Option B: Using local PostgreSQL:**
   ```bash
   ./setup-database.sh
   ```

4. Build and start all services:
```bash
./start-services.sh
```

5. Access the application at http://localhost:4200

### Database Setup

#### Using Docker (Recommended)

If you encounter issues with your local PostgreSQL installation, you can use Docker instead:

1. Ensure Docker is installed and running
2. Run the Docker PostgreSQL setup script:
   ```bash
   ./docker-postgres-start.sh
   ```

This will start a PostgreSQL container with all required databases pre-configured.

#### Using Local PostgreSQL

The `setup-database.sh` script automatically creates the required PostgreSQL databases:
- facebook_auth
- facebook_user
- facebook_post
- facebook_comment
- facebook_reaction

If you prefer to create them manually, you can use:
```sql
CREATE DATABASE facebook_auth;
CREATE DATABASE facebook_user;
CREATE DATABASE facebook_post;
CREATE DATABASE facebook_comment;
CREATE DATABASE facebook_reaction;
```

### Backend Setup

1. Build all services:
```bash
cd backend
mvn clean install
```

2. Start all services using the provided script:
```bash
./start-services.sh
```

This script will start all microservices in the background and redirect their output to log files in the `logs` directory.

### Managing Services

The application comes with useful service management scripts:

- **docker-postgres-start.sh**: Starts PostgreSQL in a Docker container
- **setup-database.sh**: Creates the required PostgreSQL databases (local installation)
- **start-services.sh**: Starts all microservices and the Angular frontend
- **stop-services.sh**: Gracefully stops all running services
- **status-services.sh**: Checks the status of all services and databases

### Accessing the Application

Once all services are running, access the application at `http://localhost:4200`

## API Documentation

Each microservice exposes its own set of RESTful APIs:

- Auth Service: `http://localhost:8081/api/auth`
- User Service: `http://localhost:8082/api/users`
- Post Service: `http://localhost:8083/api/posts`
- Comment Service: `http://localhost:8084/api/comments`

## Default Users

The system automatically creates the following roles on startup:
- ROLE_USER
- ROLE_MODERATOR
- ROLE_ADMIN

You can register new users through the UI or via API.

## Development

### Adding New Features

1. Backend: Add new endpoints in the appropriate microservice
2. Frontend: Implement new components and services
3. Update routing as needed

### Testing

Each service includes unit tests for controllers and services:

```bash
cd backend
mvn test
```

## Logs

Service logs are stored in the `logs` directory:
- auth-service.log
- user-service.log
- post-service.log
- comment-service.log
- angular.log

You can monitor logs using:
```bash
tail -f logs/auth-service.log
```
