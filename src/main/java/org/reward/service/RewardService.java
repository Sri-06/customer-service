package org.reward.service;

import org.reward.dto.RewardResponseDto;

public interface RewardService {

    RewardResponseDto calculateRewards(Long customerId);
}