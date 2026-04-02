# 🛡️ KYC Verification & Risk Scoring System

A robust backend REST API built with Spring Boot that simulates a real-world financial compliance workflow. This system handles user onboarding, secure document uploads, and features an **Automated Risk Rule Engine** to flag potential fraud based on user demographics and data.

## 🌟 Key Features

*   **Stateless Authentication:** Secure login and registration using JSON Web Tokens (JWT).
*   **Role-Based Access Control (RBAC):** Strict separation between normal `USER` endpoints and protected `ADMIN` endpoints.
*   **Automated Risk Engine:** Automatically calculates a risk score (LOW, MEDIUM, HIGH) upon KYC submission based on age, occupation, and document completeness.
*   **File Storage:** Processes `multipart/form-data` to securely save physical uploaded identity documents (PAN/Aadhaar) to the local server.
*   **Cloud Database Integration:** Fully integrated with a global Cloud MySQL database using Spring Data JPA.
*   **Interactive API Docs:** Built-in Swagger UI for testing endpoints without Postman.

## 🛠️ Tech Stack

*   **Language:** Java 17
*   **Framework:** Spring Boot 3.x
*   **Security:** Spring Security & JJWT (JSON Web Tokens)
*   **Database:** Cloud MySQL & Spring Data JPA (Hibernate)
*   **Documentation:** OpenAPI (Swagger 3)
*   **Tooling:** Maven, Lombok, Jakarta Validation

## 🧠 How the Risk Engine Works

Unlike a basic CRUD application, this project includes a **Business Rule Engine**. When a user submits their KYC, the system calculates a score:

1.  **Age Check:** Minors (< 18) receive **+80 points**. Young adults (< 21) receive **+20 points**.
2.  **Occupation Check:** High-risk sectors (e.g., Politicians, Casinos, Gambling) receive **+30 points**.
3.  **Document Check:** Submitting fewer than 2 documents adds **+25 points**.

**Classification:**
*   `0 - 19 Points` = **LOW RISK**
*   `20 - 59 Points` = **MEDIUM RISK**
*   `60+ Points` = **HIGH RISK**

## 🔌 API Endpoints Reference

### 🔓 Authentication (Public)
*   `POST /api/auth/register` - Create a new user account (Specify role as `USER` or `ADMIN`).
*   `POST /api/auth/login` - Authenticate and receive a JWT token.

### 👤 User Operations (Requires `USER` Token)
*   `POST /api/user/kyc/submit` - Submit KYC details (PAN, Aadhaar, DOB) and upload physical document images.

### 🛡️ Admin Operations (Requires `ADMIN` Token)
*   `GET /api/admin/kyc/pending` - Fetch a list of all applications awaiting manual review.
*   `GET /api/admin/kyc/high-risk` - Fetch only users flagged as `HIGH RISK` by the automated engine.
*   `PUT /api/admin/kyc/{id}/review` - Approve or Reject a specific KYC application and add review notes.

## 💻 How to Run Locally

### 1. Clone the repository
```bash
git clone https://github.com/TheManUnderTheHood/KYC-Verification-Risk-Scoring-System.git
cd KYC-Verification-Risk-Scoring-System
