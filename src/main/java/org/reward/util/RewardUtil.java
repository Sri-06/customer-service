package org.reward.util;

import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
public class RewardUtil {
    public static int calculate(BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.valueOf(50)) <= 0) {
            return 0;
        }

        int points = 0;

        if (amount.compareTo(BigDecimal.valueOf(100)) > 0) {
            points += amount.subtract(BigDecimal.valueOf(100)).multiply(BigDecimal.valueOf(2)).intValue();

            points += 50;
        } else {
            points += amount.subtract(BigDecimal.valueOf(50)).intValue();
        }

        return points;
    }
}
