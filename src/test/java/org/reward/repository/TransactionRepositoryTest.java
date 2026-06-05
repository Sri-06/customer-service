package org.reward.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reward.entity.Customer;
import org.reward.entity.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Customer savedCustomer;

    @BeforeEach
    void setUp() {
        Customer alice = new Customer();
        alice.setName("Alice");
        savedCustomer = customerRepository.save(alice);
    }

    // ──────────────────────────────────────────────────────────────────────────
    // findByCustomerIdAndTransactionDateBetween
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void findByCustomerIdAndTransactionDateBetween_transactionWithinWindow_returnsResult() {

        Transaction recentTxn = new Transaction();
        recentTxn.setCustomer(savedCustomer);
        recentTxn.setAmount(BigDecimal.valueOf(75.00));
        recentTxn.setTransactionDate(LocalDate.now().minusDays(10));
        transactionRepository.save(recentTxn);

        LocalDate windowStart = LocalDate.now().minusMonths(1);
        LocalDate windowEnd   = LocalDate.now();

        List<Transaction> found =
                transactionRepository.findByCustomerIdAndTransactionDateBetween(
                        savedCustomer.getId(), windowStart, windowEnd);

        assertFalse(found.isEmpty());
        assertEquals(1, found.size());
        assertEquals(0, found.get(0).getAmount().compareTo(BigDecimal.valueOf(75.00)));
    }

    @Test
    void findByCustomerIdAndTransactionDateBetween_multipleTransactionsInWindow_returnsAll() {

        for (int daysBack : new int[]{5, 15, 45}) {
            Transaction t = new Transaction();
            t.setCustomer(savedCustomer);
            t.setAmount(BigDecimal.valueOf(110));
            t.setTransactionDate(LocalDate.now().minusDays(daysBack));
            transactionRepository.save(t);
        }

        LocalDate windowStart = LocalDate.now().minusMonths(3);
        LocalDate windowEnd   = LocalDate.now();

        List<Transaction> found =
                transactionRepository.findByCustomerIdAndTransactionDateBetween(
                        savedCustomer.getId(), windowStart, windowEnd);

        assertEquals(3, found.size());
    }

    @Test
    void findByCustomerIdAndTransactionDateBetween_transactionOnStartBoundary_isIncluded() {

        LocalDate windowStart = LocalDate.now().minusMonths(3).withDayOfMonth(1);
        LocalDate windowEnd   = LocalDate.now();

        Transaction boundaryTxn = new Transaction();
        boundaryTxn.setCustomer(savedCustomer);
        boundaryTxn.setAmount(BigDecimal.valueOf(200));
        boundaryTxn.setTransactionDate(windowStart);
        transactionRepository.save(boundaryTxn);

        List<Transaction> found =
                transactionRepository.findByCustomerIdAndTransactionDateBetween(
                        savedCustomer.getId(), windowStart, windowEnd);

        assertFalse(found.isEmpty());
        assertTrue(found.stream().anyMatch(t -> t.getTransactionDate().equals(windowStart)));
    }

    @Test
    void findByCustomerIdAndTransactionDateBetween_transactionOnEndBoundary_isIncluded() {

        LocalDate windowStart = LocalDate.now().minusMonths(1);
        LocalDate windowEnd   = LocalDate.now();

        Transaction endBoundaryTxn = new Transaction();
        endBoundaryTxn.setCustomer(savedCustomer);
        endBoundaryTxn.setAmount(BigDecimal.valueOf(160));
        endBoundaryTxn.setTransactionDate(windowEnd);
        transactionRepository.save(endBoundaryTxn);

        List<Transaction> found =
                transactionRepository.findByCustomerIdAndTransactionDateBetween(
                        savedCustomer.getId(), windowStart, windowEnd);

        assertFalse(found.isEmpty());
        assertTrue(found.stream().anyMatch(t -> t.getTransactionDate().equals(windowEnd)));
    }

    @Test
    void findByCustomerIdAndTransactionDateBetween_noTransactionsForCustomer_returnsEmpty() {

        LocalDate windowStart = LocalDate.now().minusMonths(1);
        LocalDate windowEnd   = LocalDate.now();

        List<Transaction> found =
                transactionRepository.findByCustomerIdAndTransactionDateBetween(
                        savedCustomer.getId(), windowStart, windowEnd);

        assertTrue(found.isEmpty());
    }

    @Test
    void findByCustomerIdAndTransactionDateBetween_transactionBeforeWindow_notReturned() {

        Transaction oldTxn = new Transaction();
        oldTxn.setCustomer(savedCustomer);
        oldTxn.setAmount(BigDecimal.valueOf(300));
        oldTxn.setTransactionDate(LocalDate.now().minusMonths(6));
        transactionRepository.save(oldTxn);

        LocalDate windowStart = LocalDate.now().minusMonths(3);
        LocalDate windowEnd   = LocalDate.now();

        List<Transaction> found =
                transactionRepository.findByCustomerIdAndTransactionDateBetween(
                        savedCustomer.getId(), windowStart, windowEnd);

        assertTrue(found.isEmpty());
    }

    @Test
    void findByCustomerIdAndTransactionDateBetween_unknownCustomerId_returnsEmpty() {

        List<Transaction> found =
                transactionRepository.findByCustomerIdAndTransactionDateBetween(
                        77777L,
                        LocalDate.now().minusMonths(1),
                        LocalDate.now());

        assertTrue(found.isEmpty());
    }

    @Test
    void findByCustomerIdAndTransactionDateBetween_transactionsOfOtherCustomer_notReturned() {

        Customer bob = new Customer();
        bob.setName("Bob");
        Customer savedBob = customerRepository.save(bob);

        Transaction bobTxn = new Transaction();
        bobTxn.setCustomer(savedBob);
        bobTxn.setAmount(BigDecimal.valueOf(180));
        bobTxn.setTransactionDate(LocalDate.now().minusDays(5));
        transactionRepository.save(bobTxn);

        List<Transaction> found =
                transactionRepository.findByCustomerIdAndTransactionDateBetween(
                        savedCustomer.getId(),
                        LocalDate.now().minusMonths(1),
                        LocalDate.now());

        assertTrue(found.isEmpty());
    }

    // ──────────────────────────────────────────────────────────────────────────
    // findByTransactionDateBetween  (used by getAllRewards)
    // ──────────────────────────────────────────────────────────────────────────

    @Test
    void findByTransactionDateBetween_transactionWithinWindow_returnsResult() {

        Transaction txn = new Transaction();
        txn.setCustomer(savedCustomer);
        txn.setAmount(BigDecimal.valueOf(120));
        txn.setTransactionDate(LocalDate.now().minusDays(5));
        transactionRepository.save(txn);

        List<Transaction> found =
                transactionRepository.findByTransactionDateBetween(
                        LocalDate.now().minusMonths(1),
                        LocalDate.now());

        assertFalse(found.isEmpty());
        assertEquals(1, found.size());
    }

    @Test
    void findByTransactionDateBetween_noTransactionsInWindow_returnsEmpty() {

        List<Transaction> found =
                transactionRepository.findByTransactionDateBetween(
                        LocalDate.now().minusMonths(1),
                        LocalDate.now());

        assertTrue(found.isEmpty());
    }

    @Test
    void findByTransactionDateBetween_transactionBeforeWindow_notReturned() {

        Transaction oldTxn = new Transaction();
        oldTxn.setCustomer(savedCustomer);
        oldTxn.setAmount(BigDecimal.valueOf(200));
        oldTxn.setTransactionDate(LocalDate.now().minusMonths(5));
        transactionRepository.save(oldTxn);

        List<Transaction> found =
                transactionRepository.findByTransactionDateBetween(
                        LocalDate.now().minusMonths(3),
                        LocalDate.now());

        assertTrue(found.isEmpty());
    }

    @Test
    void findByTransactionDateBetween_transactionsAcrossMultipleCustomers_allReturned() {

        Customer bob = new Customer();
        bob.setName("Bob");
        Customer savedBob = customerRepository.save(bob);

        Transaction aliceTxn = new Transaction();
        aliceTxn.setCustomer(savedCustomer);
        aliceTxn.setAmount(BigDecimal.valueOf(150));
        aliceTxn.setTransactionDate(LocalDate.now().minusDays(10));
        transactionRepository.save(aliceTxn);

        Transaction bobTxn = new Transaction();
        bobTxn.setCustomer(savedBob);
        bobTxn.setAmount(BigDecimal.valueOf(80));
        bobTxn.setTransactionDate(LocalDate.now().minusDays(7));
        transactionRepository.save(bobTxn);

        List<Transaction> found =
                transactionRepository.findByTransactionDateBetween(
                        LocalDate.now().minusMonths(1),
                        LocalDate.now());

        assertEquals(2, found.size());
        assertTrue(found.stream().anyMatch(t -> t.getCustomer().getId().equals(savedCustomer.getId())));
        assertTrue(found.stream().anyMatch(t -> t.getCustomer().getId().equals(savedBob.getId())));
    }

    @Test
    void findByTransactionDateBetween_startBoundaryInclusive_transactionReturned() {

        LocalDate start = LocalDate.now().minusMonths(3).withDayOfMonth(1);

        Transaction txn = new Transaction();
        txn.setCustomer(savedCustomer);
        txn.setAmount(BigDecimal.valueOf(90));
        txn.setTransactionDate(start);
        transactionRepository.save(txn);

        List<Transaction> found =
                transactionRepository.findByTransactionDateBetween(start, LocalDate.now());

        assertFalse(found.isEmpty());
        assertTrue(found.stream().anyMatch(t -> t.getTransactionDate().equals(start)));
    }

    @Test
    void findByTransactionDateBetween_endBoundaryInclusive_transactionReturned() {

        LocalDate end = LocalDate.now();

        Transaction txn = new Transaction();
        txn.setCustomer(savedCustomer);
        txn.setAmount(BigDecimal.valueOf(130));
        txn.setTransactionDate(end);
        transactionRepository.save(txn);

        List<Transaction> found =
                transactionRepository.findByTransactionDateBetween(
                        LocalDate.now().minusMonths(1), end);

        assertFalse(found.isEmpty());
        assertTrue(found.stream().anyMatch(t -> t.getTransactionDate().equals(end)));
    }
}