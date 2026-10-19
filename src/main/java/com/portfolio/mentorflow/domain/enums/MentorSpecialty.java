package com.portfolio.mentorflow.domain.enums;

public enum MentorSpecialty {
    BACKEND_JAVA("Engenharia Backend & Java/Spring"),
    SYSTEM_DESIGN("Arquitetura de Software & System Design"),
    CLOUD_DEVOPS("Cloud Computing, Kubernetes & SRE"),
    DATA_ENGINEERING("Engenharia de Dados & IA"),
    CAREER_INTERVIEW("Carreira Tech, Entrevistas & Código Limpo");

    private final String displayName;

    MentorSpecialty(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
