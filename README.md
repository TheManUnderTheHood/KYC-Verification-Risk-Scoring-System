# 🛡️ Full-Stack KYC Verification & Neo-Bank Onboarding System

A complete, enterprise-grade full-stack application that simulates a real-world financial compliance and onboarding workflow. This system handles secure user registration, automated fraud risk scoring, manual admin reviews, and final wallet activation via Stripe payments.

## 🌟 Key Features

*   **🎨 Modern Animated Frontend:** Built with React, Vite, and **Tailwind CSS v4**. Features premium glassmorphism UI, staggered table reveals, and physics-based animations using **Framer Motion**.
*   **💳 Payment Integration (Stripe):** Once KYC is approved, users must make a simulated $50 initial deposit via a secure Stripe Checkout session to activate their neo-bank wallet.
*   **🔒 Stateless Authentication (JWT):** Secure login and registration using JSON Web Tokens.
*   **🛡️ Role-Based Access Control (RBAC):** Strict security separation between normal `USER` dashboards and protected `ADMIN` review panels.
*   **🧠 Automated Risk Engine:** Automatically calculates a risk score (LOW, MEDIUM, HIGH) upon KYC submission based on age, occupation, and document completeness.
*   **📁 Secure File Storage:** Processes `multipart/form-data` to securely upload and save physical identity documents (PAN/Aadhaar).

## 🛠️ Tech Stack

### Frontend
*   **Framework:** React + Vite
*   **Styling:** Tailwind CSS v4 (Latest)
*   **Animations:** Framer Motion
*   **Icons & Routing:** Lucide React, React Router, Axios

### Backend
*   **Language & Framework:** Java 17, Spring Boot 3.x
*   **Security:** Spring Security & JJWT (JSON Web Tokens)
*   **Database:** Cloud MySQL & Spring Data JPA (Hibernate)
*   **Payment Gateway:** Stripe Java SDK
*   **Documentation:** OpenAPI (Swagger 3)

## 🚦 The Onboarding User Journey

1.  **Registration:** User creates an account and receives a secure JWT.
2.  **Identity Verification:** User submits PII (PAN, Aadhaar) and uploads document images.
3.  **Algorithmic Scoring:** The backend Business Rule Engine calculates a fraud risk score.
    *   *Minors (+80 pts), High-Risk Jobs like Casinos (+30 pts), Missing Docs (+25 pts).*
4.  **Compliance Review:** Admin logs in, views the risk queue, and Approves or Rejects the profile.
5.  **Wallet Activation:** The user is prompted to pay a $50 deposit via **Stripe**.
6.  **Success:** Backend verifies the Stripe webhook/session and officially activates the user's account.

## 🔌 API Endpoints Reference

### 🔓 Authentication (Public)
*   `POST /api/auth/register` - Create account (`USER` or `ADMIN`)
*   `POST /api/auth/login` - Authenticate and receive JWT

### 👤 User Operations (Requires `USER` Token)
*   `GET /api/user/kyc/status` - Fetch real-time KYC & Funding status
*   `POST /api/user/kyc/submit` - Submit KYC text data and physical files
*   `POST /api/user/payment/initiate` - Generate a Stripe Checkout URL
*   `GET /api/user/payment/success` - Verify Stripe payment and activate wallet

### 🛡️ Admin Operations (Requires `ADMIN` Token)
*   `GET /api/admin/kyc/pending` - Fetch applications awaiting review
*   `GET /api/admin/kyc/high-risk` - Fetch applications flagged as `HIGH RISK`
*   `PUT /api/admin/kyc/{id}/review` - Approve or Reject a specific application

## 💻 How to Run Locally

### Prerequisites
*   Java 17 & Maven
*   Node.js (v20+)
*   A free [Stripe](https://stripe.com/) Developer Account (Test Mode)

### 1. Clone the repository
```bash
git clone https://github.com/TheManUnderTheHood/KYC-Verification-Risk-Scoring-System.git
cd KYC-Verification-Risk-Scoring-System
