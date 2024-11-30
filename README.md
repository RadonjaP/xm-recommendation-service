# XM Recommendation Service

## Overview

The XM Recommendation Service is a Java-based application that provides recommendations for best investment in crypto
world based on various data sources.
It uses Spring Boot for application configuration and management, and H2 as the in-memory database for data storage.

## Features

- Ingestion of data from multiple sources
    - Application supports ingestion of data from multiple sources, such as REST APIs, CSV files, and databases.
- Storage and retrieval of rates and symbol properties
- Adjustable normalization range
    - The normalization range can be adjusted for each cryptocurrency (monthly, half-yearly, yearly).
- Transparency over data status and ingestion process
    - Provides information about the status of the data precision through RAG system.
    - Provides information about the ingestion process.
- Locking mechanism for symbol properties
    - Contributes to data consistency and integrity by preventing concurrent updates to symbol properties.
- Asynchronous processing
    - Ingestion process can be started asynchronously to improve performance of reading from multiple sources.
- Caching for improved performance
- Rate Limiting
    - Prevents abuse of the application by limiting the number of requests that can be made in a given time period.
- Basic Authentication


### Endpoints

Available at [Swagger UI](http://localhost:8080/swagger-ui.html) after application is started.

**Ingestion Controller**
- **GET /api/v1/ingestion/start**
    - **Description**: Starts the ingestion process for all files in the source location.
    - **Responses**:
        - `200 OK`: Ingestion started successfully.
        - `500 Internal Server Error`: Error fetching file paths from the source location.

**Recommendation Controller**

- **GET /api/v1/recommendation/normalized-range**
    - **Description**: Fetch descending sorted list of all cryptos comparing normalized range.
    - **Responses**:
        - `200 OK`: Success.
        - 
- **GET /api/v1/recommendation/normalized-range/highest**
    - **Description**: Get the crypto with the highest normalized range for a specific day.
    - **Parameters**:
        - `date` (query parameter, required): Specific day in format `dd-MM-yyyy`.
    - **Responses**:
        - `200 OK`: Success.
        - `400 Bad Request`: Invalid date format.
        - 
- **GET /api/v1/recommendation/stats/{symbol}/info**
    - **Description**: Get the oldest, newest, min, and max values for a requested crypto.
    - **Parameters**:
        - `symbol` (path variable, required): Symbol of the crypto.
    - **Responses**:
        - `200 OK`: Success.
        - `404 Not Found`: Crypto not found.

## Architecture
    
Application follows basic level of [Hexagonal Architecture](https://alistaircockburn.com/Hexagonal%20Budapest%2023-05-18.pdf), where the core business logic is separated from the
infrastructure and external dependencies. This allows for easier testing and maintenance of the application.
The idea was to keep application modular and technology completely replaceable, so that the application can be easily adapted to new
requirements or changes in the environment.

Instead of having multiple modules for adapters, domain, and application, the application is divided into packages.
This is done to keep the application simple and easy to understand, as well as to avoid over-engineering.

Separating business logic can go as far as decoupling from Spring Boot so for this reason annotations like 
@Component, @Service, @Repository are avoided in the core business logic.

Whole functionality is also represented as [CQRS](https://martinfowler.com/bliki/CQRS.html) pattern, where the application is divided into two parts:
- Command: Ingestion of data from multiple sources
- Query: Querying of data for recommendations

### Ingestion

Note: To perform CSV ingestion, the CSV file must be placed in the directory specified in variable SOURCE_PATH.

Designed as ETL pipeline it consists of several steps:

1. Register new ingestion process for symbol.
2. Try to acquire lock for symbol properties.
   - If lock is acquired, proceed to next step.
   - If lock is not acquired due to missing support for symbol, stop ingestion process.
   - If lock is not acquired due to already running ingestion process for symbol, stop ingestion process.
3. Fetch data from source.
4. Fetch existing data for time window. (eg. last month, last half year, last year)
5. Consolidate fetched data with existing data to get more complete data.
   - If price exists for given timestamp, stop the process.
6. Perform calculation and normalization of data.
7. Store rates into database.
8. Store calculated status into database.
9. Mark ingestion as successful.
10. Release lock for symbol properties.

In case of any error during the process, the ingestion process will be marked as failed and lock will be released.

### Querying

Querying is done through several endpoints that provide recommendations for best investment in crypto world based on various data sources.
There are no advanced capabilities for analytics over complete database, however it can be easily added with extensions in the future.

- Relevant endpoints will first verify that symbol is supported, otherwise the call will fail.
- Caching has been implemented to improve performance of the application.
- All endpoints are rate limited to prevent abuse of the application.

## Technologies Used

- Java 17
- Spring Boot
- H2 Database
- Maven
- SQL

Selected technologies are used to ensure that the application is robust, scalable, and maintainable.
Simplicity and ease of use are also considered in the selection of technologies.
They are widely used in the industry and have good community support, they are easy and quick to set up.
Unfortunately they do impose many limitations and possibilities, especially H2 Database comparing to other databases.

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.6.0 or higher

### Installation

1. Clone the repository:
    ```sh
    git clone https://github.com/RadonjaP/xm-recommendation-service.git
    ```
2. Navigate to the project directory:
    ```sh
    cd xm-recommendation-service
    ```
3. Build the project using Maven:
    ```sh
    mvn clean install
    ```

### Running the Application

To run the application, use the following command:

```sh
mvn spring-boot:run "-Dspring-boot.run.jvmArguments=-DSOURCE_PATH=<path_to_csv_files>"
```

For authentication, use the following credentials:
- Username: `user'
- Password: Auto generated password can be found in the console output after starting the application.

### Limitations

- H2 Database
    - Currently, it is used as the in-memory database for data storage. This means it will be reset every time the
      application is restarted. This is not suitable for production use but is used here for simplicity.
- Docker & Kubernetes
    - The application is not yet containerized using Docker due to lack of possibility to test containerization.
    - Dockerfile, deployment.yaml and service.yaml are already prepared, but not tested yet.
- Security
    - The application has only elementary security features, such as basic authentication.