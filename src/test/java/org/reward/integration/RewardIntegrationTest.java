package org.reward.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class RewardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
                .andExpect(jsonPath("$.totalPoints").value(org.hamcrest.Matchers.greaterThanOrEqualTo(0)));
    }
}