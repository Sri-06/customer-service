package org.reward.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reward.dto.RewardResponseDto;
import org.reward.entity.Transaction;
import org.reward.exception.CustomerNotFoundException;
import org.reward.repository.CustomerRepository;
import org.reward.repository.TransactionRepository;
import org.reward.service.RewardService;
import org.reward.util.RewardUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RewardServiceImpl implements RewardService {

    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    private final RewardUtil rewardUtil;

    @Value("${reward.month}")
    private Long rewardCalculationMonths;
    @Override
    public RewardResponseDto calculateRewards(Long customerId) {
        validateCustomer(customerId);
        List<Transaction> transactions = getLastThreeMonthsTransactions(customerId);
        if (transactions.isEmpty()) {
            return buildEmptyResponse(customerId);
        }
        return generateRewardResponseDto(transactions, customerId);
    }

    @Override
    public List<RewardResponseDto> getAllRewards() {
        List<RewardResponseDto> rewardResponseDtoList = new ArrayList<>();
        LocalDate startDate = LocalDate.now().minusMonths(rewardCalculationMonths).withDayOfMonth(1);
        LocalDate endDate = LocalDate.now();

        List<Transaction> transactions = transactionRepository.findByTransactionDateBetween(startDate,endDate);
        Map<Long, List<Transaction>> customerMap = transactions.stream().collect(Collectors.groupingBy(data -> data.getCustomer().getId()));
        customerMap.forEach((customer, trans) -> {
            rewardResponseDtoList.add(generateRewardResponseDto(trans, customer));
        });
        return rewardResponseDtoList;
    }

    RewardResponseDto generateRewardResponseDto(List<Transaction> transactions,Long customerId){
        Map<String, Integer> monthlyPoints = calculateMonthlyPoints(transactions);
        int totalPoints = calculateTotalPoints(monthlyPoints);
        return new RewardResponseDto(customerId, monthlyPoints, totalPoints);
    }
    private void validateCustomer(Long customerId) {
        customerRepository.findById(customerId).orElseThrow(() -> new CustomerNotFoundException("Customer not found: " + customerId));
    }

    private List<Transaction> getLastThreeMonthsTransactions(Long customerId) {
        LocalDate startDate = LocalDate.now().minusMonths(3).withDayOfMonth(1);
        LocalDate endDate = LocalDate.now();
        return transactionRepository.findByCustomerIdAndTransactionDateBetween(customerId, startDate, endDate);
    }

    private RewardResponseDto buildEmptyResponse(Long customerId) {
        return new RewardResponseDto(customerId, Map.of(), 0);
    }

    private Map<String, Integer> calculateMonthlyPoints(List<Transaction> transactions) {
        return transactions.stream().collect(Collectors.groupingBy(transaction -> transaction.getTransactionDate().getMonth().toString(), Collectors.summingInt(transaction -> rewardUtil.calculate(transaction.getAmount()))));
    }

    private int calculateTotalPoints(Map<String, Integer> monthlyPoints) {
        return monthlyPoints.values().stream().mapToInt(Integer::intValue).sum();
    }
}