package com.na.mb_backend.entities.DTOs;

import com.na.mb_backend.entities.Prescription;
import com.na.mb_backend.entities.PrescriptionItem;
import lombok.Getter;

@Getter
public class DispenseResult {
    public enum Outcome {
        SUCCESS,
        EXPIRED_WARNING,
        EARLY_DISPENSE_WARNING
    }

    private final Outcome outcome;
    private final PrescriptionItem dispensedItem;
    private final boolean prescriptionCompleted;
    private final boolean dispensedWhileExpired;
    private final boolean dispensedEarly;
    private final String message;

    private DispenseResult(Outcome outcome, PrescriptionItem dispensedItem,
                           boolean prescriptionCompleted, boolean dispensedWhileExpired,
                           boolean dispensedEarly, String message) {
        this.outcome = outcome;
        this.dispensedItem = dispensedItem;
        this.prescriptionCompleted = prescriptionCompleted;
        this.dispensedWhileExpired = dispensedWhileExpired;
        this.dispensedEarly = dispensedEarly;
        this.message = message;
    }


    public static DispenseResult success(PrescriptionItem item,
                                         boolean completed,
                                         boolean wasExpired,
                                         boolean wasEarly) {
        String msg = completed
                ? "Médicament livré. Ordonnance complète."
                : "Médicament livré.";
        return new DispenseResult(Outcome.SUCCESS, item, completed, wasExpired, wasEarly, msg);
    }

    public static DispenseResult expiredWarning(Prescription prescription) {
        return new DispenseResult(
                Outcome.EXPIRED_WARNING,
                null, false, false, false,
                "Ordonnance expirée le " + prescription.getEndDate() +
                        ". Confirmer pour continuer."
        );
    }

    public static DispenseResult earlyDispenseWarning(PrescriptionItem item) {
        return new DispenseResult(
                Outcome.EARLY_DISPENSE_WARNING,
                null, false, false, false,
                "Ce médicament appartient au mois " + item.getMonthNumber() +
                        ". Confirmer pour livrer en avance."
        );
    }

    // ─── Convenience checks ───────────────────────────────────────────────────

    public boolean isSuccess()                { return outcome == Outcome.SUCCESS; }
    public boolean requiresExpiredConfirm()   { return outcome == Outcome.EXPIRED_WARNING; }
    public boolean requiresEarlyConfirm()     { return outcome == Outcome.EARLY_DISPENSE_WARNING; }
}
