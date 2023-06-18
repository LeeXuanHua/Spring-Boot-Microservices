# Notes & Learning Summary

## Table of Contents
1. [Gradle](#gradle)
2. [Understanding Java Spring Boot Layout and Annotations](#understanding-java-spring-boot-layout-and-annotations)
    - [Typical Structure of a Spring Boot Service](#typical-structure-of-a-spring-boot-service)
    - [Annotations](#annotations)
    - [JPA](#jpa)
3. [Spring Boot Testing](#spring-boot-testing)
4. [Inter-Process Communication](#inter-process-communication)
    - [Basics to Servlet (Spring MVC) vs Reactive (Spring WebFlux)](#basics-to-servlet--spring-mvc--vs-reactive--spring-webflux-)
4. [Spring Boot Cloud](#spring-boot-cloud)
    - [Service Discovery](#service-discovery)
    - [Load Balancer (Client-Side)](#load-balancer--client-side-)
    - [API Gateway](#api-gateway)
    - [Circuit Breaker](#circuit-breaker)
    - [Configuration Server](#config-server)
    - [Distributed Tracing](#distributed-tracing)
    - [Fault Tolerance](#fault-tolerance)
    - [Messaging / Message Broker](#messaging--message-broker)
    - [Distributed Session](#distributed-session)
    - [Distributed Cache](#distributed-cache)
    - [Monitoring](#distributed-tracing)
4. [Spring Boot Security](#spring-boot-security)
5. [Spring Boot Logging](#spring-boot-logging)

## Gradle
1. Clean, rebuild and run project
    ```bash
    gradle clean build bootRun
    ```
2. Multi-project build defined by the `build.gradle` within each project
    ```bash
    # Therefore, adding this to settings.gradle and rebuilding will include the new project as a module
    include 'order-service'
    ```

## Understanding Java Spring Boot Layout and Annotations

### Typical Structure of a Spring Boot Service
- This applies to both REST services and microservices
    ```
    <module_name>
    ├── src/
    │ ├── main/
    │ │ ├── java/
    │ │ │ └── com.<name1>.<name2>
    │ │ │   ├── controller/
    │ │ │   ├── dto/
    │ │ │   ├── exception/
    │ │ │   ├── model/
    │ │ │   ├── repository/
    │ │ │   └── service/
    │ │ │     ├── impl/
    │ │ │     └── <interface_name>.java
    │ │ └── resources
    │ │   ├── static/
    │ │   ├── templates/
    │ │   └── application.properties
    │ └── test/java/com.<name1>.<name2>
    │   ├── unit/
    │   └── integration/
    ├── build.gradle
    ├── ……
    ```
- Main folders (in the context of a Product microservice)
    - `model`: Product is used to store data into the database.
    - `repository`: ProductRepository is used to do CRUD operations with the database.
    - `service`: ProductService is used to implement business logic.
    - `controller`: ProductController is used to handle HTTP endpoints.
    - `dto`: ProductDto is used to transfer data between layers.
        - `request`: ProductRequest is used to transfer data from the client to the server.
        - `response`: ProductResponse is used to transfer data from the server to the client.
    - `exception`: ProductException is used to handle exceptions.
    - `resources`: contains static resources and property files.
    - `test`: contains unit and integration tests.
    - `application.properties`: contains the properties for Spring Boot application.
- Some folders (e.g. service, dto, exception) may contain both interfaces and implementations
- Other files
    - `build.gradle`: contains the build script for Gradle
    - `settings.gradle`: contains the settings for Gradle
    - `gradlew`: contains the Gradle wrapper script for Unix-based systems
    - `gradlew.bat`: contains the Gradle wrapper script for Windows
    - `gradle/wrapper/gradle-wrapper.jar`: contains the Gradle wrapper JAR
    - `gradle/wrapper/gradle-wrapper.properties`: contains the properties for the Gradle wrapper
    - `gradle.properties`: contains the properties for Gradle

### Annotations
- `@Service` annotation is used to mark the class as a service provider
- `@Autowired` annotation is used to inject the dependency automatically
- `@Transactional` annotation is used to define the scope of a single database transaction
- `@RequiredArgsConstructor` annotation is used to generate a constructor with required arguments
- `@Slf4j` annotation is used to generate a logger field
- `@Builder` annotation is used to generate builder methods
- `@AllArgsConstructor` annotation is used to generate a constructor with all arguments
- `@NoArgsConstructor` annotation is used to generate a constructor with no arguments
- `@Data` annotation is used to generate @ToString, @EqualsAndHashCode, @Getter / @Setter and @RequiredArgsConstructor
- `@Document` annotation is used to define as a MongoDB document
- `@Id` annotation is used to specify as the unique identifier

### JPA
- JPA stands for Java Persistence API
- Based on method name, Spring Data JPA can automatically generate the query. Refer [query-creation](https://docs.spring.io/spring-data/data-jpa/docs/current/reference/html/#jpa.query-methods.query-creation) and [repository-query-keywords](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repository-query-keywords)to learn more about query creation syntax.
- `readOnly` flag in `@Transactional` hints the underlying JDBC driver for performance optimizations, but does not enforce no-manipulation queries (dependent on database). Refer [](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#transactional-query-methods)

## Spring Boot Testing

### Unit Testing
- Used to test a single unit of code (e.g. a function)
- Particularly for Spring Boot, unit testing can only be achieved without using field injections (Refer to code for proper usage of constructor injections)

### Integration Testing
- Used to test the integration between multiple units of code (e.g. testing the integration between a controller and a service)
- More loosely defined than unit testing and can encompass a range of tests (e.g. testing the integration between a controller and a service, or testing the integration between a service and a database)

### Mocking
- Used to achieve isolation in unit testing, by mocking external dependencies (e.g. database, external API calls)
- Can be used in integration testing, depending on the scope and purpose of the test (e.g. testing web controller, data persistence layer, or the entire application)
- Benefits include faster startup time and less resource consumption during testing
- Understand the difference between `@Mock` and `@MockBean` [here](https://www.baeldung.com/java-spring-mockito-mock-mockbean), and a use-case is to [mock some beans in MockMvc](https://stackoverflow.com/questions/58547348/how-to-mock-some-beans-but-not-others-in-mockmvc) as seen [here](order-service/src/test/java/com/microservices/orderservice/OrderControllerIntegrationTest.java)
- Use `@MockitoSettings(strictness = Strictness.LENIENT)` to suppress errors for unused stubs (refer to [`OrderServiceUnitTest.java`](order-service/src/test/java/com/microservices/orderservice/OrderServiceUnitTest.java) for example)

### Test Coverage
- Use Jacoco to manage test coverage (Refer to `build.gradle` for configurations to generate test coverage report)
- Test coverage report can be found in `build/jacocoHtml/index.html`
- Although test coverage is not a good metric to measure the quality of tests, it is still a good indicator to see if there are any missing tests
- Cyclomatic complexity measures function complexity (incl. conditions and iterations), indicating if function ought to be decomposed further to better adhere to Single Responsibility Principle
- Ideally, the threshold for test coverage is around 80% and cyclomatic complexity <= 10


## Inter-Process Communication
**Goal**: To enable communication between microservices

**Types**:
1. Synchronous or Asynchronous
    - REST API
    - GraphQL
    - gRPC
2. Message Broker
    - RabbitMQ
    - Kafka

**Our Choice**: REST API - via Spring Boot `WebClient`
    - `WebClient` is a non-blocking, reactive HTTP client that supports sync, async and streaming scenarios
    - `RestTemplate` is a synchronous client that is less desirable as an alternative & deprecated in Spring 5.0

**Using Inter-Process Communication in the Project**:
- Source: OrderService; Sink: InventoryService
- To place an order, OrderService sends a request to InventoryService to check if the order can be fulfilled (i.e. sufficient stock)
- Calls will be made via [`WebClient`](order-service/src/main/java/com/microservices/orderservice/service/OrderService.java) defined in [`config/`](order-service/src/main/java/com/microservices/orderservice/config/)
- Remember to call `.block()` since `WebClient` is asynchronous

#### Basics to Servlet (Spring MVC) vs Reactive (Spring WebFlux)
- Major shift towards asynchronous, non-blocking concurrency in Java, JVM, etc.
- Spring Framework 5 introduces a fully non-blocking, reactive stack for web applications.
- The reactive stack handles higher concurrency with less hardware resources, and excels at streaming scenarios, both client and server side.
- Motivation:
  - Traditionally, Java used thread pools for the concurrent execution of blocking, I/O bound operations (e.g. making remote calls)
  - However, this approach can be complex:
      1. Hard to make applications work correctly when wrestling with synchronization and shared structures
      2. Hard to scale efficiently when every blocking operation requires an extra thread to sit around and wait. Thus, at the mercy of latency outside control (e.g. slow remote clients and services)
  - On the other hand, if an application is fully non-blocking, it can scale with a small, fixed number of threads
  - In short, we should avoid relying on extra threads for higher concurrency
  - To cater to asynchronous, we forfeit sequential logic associated with imperative programming, and learn to react to events they generate


## Spring Boot Cloud

### Service Discovery
- Service Discovery is used to register and discover microservices
- Problem It Addresses: Without this, REST calls via `WebClient` is made to a hard-coded URL, which may change (i.e. different IP and port)
  ![Problem Service Discovery Addresses](/figure/ServiceDiscovery_VariableUrl.png)
- How It Works: Microservices register themselves to the Service Registry to be discovered
  ![How Service Discovery Works](/figure/ServiceDiscovery_RegistryOverview.png)
    - Discovery server will send the client a copy of the registry.
    - Therefore, if client is unable to communicate with discovery server subsequently, it falls back to the local copy of the registry, before failing the communication.
      ![Eureka Client Stores Registry Locally](/figure/ServiceDiscovery_ClientSide.png)
    - We can test the above by stopping the Discovery Server (after client registration), and making an API call via Postman
- Some Service Discovery Tools: Eureka, Consul, Zookeeper
- In this project, we will be using Netflix's Eureka Service Discovery
- To define a Eureka Server, annotate the main class with `@EnableEurekaServer` and set `eureka.client.register-with-eureka=false` & `eureka.client.fetch-registry=false` in `application.properties`
  - Based on the `server.port` defined, the Eureka dashboard can be accessed via `http://localhost:<port>/`
    ![Eureka Server Dashboard](/figure/ServiceDiscovery_Dashboard.png)
- To define a Eureka Client, set `eureka.client.service-url.default-zone=http://localhost:8761/eureka` in `application.properties`
  - Note that we do not need to annotate the main class with `@EnableEurekaClient` (Refer [here](https://cloud.spring.io/spring-cloud-netflix/multi/multi__service_discovery_eureka_clients.html))
    >   Having `spring-cloud-starter-netflix-eureka-client` on the classpath makes the app into both a Eureka “instance” (that is, it registers itself) and a “client” (it can query the registry to locate other services)   
  - Change the port of the client to 0 to allow Spring to assign a random port to the client
  - Update the `localhost:8082` for the `WebClient` to `inventory-service` (i.e. service name defined in `application.properties`)

### Load Balancer (Client-Side)
- Load Balancer is used to distribute the load between microservices
- Problem It Addresses: Earlier in Service Discovery, we spun up multiple instances. Updating the `localhost:8082` for the `WebClient` to `inventory-service` raises the problem of which instance to call (when there are >1 instances registered)
  ![Error Code for Order Service](/figure/LoadBalancer_DiscoveryError.png)
- How It Works: `@LoadBalanced` from Spring Cloud Client will be applied to a **Client-Side** `RestTemplate` or `WebClient` bean to distribute the load between the instances

### API Gateway
- API Gateway is a single entry point for all the microservices
- Problem It Addresses: Even though we have Service Discovery and Load Balancer (Client-Side), we still need to call the microservices directly. This requires us to know the URL (host & port) of the microservices prior to invocation.
  ![Microservices Exposed Without API Gateway](/figure/APIGateway_ExposureProblem.png)
- How It Works: API Gateway is a single point of contact for clients, also known as Edge Server. It is a single point of failure and is also known as a **Server-Side** Load Balancer.
  ![How API Gateway Works](/figure/APIGateway_Overview.png)
  - Essentially, API Gateway can perform:
    1. Routing based on request headers (including path rewriting, predicates to match headers/parameters and filters to modify requests and responses)
    2. Security (e.g. Authentication, Authorization, rate limiting)
    3. Load balancing
    4. SSL Termination (i.e. internal communication do not require HTTPS)
- Some API Gateway Tools: Zuul, Spring Cloud Gateway
- In this project, we will be using [Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)
- Start by adding Eureka Client dependency to Spring Cloud Gateway (register as a Eureka Client to obtain the registry)
- To define a Spring Cloud Gateway, set the routing rules in `application.properties`
  - Example 1:
    ```yaml
    ## Product Service Route
    spring.cloud.gateway.routes[0].id=product-service
    spring.cloud.gateway.routes[0].uri=lb://product-service
    spring.cloud.gateway.routes[0].predicates[0]=Path=/api/product
    ```
    - We define a predicate to match all `Path` types of the pattern `/api/product`
    - Such requests will be routed to `lb://product-service` (instead of `http`, use `lb` for load-balancing)
  - Example 2:
    ```yaml
    ## Discovery Server Route
    spring.cloud.gateway.routes[2].id=discovery-server
    spring.cloud.gateway.routes[2].uri=http://localhost:8761
    spring.cloud.gateway.routes[2].predicates[0]=Path=/eureka/web
    spring.cloud.gateway.routes[2].filters[0]=SetPath=/
    
    ## Discovery Server Static Resources Route
    spring.cloud.gateway.routes[3].id=discovery-server-static
    spring.cloud.gateway.routes[3].uri=http://localhost:8761
    spring.cloud.gateway.routes[3].predicates[0]=Path=/eureka/**
    ```
      - Our goal is to access the Eureka Dashboard via the API Gateway (URL = `http://localhost:8080/eureka/web`)
      - First, we define a predicate to match all `Path` types of the pattern `/eureka/web`
      - Such requests will be routed to `lb://localhost:8761/eureka/web`
      - Since `/eureka/web` endpoint does not exist at port 8761, we set the path to `/` (i.e. root)
        - Define a `filter` to `SetPath` as `/` and route the requests to `http://localhost:8761/` instead
      - However, this fails (HTTP Status Code 503 - Service Unavailable) with Java error message `o.s.c.l.core.RoundRobinLoadBalancer      : No servers available for service: localhost`
        - We can see that the API Gateway is trying to load-balance the request to `localhost` which fails because there is no Eureka service registered with the name `localhost`
        - Therefore, for such endpoints, we should use `http` instead of `lb` to fix as `http` protocol and avoid treating the URL as a service name erroneously
      - Now we can load the dashboard via the API Gateway (URL = `http://localhost:8080/eureka/web`)
        ![Plain Eureka Dashboard without CSS and JS](/figure/APIGateway_StaticResourcesError.png)
      - However, we realise that only the HTML file was returned (missing CSS and JS files)
        - Inspecting the network packets, we can see that the static resources are not loaded via the API Gateway
        - Therefore, we need to define another route for the static resources
          ![Static Resources Failed with 404](/figure/APIGateway_StaticResourcesPath.png)
        - Based on the above image, we define a predicate to match all `Path` types of the pattern `/eureka/**` to capture all static resources
        - Such requests will be routed to `http://localhost:8761/eureka/**`
        - This works without `SetPath` filter because the static resources are located at the root of the Eureka Server
          - Inspecting the HTML file, we notice that the HTML file calls the static resources at `/eureka/css/wro.css` and `/eureka/js/wro.js`
          - Therefore, we should not change its path
      - Now we can load the dashboard fully via the API Gateway (URL = `http://localhost:8080/eureka/web`)
        ![Eureka Dashboard Loaded Properly with Static Resources](/figure/APIGateway_StaticResourcesSuccess.png)
    - **Note: The order of the routes matters. The first route that matches the request will be used.**
    - FYI: Here is the source code of the Eureka Dashboard, to understand the static resource pathing
      ![Eureka Dashboard Source Code](/figure/APIGateway_EurekaDashboardSourceCode.png)

### Circuit Breaker
- Circuit Breaker is used to handle the failure of a service

### Config Server
- Config Server is used to manage the configuration of all the microservices

### Distributed Tracing
- Distributed Tracing is used to track the request flow between microservices

### Fault Tolerance
- Fault Tolerance is used to handle the failure of a service

### Messaging / Message Broker
- Messaging is used to communicate between microservices

### Distributed Session
- Distributed Session is used to manage the session of all the microservices

### Distributed Cache
- Distributed Cache is used to manage the cache of all the microservices

### Monitoring
- Monitoring is used to monitor the health of all the microservices

## Spring Boot Security

## Spring Boot Logging
