package com.example.kycverificationandscoringsystem.repository;

import com.example.kycverificationandscoringsystem.entity.KYCProfile;
import com.example.kycverificationandscoringsystem.enums.KycStatus;
import com.example.kycverificationandscoringsystem.enums.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KYCProfileRepository extends JpaRepository<KYCProfile, Long> {
    Optional<KYCProfile> findByUserId(Long userId);
    List<KYCProfile> findByStatus(KycStatus status);
    List<KYCProfile> findByRiskLevel(RiskLevel riskLevel);
    boolean existsByPan(String pan);
    boolean existsByAadhaar(String aadhaar);
}
