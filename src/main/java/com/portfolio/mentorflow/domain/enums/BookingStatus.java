package com.portfolio.mentorflow.domain.enums;

public enum BookingStatus {
    PENDING_PAYMENT("Aguardando Pagamento"),
    CONFIRMED("Confirmada"),
    COMPLETED("Concluída"),
    CANCELLED("Cancelada e Estornada");

    private final String label;

    BookingStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
