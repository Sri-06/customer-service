package org.reward.service.impl;



import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reward.dto.RewardResponseDto;
import org.reward.entity.Customer;
import org.reward.entity.Transaction;
import org.reward.exception.CustomerNotFoundException;
import org.reward.repository.CustomerRepository;
import org.reward.repository.TransactionRepository;
import org.reward.service.impl.RewardServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RewardServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private RewardServiceImpl rewardService;


    @Test
    void calculateRewards_customerDoesNotExist_throwsCustomerNotFoundException() {

        Long absentCustomerId = 42L;
        when(customerRepository.findById(absentCustomerId))
                .thenReturn(Optional.empty());

        CustomerNotFoundException ex = assertThrows(CustomerNotFoundException.class,
                () -> rewardService.calculateRewards(absentCustomerId));

        assertTrue(ex.getMessage().contains(String.valueOf(absentCustomerId)));
        verify(customerRepository).findById(absentCustomerId);
        verifyNoInteractions(transactionRepository);
    }


    @Test
    void calculateRewards_validCustomerNoTransactions_returnsZeroPoints() {

        Customer charlie = new Customer(20L, "Charlie");
        when(customerRepository.findById(20L))
                .thenReturn(Optional.of(charlie));
        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(
                eq(20L), any(), any()))
                .thenReturn(List.of());

        RewardResponseDto result = rewardService.calculateRewards(20L);

        assertNotNull(result);
        assertEquals(20L, result.getCustomerId());
        assertEquals(0, result.getTotalPoints());
        assertTrue(result.getMonthlyPoints().isEmpty());
    }


    @Test
    void calculateRewards_singleTransactionAtExactly100_earns50Points() {

        Customer dana = new Customer(3L, "Dana");

        Transaction hundredDollarTxn = new Transaction();
        hundredDollarTxn.setAmount(BigDecimal.valueOf(100));
        hundredDollarTxn.setTransactionDate(LocalDate.now().minusDays(7));
        hundredDollarTxn.setCustomer(dana);

        when(customerRepository.findById(3L)).thenReturn(Optional.of(dana));
        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(
                eq(3L), any(), any()))
                .thenReturn(List.of(hundredDollarTxn));

        RewardResponseDto result = rewardService.calculateRewards(3L);

        assertEquals(50, result.getTotalPoints());
    }

    @Test
    void calculateRewards_singleTransactionOf120_earns90Points() {

        Customer evan = new Customer(4L, "Evan");

        Transaction txn120 = new Transaction();
        txn120.setAmount(BigDecimal.valueOf(120));
        txn120.setTransactionDate(LocalDate.now().minusDays(3));
        txn120.setCustomer(evan);

        when(customerRepository.findById(4L)).thenReturn(Optional.of(evan));
        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(
                eq(4L), any(), any()))
                .thenReturn(List.of(txn120));

        RewardResponseDto result = rewardService.calculateRewards(4L);

        // $51–$100 → 50 pts; $101–$120 → 20 * 2 = 40 pts; total = 90
        assertEquals(90, result.getTotalPoints());
    }

    @Test
    void calculateRewards_transactionAtExactly50_earnsZeroPoints() {

        Customer fiona = new Customer(5L, "Fiona");

        Transaction fiftyDollarTxn = new Transaction();
        fiftyDollarTxn.setAmount(BigDecimal.valueOf(50));
        fiftyDollarTxn.setTransactionDate(LocalDate.now().minusDays(1));
        fiftyDollarTxn.setCustomer(fiona);

        when(customerRepository.findById(5L)).thenReturn(Optional.of(fiona));
        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(
                eq(5L), any(), any()))
                .thenReturn(List.of(fiftyDollarTxn));

        RewardResponseDto result = rewardService.calculateRewards(5L);

        assertEquals(0, result.getTotalPoints());
    }


    @Test
    void calculateRewards_transactionOf51_earnsOnePoint() {

        Customer george = new Customer(6L, "George");

        Transaction txn51 = new Transaction();
        txn51.setAmount(BigDecimal.valueOf(51));
        txn51.setTransactionDate(LocalDate.now().minusDays(2));
        txn51.setCustomer(george);

        when(customerRepository.findById(6L)).thenReturn(Optional.of(george));
        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(
                eq(6L), any(), any()))
                .thenReturn(List.of(txn51));

        RewardResponseDto result = rewardService.calculateRewards(6L);

        assertEquals(1, result.getTotalPoints());
    }

    @Test
    void calculateRewards_transactionsAcrossTwoMonths_groupedCorrectlyInMonthlyMap() {

        Customer helen = new Customer(7L, "Helen");

        Transaction marchTxn1 = new Transaction();
        marchTxn1.setAmount(BigDecimal.valueOf(120));  // 90 pts
        marchTxn1.setTransactionDate(LocalDate.of(2026, 3, 10));
        marchTxn1.setCustomer(helen);

        Transaction marchTxn2 = new Transaction();
        marchTxn2.setAmount(BigDecimal.valueOf(80));   // 30 pts
        marchTxn2.setTransactionDate(LocalDate.of(2026, 3, 25));
        marchTxn2.setCustomer(helen);

        Transaction aprilTxn = new Transaction();
        aprilTxn.setAmount(BigDecimal.valueOf(200));   // 150+100 = 250 pts? → (200-100)*2+50 = 250 pts
        aprilTxn.setTransactionDate(LocalDate.of(2026, 4, 5));
        aprilTxn.setCustomer(helen);

        when(customerRepository.findById(7L)).thenReturn(Optional.of(helen));
        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(
                eq(7L), any(), any()))
                .thenReturn(List.of(marchTxn1, marchTxn2, aprilTxn));

        RewardResponseDto result = rewardService.calculateRewards(7L);

        assertEquals(2, result.getMonthlyPoints().size());
        assertTrue(result.getMonthlyPoints().containsKey("MARCH"));
        assertTrue(result.getMonthlyPoints().containsKey("APRIL"));

        // MARCH: 90 + 30 = 120 pts
        assertEquals(120, result.getMonthlyPoints().get("MARCH"));
        // APRIL: (200-100)*2 + 50 = 250 pts
        assertEquals(250, result.getMonthlyPoints().get("APRIL"));
        // Total: 120 + 250 = 370
        assertEquals(370, result.getTotalPoints());
    }


    @Test
    void calculateRewards_allTransactionsBelowThreshold_zeroTotalPoints() {

        Customer ivan = new Customer(8L, "Ivan");

        Transaction t1 = new Transaction();
        t1.setAmount(BigDecimal.valueOf(20));
        t1.setTransactionDate(LocalDate.now().minusDays(10));
        t1.setCustomer(ivan);

        Transaction t2 = new Transaction();
        t2.setAmount(BigDecimal.valueOf(35));
        t2.setTransactionDate(LocalDate.now().minusDays(20));
        t2.setCustomer(ivan);

        when(customerRepository.findById(8L)).thenReturn(Optional.of(ivan));
        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(
                eq(8L), any(), any()))
                .thenReturn(List.of(t1, t2));

        RewardResponseDto result = rewardService.calculateRewards(8L);

        assertEquals(0, result.getTotalPoints());
    }


    @Test
    void calculateRewards_highValueTransaction_correctPointsComputed() {

        Customer julia = new Customer(9L, "Julia");

        Transaction bigSpend = new Transaction();
        bigSpend.setAmount(BigDecimal.valueOf(250));
        bigSpend.setTransactionDate(LocalDate.now().minusDays(5));
        bigSpend.setCustomer(julia);

        when(customerRepository.findById(9L)).thenReturn(Optional.of(julia));
        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(
                eq(9L), any(), any()))
                .thenReturn(List.of(bigSpend));

        RewardResponseDto result = rewardService.calculateRewards(9L);

        // (250 - 100) * 2 + 50 = 300 + 50 = 350
        assertEquals(350, result.getTotalPoints());
    }


    @Test
    void calculateRewards_verifyRepositoryCalledWithCorrectDateRange() {

        Customer kim = new Customer(11L, "Kim");
        when(customerRepository.findById(11L)).thenReturn(Optional.of(kim));
        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(
                eq(11L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());

        rewardService.calculateRewards(11L);

        verify(transactionRepository).findByCustomerIdAndTransactionDateBetween(
                eq(11L),
                eq(LocalDate.now().minusMonths(3).withDayOfMonth(1)),
                eq(LocalDate.now()));
    }
}