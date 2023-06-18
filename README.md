# Spring Boot Microservice - Online Shopping Application
This project is inspired by: [Video](https://www.youtube.com/watch?v=mPPhcU7oWDU&t=20634s), [Source Code](https://github.com/SaiUpadhyayula/spring-boot-microservices)

This project features the following:
1. **Product Service** - Create and view products, act as product catalogue
2. **Order Service** - Order products
3. **Inventory Service** - Check if product is in stock or not
4. **Notification Service** - Send notifications, after order is placed

System Architecture:
![Application System Architecture](/figure/System%20Architecture.png)

## Pre-requisites
1. Java SDK 11
2. MySQL
3. MongoDB

## How to Run
#### Clone this repository and build the project
```bash
git clone https://github.com/LeeXuanHua/Spring-Boot-Microservices.git
cd spring-boot-microservices
./gradlew clean build
```

#### Set up MySQL database
1. Edit `importMySQLCredentials.sh.stub` to include your MySQL credentials
2. Rename `importMySQLCredentials.sh.stub` to `importMySQLCredentials.sh`
3. Ensure that MySQL is running with the following empty databases:
   1. `order_service`
   2. `inventory_service`

**Note that MongoDB do not require any setup for the database.**

#### To run all the microservices, run the following commands
```bash
# On all terminals from spring-boot-microservices/, run the following command:
source ./importMySQLCredentials.sh

# On separate terminals, for each of the following pair, run one of the commands:
./gradlew :product-service:bootRun
java -jar product-service/build/libs/product-service-0.0.1-SNAPSHOT.jar

./gradlew :order-service:bootRun
java -jar order-service/build/libs/order-service-0.0.1-SNAPSHOT.jar

./gradlew :inventory-service:bootRun
java -jar inventory-service/build/libs/inventory-service-0.0.1-SNAPSHOT.jar
```

#### For usage with IntelliJ IDEA
Configure environment variables for the following run configurations:
1. `product-service`
2. `order-service`
3. `inventory-service`
![IntelliJ Run Configuration](/figure/IntelliJ_RunConfiguration.png)


## How to Test Manually via Postman
Refer to [Testing](./Testing.md).


## Changes from Video Tutorial
1. Used Gradle instead of Maven
2. Implemented testings & added Jacoco for test coverage


## Notes & Learnings
Refer to [Learning Summary](./LearningSummary.md).


## Other Reference Documentation
* [Spring Cloud](https://spring.io/cloud)
* [Spring Web](https://docs.spring.io/spring-boot/docs/3.1.0/reference/htmlsingle/#web)
* [Spring Data](https://docs.spring.io/spring-boot/docs/3.1.0/reference/htmlsingle/#data)
* [Spring Messaging](https://docs.spring.io/spring-boot/docs/3.1.0/reference/htmlsingle/#messaging)