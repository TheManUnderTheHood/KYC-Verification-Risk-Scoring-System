package com.example.kycverificationandscoringsystem.dto;

import com.example.kycverificationandscoringsystem.enums.KycStatus;
import com.example.kycverificationandscoringsystem.enums.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KycResponse {

    private Long id;
    private String userName;
    private String email;
    private String pan;
    private String aadhaar;
    private String address;
    private LocalDate dob;
    private String occupation;
    private KycStatus status;
    private Integer riskScore;
    private RiskLevel riskLevel;
    private String reviewNotes;
    private List<String> documentPaths;
}
