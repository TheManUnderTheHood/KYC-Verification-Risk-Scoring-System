package com.example.kycverificationandscoringsystem.controller;

import com.example.kycverificationandscoringsystem.dto.ApiResponse;
import com.example.kycverificationandscoringsystem.dto.KycResponse;
import com.example.kycverificationandscoringsystem.dto.KycReviewRequest;
import com.example.kycverificationandscoringsystem.dto.KycSubmissionRequest;
import com.example.kycverificationandscoringsystem.service.KycService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class KycController {

    private final KycService kycService;

    @PostMapping(value = "/api/user/kyc/submit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> submitKyc(
            @Valid @ModelAttribute KycSubmissionRequest request,
            Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(kycService.submitKyc(request, userEmail));
    }

    // NEW ENDPOINT: Returns both the KYC status AND if the account is funded
    @GetMapping("/api/user/kyc/status")
    public ResponseEntity<Map<String, Object>> getMyKycStatus(Authentication authentication) {
        String userEmail = authentication.getName();

        KycResponse myKyc = kycService.getMyKyc(userEmail);
        boolean isFunded = kycService.isUserFunded(userEmail);

        if (myKyc != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", myKyc.getStatus().name());
            response.put("isFunded", isFunded);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.noContent().build(); // 204 No Content
        }
    }

    @GetMapping("/api/admin/kyc/pending")
    public ResponseEntity<List<KycResponse>> getPendingKycs() {
        return ResponseEntity.ok(kycService.getPendingKycs());
    }

    @GetMapping("/api/admin/kyc/high-risk")
    public ResponseEntity<List<KycResponse>> getHighRiskKycs() {
        return ResponseEntity.ok(kycService.getHighRiskKycs());
    }

    @PutMapping("/api/admin/kyc/{id}/review")
    public ResponseEntity<ApiResponse> reviewKyc(
            @PathVariable("id") Long kycId,
            @Valid @RequestBody KycReviewRequest request) {
        return ResponseEntity.ok(kycService.reviewKyc(kycId, request));
    }
}