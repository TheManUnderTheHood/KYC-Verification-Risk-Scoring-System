package com.example.kycverificationandscoringsystem.entity;

import com.example.kycverificationandscoringsystem.enums.KycStatus;
import com.example.kycverificationandscoringsystem.enums.RiskLevel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "kyc_profiles")
public class KYCProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true, length = 10)
    private String pan;

    @Column(nullable = false, unique = true, length = 12)
    private String aadhaar;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false)
    private LocalDate dob;

    @Column(nullable = false)
    private String occupation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycStatus status;

    @Column(nullable = false)
    private Integer riskScore;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RiskLevel riskLevel;

    @Column(length = 1000)
    private String reviewNotes;

    @OneToMany(mappedBy = "kycProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Document> documents = new ArrayList<>();
}
