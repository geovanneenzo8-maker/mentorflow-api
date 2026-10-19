package com.portfolio.mentorflow.service;

import com.portfolio.mentorflow.domain.enums.BookingStatus;
import com.portfolio.mentorflow.domain.enums.TransactionType;
import com.portfolio.mentorflow.dto.DashboardMetricsResponse;
import com.portfolio.mentorflow.dto.LedgerTransactionResponse;
import com.portfolio.mentorflow.repository.BookingRepository;
import com.portfolio.mentorflow.repository.LedgerTransactionRepository;
import com.portfolio.mentorflow.repository.MenteeRepository;
import com.portfolio.mentorflow.repository.MentorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class DashboardService {

    private final MentorRepository mentorRepository;
    private final MenteeRepository menteeRepository;
    private final BookingRepository bookingRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;

    public DashboardService(MentorRepository mentorRepository,
                            MenteeRepository menteeRepository,
                            BookingRepository bookingRepository,
                            LedgerTransactionRepository ledgerTransactionRepository) {
        this.mentorRepository = mentorRepository;
        this.menteeRepository = menteeRepository;
        this.bookingRepository = bookingRepository;
        this.ledgerTransactionRepository = ledgerTransactionRepository;
    }

    @Transactional(readOnly = true)
    public DashboardMetricsResponse getDashboardMetrics() {
        long totalMentors = mentorRepository.count();
        long totalMentees = menteeRepository.count();
        long totalBookings = bookingRepository.count();
        long confirmed = bookingRepository.countByStatus(BookingStatus.CONFIRMED);
        long completed = bookingRepository.countByStatus(BookingStatus.COMPLETED);
        long pending = bookingRepository.countByStatus(BookingStatus.PENDING_PAYMENT);
        long cancelled = bookingRepository.countByStatus(BookingStatus.CANCELLED);

        BigDecimal captured = ledgerTransactionRepository.sumAmountByType(TransactionType.PAYMENT_CAPTURED);
        BigDecimal refunded = ledgerTransactionRepository.sumAmountByType(TransactionType.REFUND_DEBIT);
        BigDecimal platformFeeGross = ledgerTransactionRepository.sumAmountByType(TransactionType.PLATFORM_FEE_CREDIT);
        BigDecimal mentorSplitGross = ledgerTransactionRepository.sumAmountByType(TransactionType.MENTOR_SPLIT_CREDIT);

        BigDecimal netGmv = captured.subtract(refunded).max(BigDecimal.ZERO);
        BigDecimal refundPlatformShare = refunded.multiply(BookingService.PLATFORM_FEE_RATE).setScale(2, java.math.RoundingMode.HALF_UP);
        BigDecimal refundMentorShare = refunded.subtract(refundPlatformShare);

        BigDecimal netPlatformRevenue = platformFeeGross.subtract(refundPlatformShare).max(BigDecimal.ZERO);
        BigDecimal netMentorPayouts = mentorSplitGross.subtract(refundMentorShare).max(BigDecimal.ZERO);

        List<LedgerTransactionResponse> recentTransactions = ledgerTransactionRepository
                .findAllWithBookingDetailsOrderByCreatedAtDesc()
                .stream()
                .limit(20)
                .map(LedgerTransactionResponse::fromEntity)
                .toList();

        return new DashboardMetricsResponse(
                totalMentors,
                totalMentees,
                totalBookings,
                confirmed,
                completed,
                pending,
                cancelled,
                netGmv,
                netPlatformRevenue,
                netMentorPayouts,
                refunded,
                recentTransactions
        );
    }
}
