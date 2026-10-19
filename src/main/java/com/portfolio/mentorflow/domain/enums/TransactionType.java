package com.portfolio.mentorflow.domain.enums;

public enum TransactionType {
    PAYMENT_CAPTURED("Pagamento Capturado (Escrow)"),
    MENTOR_SPLIT_CREDIT("Repasse ao Mentor (85%)"),
    PLATFORM_FEE_CREDIT("Taxa de Intermediação da Plataforma (15%)"),
    REFUND_DEBIT("Estorno Integral por Cancelamento");

    private final String description;

    TransactionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
