package invoice_automation;

import com.intuit.ipp.data.Customer;
import com.intuit.ipp.data.Invoice;
import com.intuit.ipp.data.ItemLineDetail;
import com.intuit.ipp.data.Line;
import com.intuit.ipp.data.LineDetailTypeEnum;
import com.intuit.ipp.data.ReferenceType;
import invoice_automation.model.InvoiceType;
import invoice_automation.model.Registration;
import lombok.NonNull;

import java.util.Objects;

public class Util {
    /**
     * Utility method to check that an Invoice corresponds to the passed Customer. If the Invoice has no CustomerRef,
     * returns false, else checks that the Customer Ref's value matches the Customer's id.
     * @param invoice - The Invoice to check
     * @param customer - The Customer to compare against
     * @return - Whether the Invoice corresponds to the passed Customer
     */
    public static boolean checkInvoiceMatchesCustomer(@NonNull Invoice invoice, @NonNull Customer customer) {
        ReferenceType customerRef = invoice.getCustomerRef();

        if (customerRef == null) {
            return false;
        }

        return customerRef.getValue().equals(customer.getId());
    }

    /**
     * Gets the InvoiceType from the Invoice, based on the Invoice's line items. Expects at least one line to be of type
     * Sales Item, and expects the Invoice to have at most one Line item corresponding to an Invoice Type, based on the
     * Line Item's name. Returns null if none match, or there are no line items of correct type.
     * @param invoice
     * @return
     */
    public static InvoiceType getInvoiceTypeFromInvoice(@NonNull Invoice invoice) {
        return invoice.getLine()
                .stream()
                .filter(line -> line.getDetailType() == LineDetailTypeEnum.SALES_ITEM_LINE_DETAIL)
                .map(Line::getSalesItemLineDetail)
                .map(ItemLineDetail::getItemRef)
                .map(ReferenceType::getName)
                .map(Util::getInvoiceTypeFromLineItemName)
                .filter(Objects::nonNull)
                .findAny().orElse(null);
    }

    private static InvoiceType getInvoiceTypeFromLineItemName(@NonNull String lineItemName) {
        if (lineItemName.equals(InvoiceType.BMUN_SCHOOL_FEE.toString())) {
            return InvoiceType.BMUN_SCHOOL_FEE;
        } else if (lineItemName.equals(InvoiceType.BMUN_DELEGATE_FEE.toString())) {
            return InvoiceType.BMUN_DELEGATE_FEE;
        } else if (lineItemName.equals(InvoiceType.FC_SCHOOL_FEE.toString())) {
            return InvoiceType.FC_SCHOOL_FEE;
        } else if (lineItemName.equals(InvoiceType.FC_DELEGATE_FEE.toString())) {
            return InvoiceType.FC_DELEGATE_FEE;
        }
        return null;
    }

    /**
     * Construct a CustomerRef ReferenceType object from a given Customer object. The ReferenceType's value is the
     * Customer's id, and its name is the Customer's display name.
     * @param customer - The Customer object from which to construct a CustomerRef
     * @return - The constructed CustomerRef
     */
    public static @NonNull ReferenceType getCustomerRefFromCustomer(@NonNull Customer customer) {
        ReferenceType customerRef = new ReferenceType();
        customerRef.setValue(customer.getId());
        customerRef.setName(customer.getDisplayName());
        return customerRef;
    }
}
