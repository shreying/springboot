package com.example.healthrxchallenge.service;

import com.example.healthrxchallenge.dto.SolutionRequest;
import com.example.healthrxchallenge.dto.WebhookRequest;
import com.example.healthrxchallenge.dto.WebhookResponse;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ChallengeRunner implements CommandLineRunner {

    private final RestTemplate restTemplate;

    // --- Configuration Details ---
    private static final String GENERATE_WEBHOOK_URL = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
    private static final String MY_NAME = "Shreya Sahu";
    private static final String MY_REG_NO = "22BCG10027";
    private static final String MY_EMAIL = "shreyasahu2022@vitbhopal.ac.in";

    public ChallengeRunner(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            System.out.println("Starting the HealthRx Challenge flow...");

            // Step 1 & 2: Generate webhook and get access token
            WebhookResponse webhookDetails = getWebhookDetails();
            System.out.println("Successfully received webhook URL and token.");

            // Step 3: Solve the SQL problem
            String finalQuery = solveSqlProblem();
            System.out.println("SQL Problem Solved. The query is:\n" + finalQuery);

            // Step 4: Submit the solution
            submitSolution(webhookDetails, finalQuery);
            System.out.println("Challenge solution submitted successfully!");

        } catch (Exception e) {
            System.err.println("An error occurred during the challenge flow: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private WebhookResponse getWebhookDetails() {
        WebhookRequest requestPayload = new WebhookRequest(MY_NAME, MY_REG_NO, MY_EMAIL);
        HttpEntity<WebhookRequest> requestEntity = new HttpEntity<>(requestPayload);

        System.out.println("STEP 1: Sending POST to " + GENERATE_WEBHOOK_URL);
        ResponseEntity<WebhookResponse> response = restTemplate.postForEntity(
            GENERATE_WEBHOOK_URL,
            requestEntity,
            WebhookResponse.class
        );

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new RuntimeException("Failed to generate webhook. Status: " + response.getStatusCode());
        }
        return response.getBody();
    }

    private String solveSqlProblem() {

        return "WITH HighestPayment AS (SELECT EMP_ID, AMOUNT FROM PAYMENTS WHERE EXTRACT(DAY FROM PAYMENT_TIME) <> 1 ORDER BY AMOUNT DESC LIMIT 1) SELECT hp.AMOUNT AS SALARY, CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, e.DOB AS DATE_OF_BIRTH, d.DEPARTMENT_NAME FROM HighestPayment hp JOIN EMPLOYEE e ON hp.EMP_ID = e.EMP_ID JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID;";
    }

    private void submitSolution(WebhookResponse webhookDetails, String finalQuery) {
        String submissionUrl = webhookDetails.getWebhook();
        String accessToken = webhookDetails.getAccessToken();

        // --- DEBUGGING STEP: Print the received values ---
        System.out.println("-----------------------------------------");
        System.out.println("DEBUG: Webhook URL Received: " + submissionUrl);
        System.out.println("DEBUG: Access Token Received: " + accessToken);
        System.out.println("-----------------------------------------");

        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new RuntimeException("FATAL: Access Token is null or empty. Cannot submit solution.");
        }
        // --- END DEBUGGING STEP ---

        String submissionUrlToUse = "https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA";

        // Prepare headers with the JWT
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(accessToken);

        // Prepare the request body
        SolutionRequest solutionPayload = new SolutionRequest(finalQuery);
        HttpEntity<SolutionRequest> requestEntity = new HttpEntity<>(solutionPayload, headers);

        System.out.println("STEP 2: Submitting solution to " + submissionUrlToUse);
        ResponseEntity<String> response = restTemplate.postForEntity(
            submissionUrlToUse,
            requestEntity,
            String.class
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to submit solution. Status: " + response.getStatusCode() + " Body: " + response.getBody());
        }
        System.out.println("Submission response: " + response.getBody());
    }

    
}