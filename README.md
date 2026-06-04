# Customer Rewards API

A Spring Boot REST API that calculates customer reward points based on their transactions from the last 3 months.

---

## Features

* Calculate reward points for a customer
* Monthly reward point aggregation
* Total reward point calculation
* Validation and exception handling
* RESTful API design
* H2 in-memory database
* Unit and Integration Tests
* Layered architecture (Controller, Service, Repository)

---

## Tech Stack

* Java 21
* Spring Boot 3.5
* Spring Data JPA
* H2 Database
* Lombok
* Jakarta Validation
* JUnit 5
* Mockito
* MockMvc

---

## Reward Calculation Logic

Rewards are calculated as follows:

* 2 points for every dollar spent over $100
* 1 point for every dollar spent between $50 and $100
* 0 points for purchases of $50 or less

### Examples

| Amount | Points |
| ------ | ------ |
| $40    | 0      |
| $75    | 25     |
| $120   | 90     |
| $180   | 210    |

Calculation for $120:

```text
50 points for amount between 50 and 100
+
20 × 2 points for amount above 100

Total = 90 points
```

---

## Project Structure

```text
src/main/java/org/reward

├── controller
├── service
├── repository
├── entity
├── dto
├── exception
├── util
└── RewardsApiApplication
```

---

## Sample Data

The application loads sample data automatically from:

```text
src/main/resources/data.sql
```

Customer:

```text
ID   Name
1    Raj
```

Sample transactions are inserted for the last three months.

---

## Running the Application

### Clone the Repository

```bash
git clone <repository-url>
```

### Build the Project

```bash
mvn clean install
```

### Run the Application

```bash
mvn spring-boot:run
```

Application starts on:

```text
http://localhost:8080
```

---

## H2 Database Console

URL:

```text
http://localhost:8080/h2-console
```

Example Configuration:

```text
JDBC URL : jdbc:h2:mem:testdb
Username : sa
Password :
```

---

## API Endpoints

### Get Rewards for a Customer

```http
GET /api/v1/rewards/{customerId}
```

Example:

```http
GET http://localhost:8080/api/v1/rewards/1
```

### Sample Response

```json
{
  "customerId": 1,
  "monthlyPoints": {
    "JUNE": 120,
    "MARCH": 300,
    "APRIL": 235
  },
  "totalPoints": 655
}
```

---

## Error Responses

### Customer Not Found

Request:

```http
GET /api/v1/rewards/99
```

Response:

```json
{
  "timestamp": "2026-06-04T20:15:30",
  "status": 404,
  "message": "Customer not found: 99"
}
```

---

### Invalid Customer Id

Request:

```http
GET /api/v1/rewards/me
```

Response:

```json
{
  "timestamp": "2026-06-04T20:15:30",
  "status": 400,
  "message": "Invalid customer id"
}
```

---

## Testing

Run all tests:

```bash
mvn test
```

Test coverage includes:

* Reward utility calculations
* Service layer
* Controller layer
* Repository layer
* Integration testing using MockMvc

---

## Design Decisions

* Spring Boot auto-configuration used for rapid development.
* Reward calculation logic isolated in `RewardUtil`.
* Global exception handling implemented using `@RestControllerAdvice`.
* Constructor injection used throughout the application.
* H2 database used for lightweight local execution and testing.

---

## Future Enhancements

* Pagination support
* Swagger/OpenAPI documentation
* Docker support
* Authentication and Authorization using Spring Security
* External database support (MySQL/PostgreSQL)
* Caching using Redis

---

## Author

Sri Raja Rajeshwari

Java / Spring Boot Developer
