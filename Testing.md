# Testing Manually via Postman

## Notes
1. The JSON keys are case-sensitive and must follow the name defined in Java (i.e. in `dto/` or `model/`)
2. Include `lombok.config` to avoid writing unit tests for data classes (currently placed at root directory, but can also be placed in individual microservice project)
3. Unit tests are not written to satisfy the code coverage tool (i.e. do not need to test `SpringApplication.run(ProductServiceApplication.class, args)`)
4. Spring expects the data to be in JSON format, otherwise HTTP 400 Bad Request will be returned

## Prior to Implementing Service Discovery
- Product service is defined at port 8080
- Order service is defined at port 8081
- Inventory service is defined at port 8082

**After implementing service discovery (specifically `server.port=0`), the port number is now varied. Therefore, change the ports below accordingly.**

### Product Service
1. POST localhost:8080/api/product
    ```json
    {
        "name": "Iphone 12",
        "description": "iphone 12",
        "price": 1000
    }
    ```
2. GET localhost:8080/api/product


### Order Service
1. POST localhost:8081/api/order 
   HTTP Status 200 - Order placed successfully
   ```json
      {
         "orderLineItemsDtoList":[
            {
               "skuCode": "iphone_13",
               "price": 800,
               "quantity": 2
            },
            {
               "skuCode": "iphone_13_pro",
               "price": 1000,
               "quantity": 1
            }
         ]
      }
   ```
   HTTP Status 500 - Internal Server Error
   ```json
      {
         "orderLineItemsDtoList":[
            {
               "skuCode": "Samsung",
               "price": 800,
               "quantity": 1
            },
            {
               "skuCode": "Amazon Prime",
               "price": 10,
               "quantity": 4
            }
         ]
      }
   ```

### Inventory Service
~~1. GET localhost:8082/api/inventory/iphone_13~~ (Deprecated after implementing inter-process communication
1. GET localhost:8082/api/inventory?skucode=iphone_13&skuCode=iphone_13_pro&skuCode=samsung
   ```json
   [
       {
           "skuCode": "iphone_13_pro",
           "inStock": true
       }
   ]
   ```
   - Should return only 1 output, since `skucode` is not defined as params and `samsung` is not defined in database (Refer to `CommandLineRunner` in [InventoryServiceApplication.java](inventory-service/src/main/java/com/microservices/inventoryservice/InventoryServiceApplication.java))


## After Implementing Service Discovery, API Gateway, and OIDC Spring Security

### Obtaining Access Token via Postman
1. From Postman, go to `Authorization` tab and set the data according to the screenshot below:
    ![Sample of Obtaining Access Token via Postman](/figure/Security_PostmanSample.png)
   **Note**: `client_id` and `client_secret` are obtained from Keycloak, and Access Token URL resembles `http://localhost:8181/realms/spring-boot-microservices-realm/protocol/openid-connect/token`


### Making Queries to API Gateway
1. Set Authorization to OAuth 2.0 Bearer Token, using the access token obtained above
2. Similar to the above, but replace `localhost:<port>` with `localhost:8080` (where port 8080 is the port number of API Gateway)
3. Inventory Service is not exposed to API Gateway (undefined in api-gateway `application.properties` file). Instead, it is queried internally by Order Service. Therefore, the queries to Inventory Service will return `404 Not Found`.