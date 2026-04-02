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

import java.util.List;

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
