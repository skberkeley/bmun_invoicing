package invoice_automation.model;

import invoice_automation.Consts;

/**
 * Enum to represent BMUN's different conferences
 * Used to determine to which line items to use when creating invoices
 * DW is omitted since invoices are not required since it is free to participate
 *
 * @author skberkeley
 */
public enum Conference {
    BMUN {
        @Override
        public String toString() {
            return String.format("BMUN %d", Consts.BMUN_SESSION_NO);
        }
    },
    FC {
        @Override
        public String toString() {
            return String.format("FC %d", Consts.FC_NO);
        }
    },
    TEST {
        @Override
        public String toString() {
            return "Test";
        }
    };
}
