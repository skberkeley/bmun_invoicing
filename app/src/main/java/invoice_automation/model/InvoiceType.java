package invoice_automation.model;

import invoice_automation.Consts;
import lombok.Getter;

import java.math.BigDecimal;

public enum InvoiceType {
    BMUN_SCHOOL_FEE(BigDecimal.valueOf(60)) {
        @Override
        public String toString() {
            return String.format("%s %s", Conference.BMUN, Consts.SCHOOL_FEE);
        }
    },
    BMUN_DELEGATE_FEE(BigDecimal.valueOf(85)) {
        @Override
        public String toString() {
            return String.format("%s %s", Conference.BMUN, Consts.DELEGATE_FEE);
        }
    },
    FC_SCHOOL_FEE(BigDecimal.valueOf(35)) {
        @Override
        public String toString() {
            return String.format("%s %s", Conference.FC, Consts.SCHOOL_FEE);
        }
    },
    FC_DELEGATE_FEE(BigDecimal.valueOf(30)) {
        @Override
        public String toString() {
            return String.format("%s %s", Conference.FC, Consts.DELEGATE_FEE);
        }
    };

    @Getter
    private final BigDecimal unitCost;
    InvoiceType(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }
}
