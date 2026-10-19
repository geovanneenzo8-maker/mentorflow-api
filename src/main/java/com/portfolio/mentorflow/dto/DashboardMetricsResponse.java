package com.portfolio.mentorflow.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardMetricsResponse(
        long totalMentors,
        long totalMentees,
        long totalBookings,
        long confirmedBookings,
        long completedBookings,
        long pendingBookings,
        long cancelledBookings,
        BigDecimal grossMerchandiseVolume,
        BigDecimal platformNetRevenue,
        BigDecimal mentorTotalPayouts,
        BigDecimal totalRefunded,
        List<LedgerTransactionResponse> recentTransactions
) {
}
