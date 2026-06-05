package org.reward.service;

import org.reward.dto.RewardResponseDto;

import java.util.List;

public interface RewardService {

    RewardResponseDto calculateRewards(Long customerId);
    List<RewardResponseDto> getAllRewards();
}