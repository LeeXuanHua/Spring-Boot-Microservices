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
5. [Spring Boot Cloud](#spring-boot-cloud)
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
6. [Spring Boot Security](#spring-boot-security)
   - [OAuth 2.0](#oauth-20)
   - [OAuth 2.0 with Spring Security Resource Server](#oauth-20-with-spring-security-resource-server)
   - [Keycloak Configuration](#keycloak-configuration)
   - [Analysing Our Java Spring Boot Code](#analysing-our-java-spring-boot-code)
   - [Other Spring Security Features Pending Implementation](#other-spring-security-features-pending-implementation)
7. [Spring Boot Logging](#spring-boot-logging)

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
- The reactive stack handles higher concurrency with less hardware resources, and excels at streaming & real-time data processing scenarios, both client and server side.
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

References:
- [Spring Security - Servlet Oauth2 Login](https://docs.spring.io/spring-security/reference/servlet/oauth2/login/index.html)
- [OAuth 2.0 Authorization Framework by Internet Engineering Task Force (IETF)](https://datatracker.ietf.org/doc/html/rfc6749)
- [OAuth 2.0 JSON Web Token (JWT) Profile by Internet Engineering Task Force (IETF)](https://datatracker.ietf.org/doc/html/rfc7523)
- [Spring Security - Password Encryption](https://docs.spring.io/spring-security/reference/features/authentication/password-storage.html)

### OAuth 2.0
- OAuth 2.0 is an open standard for access delegation, commonly used as a way for Internet users to grant websites or applications access to their information on **other websites** but **without giving them the passwords**
> The OAuth 2.0 authorization framework enables a third-party application to obtain limited access to an HTTP service, either on
behalf of a resource owner by orchestrating an approval interaction between the resource owner and the HTTP service, or by allowing the
third-party application to obtain access on its own behalf.  This specification replaces and obsoletes the OAuth 1.0 protocol described in RFC 5849.

![Generic OAuth 2.0 Protocol Flow](/figure/Security_OAuth2.0ProtocolFlow.png)
- Roles:
  1. **Resource owner** - Grants access to a protected resource. If resource owner is a person, it is referred to as an end-user.
  2. **Resource server** - Server hosting the protected resources, capable of accepting and responding to protected resource requests using access tokens.
  3. **Client** - Application making protected resource requests on behalf of the resource owner and with its authorization. "client" does not imply implementation (e.g. server, desktop, or other devices).
  4. **Authorization server** - Server issuing access tokens to the client after successfully authenticating the resource owner and obtaining authorization.
- Authorization server may be the same server as the resource server or a separate entity
- Authorization grant is a credential representing the resource owner's authorization (to access its protected resources) and used by the client to obtain an access token
  - Consists of 4 types:
    1. **Authorization code**
       - Between client and resource owner (authorization server is intermediary)
       - Instead of requesting authorization directly from the resource owner, the client directs the resource owner to an authorization server (via its user-agent), which in turn directs the resource owner back to the client with the authorization code
       - This is the most commonly used authorization grant type, where resource owner is directed to authorization server login page, before being redirected back to the client with the authorization code
       - Security benefits include the ability to authenticate the client, as well as the transmission of the access token directly to the client **without passing it through the resource owner's user-agent** and potentially exposing it to others, including the resource owner
    2. **Implicit**
        - Simplified authorization code flow optimized for clients implemented in a browser using a scripting language such as JavaScript
        - Client is **issued an access token directly** (instead of an authorization code), therefore access token may be exposed to the resource owner or other applications with access to the resource owner's user-agent
        - Grant type is implicit, as no intermediate credentials (such as an authorization code) are issued (and later used to obtain an access token)
        - Implicit grants improve the responsiveness and efficiency of some clients (e.g. in-browser application), since it reduces the number of round trips required to obtain an access token
        - However, this is a huge security risk and is not recommended at all
    3. **Resource owner password credentials**
        - Resource owner password credentials (i.e. username and password) can be used directly as an authorization grant to obtain an access token
        - This should only be used when there is a **high degree of trust between the resource owner and the client** (e.g. client is part of the device operating system or a highly privileged application), and when other authorization grant types are not available (such as an authorization code)
        - Although this requires resource owner credentials, they are used for a single request and are exchanged for an access token. This eliminates the need for the client to store the resource owner credentials for future use, by exchanging the credentials with a long-lived access token or refresh token
    4. **Client credentials**
        - **MUST only be used by confidential clients (a.k.a. clients fully under control of organisation)**
        - Used when the authorization scope is limited to the protected resources **under the control of the client**, or to protected **resources previously arranged with the authorization server**
        - Used typically when the client is acting on its own behalf (the client is also the resource owner) or is requesting access to protected resources based on an authorization previously arranged with the authorization server

**OAuth 2.0 Flow for Authorization Code**
![OAuth 2.0 Flow for Authorization Code](/figure/Security_OAuth2.0AuthorizationCodeFlow.png)
1. Client initiates flow by directing the resource owner's user-agent to the authorization endpoint. The client includes its **client identifier**, **requested scope**, local state, and a **redirection URI** to which the authorization server will send the user-agent back once access is granted (or denied)
2. Authorization server authenticates the resource owner (via the user-agent) and establishes whether the resource owner grants or denies the client's access request
3. Assuming the resource owner grants access, the authorization server redirects the user-agent back to the client using the redirection URI provided earlier (in the request and during client registration). The redirection URI includes an **authorization code** and any local state provided by the client earlier
4. Client requests an access token from the authorization server's token endpoint by including the **authorization code** received in the previous step. When making the request, the client authenticates with the authorization server. The client includes the **redirection URI** used to obtain the authorization code for verification
5. Authorization server authenticates the client, validates the authorization code, and ensures that the **redirection URI received matches the URI used to redirect the client** in step (C). If valid, the authorization server responds back with an **access token** and, optionally, a **refresh token**

**OAuth 2.0 Flow for Implicit Grant**
![OAuth 2.0 Flow for Implicit Grant](/figure/Security_OAuth2.0ImplicitFlow.png)
1. Client initiates flow by directing the resource owner's user-agent to the authorization endpoint. The client includes its **client identifier**, **requested scope**, local state, and a **redirection URI** to which the authorization server will send the user-agent back once access is granted (or denied)
2. Authorization server authenticates the resource owner (via the user-agent) and establishes whether the resource owner grants or denies the client's access request
3. Assuming the resource owner grants access, the authorization server redirects the user-agent back to the client using the redirection URI provided earlier. The redirection URI includes the **access token** in the **URI fragment**
4. User-agent follows the redirection instructions by making a request to the web-hosted client resource (which does not include the fragment). The **user-agent retains the fragment information locally**
5. Web-hosted client resource returns a web page (typically an HTML document with an embedded script) capable of accessing the **full redirection URI** including the fragment retained by the user-agent, and **extracting the access token** (and other parameters) contained in the fragment
6. User-agent executes the script provided by the web-hosted client resource locally, which extracts the access token
7. User-agent passes the access token to the client

**OAuth 2.0 Flow for Resource Owner Password Credentials**
![OAuth 2.0 Flow for Resource Owner Password Credentials](/figure/Security_OAuth2.0ResourceOwnerPasswordCredentialsFlow.png)
1. Resource owner provides the client with its **username** and **password** 
2. Client requests an access token from the authorization server's token endpoint by including the credentials received from the resource owner. When making the request, the client authenticates with the authorization server
3. Authorization server authenticates the client and validates the resource owner credentials, and if valid, issues an **access token**

**OAuth 2.0 Flow for Client Credentials**
![OAuth 2.0 Flow for Client Credentials](/figure/Security_OAuth2.0ClientCredentialsFlow.png)
1. Client authenticates with the authorization server and requests an **access token** from the token endpoint
2. Authorization server authenticates the client, and if valid, issues an access token

- Usage of JSON Web Token (JWT) Profile
  - JWT is a security token issued by an Identity Provider and consumed by a Relying Party to identify the token's subject for security-related purposes
  - Authorization grant can manifest itself in many forms (e.g. assertions via JWT or SAML)
  - JWT Profile for OAuth 2.0 Authorization Grants defines a method for encoding authorization grants in a JWT and defines how to request and process them
  - To use Bearer JWTs as **Authorization Grants**:
    - `grant_type` = `urn:ietf:params:oauth:grant-type:jwt-bearer`
    - `assertion` = `<A Single JWT>`
    - `client_id` is optional
  - To use Bearer JWTs for **Client Authentication**:
    - `client_assertion_type` = `urn:ietf:params:oauth:client-assertion-type:jwt-bearer`
    - `client_assertion` = `<A Single JWT>`
  - Authorization server will check that the JWTs must contain:
    1. `iss` (issuer) claim - entity that issued the JWT
    2. `sub` (subject) claim - principal of the JWT (if authorization grant, typically is the resource owner or a pseudonymous identifier; if client authentication, MUST be the `client_id` of the OAuth client)
    3. `aud` (audience) claim - identifies the authorization server as an intended audience
    4. `exp` (expiration time) claim - limits the time window during which the JWT can be used
    5. [Optional] `nbf` (not before) claim - identifies the time before which the token MUST NOT be accepted for processing
    6. [Optional] `iat` (issued at) claim - identifies the time at which the JWT was issued (authorization server may reject JWTs with an "iat" claim value that is unreasonably far in the past)
    7. [Optional] `jti` (JWT ID) claim - provides a unique identifier for the JWT (for authorization server to ensure that JWTs are not replayed by maintaining the set of used `jti` values for the length of time for which the JWT would be considered valid based on the applicable `exp` instant)
    8. JWT MUST be digitally signed or have a Message Authentication Code (MAC) applied by the issuer
  - Essentially, the JWT is a digitally signed assertion that the client can present to the authorization server to prove its identity


### OAuth 2.0 with Spring Security Resource Server
- Reference Articles & Documentations:
  - [JWT Authentication With Spring Boot’s Inbuilt OAuth2 Resource Server (Oct 26, 2020)](https://medium.com/swlh/stateless-jwt-authentication-with-spring-boot-a-better-approach-1f5dbae6c30f)
  - [JWT Authentication with OAuth2 Resource Server and an external Authorization Server (Jul 15, 2022)](https://medium.com/geekculture/jwt-authentication-with-oauth2-resource-server-and-an-external-authorization-server-2b8fd1524fc8)
  - [Overview of Spring Security with OAuth 2.0 JWT](https://www.baeldung.com/spring-security-oauth-jwt)
  - [Spring Security OAuth 2.0 Documentation for **Servlet**](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html)
  - [Spring Security OAuth 2.0 Documentation for **Reactive**](https://docs.spring.io/spring-security/reference/reactive/oauth2/resource-server/jwt.html)
- Article 1 Summary:
  - JWT authentication for SPA web application's backend REST APIs using Spring Boot’s inbuilt OAuth2 Resource Server
    - **More Secure** — Use an RSA private key instead of a single secret token (symmetric key) to sign JWTs and RSA public key for signature verification
    - **Convenient** — An endpoint (`/login`) to obtain a signed JWT in exchange for valid user credentials
    - **Authorization** — Spring Security’s method security can be used since the JWT information is available as Authentication at controller level; Can use `@PreAuthorize`, `@PostAuthorize` annotations with SPEL for complex authorization needs
    - **Extendable** — Can be extended to support federated authentication (ex: “Login with Google”, etc.) and to support refresh_tokens and client side JWT validation using `/jwt` endpoint
    - **Best Practices** — Use Spring Boot’s inbuilt OAuth2 Resource Server for inbound request authentication with JWT
    - **Scalable** — This approach is stateless and JWT authentication can be scaled horizontally as desired
  - Session-based Authentication vs Stateless Authentication:
    - Session-based Authentication requires the backend to maintain each user’s session data (aka. state)
      1. Sharing session data across backend servers (without sticky sessions)
      2. Session aware load balancing (sticky sessions) when scaling horizontally
    - Stateless Authentication can be achieved by either:
      1. Opaque Tokens - access token not containing any user data, usually issued by a third party who is contacted each time to validate the access token
      2. JSON Web Tokens (JWTs) - a JSON token with 3 sections (header, claims,and signature) containing personal/application data. The signature can be validated by the receiving party itself without contacting the third party that issued it.
- Article 2 Summary:
  - OIDC returns 2 tokens:
    1. `id_token` - used by SPA to know if user is authenticated, containing user information like username, email and display name depending on the scopes requested
    2. `access_token` - used to authenticate API calls made to backend APIs, requiring authorization_code flow instead. For SPAs and mobile apps, use Authorization Code with PKCE (PKCE = proof key for code exchange) for security
  - How it Works:
    - Once frontend has obtained the access_token, pass that JWT as the Bearer token in the Authorization header when invoking the backend API
    - Backend then validates the signature and the content of the JWT to authenticate the API call against the user
      **Note: Can extend authorization code flow and perform a token exchange in one of the services to exchange the JWT token issued by the authorization server into an application specific JWT. With that approach, we can add more claims to the JWT like user’s roles and tenant/organization information.**
- Article 3 Summary:
  - JWTs contain all the necessary information, so the Resource Server must verify Token signature to ensure data integrity
  - To do so, `jwk-set-uri` contains the public key from the authorization server to decode the JWT
    - If `jwk-set-uri` is not set, Resource Server will use `issuer-uri` to determine the endpoint of the public key from the Authorization Server metadata endpoint
    - Adding `issuer-uri` mandates that the **Authorization Server** must run before starting the **Resource Server** application (therefore, the `issuer-uri` server must be up and running before the resource server can start)


### Keycloak Configuration
**Background**
- A realm in Keycloak is equivalent to a tenant. Each realm allows an administrator to create isolated groups of applications and users
- Initially, Keycloak includes a single realm, called master. Use this realm only for managing Keycloak and not for managing any applications
- 

1. Login to Keycloak admin console at `http://localhost:8181/admin`
2. Create a realm `spring-boot-microservices-realm` and enable client credential grant by disabling `Standard Flow Enabled` and `Direct Access Grants Enabled`, while enabling `Service Accounts Enabled`
3. View the OpenID Configurations at http://localhost:8181/realms/spring-boot-microservices-realm/.well-known/openid-configuration


### Analysing Our Java Spring Boot Code
1. Define Spring Security rules under `config/SecurityConfig.java` in API Gateway
   ```java
    @Configuration
    @EnableWebFluxSecurity
    public class SecurityConfig {
        @Bean
        public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity serverHttpSecurity) {
            serverHttpSecurity.cors(Customizer.withDefaults())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)                 // disable CSRF for Postman testing
                .authorizeExchange(exchange -> exchange
                    .pathMatchers("/eureka/**").permitAll()                 // permit all requests to /eureka/**
                    .anyExchange().authenticated()                          // authenticate all other requests
                )
                /*
                Either of the following works too:
                .oauth2ResourceServer(ServerHttpSecurity.OAuth2ResourceServerSpec::jwt);  // use JWT for OAuth2 resource server
                .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(withDefaults()));
                */
                .oauth2ResourceServer(oauth2 -> oauth2
                    .jwt(jwt -> jwt.jwtDecoder(jwtDecoder())));
    
            return serverHttpSecurity.build();
        }
    }
   ```
   - A ServerHttpSecurity is similar to Spring Security's HttpSecurity but for WebFlux. It allows configuring web based security for specific http requests
   - `.cors()`, `.csrf()`, `.oauth2ResourceServer()`, `.authorizeExchange()` are marked for deprecation. Instead, we should use `.cors(<org.springframework.security.config.Customizer>)`, `.csrf(<org.springframework.security.config.Customizer>)`, `.oauth2ResourceServer(<org.springframework.security.config.Customizer>)`, `.authorizeExchange(<org.springframework.security.config.Customizer>)`
   - In the above, all requests to `/eureka/**` (discovery server dashboard) are permitted, while all other requests must be authenticated using JWT (i.e. the OIDC Access Token)
2. Define custom JWT decoder rules under `config/SecurityConfig.java` in API Gateway
   ```java
    @Configuration
    @EnableWebFluxSecurity
    public class SecurityConfig {
        @Bean
        public NimbusReactiveJwtDecoder jwtDecoder() {
            NimbusReactiveJwtDecoder jwtDecoder = (NimbusReactiveJwtDecoder) ReactiveJwtDecoders.fromIssuerLocation(issuerUri);
            OAuth2TokenValidator<Jwt> oAuth2TokenValidator = new DelegatingOAuth2TokenValidator<>(
                    new JwtTimestampValidator(),
                    new JwtIssuerValidator(issuerUri),
                    new JwtClaimValidator<List<String>>("aud",
                          aud -> aud.stream().allMatch(s -> s.equals(resourceServerAudience))),
                    new JwtClaimValidator<String>("typ",
                          typ -> typ.equals(jwtType)),
                 new JwtClaimValidator<String>("preferred_username",
                          preferred_username -> !preferred_username.isEmpty())
            );
            jwtDecoder.setJwtValidator(oAuth2TokenValidator);

            return jwtDecoder;
        }
    }
   ```
   - Remember to use `ReactiveJwtDecoders` instead of `JwtDecoders` since we are using WebFlux (not Spring MVC)
     - Otherwise, `JwtDecoder` may be set already or the validations will not apply properly
   - Do not use `NimbusReactiveJwtDecoder` directly
     - Instead use `ReactiveJwtDecoders.fromIssuerLocation(issuerUri)` to create a `NimbusReactiveJwtDecoder` instance
     - Otherwise, the `NimbusReactiveJwtDecoder` instance will not have the `setJwtValidator()` method
   - The above validates the following claims:
     - `exp` (expiration time) - the time on or after which the token MUST NOT be accepted for processing
     - `iss` (issuer) - the issuer of the token
     - `aud` (audience) - the audience the token is intended for
     - `typ` (token type) - the type of the token
     - `preferred_username` - the username of the user (not empty)


### Other Spring Security Features Pending Implementation
1. [Authentication](https://docs.spring.io/spring-security/reference/features/authentication/index.html)
2. [Protection Against Exploits](https://docs.spring.io/spring-security/reference/features/exploits/index.html)
3. [Other Integrations](https://docs.spring.io/spring-security/reference/features/integrations/index.html)


## Spring Boot Logging
