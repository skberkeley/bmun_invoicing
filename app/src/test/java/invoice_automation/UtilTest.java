package invoice_automation;

import com.intuit.ipp.data.Customer;
import com.intuit.ipp.data.Invoice;
import com.intuit.ipp.data.Item;
import com.intuit.ipp.data.Line;
import com.intuit.ipp.data.LineDetailTypeEnum;
import com.intuit.ipp.data.ReferenceType;
import com.intuit.ipp.data.SalesItemLineDetail;
import invoice_automation.model.Conference;
import invoice_automation.model.InvoiceType;
import invoice_automation.model.ItemType;
import invoice_automation.model.PaymentMethod;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class UtilTest {
    private static final String CUSTOMER_ID = "ID";
    private static final String CUSTOMER_ID1 = "id";
    private static final String SCHOOL_NAME = "Cal";
    private static final String ITEM_ID = "itemid";
    private static final String ITEM_ID1 = "ITEM ID";
    private static final String ITEM_NAME = "bonus item";
    private static final String ITEM_NAME1 = "bonus item 2";
    private static final BigDecimal NUM_DELEGATES = BigDecimal.valueOf(24);
    private static final BigDecimal DEL_FEE_AMT = BigDecimal.valueOf(85);
    private static final BigDecimal CREDIT_CARD_FEE_AMT = BigDecimal.valueOf(34.3);
    // TODO get rid of this and change references to refer to a value in Consts
    private static final BigDecimal FC_SCHOOL_FEE_AMT = BigDecimal.valueOf(25);
    // TODO: Change these to Dates
    private static final LocalDate INVOICE_DATE = LocalDate.of(2023, 11, 6);
    private static final LocalDate DUE_DATE = LocalDate.of(2023, 12, 15);

    @Mock
    private Invoice invoice;
    @Mock
    private Customer customer;
    @Mock
    private ReferenceType customerRef;
    @Mock
    private Line line1;
    @Mock
    private Line line2;
    @Mock
    private SalesItemLineDetail salesItemLineDetail;
    @Mock
    private ReferenceType itemRef;
    @Mock
    private Item item;

    @Test
    public void testCheckInvoiceMatchesCustomer_happyPath_invoiceAndCustomerMatch() {
        // Setup
        when(invoice.getCustomerRef()).thenReturn(customerRef);
        when(customerRef.getValue()).thenReturn(CUSTOMER_ID);
        when(customer.getId()).thenReturn(CUSTOMER_ID);

        // Do
        boolean match = Util.checkInvoiceMatchesCustomer(invoice, customer);

        // Check
        assertTrue(match);
    }

    @Test
    public void testCheckInvoiceMatchesCustomer_happyPath_invoiceAndCustomerNoMatch() {
        // Setup
        when(invoice.getCustomerRef()).thenReturn(customerRef);
        when(customerRef.getValue()).thenReturn(CUSTOMER_ID);
        when(customer.getId()).thenReturn(CUSTOMER_ID1);

        // Do
        boolean match = Util.checkInvoiceMatchesCustomer(invoice, customer);

        // Check
        assertFalse(match);
    }

    @Test
    public void testCheckInvoiceMatchesCustomer_happyPath_invoiceCustomerRefIsNull() {
        // Setup
        when(invoice.getCustomerRef()).thenReturn(null);

        // Do
        boolean match = Util.checkInvoiceMatchesCustomer(invoice, customer);

        // Check
        assertFalse(match);
    }

    @Test
    public void testGetInvoiceTypeFromInvoice_happyPath() {
        // Setup
        when(invoice.getLine()).thenReturn(List.of(line1, line2));
        when(line1.getDetailType()).thenReturn(LineDetailTypeEnum.TAX_LINE_DETAIL);
        when(line2.getDetailType()).thenReturn(LineDetailTypeEnum.SALES_ITEM_LINE_DETAIL);
        when(line2.getSalesItemLineDetail()).thenReturn(salesItemLineDetail);
        when(salesItemLineDetail.getItemRef()).thenReturn(itemRef);

        // Do, Check
        when(itemRef.getName()).thenReturn(InvoiceType.BMUN_SCHOOL_FEE.toString());
        assertEquals(InvoiceType.BMUN_SCHOOL_FEE, Util.getInvoiceTypeFromInvoice(invoice));

        when(itemRef.getName()).thenReturn(InvoiceType.BMUN_DELEGATE_FEE.toString());
        assertEquals(InvoiceType.BMUN_DELEGATE_FEE, Util.getInvoiceTypeFromInvoice(invoice));

        when(itemRef.getName()).thenReturn(InvoiceType.FC_SCHOOL_FEE.toString());
        assertEquals(InvoiceType.FC_SCHOOL_FEE, Util.getInvoiceTypeFromInvoice(invoice));

        when(itemRef.getName()).thenReturn(InvoiceType.FC_DELEGATE_FEE.toString());
        assertEquals(InvoiceType.FC_DELEGATE_FEE, Util.getInvoiceTypeFromInvoice(invoice));
    }

    @Test
    public void testGetInvoiceTypeFromInvoice_noSalesLineItemMatches() {
        // Setup
        when(invoice.getLine()).thenReturn(List.of(line1, line2));
        when(line1.getDetailType()).thenReturn(LineDetailTypeEnum.TAX_LINE_DETAIL);
        when(line2.getDetailType()).thenReturn(LineDetailTypeEnum.SALES_ITEM_LINE_DETAIL);
        when(line2.getSalesItemLineDetail()).thenReturn(salesItemLineDetail);
        when(salesItemLineDetail.getItemRef()).thenReturn(itemRef);
        when(itemRef.getName()).thenReturn("UCBMUN");

        // Do, Check
        assertNull(Util.getInvoiceTypeFromInvoice(invoice));
    }

    @Test
    public void testGetInvoiceTypeFromInvoice_noSalesLineItems() {
        // Setup
        when(invoice.getLine()).thenReturn(List.of(line1, line2));
        when(line1.getDetailType()).thenReturn(LineDetailTypeEnum.TAX_LINE_DETAIL);
        when(line2.getDetailType()).thenReturn(LineDetailTypeEnum.TAX_LINE_DETAIL);

        // Do, Check
        assertNull(Util.getInvoiceTypeFromInvoice(invoice));
    }

    @Test
    public void testGetCustomerRefFromCustomer_happyPath() {
        // Setup
        when(customer.getId()).thenReturn(CUSTOMER_ID);
        when(customer.getDisplayName()).thenReturn(SCHOOL_NAME);

        // Run
        ReferenceType customerRef = Util.getCustomerRefFromCustomer(customer);

        // Verify
        ReferenceType expectedCustomerRef = new ReferenceType();
        expectedCustomerRef.setValue(CUSTOMER_ID);
        expectedCustomerRef.setName(SCHOOL_NAME);
        assertEquals(expectedCustomerRef, customerRef);
    }

    @Test
    public void testGetItemRefFromItem_happyPath() {
        // Setup
        when(item.getId()).thenReturn(ITEM_ID);
        when(item.getName()).thenReturn(ITEM_NAME);

        // Run
        ReferenceType itemRef = Util.getItemRefFromItem(item);

        // Verify
        ReferenceType expectedItemRef = new ReferenceType();
        expectedItemRef.setValue(ITEM_ID);
        expectedItemRef.setName(ITEM_NAME);
        assertEquals(expectedItemRef, itemRef);
    }

    @Test
    public void testGetRegistrationRoundFromRegistrationDate() {
        // TODO
        assert false;
    }

    @Test
    public void testCalculateCreditCardProcessingFee() {
        // TODO
        assert false;
    }

    @Test
    public void testConstructInvoice_happyPath() {
        // Setup
        // Construct what would be needed for a mock BMUN del fee invoice
        ReferenceType customerRef = new ReferenceType();
        customerRef.setName(SCHOOL_NAME);
        customerRef.setValue(CUSTOMER_ID);
        Map<ItemType, BigDecimal> itemQuantityMap = Map.of(
                ItemType.BMUN_DELEGATE_FEE,
                NUM_DELEGATES,
                ItemType.CREDIT_CARD_PROCESSING_FEE,
                BigDecimal.valueOf(1)
        );
        Map<ItemType, BigDecimal> itemRateMap = Map.of(
                ItemType.BMUN_DELEGATE_FEE,
                DEL_FEE_AMT,
                ItemType.CREDIT_CARD_PROCESSING_FEE,
                CREDIT_CARD_FEE_AMT
        );
        ReferenceType itemRef1 = new ReferenceType();
        itemRef1.setName(ITEM_NAME);
        itemRef1.setValue(ITEM_ID);
        ReferenceType itemRef2 = new ReferenceType();
        itemRef2.setName(ITEM_NAME1);
        itemRef2.setValue(ITEM_ID1);
        Map<ItemType, ReferenceType> itemRefMap =
                Map.of(ItemType.BMUN_DELEGATE_FEE, itemRef1, ItemType.CREDIT_CARD_PROCESSING_FEE, itemRef2);

        // Run
        // TODO: fix call to pass date
        /*
        Invoice invoice = Util.constructInvoice(
                customerRef,
                itemQuantityMap,
                itemRateMap,
                itemRefMap,
                PaymentMethod.CARD
        )
         */
        ;

        // Verify
        // Construct expected Invoice
        Invoice expectedInvoice = new Invoice();
        expectedInvoice.setCustomerRef(customerRef);
        // TODO: set invoice date, due date
        expectedInvoice.setAllowOnlineCreditCardPayment(true);
        // Construct the lines
        Line delFeeLineItem = new Line();
        delFeeLineItem.setDetailType(LineDetailTypeEnum.SALES_ITEM_LINE_DETAIL);
        delFeeLineItem.setAmount(DEL_FEE_AMT.multiply(NUM_DELEGATES));
        SalesItemLineDetail delFeeLineDetail = new SalesItemLineDetail();
        delFeeLineDetail.setItemRef(itemRef1);
        delFeeLineDetail.setQty(NUM_DELEGATES);
        delFeeLineDetail.setUnitPrice(DEL_FEE_AMT);
        delFeeLineItem.setSalesItemLineDetail(delFeeLineDetail);
        Line creditCardFeeItem = new Line();
        creditCardFeeItem.setDetailType(LineDetailTypeEnum.SALES_ITEM_LINE_DETAIL);
        creditCardFeeItem.setAmount(CREDIT_CARD_FEE_AMT);
        SalesItemLineDetail creditCardFeeLineDetail = new SalesItemLineDetail();
        creditCardFeeLineDetail.setItemRef(itemRef2);
        creditCardFeeLineDetail.setQty(BigDecimal.valueOf(1));
        creditCardFeeLineDetail.setUnitPrice(CREDIT_CARD_FEE_AMT);
        creditCardFeeItem.setSalesItemLineDetail(creditCardFeeLineDetail);
        expectedInvoice.setLine(List.of(delFeeLineItem, creditCardFeeItem));

        assertEquals(expectedInvoice, invoice);
    }

    @Test
    public void testConstructItemQuantityMap_schoolFeeWithCard() {
        // Run
        Map<ItemType, BigDecimal> itemQuantityMap =
                Util.constructItemQuantityMap(Conference.BMUN, PaymentMethod.CARD, 0);

        // Verify
        Map<ItemType, BigDecimal> expectedMap = Map.of(
                ItemType.BMUN_SCHOOL_FEE,
                BigDecimal.valueOf(1),
                ItemType.CREDIT_CARD_PROCESSING_FEE,
                BigDecimal.valueOf(1)
        );
        assertEquals(expectedMap, itemQuantityMap);
    }

    @Test
    public void testConstructItemQuantityMap_delegateFeeNoCard() {
        // Run
        Map<ItemType, BigDecimal> itemQuantityMap =
                Util.constructItemQuantityMap(Conference.BMUN, PaymentMethod.CHECK, NUM_DELEGATES.intValue());

        // Verify
        Map<ItemType, BigDecimal> expectedMap =
                Map.of(ItemType.BMUN_DELEGATE_FEE, NUM_DELEGATES);
        assertEquals(expectedMap, itemQuantityMap);
    }

    @Test
    public void testConstructItemRateMap_delegateFeeWithCard() {
        // Run
        Map<ItemType, BigDecimal> itemRateMap =
                Util.constructItemRateMap(Conference.BMUN, false, CREDIT_CARD_FEE_AMT);

        // Verify
        Map<ItemType, BigDecimal> expectedMap = Map.of(
                ItemType.BMUN_DELEGATE_FEE,
                DEL_FEE_AMT,
                ItemType.CREDIT_CARD_PROCESSING_FEE,
                CREDIT_CARD_FEE_AMT
        );
        assertEquals(expectedMap, itemRateMap);
    }

    @Test
    public void testConstructItemRateMap_schoolFeeNoCard() {
        // Run
        Map<ItemType, BigDecimal> itemRateMap =
                Util.constructItemRateMap(Conference.FC, true, BigDecimal.valueOf(0));

        // Verify
        Map<ItemType, BigDecimal> expectedMap =
                Map.of(ItemType.FC_SCHOOL_FEE, DEL_FEE_AMT);
        assertEquals(expectedMap, itemRateMap);
    }

    private Map<ItemType, ReferenceType> constructAllItemRefsMap() {
        return Map.of(
                ItemType.BMUN_SCHOOL_FEE, new ReferenceType(),
                ItemType.BMUN_DELEGATE_FEE, new ReferenceType(),
                ItemType.FC_SCHOOL_FEE, new ReferenceType(),
                ItemType.FC_DELEGATE_FEE, new ReferenceType(),
                ItemType.CREDIT_CARD_PROCESSING_FEE, new ReferenceType()
        );
    }

    @Test
    public void testConstructItemRefMap_BMUNSchoolFeeWithCard() {
        // Setup
        Map<ItemType, ReferenceType> allRefsMap = constructAllItemRefsMap();

        // Run
        Map<ItemType, ReferenceType> itemRefMap =
                Util.constructItemRefMap(allRefsMap, true, PaymentMethod.CARD, Conference.BMUN);

        // Verify
        ReferenceType schoolFeeItemRef = allRefsMap.get(ItemType.BMUN_SCHOOL_FEE);
        ReferenceType creditCardFeeItemRef = allRefsMap.get(ItemType.CREDIT_CARD_PROCESSING_FEE);
        Map<ItemType, ReferenceType> expectedMap = Map.of(
                ItemType.BMUN_SCHOOL_FEE, schoolFeeItemRef,
                ItemType.CREDIT_CARD_PROCESSING_FEE, creditCardFeeItemRef
        );
        assertEquals(expectedMap, itemRefMap);
    }

    @Test
    public void testConstructItemRefMap_FCDelFeeNoCard() {
        // Setup
        Map<ItemType, ReferenceType> allRefsMap = constructAllItemRefsMap();

        // Run
        Map<ItemType, ReferenceType> itemRefMap =
                Util.constructItemRefMap(allRefsMap, false, PaymentMethod.CHECK, Conference.FC);

        // Verify
        ReferenceType delFeeItemRef = allRefsMap.get(ItemType.FC_DELEGATE_FEE);
        Map<ItemType, ReferenceType> expectedMap = Map.of(ItemType.FC_DELEGATE_FEE, delFeeItemRef);
        assertEquals(expectedMap, itemRefMap);
    }
}
