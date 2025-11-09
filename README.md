# JavaFest MCP Internals, Security and Cloud Deployments

A comprehensive tutorial series demonstrating how to build production-ready Model Context Protocol (MCP) servers with Spring Boot, covering everything from basic implementation to advanced security and cloud deployment.

## 📚 Overview

This repository contains a progressive, hands-on workshop on building MCP (Model Context Protocol) servers using Spring Boot and Spring AI. Through four chapters, you'll learn how to create a Todo application that evolves from a simple MCP server to a production-ready, secure, cloud-deployable application with OAuth2 authentication.

## 🎯 What You'll Learn

- **MCP Server Development**: Build servers supporting multiple protocols (STDIO, SSE, Stateless)
- **Firebase Authentication**: Implement user-specific data access with JWT validation
- **OAuth2 Integration**: Add OAuth2 resource server capabilities with RFC-compliant metadata endpoints
- **Production Deployment**: Deploy to AWS Fargate, Google Cloud Run, and Azure Container Apps
- **Security Best Practices**: Custom authentication entry points, standardized error responses
- **Spring AI Integration**: Leverage Spring AI's MCP annotations and tooling

## 🏗️ Repository Structure

```
javafest-mcp-internals-security-cd/
├── chapter-1/          # Basic MCP Server with Multi-Protocol Support
├── chapter-2/          # Firebase Authentication & User Context
├── chapter-3/          # OAuth2 Resource Metadata & Enhanced Security
├── chapter-4/          # Production Deployment & Cloud Configuration
└── deck/              # Presentation materials
```

## 📖 Chapter Overview

### Chapter 1: Building a Multi-Protocol MCP Server
**Focus**: Foundation & Multi-Protocol Support

**What's Covered**:
- Spring Boot project setup with Spring AI MCP dependencies
- H2 in-memory database configuration
- Todo CRUD operations with MCP tools (`@McpTool` annotations)
- Multiple communication protocols:
  - **STDIO**: Standard input/output for command-line integration
  - **SSE**: Server-Sent Events for real-time web communication
  - **Stateless**: HTTP-based stateless communication
- Profile-based configuration management

**Key Technologies**:
- Spring Boot 3.5.7
- Spring AI MCP 1.1.0-M4
- Java 21
- H2 Database
- Lombok

**Run Instructions**:
```bash
cd chapter-1
./gradlew bootRun --args='--spring.profiles.active=sse'
# Or use Taskfile
task dev:sse
```

**Learn More**: [chapter-1/README.md](chapter-1/README.md)

---

### Chapter 2: Adding Firebase Authentication
**Focus**: User Authentication & Authorization

**What's Covered**:
- Firebase project setup and configuration
- Multiple authentication providers (Email/Password, Google, GitHub)
- JWT token validation with Firebase Admin SDK
- User-specific Todo management
- Custom JWT decoder implementation
- Security configuration with Spring Security

**Key Technologies**:
- Spring Security
- Firebase Admin SDK 9.4.1
- OAuth2 Resource Server
- Nimbus JOSE JWT 9.39.1

**New Features**:
- User-scoped data access
- JWT-based authentication
- MCPContextHolder for user context management
- Firebase authentication integration

**Setup Requirements**:
1. Create Firebase project
2. Configure authentication providers
3. Download service account key
4. Update `application.properties` with Firebase credentials

**Learn More**: [chapter-2/README.md](chapter-2/README.md)

---

### Chapter 3: OAuth2 Resource Metadata & Enhanced Security
**Focus**: OAuth2 Standards & MCP Security Framework

**What's Covered**:
- Custom authentication entry point with RFC 6750/8707 compliance
- OAuth2 Protected Resource Metadata endpoints
- MCP Server Security framework integration
- WWW-Authenticate headers for proper authentication challenges
- Standardized error responses
- Integration with MCP Firebase Auth Proxy

**Key Technologies**:
- MCP Server Security 0.0.3
- OAuth2 Authorization Server Metadata (RFC 8414)
- OAuth2 Protected Resource Metadata (RFC 8707)

**New Endpoints**:
- `/.well-known/oauth-protected-resource`: Resource metadata discovery
- Custom authentication entry points
- Enhanced error handling

**Architecture**:
```
MCP Client → Your MCP Server (:8080) → Firebase Auth Proxy (:9000) → Firebase Authentication
```

