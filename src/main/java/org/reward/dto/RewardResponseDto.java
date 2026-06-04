package org.reward.dto;
import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewardResponseDto {

    private Long customerId;
    private Map<String, Integer> monthlyPoints;
    private Integer totalPoints;
}
