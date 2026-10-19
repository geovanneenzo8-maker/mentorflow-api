package com.portfolio.mentorflow.controller;

import com.portfolio.mentorflow.dto.DashboardMetricsResponse;
import com.portfolio.mentorflow.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "5. Dashboard Executivo & Ledger Financeiro", description = "Métricas consolidadas de GMV, receita da plataforma (15%), repasses a mentores (85%) e auditoria contábil")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/metrics")
    @Operation(
            summary = "Obter métricas consolidadas e extrato do Ledger",
            description = "Retorna GMV líquido, receita da plataforma, repasses aos mentores, contadores por status e as últimas transações auditáveis do Ledger."
    )
    public DashboardMetricsResponse getDashboardMetrics() {
        return dashboardService.getDashboardMetrics();
    }
}
