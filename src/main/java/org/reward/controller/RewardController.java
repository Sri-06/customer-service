package org.reward.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reward.dto.RewardResponseDto;
import org.reward.service.RewardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/rewards")
@RequiredArgsConstructor
public class RewardController {
    private final RewardService rewardService;

    @GetMapping("/{customerId}")
    public ResponseEntity<RewardResponseDto> getRewards(@PathVariable @NotNull Long customerId) {
        RewardResponseDto response = rewardService.calculateRewards(customerId);
        return ResponseEntity.ok(response);
    }
}