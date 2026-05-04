# Freelancer OS Backend

A Spring Boot application for managing freelancers, clients, invoices, projects, and expenses.

## Features

- User authentication and authorization
- Client management
- Invoice generation and management
- Project tracking
- Expense tracking
- Recurring invoices
- PDF invoice generation
- Email notifications

## Tech Stack

- Java 17
- Spring Boot
- Spring Security with JWT
- Spring Data JPA
- H2 Database (for development)
- Maven
- Email service integration

## Prerequisites

- Java 17 or higher
- Maven 3.6+

## Setup and Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/sohan1625/freelancer-os.git
   cd freelancer-os
   ```

2. Navigate to the project directory:
   ```bash
   cd freelancer-os
   ```

3. Build the project:
   ```bash
   ./mvnw clean install
   ```

4. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

The application will start on `http://localhost:8081`.

## API Documentation

The API endpoints are documented using Swagger. Once the application is running, visit `http://localhost:8081/swagger-ui.html` for interactive API documentation.

## Database

The application uses H2 in-memory database for development. Data is not persisted between restarts.

## Frontend

The frontend dashboard is available at: [Freelancer Dashboard](https://github.com/sohan1625/freelancer-dashboard)

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License.