**Learn More**: [chapter-3/README.md](chapter-3/README.md)

---

### Chapter 4: Production Deployment & Cloud Configuration
**Focus**: Production Readiness & Multi-Cloud Deployment

**What's Covered**:
- OAuth2 resource metadata controller implementation
- OpenID Connect discovery endpoints
- Production health monitoring and metrics
- Docker containerization with multi-stage builds
- Cloud deployment configurations:
  - **AWS Fargate**: ECS task definitions
  - **Google Cloud Run**: Serverless deployment
  - **Azure Container Apps**: Container app configuration
- Environment-based configuration management
- Resource discovery for automatic client configuration

**Key Technologies**:
- Docker
- Docker Compose
- AWS ECS/Fargate
- Google Cloud Run
- Azure Container Apps
- Alpine Linux (production runtime)

**Production Features**:
- Multi-stage Docker builds for minimal image size
- Health checks and readiness probes
- Container-optimized JVM settings
- Environment variable configuration
- Logging and monitoring setup

**Deployment Configurations**:
- `config/fargateconfig.yaml`: AWS Fargate task definition
- `config/cloudrunconfig.yaml`: Google Cloud Run service
- `config/azurecontainerappsconfig.yaml`: Azure Container Apps
- `Dockerfile`: Production-ready container image
- `docker-compose.yml`: Local development environment

**Run Locally with Docker**:
```bash
cd chapter-4
docker-compose up --build
```

**Learn More**: [chapter-4/README.md](chapter-4/README.md)

---

## 🚀 Quick Start

### Prerequisites
- Java 21 or higher
- Gradle (included via wrapper)
- Docker (for Chapter 4)
- Firebase account (for Chapters 2-4)
- Task (optional, for using Taskfile)

### Running Each Chapter

#### Chapter 1 - Basic MCP Server
```bash
cd chapter-1
# SSE mode (web-based)
./gradlew bootRun --args='--spring.profiles.active=sse'
# Or use Task
task dev:sse
```

#### Chapter 2 - With Firebase Auth
```bash
cd chapter-2
# 1. Configure Firebase credentials in application.properties
# 2. Run with SSE profile
./gradlew bootRun --args='--spring.profiles.active=sse'
```

#### Chapter 3 - With OAuth2 Resource Metadata
```bash
cd chapter-3
# Configure OAuth2 settings
./gradlew bootRun --args='--spring.profiles.active=sse'
```

#### Chapter 4 - Production Deployment
```bash
cd chapter-4
# Local with Docker
docker-compose up --build

# Or with Gradle
./gradlew bootRun --args='--spring.profiles.active=stateless'
```

## 🔧 Technology Stack

### Core Framework
- **Spring Boot 3.5.7**: Application framework
- **Spring AI 1.1.0-M4**: MCP server capabilities
- **Java 21**: Programming language

### MCP & Annotations
- `spring-ai-starter-mcp-server`: STDIO protocol support
- `spring-ai-starter-mcp-server-webmvc`: SSE/HTTP protocol support
- `spring-ai-mcp-annotations`: MCP tool annotations

### Security
- **Spring Security**: Authentication and authorization
- **Firebase Admin SDK**: Firebase integration
- **OAuth2 Resource Server**: JWT validation
- **MCP Server Security**: MCP-specific security framework

### Database
- **H2**: In-memory database
- **Spring Data JPA**: Data access layer

### Utilities
- **Lombok**: Code generation
- **Jakarta Validation**: Input validation

### DevOps
- **Docker**: Containerization
- **Gradle**: Build automation
- **Task**: Task runner (optional)

## 📝 MCP Tools Implemented

Each chapter implements the following MCP tools using `@McpTool` annotations:

- **`fetch-all-todos`**: Get all todo items (user-scoped in Chapters 2-4)
- **`fetch-todo-by-id`**: Get a specific todo by ID
- **`make-todo`**: Create a new todo item
- **`change-todo`**: Update an existing todo
- **`remove-todo`**: Delete a todo by ID

## 🔐 Security Features

### Chapter 2+
- JWT-based authentication
- Firebase token validation
- User-specific data isolation

### Chapter 3+
- Custom authentication entry points
- RFC-compliant WWW-Authenticate headers
- OAuth2 resource metadata endpoints
- Standardized error responses

### Chapter 4
- Production security configuration
- Environment-based secrets management
- Container security best practices
- Health check endpoints

