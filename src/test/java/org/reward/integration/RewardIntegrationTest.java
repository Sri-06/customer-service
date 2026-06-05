package org.reward.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class RewardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/v1/rewards/{customerId}
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void rewardEndpoint_seededCustomer_returns200WithRewardData() throws Exception {

        mockMvc.perform(get("/api/v1/rewards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(1))
                .andExpect(jsonPath("$.totalPoints").isNumber())
                .andExpect(jsonPath("$.monthlyPoints").isMap());
    }

    @Test
    void rewardEndpoint_nonExistentCustomer_returns404() throws Exception {

        mockMvc.perform(get("/api/v1/rewards/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rewardEndpoint_negativeCustomerId_returns404() throws Exception {

        mockMvc.perform(get("/api/v1/rewards/-1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void rewardEndpoint_seededCustomer_totalPointsIsNonNegative() throws Exception {

        mockMvc.perform(get("/api/v1/rewards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPoints").value(greaterThanOrEqualTo(0)));
    }

    @Test
    void rewardEndpoint_secondSeededCustomer_returns200WithRewardData() throws Exception {

        mockMvc.perform(get("/api/v1/rewards/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(2))
                .andExpect(jsonPath("$.totalPoints").isNumber())
                .andExpect(jsonPath("$.monthlyPoints").isMap());
    }

    @Test
    void rewardEndpoint_nonNumericCustomerId_returns400() throws Exception {

        mockMvc.perform(get("/api/v1/rewards/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rewardEndpoint_seededCustomer_monthlyPointsMapIsNotEmpty() throws Exception {

        mockMvc.perform(get("/api/v1/rewards/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthlyPoints").isNotEmpty());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // GET /api/v1/rewards/getAllRewards
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void getAllRewardsEndpoint_returns200() throws Exception {

        mockMvc.perform(get("/api/v1/rewards/getAllRewards"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllRewardsEndpoint_returnsJsonArray() throws Exception {

        mockMvc.perform(get("/api/v1/rewards/getAllRewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAllRewardsEndpoint_seededData_returnsAtLeastTwoCustomers() throws Exception {

        mockMvc.perform(get("/api/v1/rewards/getAllRewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));
    }

    @Test
    void getAllRewardsEndpoint_eachEntryHasCustomerId() throws Exception {

        mockMvc.perform(get("/api/v1/rewards/getAllRewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].customerId").exists());
    }

    @Test
    void getAllRewardsEndpoint_eachEntryHasTotalPoints() throws Exception {

        mockMvc.perform(get("/api/v1/rewards/getAllRewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].totalPoints").exists());
    }

    @Test
    void getAllRewardsEndpoint_eachEntryHasMonthlyPoints() throws Exception {

        mockMvc.perform(get("/api/v1/rewards/getAllRewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].monthlyPoints").exists());
    }

    @Test
    void getAllRewardsEndpoint_allTotalPointsAreNonNegative() throws Exception {

        mockMvc.perform(get("/api/v1/rewards/getAllRewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].totalPoints", everyItem(greaterThanOrEqualTo(0))));
    }

    @Test
    void getAllRewardsEndpoint_containsBothSeededCustomerIds() throws Exception {

        mockMvc.perform(get("/api/v1/rewards/getAllRewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].customerId", hasItems(1, 2)));
    }
}