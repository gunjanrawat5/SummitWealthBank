# Summit Wealth Bank

Summit Wealth Bank is a simulated retail banking application built with Java Spring Boot.  

The backend functionality is complete, including core banking, transaction handling, and wealth simulation features.
The frontend is under development with basic wiring planned, and security features (such as authentication and authorization) are scheduled for implementation in a later phase.

## Team Members

- Pranav Habib - phabib1@stevens.edu 
- Gunjan Rawat - grawat1@stevens.edu  
- Sayan Seal - sseal1@stevens.edu  
 

## Features Implemented (Backend)

### Account Management
- Open checking and savings accounts
- View all accounts by user ID
- Freeze/unfreeze account functionality via admin endpoints

### Transfers
- Internal transfers between accounts
- Validation for frozen status and sufficient balance


### Admin Ops
- Simulated KYC freeze/unfreeze flag
- Prevents transfers or wealth activity when frozen

### Wealth Module
- Risk questionnaire input 
- Auto-assigned ETF allocation 
- Simulated buy/sell of ETF units using account balance
- Fake portfolio tracking using geometric random walk for price simulation



### Backend
- Java 21
- Spring Boot (REST API)
- Spring Data JPA / Hibernate
- Postgres / H2 (for development)
- Maven
- JUnit5 (planned for unit testing)

## Frontend
