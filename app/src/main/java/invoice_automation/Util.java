package invoice_automation;

import com.intuit.ipp.data.Customer;
import com.intuit.ipp.data.Invoice;
import com.intuit.ipp.data.Item;
import com.intuit.ipp.data.ItemLineDetail;
import com.intuit.ipp.data.Line;
import com.intuit.ipp.data.LineDetailTypeEnum;
import com.intuit.ipp.data.ReferenceType;
import com.intuit.ipp.data.SalesItemLineDetail;
import invoice_automation.model.Conference;
import invoice_automation.model.InvoiceType;
import invoice_automation.model.ItemType;
import invoice_automation.model.PaymentMethod;
import invoice_automation.model.Registration;
import invoice_automation.model.RegistrationRound;
import lombok.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    /**
     * Construct an ItemRef ReferenceType object from the passed Item object. The ReferenceType's value is the Item's id
     * and its name is the Item's name.
     * @param item - The Item object from which to construct an ItemRef
     * @return - The constructed ItemRef
     */
    public static @NonNull ReferenceType getItemRefFromItem(@NonNull Item item) {
        ReferenceType itemRef = new ReferenceType();
        itemRef.setValue(item.getId());
        itemRef.setName(item.getName());
        return itemRef;
    }

    /**
     * Determine the registration round that the passed date falls into
     * @return - The registration round
     */
    public static RegistrationRound getRegistrationRound(
            @NonNull LocalDate registrationDate,
            @NonNull Conference conference
    ) {
        if (conference == Conference.BMUN) {
            if (registrationDate.isBefore(RegistrationRound.BMUN_ROUND_2.getRoundStartDate())) {
                return RegistrationRound.BMUN_ROUND_1;
            } else if (registrationDate.isBefore(RegistrationRound.BMUN_ROUND_3.getRoundStartDate())) {
                return RegistrationRound.BMUN_ROUND_2;
            } else if (registrationDate.isBefore(RegistrationRound.BMUN_ROUND_4.getRoundStartDate())) {
                return RegistrationRound.BMUN_ROUND_3;
            } else {
                return RegistrationRound.BMUN_ROUND_4;
            }
        } else {
            if (registrationDate.isBefore(RegistrationRound.FC_ROUND_2.getRoundStartDate())) {
                return RegistrationRound.FC_ROUND_1;
            } else {
                return RegistrationRound.FC_ROUND_2;
            }
        }
    }

    /**
     * Calculates the credit card processing fee for the specified invoice to be assessed for the passed Registration
     * by calculating the subtotal and then the fee amount. Assumes the registration's payment method is set to card.
     * @param registration - The registration for which to calculate the fee.
     * @param isForSchoolFee - Whether to compute the fee for the school fee invoice. If false, computes the fee for the
     *                       delegate fee invoice.
     * @return The fee, as a float
     */
    public static BigDecimal calculateCreditCardProcessingFee(@NonNull Registration registration, boolean isForSchoolFee) {
        BigDecimal subtotal = calculateSubtotal(registration, isForSchoolFee);
        return calculateCreditCardProcessingFee(subtotal);
    }

    /**
     * Calculates the subtotal for the specified invoice for the given registration.
     * @param registration - The registration for which to calcuate the subtoal.
     * @param isForSchoolFee - Whether to compute the subtotal for the school fee invoice. If false, computes the
     *                       subtotal for the delegate fee invoice.
     * @return - The subtotal as a float
     */
    private static BigDecimal calculateSubtotal(@NonNull Registration registration, boolean isForSchoolFee) {
        if (isForSchoolFee) {
            if (registration.getConference() == Conference.BMUN) {
                return InvoiceType.BMUN_SCHOOL_FEE.getUnitCost();
            } else {
                return InvoiceType.FC_SCHOOL_FEE.getUnitCost();
            }
        } else {
            BigDecimal unitCost;
            if (registration.getConference() == Conference.BMUN) {
                unitCost = InvoiceType.BMUN_DELEGATE_FEE.getUnitCost();
            } else {
                unitCost = InvoiceType.FC_DELEGATE_FEE.getUnitCost();
            }
            BigDecimal numDelegates = BigDecimal.valueOf(registration.getNumDelegates());
            return unitCost.multiply(numDelegates);
        }
    }

    /**
     * Calculates the credit card processing fee for an invoice from its subtotal.
     * @param subtotal - The subtotal of the invoice for which to compute the fee.
     * @return - The fee amount as a float
     */
    private static BigDecimal calculateCreditCardProcessingFee(BigDecimal subtotal) {
        BigDecimal fee = BigDecimal.valueOf(0.029).multiply(subtotal);
        fee = fee.add(BigDecimal.valueOf(0.25));
        fee = fee.divide(BigDecimal.valueOf(0.971), 2, RoundingMode.CEILING);
        return fee;
    }

    /**
     * A helper function to construct an Invoice object. Using the Map items, SalesLineItems are created and set for the
     * invoice's Line.
     * @param customerRef - An ReferenceType object for the Customer being invoiced
     * @param invoiceDate - The date to attach to the Invoice
     * @param dueDate - The Invoice's due date
     * @param itemQuantityMap - A map from ItemType to the quantity of that item to be billed in the Invoice.
     * @param itemRateMap - A map from ItemType to the per-item price of that item to be billed in the Invoice.
     * @param itemRefMap - A map from ItemType to ReferenceType item references.
     * @param paymentMethod - The Payment method being used to pay. If CARD, then AllowOnlineCreditCardPayment should be
     *                      set to true
     * @return - The constructed invoice
     */
    public static Invoice constructInvoice(
            @NonNull ReferenceType customerRef,
            @NonNull Date invoiceDate,
            @NonNull Date dueDate,
            @NonNull Map<ItemType, BigDecimal> itemQuantityMap,
            @NonNull Map<ItemType, BigDecimal> itemRateMap,
            @NonNull Map<ItemType, ReferenceType> itemRefMap,
            @NonNull PaymentMethod paymentMethod
    ) {
        Invoice invoice = new Invoice();

        invoice.setCustomerRef(customerRef);
        invoice.setTxnDate(invoiceDate);
        invoice.setDueDate(dueDate);
        invoice.setAllowOnlineCreditCardPayment(paymentMethod == PaymentMethod.CARD);

        List<Line> lineItems = new ArrayList<>(2);
        for (ItemType itemType: itemRefMap.keySet()) {
            BigDecimal itemQuantity = itemQuantityMap.get(itemType);
            BigDecimal itemRate = itemRateMap.get(itemType);

            Line lineItem = new Line();
            lineItem.setDetailType(LineDetailTypeEnum.SALES_ITEM_LINE_DETAIL);
            lineItem.setAmount(itemQuantity.multiply(itemRate));

            SalesItemLineDetail salesItemLineDetail = new SalesItemLineDetail();
            salesItemLineDetail.setItemRef(itemRefMap.get(itemType));
            salesItemLineDetail.setQty(itemQuantity);
            salesItemLineDetail.setUnitPrice(itemRate);
            lineItem.setSalesItemLineDetail(salesItemLineDetail);

            lineItems.add(lineItem);
        }
        invoice.setLine(lineItems);

        return invoice;
    }

    /**
     * Helper function to construct a map from item type to the quantity of that map in an invoice.
     * @param conference - The conference for which the invoice would be issued
     * @param paymentMethod - The payment which would be used to pay for the invoice
     * @param numDelegates - The number of delegates for which an invoice would be issued. If 0, then compute the map
     *                     for a school fee invoice, otherwise compute one for a delegate fee invoice.
     * @return - The constructed map
     */
    public static @NonNull Map<ItemType, BigDecimal> constructItemQuantityMap(
            Conference conference,
            PaymentMethod paymentMethod,
            int numDelegates) {
        Map<ItemType, BigDecimal> itemQuantityMap = new HashMap<>();

        if (numDelegates == 0) {
            ItemType schoolFeeItemType =
                    conference == Conference.BMUN ? ItemType.BMUN_SCHOOL_FEE : ItemType.FC_SCHOOL_FEE;
            itemQuantityMap.put(schoolFeeItemType, BigDecimal.valueOf(1));
        } else {
            ItemType delegateFeeItemType =
                    conference == Conference.BMUN ? ItemType.BMUN_DELEGATE_FEE : ItemType.FC_DELEGATE_FEE;
            itemQuantityMap.put(delegateFeeItemType, BigDecimal.valueOf(numDelegates));
        }

        if (paymentMethod == PaymentMethod.CARD) {
            itemQuantityMap.put(ItemType.CREDIT_CARD_PROCESSING_FEE, BigDecimal.valueOf(1));
        }

        return itemQuantityMap;
    }

    /**
     * Helper function to construct a map from ItemType to item rate for use in constructing an invoice.
     * @param conference - The Conference for which an invoice will be constructed
     * @param isSchoolFee - Whether the invoice is for a school fee. If false, the invoice is for a delegate fee
     * @param creditCardProcessingFeeAmount - The amount of the credit card processing fee. If 0, the payment method is
     *                                      assumed to be Check, and an entry for the credit card fee is not added to
     *                                      the returned map.
     * @return - The constructed map
     */
    public static Map<ItemType, BigDecimal> constructItemRateMap(
            Conference conference,
            boolean isSchoolFee,
            BigDecimal creditCardProcessingFeeAmount) {
        Map<ItemType, BigDecimal> itemRateMap = new HashMap<>();

        if (isSchoolFee) {
            if (conference == Conference.FC) {
                itemRateMap.put(ItemType.FC_SCHOOL_FEE, InvoiceType.FC_SCHOOL_FEE.getUnitCost());
            } else {
                itemRateMap.put(ItemType.BMUN_SCHOOL_FEE, InvoiceType.BMUN_SCHOOL_FEE.getUnitCost());
            }
        } else {
            if (conference == Conference.FC) {
                itemRateMap.put(ItemType.FC_DELEGATE_FEE, InvoiceType.FC_DELEGATE_FEE.getUnitCost());
            } else {
                itemRateMap.put(ItemType.BMUN_DELEGATE_FEE, InvoiceType.BMUN_DELEGATE_FEE.getUnitCost());
            }
        }

        if (!creditCardProcessingFeeAmount.equals(BigDecimal.ZERO)) {
            itemRateMap.put(ItemType.CREDIT_CARD_PROCESSING_FEE, creditCardProcessingFeeAmount);
        }

        return itemRateMap;
    }

    /**
     * Helper function to construct the item ref map for a particular invoice.
     * @param allItemRefsMap - A map containing all available item refs. The returned map is a subset of this map
     * @param isSchoolFee - Whether the invoice this map is being constructed for is for a school fee. If false, this
     *                    map is being constructed for a delegate fee invoice.
     * @param paymentMethod - The payment method for the invoice
     * @param conference - The Conference for the invoice
     * @return - The constructed map
     */
    public static Map<ItemType, ReferenceType> constructItemRefMap(
            Map<ItemType, ReferenceType> allItemRefsMap,
            boolean isSchoolFee,
            PaymentMethod paymentMethod,
            Conference conference
    ) {
        Map<ItemType, ReferenceType> itemRefMap = new HashMap<>();

        ItemType itemType;
        if (conference == Conference.BMUN) {
            if (isSchoolFee) {
                itemType = ItemType.BMUN_SCHOOL_FEE;
            } else {
                itemType = ItemType.BMUN_DELEGATE_FEE;
            }
        } else {
            if (isSchoolFee) {
                itemType = ItemType.FC_SCHOOL_FEE;
            } else {
                itemType = ItemType.FC_DELEGATE_FEE;
            }
        }
        itemRefMap.put(itemType, allItemRefsMap.get(itemType));

        if (paymentMethod == PaymentMethod.CARD) {
            itemRefMap.put(
                    ItemType.CREDIT_CARD_PROCESSING_FEE,
                    allItemRefsMap.get(ItemType.CREDIT_CARD_PROCESSING_FEE)
            );
        }

        return itemRefMap;
    }

    /**
     * Helper function to cast a LocalDate object to a java.util.Date object.
     * @param localDate - The LocalDate object to cast
     * @return - The resulting java.util.Date object
     */
    public static Date getDate(LocalDate localDate) {
        return java.sql.Date.valueOf(localDate);
    }
}
