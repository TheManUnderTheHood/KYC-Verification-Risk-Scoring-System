package com.example.kycverificationandscoringsystem.controller;

import com.example.kycverificationandscoringsystem.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Starts Stripe Checkout for the initial wallet activation deposit.
     */
    @PostMapping("/initiate")
    public ResponseEntity<Map<String, Object>> initiatePayment(Authentication authentication) {
        String userEmail = authentication.getName();
        Map<String, Object> response = paymentService.initiateActivationPayment(userEmail);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/success")
    public ResponseEntity<String> paymentSuccess(
            Authentication authentication,
            @RequestParam(name = "sessionId", required = false) String sessionId,
            @RequestParam(name = "session_id", required = false) String stripeSessionId
    ) {
        String userEmail = authentication.getName();
        String resolvedSessionId = (sessionId != null && !sessionId.isBlank()) ? sessionId : stripeSessionId;
        String successMessage = paymentService.verifyPaymentAndActivateAccount(userEmail, resolvedSessionId);
        return ResponseEntity.ok(successMessage);
    }
}