## 🌐 Deployment Options

### Local Development
- STDIO mode for command-line integration
- SSE mode for web-based development (port 8080)
- Docker Compose for containerized local development

### Cloud Platforms

#### AWS Fargate
- ECS task definition included
- Container registry: AWS ECR
- Network: VPC with public subnets
- Load balancer: Application Load Balancer (optional)

#### Google Cloud Run
- Fully managed serverless
- Auto-scaling configuration
- Cloud Build integration
- IAM-based access control

#### Azure Container Apps
- Container Apps environment
- Ingress configuration
- Auto-scaling rules
- Azure AD integration support

## 📊 Configuration Profiles

Each chapter supports multiple Spring profiles:

- **`stdio`**: Standard input/output (no web server)
- **`sse`**: Server-Sent Events (port 8080)
- **`stateless`**: HTTP stateless communication
- **`streamable`**: Streamable responses (Chapter 1)

## 🧪 Testing

```bash
# Run tests for any chapter
cd chapter-X
./gradlew test

# Build without tests
./gradlew build -x test
```

## 📚 Additional Resources

### Documentation
- Each chapter has detailed README with step-by-step instructions
- `docs/` folder in each chapter contains supplementary documentation
- JavaFest presentation deck: `deck/javafest_deck.pdf`

### Starter Repositories
- [Chapter 1 Starter](https://github.com/Shaamam/javafest-mcp-internals-security-cd-chapter-1)
- [Chapter 2 Starter](https://github.com/Shaamam/javafest-mcp-internals-security-cd-chapter-2)
- [Chapter 3 Starter](https://github.com/Shaamam/javafest-mcp-internals-security-cd-chapter-3)
- [Chapter 4 Starter](https://github.com/Shaamam/javafest-mcp-internals-security-cd-chapter-4)

### External Resources
- [Spring AI Documentation](https://docs.spring.io/spring-ai/reference/)
- [Model Context Protocol Specification](https://modelcontextprotocol.io/)
- [Firebase Console](https://console.firebase.google.com/)
- [Firebase JWT Testing Tool](https://firebasejwt.muthuishere.site/)
- [MCP Firebase Auth Proxy](https://mcpfirebaseauthserver.muthuishere.site/)

## 🤝 Workshop Structure

This repository is designed for a hands-on workshop with the following progression:

1. **Foundation (30 mins)**: Chapter 1 - Understanding MCP and basic implementation
2. **Authentication (45 mins)**: Chapter 2 - Adding Firebase authentication
3. **OAuth2 Integration (45 mins)**: Chapter 3 - Enhanced security and standards compliance
4. **Production Deployment (30 mins)**: Chapter 4 - Cloud deployment strategies

**Total Workshop Duration**: ~2.5-3 hours

## 🛠️ Development Tools

### Recommended IDE Setup
- **IntelliJ IDEA** or **Eclipse** with:
  - Lombok plugin
  - Spring Boot support
  - Gradle support

### Useful Commands

```bash
# Build all chapters (run from root)
for dir in chapter-*; do (cd $dir && ./gradlew build); done

# Clean all chapters
for dir in chapter-*; do (cd $dir && ./gradlew clean); done

# Check for dependency updates
./gradlew dependencyUpdates
```

## 🐛 Troubleshooting

### Common Issues

**Port Already in Use**
```bash
# Check what's using port 8080
lsof -i :8080
# Kill the process or change the port in application.properties
```

**Firebase Authentication Errors**
- Ensure service account key is properly configured
- Verify Firebase project ID matches your configuration
- Check that authentication providers are enabled in Firebase Console

**Docker Build Issues**
```bash
# Clear Docker cache
docker system prune -a
# Rebuild without cache
docker-compose build --no-cache
```

## 📄 License

This project is part of the JavaFest workshop materials.

## 👥 Contributors

Workshop materials created for JavaFest by:

- **Muthukumaran** - [LinkedIn](https://www.linkedin.com/in/muthuishere/)
- **Shaama M** - [LinkedIn](https://www.linkedin.com/in/shaama-m-030115237)

## 🙏 Acknowledgments

- Spring AI team for the MCP framework
- Firebase team for authentication services
- Spring Boot team for the excellent framework

---

**Ready to get started?** Jump into [Chapter 1](chapter-1/README.md) and begin your journey building production-ready MCP servers!