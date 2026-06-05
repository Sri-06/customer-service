package org.reward.util;

import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.reward.util.RewardUtil;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RewardUtilTest {

    @Spy
    private  RewardUtil rewardUtil;

    @Test
    void test150Amount() {
        assertEquals(150, rewardUtil.calculate(BigDecimal.valueOf(150)));
    }

    @Test
    void test80Amount() {
        assertEquals(30, rewardUtil.calculate(BigDecimal.valueOf(80)));
    }

    @Test
    void test45Amount() {
        assertEquals(0, rewardUtil.calculate(BigDecimal.valueOf(45)));
    }

    @Test
    void test55Amount() {
        assertEquals(5, rewardUtil.calculate(BigDecimal.valueOf(55)));
    }

    @Test
    void test110Amount() {
        assertEquals(70, rewardUtil.calculate(BigDecimal.valueOf(110)));
    }

    @Test
    void test200Amount() {
        assertEquals(250, rewardUtil.calculate(BigDecimal.valueOf(200)));
    }

    @Test
    void testZeroAmount() {
        assertEquals(0, rewardUtil.calculate(BigDecimal.ZERO));
    }

    @Test
    void testNegativeAmount() {
        assertEquals(0, rewardUtil.calculate(BigDecimal.valueOf(-25)));
    }

    @Test
    void testNullAmount() {
        assertEquals(0, rewardUtil.calculate(null));
    }
}