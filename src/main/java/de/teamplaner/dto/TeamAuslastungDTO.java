package de.teamplaner.dto;

public record TeamAuslastungDTO(long offen, long inBearbeitung, long abgeschlossen) {

    public long aktiv() {
        return offen + inBearbeitung;
    }

    public long gesamt() {
        return offen + inBearbeitung + abgeschlossen;
    }

    public int auslastungProzent() {
        if (gesamt() == 0) return 0;
        return (int) Math.round(aktiv() * 100.0 / gesamt());
    }

    public String bootstrapFarbe() {
        int p = auslastungProzent();
        if (p >= 80) return "danger";
        if (p >= 50) return "warning";
        return "success";
    }
}
