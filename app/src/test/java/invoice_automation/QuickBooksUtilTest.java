package invoice_automation;

import com.intuit.ipp.data.Customer;
import com.intuit.ipp.data.EmailAddress;
import com.intuit.ipp.data.Invoice;
import com.intuit.ipp.data.Item;
import com.intuit.ipp.data.Line;
import com.intuit.ipp.data.LineDetailTypeEnum;
import com.intuit.ipp.data.PhysicalAddress;
import com.intuit.ipp.data.ReferenceType;
import com.intuit.ipp.data.SalesItemLineDetail;
import com.intuit.ipp.data.TelephoneNumber;
import invoice_automation.model.Address;
import invoice_automation.model.Conference;
import invoice_automation.model.InvoiceType;
import invoice_automation.model.ItemType;
import invoice_automation.model.PaymentMethod;
import invoice_automation.model.School;
import invoice_automation.utils.QuickBooksUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class QuickBooksUtilTest {
    private static final String CUSTOMER_ID = "ID";
    private static final String CUSTOMER_ID1 = "id";
    private static final String SCHOOL_NAME = "Cal";
    private static final String ITEM_ID = "itemid";
    private static final String ITEM_NAME = "bonus item";
    private static final BigDecimal NUM_DELEGATES = BigDecimal.valueOf(24);
    private static final BigDecimal FC_DEL_FEE_AMT = BigDecimal.valueOf(35);
    private static final BigDecimal BMUN_DEL_FEE_AMT = BigDecimal.valueOf(85);
    private static final BigDecimal CREDIT_CARD_FEE_AMT = BigDecimal.valueOf(34.3);

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

    private final List<String> phoneNumbers = List.of("1234567890", "0987654321");
    private final Address address = new Address("110 Sproul Hall", "", "Berkeley", "CA", "US", "94720");
    private final School school = School.builder()
            .schoolName("Berkeley")
            .address(address)
            .email("oski@berkeley.edu")
            .phoneNumbers(phoneNumbers)
            .build();

    @Test
    public void testCheckInvoiceMatchesCustomer_happyPath_invoiceAndCustomerMatch() {
        // Setup
        when(invoice.getCustomerRef()).thenReturn(customerRef);
        when(customerRef.getValue()).thenReturn(CUSTOMER_ID);
        when(customer.getId()).thenReturn(CUSTOMER_ID);

        // Do
        boolean match = QuickBooksUtil.checkInvoiceMatchesCustomer(invoice, customer);

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
        boolean match = QuickBooksUtil.checkInvoiceMatchesCustomer(invoice, customer);

        // Check
        assertFalse(match);
    }

    @Test
    public void testCheckInvoiceMatchesCustomer_happyPath_invoiceCustomerRefIsNull() {
        // Setup
        when(invoice.getCustomerRef()).thenReturn(null);

        // Do
        boolean match = QuickBooksUtil.checkInvoiceMatchesCustomer(invoice, customer);

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
        assertEquals(InvoiceType.BMUN_SCHOOL_FEE, QuickBooksUtil.getInvoiceTypeFromInvoice(invoice));

        when(itemRef.getName()).thenReturn(InvoiceType.BMUN_DELEGATE_FEE.toString());
        assertEquals(InvoiceType.BMUN_DELEGATE_FEE, QuickBooksUtil.getInvoiceTypeFromInvoice(invoice));

        when(itemRef.getName()).thenReturn(InvoiceType.FC_SCHOOL_FEE.toString());
        assertEquals(InvoiceType.FC_SCHOOL_FEE, QuickBooksUtil.getInvoiceTypeFromInvoice(invoice));

        when(itemRef.getName()).thenReturn(InvoiceType.FC_DELEGATE_FEE.toString());
        assertEquals(InvoiceType.FC_DELEGATE_FEE, QuickBooksUtil.getInvoiceTypeFromInvoice(invoice));
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
        assertNull(QuickBooksUtil.getInvoiceTypeFromInvoice(invoice));
    }

    @Test
    public void testGetInvoiceTypeFromInvoice_noSalesLineItems() {
        // Setup
        when(invoice.getLine()).thenReturn(List.of(line1, line2));
        when(line1.getDetailType()).thenReturn(LineDetailTypeEnum.TAX_LINE_DETAIL);
        when(line2.getDetailType()).thenReturn(LineDetailTypeEnum.TAX_LINE_DETAIL);

        // Do, Check
        assertNull(QuickBooksUtil.getInvoiceTypeFromInvoice(invoice));
    }

    @Test
    public void testGetCustomerRefFromCustomer_happyPath() {
        // Setup
        when(customer.getId()).thenReturn(CUSTOMER_ID);
        when(customer.getDisplayName()).thenReturn(SCHOOL_NAME);

        // Run
        ReferenceType customerRef = QuickBooksUtil.getCustomerRefFromCustomer(customer);

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
        ReferenceType itemRef = QuickBooksUtil.getItemRefFromItem(item);

        // Verify
        ReferenceType expectedItemRef = new ReferenceType();
        expectedItemRef.setValue(ITEM_ID);
        expectedItemRef.setName(ITEM_NAME);
        assertEquals(expectedItemRef, itemRef);
    }

    @Test
    public void testConstructItemQuantityMap_schoolFeeWithCard() {
        // Run
        Map<ItemType, BigDecimal> itemQuantityMap =
                QuickBooksUtil.constructItemQuantityMap(Conference.BMUN, PaymentMethod.CARD, 0);

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
                QuickBooksUtil.constructItemQuantityMap(Conference.BMUN, PaymentMethod.CHECK, NUM_DELEGATES.intValue());

        // Verify
        Map<ItemType, BigDecimal> expectedMap =
                Map.of(ItemType.BMUN_DELEGATE_FEE, NUM_DELEGATES);
        assertEquals(expectedMap, itemQuantityMap);
    }

    @Test
    public void testConstructItemRateMap_delegateFeeWithCard() {
        // Run
        Map<ItemType, BigDecimal> itemRateMap =
                QuickBooksUtil.constructItemRateMap(Conference.BMUN, false, CREDIT_CARD_FEE_AMT);

        // Verify
        Map<ItemType, BigDecimal> expectedMap = Map.of(
                ItemType.BMUN_DELEGATE_FEE,
                BMUN_DEL_FEE_AMT,
                ItemType.CREDIT_CARD_PROCESSING_FEE,
                CREDIT_CARD_FEE_AMT
        );
        assertEquals(expectedMap, itemRateMap);
    }

    @Test
    public void testConstructItemRateMap_schoolFeeNoCard() {
        // Run
        Map<ItemType, BigDecimal> itemRateMap =
                QuickBooksUtil.constructItemRateMap(Conference.FC, true, BigDecimal.valueOf(0));

        // Verify
        Map<ItemType, BigDecimal> expectedMap =
                Map.of(ItemType.FC_SCHOOL_FEE, FC_DEL_FEE_AMT);
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
                QuickBooksUtil.constructItemRefMap(allRefsMap, true, PaymentMethod.CARD, Conference.BMUN);

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
                QuickBooksUtil.constructItemRefMap(allRefsMap, false, PaymentMethod.CHECK, Conference.FC);

        // Verify
        ReferenceType delFeeItemRef = allRefsMap.get(ItemType.FC_DELEGATE_FEE);
        Map<ItemType, ReferenceType> expectedMap = Map.of(ItemType.FC_DELEGATE_FEE, delFeeItemRef);
        assertEquals(expectedMap, itemRefMap);
    }
    @Test
    public void getCustomerFromSchool_happyPath() {
        // Run
        Customer customer = QuickBooksUtil.getCustomerFromSchool(school);

        // Verify
        Customer expectedCustomer = new Customer();
        expectedCustomer.setCompanyName("Berkeley");
        expectedCustomer.setDisplayName("Berkeley");
        expectedCustomer.setPrimaryEmailAddr(new EmailAddress());
        expectedCustomer.getPrimaryEmailAddr().setAddress("oski@berkeley.edu");
        PhysicalAddress schoolAddress = new PhysicalAddress();
        schoolAddress.setLine1("110 Sproul Hall");
        schoolAddress.setLine2("");
        schoolAddress.setCity("Berkeley");
        schoolAddress.setCountrySubDivisionCode("CA");
        schoolAddress.setCountry("US");
        schoolAddress.setPostalCode("94720");
        expectedCustomer.setBillAddr(schoolAddress);
        expectedCustomer.setShipAddr(schoolAddress);
        expectedCustomer.setPrimaryPhone(new TelephoneNumber());
        expectedCustomer.getPrimaryPhone().setFreeFormNumber(phoneNumbers.get(0));
        expectedCustomer.setAlternatePhone(new TelephoneNumber());
        expectedCustomer.getAlternatePhone().setFreeFormNumber(phoneNumbers.get(1));
        assertEquals(expectedCustomer, customer);
    }
}
