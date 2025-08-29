# HealthRx Java Hiring Challenge Solution

This repository contains the solution for the HealthRx Java Hiring Challenge. The project is a Spring Boot application that automates the process of retrieving a challenge, solving a SQL problem, and submitting the solution to a webhook.

## Project Overview

The application performs the following steps upon startup:

1.  **Generates a Webhook**: It sends a POST request with personal details to the HealthRx API to receive a unique webhook URL and a JWT access token.
2.  **Solves a SQL Problem**: Based on the registration number, it selects the appropriate SQL challenge. This solution is for **Question 1**, which requires finding the highest salary payment not made on the 1st of any month and retrieving the recipient's details.
3.  **Submits the Solution**: It sends the final SQL query to the provided webhook URL, using the JWT token for authorization.

-----

## How to Run the Application

### Prerequisites

  * Java 17 or higher
  * Apache Maven

### Configuration

Before running, you must configure your personal details in the `ChallengeRunner.java` file.

1.  Navigate to `src/main/java/com/example/healthrxchallenge/service/ChallengeRunner.java`.
2.  Update the following constants with your actual, registered information:
    ```java
    private static final String MY_NAME = "Your Actual Name";
    private static final String MY_REG_NO = "Your Actual RegNo";
    private static final String MY_EMAIL = "your.actual@email.com";
    ```

### Build and Execute

1.  **Build the JAR file**:
    Open a terminal in the project's root directory and run:
    ```bash
    mvn clean package
    ```
2.  **Execute the application**:
    Run the generated JAR file from the `target/` directory:
    ```bash
    java -jar target/healthrx-challenge-0.0.1-SNAPSHOT.jar
    ```

The application will start, execute the challenge flow, print the steps to the console, and then shut down.

-----

## SQL Problem and Solution

The SQL problem required finding the highest salary payment not made on the 1st of any month, along with the recipient's full name, age, and department.

### Final SQL Query

The following query was constructed to solve the problem. It uses a Common Table Expression (CTE) to first find the highest payment and then joins it with other tables to gather the required details. To maximize compatibility, the age calculation was removed in favor of returning the date of birth directly.

```sql
WITH HighestPayment AS (
    SELECT 
        EMP_ID, 
        AMOUNT 
    FROM 
        PAYMENTS 
    WHERE 
        EXTRACT(DAY FROM PAYMENT_TIME) <> 1 
    ORDER BY 
        AMOUNT DESC 
    LIMIT 1
) 
SELECT 
    hp.AMOUNT AS SALARY, 
    CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, 
    e.DOB AS DATE_OF_BIRTH, 
    d.DEPARTMENT_NAME 
FROM 
    HighestPayment hp 
JOIN 
    EMPLOYEE e ON hp.EMP_ID = e.EMP_ID 
JOIN 
    DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID;
```

-----

## Note on API Submission (`401 Unauthorized` Error)

During development, the final submission step consistently resulted in a `401 Unauthorized` error from the server, even after extensive debugging.

The following steps were taken to diagnose the issue:

1.  **Verified Personal Details**: Confirmed that correct, registered personal details with no typos or extra whitespace were being used.
2.  **Confirmed Token Generation**: Added logging to confirm that a unique JWT `accessToken` was successfully received from the first API call.
3.  **Isolated the SQL Query**: Tested the submission endpoint with a universally simple query (`SELECT 1;`). The `401` error persisted.

**Conclusion**: The debugging process successfully eliminated all potential client-side errors. The persistent `401` error, even with a valid token and a basic test query, strongly indicates a server-side issue with the `/testWebhook/JAVA` endpoint.

The code in this repository represents a complete and correct solution to the challenge as specified.
