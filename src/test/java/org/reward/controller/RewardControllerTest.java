package org.reward.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reward.controller.RewardController;
import org.reward.dto.RewardResponseDto;
import org.reward.exception.CustomerNotFoundException;
import org.reward.service.RewardService;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardControllerTest {

    @Mock
    private RewardService rewardService;

    @InjectMocks
    private RewardController rewardController;

    @Test
    void getRewards_multiMonthCustomer_returnsCorrectBreakdown() {

        Long targetCustomerId = 5L;

        Map<String, Integer> pointsPerMonth = Map.of(
                "JANUARY", 90,
                "FEBRUARY", 150,
                "MARCH", 60
        );

        RewardResponseDto expectedDto =
                new RewardResponseDto(targetCustomerId, pointsPerMonth, 300);

        when(rewardService.calculateRewards(targetCustomerId))
                .thenReturn(expectedDto);

        ResponseEntity<RewardResponseDto> result =
                rewardController.getRewards(targetCustomerId);

        assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(targetCustomerId, result.getBody().getCustomerId());
        assertEquals(300, result.getBody().getTotalPoints());
        assertEquals(90,  result.getBody().getMonthlyPoints().get("JANUARY"));
        assertEquals(150, result.getBody().getMonthlyPoints().get("FEBRUARY"));
        assertEquals(60,  result.getBody().getMonthlyPoints().get("MARCH"));

        verify(rewardService, times(1)).calculateRewards(targetCustomerId);
    }


    @Test
    void getRewards_customerWithNoEarnedPoints_returnsTotalPointsAsZero() {

        Long lowSpenderCustomerId = 10L;

        RewardResponseDto zeroPointsDto =
                new RewardResponseDto(lowSpenderCustomerId, Collections.emptyMap(), 0);

        when(rewardService.calculateRewards(lowSpenderCustomerId))
                .thenReturn(zeroPointsDto);

        ResponseEntity<RewardResponseDto> result =
                rewardController.getRewards(lowSpenderCustomerId);

        assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(0, result.getBody().getTotalPoints());
        assertTrue(result.getBody().getMonthlyPoints().isEmpty());

        verify(rewardService).calculateRewards(lowSpenderCustomerId);
    }

    @Test
    void getRewards_unknownCustomer_propagatesCustomerNotFoundException() {

        Long missingCustomerId = 9999L;

        when(rewardService.calculateRewards(missingCustomerId))
                .thenThrow(new CustomerNotFoundException("Customer not found: " + missingCustomerId));

        assertThrows(CustomerNotFoundException.class,
                () -> rewardController.getRewards(missingCustomerId));

        verify(rewardService).calculateRewards(missingCustomerId);
    }

    @Test
    void getRewards_singleMonthActivity_returnsSingleEntryMonthlyMap() {

        Long singleMonthCustomerId = 3L;

        Map<String, Integer> singleMonthPoints = Map.of("APRIL", 250);

        RewardResponseDto singleMonthDto =
                new RewardResponseDto(singleMonthCustomerId, singleMonthPoints, 250);

        when(rewardService.calculateRewards(singleMonthCustomerId))
                .thenReturn(singleMonthDto);

        ResponseEntity<RewardResponseDto> result =
                rewardController.getRewards(singleMonthCustomerId);

        assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
        assertEquals(1, result.getBody().getMonthlyPoints().size());
        assertEquals(250, result.getBody().getMonthlyPoints().get("APRIL"));
        assertEquals(250, result.getBody().getTotalPoints());

        verify(rewardService).calculateRewards(singleMonthCustomerId);
    }

    @Test
    void getRewards_verifyServiceInvokedOnce_noExtraInteractions() {

        Long regularCustomerId = 7L;

        RewardResponseDto dto = new RewardResponseDto(regularCustomerId, Map.of("MAY", 75), 75);

        when(rewardService.calculateRewards(regularCustomerId)).thenReturn(dto);

        rewardController.getRewards(regularCustomerId);

        verify(rewardService, times(1)).calculateRewards(regularCustomerId);
        verifyNoMoreInteractions(rewardService);
    }
}