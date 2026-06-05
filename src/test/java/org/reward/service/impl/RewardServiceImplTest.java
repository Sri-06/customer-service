package org.reward.service.impl;

import org.junit.jupiter.api.BeforeEach;
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
import org.reward.util.RewardUtil;
import org.springframework.test.util.ReflectionTestUtils;

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

    @Mock
    private RewardUtil rewardUtil;

    @InjectMocks
    private RewardServiceImpl rewardService;

    // ──────────────────────────────────────────────────────────────────────────
    // calculateRewards
    // ──────────────────────────────────────────────────────────────────────────

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
        when(rewardUtil.calculate(BigDecimal.valueOf(100))).thenReturn(50);

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
        when(rewardUtil.calculate(BigDecimal.valueOf(120))).thenReturn(90);

        RewardResponseDto result = rewardService.calculateRewards(4L);

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
        when(rewardUtil.calculate(BigDecimal.valueOf(50))).thenReturn(0);

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
        when(rewardUtil.calculate(BigDecimal.valueOf(51))).thenReturn(1);

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
        aprilTxn.setAmount(BigDecimal.valueOf(200));   // 250 pts
        aprilTxn.setTransactionDate(LocalDate.of(2026, 4, 5));
        aprilTxn.setCustomer(helen);

        when(customerRepository.findById(7L)).thenReturn(Optional.of(helen));
        when(transactionRepository.findByCustomerIdAndTransactionDateBetween(
                eq(7L), any(), any()))
                .thenReturn(List.of(marchTxn1, marchTxn2, aprilTxn));
        when(rewardUtil.calculate(BigDecimal.valueOf(120))).thenReturn(90);
        when(rewardUtil.calculate(BigDecimal.valueOf(80))).thenReturn(30);
        when(rewardUtil.calculate(BigDecimal.valueOf(200))).thenReturn(250);

        RewardResponseDto result = rewardService.calculateRewards(7L);

        assertEquals(2, result.getMonthlyPoints().size());
        assertTrue(result.getMonthlyPoints().containsKey("MARCH"));
        assertTrue(result.getMonthlyPoints().containsKey("APRIL"));
        assertEquals(120, result.getMonthlyPoints().get("MARCH"));
        assertEquals(250, result.getMonthlyPoints().get("APRIL"));
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
        when(rewardUtil.calculate(BigDecimal.valueOf(20))).thenReturn(0);
        when(rewardUtil.calculate(BigDecimal.valueOf(35))).thenReturn(0);

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
        when(rewardUtil.calculate(BigDecimal.valueOf(250))).thenReturn(350);

        RewardResponseDto result = rewardService.calculateRewards(9L);

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

    // ──────────────────────────────────────────────────────────────────────────
    // getAllRewards
    // ──────────────────────────────────────────────────────────────────────────
    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(rewardService, "rewardCalculationMonths", 3L);
    }
    @Test
    void getAllRewards_noTransactionsInWindow_returnsEmptyList() {

        when(transactionRepository.findByTransactionDateBetween(any(), any()))
                .thenReturn(List.of());

        List<RewardResponseDto> result = rewardService.getAllRewards();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(transactionRepository).findByTransactionDateBetween(any(), any());
    }

    @Test
    void getAllRewards_twoCustomersWithTransactions_returnsTwoEntries() {

        Customer alice = new Customer(1L, "Alice");
        Customer bob   = new Customer(2L, "Bob");

        Transaction aliceTxn = new Transaction();
        aliceTxn.setCustomer(alice);
        aliceTxn.setAmount(BigDecimal.valueOf(120));
        aliceTxn.setTransactionDate(LocalDate.now().minusDays(5));

        Transaction bobTxn = new Transaction();
        bobTxn.setCustomer(bob);
        bobTxn.setAmount(BigDecimal.valueOf(80));
        bobTxn.setTransactionDate(LocalDate.now().minusDays(3));

        when(transactionRepository.findByTransactionDateBetween(any(), any()))
                .thenReturn(List.of(aliceTxn, bobTxn));
        when(rewardUtil.calculate(BigDecimal.valueOf(120))).thenReturn(90);
        when(rewardUtil.calculate(BigDecimal.valueOf(80))).thenReturn(30);

        List<RewardResponseDto> result = rewardService.getAllRewards();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(r -> r.getCustomerId().equals(1L)));
        assertTrue(result.stream().anyMatch(r -> r.getCustomerId().equals(2L)));
    }

    @Test
    void getAllRewards_singleCustomerMultipleTransactions_aggregatedIntoOneEntry() {

        Customer alice = new Customer(1L, "Alice");

        Transaction txn1 = new Transaction();
        txn1.setCustomer(alice);
        txn1.setAmount(BigDecimal.valueOf(120));
        txn1.setTransactionDate(LocalDate.now().minusDays(5));

        Transaction txn2 = new Transaction();
        txn2.setCustomer(alice);
        txn2.setAmount(BigDecimal.valueOf(80));
        txn2.setTransactionDate(LocalDate.now().minusDays(10));

        when(transactionRepository.findByTransactionDateBetween(any(), any()))
                .thenReturn(List.of(txn1, txn2));
        when(rewardUtil.calculate(BigDecimal.valueOf(120))).thenReturn(90);
        when(rewardUtil.calculate(BigDecimal.valueOf(80))).thenReturn(30);

        List<RewardResponseDto> result = rewardService.getAllRewards();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getCustomerId());
        assertEquals(120, result.get(0).getTotalPoints());
    }

    @Test
    void getAllRewards_customerWithTransactionsInTwoMonths_monthlyPointsGroupedCorrectly() {

        Customer alice = new Customer(1L, "Alice");

        Transaction marchTxn = new Transaction();
        marchTxn.setCustomer(alice);
        marchTxn.setAmount(BigDecimal.valueOf(150));
        marchTxn.setTransactionDate(LocalDate.of(2026, 3, 15));

        Transaction aprilTxn = new Transaction();
        aprilTxn.setCustomer(alice);
        aprilTxn.setAmount(BigDecimal.valueOf(200));
        aprilTxn.setTransactionDate(LocalDate.of(2026, 4, 10));

        when(transactionRepository.findByTransactionDateBetween(any(), any()))
                .thenReturn(List.of(marchTxn, aprilTxn));
        when(rewardUtil.calculate(BigDecimal.valueOf(150))).thenReturn(150);
        when(rewardUtil.calculate(BigDecimal.valueOf(200))).thenReturn(250);

        List<RewardResponseDto> result = rewardService.getAllRewards();

        assertEquals(1, result.size());
        RewardResponseDto dto = result.get(0);
        assertEquals(2, dto.getMonthlyPoints().size());
        assertEquals(150, dto.getMonthlyPoints().get("MARCH"));
        assertEquals(250, dto.getMonthlyPoints().get("APRIL"));
        assertEquals(400, dto.getTotalPoints());
    }

    @Test
    void getAllRewards_allTransactionsBelowThreshold_totalPointsIsZero() {

        Customer alice = new Customer(1L, "Alice");

        Transaction txn = new Transaction();
        txn.setCustomer(alice);
        txn.setAmount(BigDecimal.valueOf(30));
        txn.setTransactionDate(LocalDate.now().minusDays(5));

        when(transactionRepository.findByTransactionDateBetween(any(), any()))
                .thenReturn(List.of(txn));
        when(rewardUtil.calculate(BigDecimal.valueOf(30))).thenReturn(0);

        List<RewardResponseDto> result = rewardService.getAllRewards();

        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getTotalPoints());
    }

    @Test
    void getAllRewards_verifyRepositoryCalledWithCorrectDateWindow() {

        when(transactionRepository.findByTransactionDateBetween(any(), any()))
                .thenReturn(List.of());

        rewardService.getAllRewards();

        verify(transactionRepository).findByTransactionDateBetween(
                eq(LocalDate.now().minusMonths(3).withDayOfMonth(1)),
                eq(LocalDate.now()));
    }
}