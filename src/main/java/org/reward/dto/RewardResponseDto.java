package org.reward.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
