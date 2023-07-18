# Spring Boot Microservice - Online Shopping Application
This project is inspired by: [Video](https://www.youtube.com/watch?v=mPPhcU7oWDU&t=20634s), [Source Code](https://github.com/SaiUpadhyayula/spring-boot-microservices)

This project features the following:
1. **Product Service** - Create and view products, act as product catalogue
2. **Order Service** - Order products
3. **Inventory Service** - Check if product is in stock or not
4. **Notification Service** - Send notifications, after order is placed
5. **API Gateway** - Single entry point for all services
6. **Discovery Server** - Service discovery for multiple instances of each service
7. **OIDC Security via Keycloak** - Secure backend resource server using OIDC and OAuth 2.0 from self-hosted Identity Provider & Authorization Server at `http://localhost:8181/admin`
8. **Distributed Tracing via Zipkin** - View the flow of requests across services at `http://localhost:9411`

System Architecture:
![Application System Architecture](/figure/System%20Architecture.png)

## Pre-requisites
1. Java SDK 11
2. MySQL
3. MongoDB
4. Docker

## How to Run
#### Clone this repository (build the project only after running the following subsequent instructions)
```bash
git clone https://github.com/LeeXuanHua/Spring-Boot-Microservices.git
cd spring-boot-microservices
```

#### Set up MySQL database
1. Edit `importMySQLCredentials.sh.stub` to include your MySQL credentials
2. Rename `importMySQLCredentials.sh.stub` to `importMySQLCredentials.sh`
3. Ensure that MySQL is running with the following empty databases:
   1. `order_service`
   2. `inventory_service`

**Note that MongoDB do not require any setup for the database.**

#### Set up OIDC Spring Security via Keycloak
Refer to [Keycloak Docker Setup](https://www.keycloak.org/getting-started/getting-started-docker).

For our case, our initial Keycloak setup is as follows:
```bash
docker run -p 8181:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:21.1.1 start-dev
```

#### Set up Distributed Tracing via Zipkin
```bash
docker run -d -p 9411:9411 openzipkin/zipkin
```

#### To build and run all the microservices, execute the following commands
```bash
# Build the project
./gradlew clean build

# On all terminals from spring-boot-microservices/, run the following command:
source ./importMySQLCredentials.sh

# On separate terminals, for each of the following pair, run one of the commands:
./gradlew :product-service:bootRun
java -jar product-service/build/libs/product-service-0.0.1-SNAPSHOT.jar

./gradlew :order-service:bootRun
java -jar order-service/build/libs/order-service-0.0.1-SNAPSHOT.jar

./gradlew :inventory-service:bootRun
java -jar inventory-service/build/libs/inventory-service-0.0.1-SNAPSHOT.jar

./gradlew :api-gateway:bootRun
java -jar api-gateway/build/libs/api-gateway-0.0.1-SNAPSHOT.jar

./gradlew :discovery-server:bootRun
java -jar discovery-server/build/libs/discovery-server-0.0.1-SNAPSHOT.jar
```

#### For usage with IntelliJ IDEA
Configure environment variables for the following run configurations:
1. `order-service`
2. `inventory-service`
![IntelliJ Run Configuration](/figure/IntelliJ_RunConfiguration.png)


## How to Test Manually via Postman
Refer to [Testing](./Testing.md).


## Changes from Video Tutorial
1. Used Gradle instead of Maven
2. Implemented testings & added Jacoco for test coverage (editing the tests cases alongside the code and using `lombok.config`)
3. Handled MySQL login credentials as environment variables (instead of hardcoding in `application.properties`)
4. Fixed versioning issues (Spring Boot, Spring Security, Eureka Server, OIDC Authentication, etc)


## Notes & Learnings
Refer to [Learning Summary](./LearningSummary.md).


## Other Reference Documentation
* [Spring Cloud](https://spring.io/cloud)
* [Spring Web](https://docs.spring.io/spring-boot/docs/3.1.0/reference/htmlsingle/#web)
* [Spring Data](https://docs.spring.io/spring-boot/docs/3.1.0/reference/htmlsingle/#data)
* [Spring Messaging](https://docs.spring.io/spring-boot/docs/3.1.0/reference/htmlsingle/#messaging)