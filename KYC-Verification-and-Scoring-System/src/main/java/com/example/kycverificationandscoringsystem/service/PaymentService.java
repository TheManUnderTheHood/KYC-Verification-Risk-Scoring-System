package com.example.kycverificationandscoringsystem.service;

import com.example.kycverificationandscoringsystem.entity.User;
import com.example.kycverificationandscoringsystem.enums.KycStatus;
import com.example.kycverificationandscoringsystem.repository.KYCProfileRepository;
import com.example.kycverificationandscoringsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final UserRepository userRepository;
    private final KYCProfileRepository kycProfileRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${stripe.api.secret-key:}")
    private String stripeSecretKey;

    @Value("${stripe.api.environment:https://api.stripe.com/v1}")
    private String stripeBaseUrl;

    @Value("${stripe.currency:inr}")
    private String stripeCurrency;

    @Value("${wallet.activation.amount.paise:50000}")
    private int walletActivationAmountPaise;

    @Value("${wallet.activation.success-url:http://localhost:5173/dashboard}")
    private String walletActivationSuccessUrl;

    @Value("${wallet.activation.cancel-url:http://localhost:5173/dashboard}")
    private String walletActivationCancelUrl;

    @Value("${stripe.mock-on-auth-failure:true}")
    private boolean mockOnStripeAuthFailure;

    /**
     * Starts a wallet activation payment for a user whose KYC is APPROVED.
     */
    public Map<String, Object> initiateActivationPayment(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        validateEligibility(user);

        String orderId = "WALLET_" + user.getId() + "_" + UUID.randomUUID().toString().substring(0, 8);

        if (stripeSecretKey == null || stripeSecretKey.isBlank()) {
            return buildMockInitResponse(orderId, walletActivationAmountPaise, "Stripe secret key is missing. Returning mock link for local testing.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBearerAuth(stripeSecretKey.trim());

        String body = buildStripeCheckoutPayload(orderId, user);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    stripeBaseUrl + "/checkout/sessions",
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );
            return buildStripeInitResponse(orderId, walletActivationAmountPaise, response.getBody());
        } catch (HttpClientErrorException.Unauthorized ex) {
            if (mockOnStripeAuthFailure) {
                return buildMockInitResponse(orderId, walletActivationAmountPaise, "Stripe credentials are unauthorized. Returning mock link for local testing.");
            }
            throw new RuntimeException("Stripe authentication failed (401). Verify stripe.api.secret-key.", ex);
        } catch (RestClientResponseException ex) {
            throw new RuntimeException("Failed to initialize payment with Stripe: HTTP " + ex.getStatusCode().value() + " - " + ex.getResponseBodyAsString(), ex);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to initialize payment with Stripe: " + ex.getMessage(), ex);
        }
    }

    /**
     * Verifies Stripe session payment and marks the wallet as ACTIVE_AND_FUNDED.
     */
    public String verifyPaymentAndActivateAccount(String userEmail, String sessionId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        validateEligibility(user);

        if (sessionId == null || sessionId.isBlank()) {
            throw new RuntimeException("Missing Stripe sessionId. Complete payment first and retry.");
        }

        if ((stripeSecretKey == null || stripeSecretKey.isBlank()) && mockOnStripeAuthFailure) {
            user.setAccountFunded(true);
            userRepository.save(user);
            return "Mock payment confirmed. Your wallet is now ACTIVE_AND_FUNDED.";
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(stripeSecretKey.trim());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    stripeBaseUrl + "/checkout/sessions/" + encode(sessionId),
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> responseBody = response.getBody();
            String paymentStatus = responseBody == null ? null : String.valueOf(responseBody.get("payment_status"));
            String clientReferenceId = responseBody == null ? null : String.valueOf(responseBody.get("client_reference_id"));

            if (!"paid".equalsIgnoreCase(paymentStatus)) {
                throw new RuntimeException("Payment is not completed yet. Current Stripe payment_status: " + paymentStatus);
            }

            if (clientReferenceId != null && !"null".equals(clientReferenceId) && !String.valueOf(user.getId()).equals(clientReferenceId)) {
                throw new RuntimeException("Payment session does not belong to this user.");
            }

            user.setAccountFunded(true);
            userRepository.save(user);
            return "Payment successful. Your wallet is now ACTIVE_AND_FUNDED.";
        } catch (RestClientResponseException ex) {
            throw new RuntimeException("Failed to verify Stripe payment: HTTP " + ex.getStatusCode().value() + " - " + ex.getResponseBodyAsString(), ex);
        }
    }

    private void validateEligibility(User user) {
        boolean isKycApproved = kycProfileRepository.findByUserId(user.getId())
                .map(kyc -> kyc.getStatus() == KycStatus.APPROVED)
                .orElse(false);

        if (!isKycApproved) {
            throw new RuntimeException("Your KYC must be APPROVED by an Admin before you can activate your wallet.");
        }

        if (user.isAccountFunded()) {
            throw new RuntimeException("Your account is already activated and funded!");
        }
    }

    private String buildStripeCheckoutPayload(String orderId, User user) {
        Map<String, String> params = new LinkedHashMap<>();
        params.put("mode", "payment");
        params.put("success_url", walletActivationSuccessUrl + "?session_id={CHECKOUT_SESSION_ID}");
        params.put("cancel_url", walletActivationCancelUrl);
        params.put("client_reference_id", String.valueOf(user.getId()));
        params.put("customer_email", user.getEmail());
        params.put("metadata[orderId]", orderId);
        params.put("metadata[userId]", String.valueOf(user.getId()));
        params.put("line_items[0][price_data][currency]", stripeCurrency);
        params.put("line_items[0][price_data][unit_amount]", String.valueOf(walletActivationAmountPaise));
        params.put("line_items[0][price_data][product_data][name]", "Wallet Activation Deposit");
        params.put("line_items[0][quantity]", "1");

        StringBuilder encoded = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (encoded.length() > 0) {
                encoded.append('&');
            }
            encoded.append(encode(entry.getKey())).append('=').append(encode(entry.getValue()));
        }
        return encoded.toString();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private Map<String, Object> buildStripeInitResponse(String orderId, int amountPaise, Map<String, Object> responseBody) {
        String sessionId = responseBody == null ? null : String.valueOf(responseBody.get("id"));
        String checkoutUrl = responseBody == null ? null : String.valueOf(responseBody.get("url"));

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("amountPaise", amountPaise);
        result.put("sessionId", sessionId);
        result.put("message", "Stripe Checkout session created. Open the link and complete wallet activation payment.");
        result.put("paymentLink", checkoutUrl);
        result.put("gateway", "STRIPE");
        return result;
    }

    private Map<String, Object> buildMockInitResponse(String orderId, int amountPaise, String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("orderId", orderId);
        result.put("amountPaise", amountPaise);
        result.put("sessionId", "mock-session-" + UUID.randomUUID());
        result.put("message", message);
        result.put("paymentLink", walletActivationSuccessUrl);
        result.put("gateway", "STRIPE_MOCK");
        return result;
    }
}
