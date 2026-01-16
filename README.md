# Testing-WorldCup

## 1. Introduction
This project is a **World Cup management system simulation developed in Java**, with a primary focus on **software testing**.

The goal of the project is to design a football tournament management domain and **apply comprehensive testing strategies**, including **Unit Testing, Integration Testing, and Automated Testing**, to ensure correctness and reliability of the system.

---

## 2. Main Features
- Manage core World Cup entities:
  - Teams
  - Players
  - Matches
  - Goals
  - Group stages and knockout stages
- Generate sample data:
  - Sample teams
  - Match fixtures
  - Match results calculation
- Implemented **200+ Unit Test cases** for core components:
  - Player
  - Team
  - Match
  - Goal
  - Tournament
- Performed **Integration Testing** to verify interactions between modules
- Applied **automated testing** using JUnit

---

## 3. Technologies Used
- **Programming Language:** Java
- **Testing Framework:** JUnit
- **Build & Dependency Management:** Maven (`pom.xml`)
- **Database:** SimpleDB (simulated data storage)

---

## 4. Project Structure
- `src/main/java/com/worldcup/`  
  Core application logic and domain models  
  (Player, Team, Match, Tournament, Services, DAOs)

- `src/test/java/com/worldcup/`  
  Unit Tests and Integration Tests

- `data/worldcup.db`  
  Simulated database file

- `pom.xml`  
  Maven configuration file

---

## 5. Installation & Run Guide
1. Clone the project from GitHub
2. Open the project using **IntelliJ IDEA** or **VS Code**
3. Build the project using Maven:
   ```bash
   mvn clean install
4. Run all test cases by "mvn test"

---

## 6. 
- Designed a clear domain model for a football tournament system
- Applied Unit Testing extensively to validate individual components
- Implemented Integration Tests to verify data flow between services and DAOs
- Used JUnit assertions to validate business rules and match logic
- Automated the entire test execution process using Maven lifecycle
- Structured codebase to ensure testability and maintainability

---

## 7. What I Learned
- Writing effective Unit Tests for business logic
- Designing and executing Integration Tests
- Using JUnit for automated testing
- Managing Java projects with Maven
- Improving code quality through test-driven and test-focused development
- Understanding testing strategies in a real-world domain model

---

## 8. Screenshots

Unit Test Results

<img width="871" height="134" alt="Screenshot 2025-09-05 at 19 23 29" src="https://github.com/user-attachments/assets/8a0ab2bb-de66-4304-8de8-27cb6ab8d976" />



Integration Test Results

<img width="708" height="222" alt="Screenshot 2025-09-05 at 19 23 46" src="https://github.com/user-attachments/assets/6d96eccb-87a2-4e2a-bad8-1db30a446e1a" />



Match Simulation Output (Partial)

<img width="398" height="235" alt="Screenshot 2025-09-05 at 19 26 52" src="https://github.com/user-attachments/assets/2a64bdc1-aaeb-4c32-b112-d48687fe9d01" />

<img width="398" height="235" alt="Screenshot 2025-09-05 at 19 26 52" src="https://github.com/user-attachments/assets/fcefb9a9-23c9-4cbe-a953-9ded0ba2ae5e" />

<img width="333" height="413" alt="Screenshot 2025-09-05 at 19 27 43" src="https://github.com/user-attachments/assets/ac525891-a846-41a2-8bec-525dbb12127c" />

<img width="383" height="123" alt="Screenshot 2025-09-05 at 19 27 50" src="https://github.com/user-attachments/assets/4849c085-1dc2-4104-8eb3-85ba95f531f7" />
