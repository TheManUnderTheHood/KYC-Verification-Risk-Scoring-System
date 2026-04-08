package com.example.kycverificationandscoringsystem.service;

import com.example.kycverificationandscoringsystem.dto.ApiResponse;
import com.example.kycverificationandscoringsystem.dto.KycResponse;
import com.example.kycverificationandscoringsystem.dto.KycReviewRequest;
import com.example.kycverificationandscoringsystem.dto.KycSubmissionRequest;
import com.example.kycverificationandscoringsystem.entity.Document;
import com.example.kycverificationandscoringsystem.entity.KYCProfile;
import com.example.kycverificationandscoringsystem.entity.User;
import com.example.kycverificationandscoringsystem.enums.KycStatus;
import com.example.kycverificationandscoringsystem.enums.RiskLevel;
import com.example.kycverificationandscoringsystem.repository.KYCProfileRepository;
import com.example.kycverificationandscoringsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KycService {

    private final KYCProfileRepository kycProfileRepository;
    private final UserRepository userRepository;

    public ApiResponse submitKyc(KycSubmissionRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (kycProfileRepository.findByUserId(user.getId()).isPresent()) {
            throw new RuntimeException("You have already submitted your KYC!");
        }

        KYCProfile kycProfile = KYCProfile.builder()
                .user(user)
                .pan(request.getPan().toUpperCase())
                .aadhaar(request.getAadhaar())
                .address(request.getAddress())
                .dob(request.getDob())
                .occupation(request.getOccupation())
                .status(KycStatus.PENDING)
                .documents(new ArrayList<>())
                .build();

        if (request.getDocuments() != null) {
            for (MultipartFile file : request.getDocuments()) {
                if (!file.isEmpty()) {
                    String savedPath = saveFileLocally(file);
                    kycProfile.getDocuments().add(new Document(null, file.getOriginalFilename(), savedPath, null, kycProfile));
                }
            }
        }

        calculateRisk(kycProfile);

        kycProfileRepository.save(kycProfile);
        return new ApiResponse(true, "KYC Submitted successfully.");
    }

    // NEW METHOD: Fetch the current user's actual KYC status from the database
    public KycResponse getMyKyc(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return kycProfileRepository.findByUserId(user.getId())
                .map(this::mapToDto)
                .orElse(null); // Returns null if they haven't submitted yet
    }

    // NEW METHOD: Check if the user's account is already funded
    public boolean isUserFunded(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.isAccountFunded();
    }

    private String saveFileLocally(MultipartFile file) {
        try {
            Path uploadDir = Paths.get("uploads").toAbsolutePath().normalize();
            Files.createDirectories(uploadDir);

            String fileName = UUID.randomUUID().toString() + "_" + StringUtils.cleanPath(file.getOriginalFilename());
            Path targetLocation = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return "uploads/" + fileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not save file", ex);
        }
    }

    private void calculateRisk(KYCProfile profile) {
        int score = 0;
        int age = profile.getDob() != null ? Period.between(profile.getDob(), LocalDate.now()).getYears() : 0;

        if (age < 18) score += 80;
        else if (age < 21) score += 20;

        List<String> riskyJobs = Arrays.asList("POLITICIAN", "GAMBLING", "CASINO");
        if (riskyJobs.contains(profile.getOccupation().toUpperCase())) score += 30;

        if (profile.getDocuments().size() < 2) score += 25;

        profile.setRiskScore(score);
        profile.setRiskLevel(score >= 60 ? RiskLevel.HIGH : (score >= 20 ? RiskLevel.MEDIUM : RiskLevel.LOW));
    }

    public List<KycResponse> getPendingKycs() {
        return kycProfileRepository.findByStatus(KycStatus.PENDING).stream().map(this::mapToDto).toList();
    }

    public List<KycResponse> getHighRiskKycs() {
        return kycProfileRepository.findByRiskLevel(RiskLevel.HIGH).stream().map(this::mapToDto).toList();
    }

    public ApiResponse reviewKyc(Long kycId, KycReviewRequest request) {
        KYCProfile kycProfile = kycProfileRepository.findById(kycId)
                .orElseThrow(() -> new RuntimeException("KYC Not Found"));

        kycProfile.setStatus(request.getStatus());
        kycProfile.setReviewNotes(request.getReviewNotes());
        kycProfileRepository.save(kycProfile);
        return new ApiResponse(true, "KYC Reviewed Successfully");
    }

    private KycResponse mapToDto(KYCProfile profile) {
        List<String> docPaths = profile.getDocuments().stream().map(Document::getFilePath).toList();
        return new KycResponse(profile.getId(), profile.getUser().getName(), profile.getUser().getEmail(),
                profile.getPan(), profile.getAadhaar(), profile.getAddress(), profile.getDob(),
                profile.getOccupation(), profile.getStatus(), profile.getRiskScore(), profile.getRiskLevel(),
                profile.getReviewNotes(), docPaths);
    }
}