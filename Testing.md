# Testing Manually via Postman

## Notes
1. The JSON keys are case-sensitive and must follow the name defined in Java (i.e. in `dto/` or `model/`)
2. Include `lombok.config` to avoid writing unit tests for data classes (currently placed at root directory, but can also be placed in individual microservice project)
3. Unit tests are not written to satisfy the code coverage tool (i.e. do not need to test `SpringApplication.run(ProductServiceApplication.class, args)`)
4. Spring expects the data to be in JSON format, otherwise HTTP 400 Bad Request will be returned

## Prior to Implementing Discovery Service
- Product service is defined at port 8080
- Order service is defined at port 8081
- Inventory service is defined at port 8082

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
1. GET localhost:8082/api/inventory/iphone_13