package com.example.kycverificationandscoringsystem.dto;

import com.example.kycverificationandscoringsystem.enums.KycStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KycReviewRequest {

    @NotNull(message = "Status is required (APPROVED or REJECTED)")
    private KycStatus status;

    private String reviewNotes;
}
