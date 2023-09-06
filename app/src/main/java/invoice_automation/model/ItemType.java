package invoice_automation.model;

import invoice_automation.Consts;

public enum ItemType {
    BMUN_SCHOOL_FEE {
        @Override
        public String toString() {
            return String.format("%s %s", Conference.BMUN, Consts.SCHOOL_FEE);
        }
    },
    BMUN_DELEGATE_FEE {
        @Override
        public String toString() {
            return String.format("%s %s", Conference.BMUN, Consts.DELEGATE_FEE);
        }
    },
    FC_SCHOOL_FEE {
        @Override
        public String toString() {
            return String.format("%s %s", Conference.FC, Consts.SCHOOL_FEE);
        }
    },
    FC_DELEGATE_FEE {
        @Override
        public String toString() {
            return String.format("%s %s", Conference.FC, Consts.DELEGATE_FEE);
        }
    },
    CREDIT_CARD_PROCESSING_FEE {
        @Override
        public String toString() {
            return Consts.CREDIT_CARD_PROCESSING_FEE;
        }
    }
}
