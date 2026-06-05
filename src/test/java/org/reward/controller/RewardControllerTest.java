package org.reward.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reward.dto.RewardResponseDto;
import org.reward.exception.CustomerNotFoundException;
import org.reward.service.RewardService;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardControllerTest {

    @Mock
    private RewardService rewardService;

    @InjectMocks
    private RewardController rewardController;

    // ──────────────────────────────────────────────────────────────────────────
    // getRewards (single customer)
    // ──────────────────────────────────────────────────────────────────────────

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

    // ──────────────────────────────────────────────────────────────────────────
    // getAllRewards
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void getAllRewards_multipleCustomers_returns200WithList() {

        RewardResponseDto customer1Dto = new RewardResponseDto(1L, Map.of("JANUARY", 90), 90);
        RewardResponseDto customer2Dto = new RewardResponseDto(2L, Map.of("JANUARY", 150, "FEBRUARY", 60), 210);

        when(rewardService.getAllRewards()).thenReturn(List.of(customer1Dto, customer2Dto));

        ResponseEntity<List<RewardResponseDto>> result = rewardController.getAllRewards();

        assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(2, result.getBody().size());

        verify(rewardService, times(1)).getAllRewards();
    }

    @Test
    void getAllRewards_noCustomersWithTransactions_returns200WithEmptyList() {

        when(rewardService.getAllRewards()).thenReturn(Collections.emptyList());

        ResponseEntity<List<RewardResponseDto>> result = rewardController.getAllRewards();

        assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isEmpty());

        verify(rewardService, times(1)).getAllRewards();
    }

    @Test
    void getAllRewards_singleCustomer_returnsListWithOneEntry() {

        RewardResponseDto singleDto = new RewardResponseDto(1L, Map.of("MARCH", 300), 300);

        when(rewardService.getAllRewards()).thenReturn(List.of(singleDto));

        ResponseEntity<List<RewardResponseDto>> result = rewardController.getAllRewards();

        assertNotNull(result);
        assertEquals(200, result.getStatusCode().value());
        assertEquals(1, result.getBody().size());
        assertEquals(1L, result.getBody().get(0).getCustomerId());
        assertEquals(300, result.getBody().get(0).getTotalPoints());

        verify(rewardService, times(1)).getAllRewards();
    }

    @Test
    void getAllRewards_verifyCorrectCustomerIdsInResponse() {

        RewardResponseDto dto1 = new RewardResponseDto(10L, Map.of("APRIL", 100), 100);
        RewardResponseDto dto2 = new RewardResponseDto(20L, Map.of("APRIL", 200), 200);
        RewardResponseDto dto3 = new RewardResponseDto(30L, Map.of("APRIL", 50), 50);

        when(rewardService.getAllRewards()).thenReturn(List.of(dto1, dto2, dto3));

        ResponseEntity<List<RewardResponseDto>> result = rewardController.getAllRewards();

        assertNotNull(result.getBody());
        assertEquals(3, result.getBody().size());
        assertTrue(result.getBody().stream().anyMatch(r -> r.getCustomerId().equals(10L)));
        assertTrue(result.getBody().stream().anyMatch(r -> r.getCustomerId().equals(20L)));
        assertTrue(result.getBody().stream().anyMatch(r -> r.getCustomerId().equals(30L)));

        verify(rewardService, times(1)).getAllRewards();
    }

    @Test
    void getAllRewards_verifyServiceInvokedOnce_noExtraInteractions() {

        when(rewardService.getAllRewards()).thenReturn(Collections.emptyList());

        rewardController.getAllRewards();

        verify(rewardService, times(1)).getAllRewards();
        verifyNoMoreInteractions(rewardService);
    }

    @Test
    void getAllRewards_eachResponseContainsTotalPoints() {

        RewardResponseDto dto1 = new RewardResponseDto(1L, Map.of("MAY", 90, "JUNE", 50), 140);
        RewardResponseDto dto2 = new RewardResponseDto(2L, Map.of("MAY", 0), 0);

        when(rewardService.getAllRewards()).thenReturn(List.of(dto1, dto2));

        ResponseEntity<List<RewardResponseDto>> result = rewardController.getAllRewards();

        assertNotNull(result.getBody());
        result.getBody().forEach(r -> assertNotNull(r.getTotalPoints()));

        verify(rewardService, times(1)).getAllRewards();
    }
}
