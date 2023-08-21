package invoice_automation.module;

import com.intuit.ipp.core.Context;
import com.intuit.ipp.data.Customer;
import com.intuit.ipp.data.EmailAddress;
import com.intuit.ipp.data.EmailStatusEnum;
import com.intuit.ipp.data.Invoice;
import com.intuit.ipp.data.Item;
import com.intuit.ipp.data.Line;
import com.intuit.ipp.data.LineDetailTypeEnum;
import com.intuit.ipp.data.MemoRef;
import com.intuit.ipp.data.ReferenceType;
import com.intuit.ipp.data.SalesItemLineDetail;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.services.DataService;
import com.intuit.ipp.util.Config;
import invoice_automation.QuickBooksException;
import invoice_automation.Util;
import invoice_automation.model.Address;
import invoice_automation.model.Conference;
import invoice_automation.model.InvoiceType;
import invoice_automation.model.ItemType;
import invoice_automation.model.PaymentMethod;
import invoice_automation.model.Registration;
import invoice_automation.model.RegistrationRound;
import invoice_automation.model.School;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static invoice_automation.Consts.SANDBOX_BASE_URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({QuickBooksModule.class, Config.class, Util.class})
public class QuickBooksModuleTest {
    private static final String ACCESS_TOKEN = "access token";
    private static final String REALM_ID = "realm id";
    private static final String EMAIL_ADDRESS = "oski@berkeley.edu";
    private static final String MEMO = "memo123";
    private static final String SCHOOL_NAME = "Cal";
    private static final String CUSTOMER_ID = "customer id";
    private static final String FC_SCHOOL_FEE_ITEM_ID = "fc school fee item id";
    private static final String FC_DEL_FEE_ITEM_ID = "fc del fee item id";
    private static final String CC_FEE_ITEM_ID = "credit card processing fee id";
    private static final int NUM_DELEGATES = 10;

    @Mock
    private DataService dataService;
    @Mock
    private EmailAddress billEmail;
    @Mock
    private Customer customer;
    private Invoice invoice;
    private QuickBooksModule quickBooksModule;
    private final Address address = new Address(
            "",
            "",
            "",
            "",
            "",
            ""
    );
    private final School school = School.builder()
            .schoolName(SCHOOL_NAME)
            .email("cal@cal.edu")
            .phoneNumbers(List.of())
            .address(address)
            .build();
    private final LocalDate registrationDate = LocalDate.of(2023, 9, 4);
    private final Registration registration = Registration.builder()
            .school(school)
            .numDelegates(NUM_DELEGATES)
            .registrationDate(registrationDate)
            .paymentMethod(PaymentMethod.CARD)
            .conference(Conference.FC)
            .build();

    @Test
    public void testQuickBooksModuleConstructor_noSandbox_happyPath() {
        // Setup
        mockStatic(Config.class);

        // Run
        new QuickBooksModule(ACCESS_TOKEN, REALM_ID, false);

        // Verify
        verifyStatic(Config.class, Mockito.never());
        Config.setProperty(anyString(), anyString());
    }

    @Test
    public void testQuickBooksModuleConstructor_sandbox_happyPath() {
        // Setup
        mockStatic(Config.class);

        // Run
        new QuickBooksModule(ACCESS_TOKEN, REALM_ID, true);

        // Verify
        verifyStatic(Config.class);
        Config.setProperty(Config.BASE_URL_QBO, SANDBOX_BASE_URL);
    }

    @Test(expected = QuickBooksException.class)
    public void testQuickBooksModuleConstructor_contextConstructorThrows() throws Exception {
        // Setup
        whenNew(Context.class).withAnyArguments().thenThrow(FMSException.class);

        // Run
        new QuickBooksModule(ACCESS_TOKEN, REALM_ID, false);
    }

    private void setupMethodTests() throws Exception {
        whenNew(DataService.class).withAnyArguments().thenReturn(dataService);

        quickBooksModule = new QuickBooksModule(ACCESS_TOKEN, REALM_ID, false);
    }

    private void setupInvoiceTests() {
        invoice = new Invoice();
        invoice.setBillEmail(billEmail);
    }

    @Test
    public void testGetAllCustomers_happyPath() throws Exception {
        // Setup
        setupMethodTests();
        List<Customer> customerList = List.of();
        when(dataService.findAll(any(Customer.class))).thenReturn(customerList);

        // Run
        List<Customer> returnedCustomers = quickBooksModule.getAllCustomers();

        // Verify
        assertEquals(customerList, returnedCustomers);
    }

    @Test(expected = QuickBooksException.class)
    public void testGetAllCustomers_dataServiceThrows() throws Exception {
        // Setup
        setupMethodTests();
        when(dataService.findAll(any())).thenThrow(FMSException.class);

        // Run
        quickBooksModule.getAllCustomers();
    }

    @Test
    public void testSendInvoice_happyPath() throws Exception {
        // Setup
        setupMethodTests();
        setupInvoiceTests();
        when(billEmail.getAddress()).thenReturn(EMAIL_ADDRESS);

        // Run
        quickBooksModule.sendInvoice(invoice);

        // Verify
        Mockito.verify(dataService).sendEmail(invoice, EMAIL_ADDRESS);
        assertEquals(EmailStatusEnum.EMAIL_SENT, invoice.getEmailStatus());
        verify(dataService).sendEmail(invoice, EMAIL_ADDRESS);
    }

    @Test(expected = QuickBooksException.class)
    public void testSendInvoice_dataServiceThrows() throws Exception {
        // Setup
        setupMethodTests();
        setupInvoiceTests();
        when(dataService.sendEmail(any(), any())).thenThrow(FMSException.class);

        // Run
        quickBooksModule.sendInvoice(invoice);
    }

    @Test
    public void testGetInvoiceWithMatchingMemo_happyPath() throws Exception {
        // Setup
        setupMethodTests();
        Invoice nonMatchingInvoice = new Invoice();
        Invoice matchingInvoice = new Invoice();
        matchingInvoice.setCustomerMemo(new MemoRef());
        matchingInvoice.getCustomerMemo().setValue(MEMO);
        List<Invoice> invoices = List.of(nonMatchingInvoice, matchingInvoice);
        when(dataService.findAll(any(Invoice.class))).thenReturn(invoices);

        // Run
        Invoice returnedInvoice = quickBooksModule.getInvoiceWithMatchingMemo(MEMO);

        // Verify
        assertEquals(matchingInvoice, returnedInvoice);
    }

    @Test(expected = QuickBooksException.class)
    public void testGetInvoiceWithMatchingMemo_dataServiceThrows() throws Exception {
        // Setup
        setupMethodTests();
        when(dataService.findAll(any(Invoice.class))).thenThrow(FMSException.class);

        // Run
        quickBooksModule.getInvoiceWithMatchingMemo(MEMO);
    }

    @Test
    public void testGetInvoiceWithMatchingMemo_noMatchingInvoice() throws Exception {
        // Setup
        setupMethodTests();
        Invoice nonMatchingInvoice = new Invoice();
        Invoice nonMatchingInvoice1 = new Invoice();
        nonMatchingInvoice1.setCustomerMemo(new MemoRef());
        nonMatchingInvoice1.getCustomerMemo().setValue(EMAIL_ADDRESS);
        List<Invoice> invoices = List.of(nonMatchingInvoice, nonMatchingInvoice1);
        when(dataService.findAll(any(Invoice.class))).thenReturn(invoices);

        // Run
        Invoice returnedInvoice = quickBooksModule.getInvoiceWithMatchingMemo(MEMO);

        // Verify
        assertNull(returnedInvoice);
    }

    public Customer getExpectedCustomer() {
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
        return expectedCustomer;
    }

    @Test
    public void testUpdateCustomerFromSchool_addCustomer() throws Exception {
        // Setup
        setupMethodTests();
        Customer expectedCustomer = getExpectedCustomer();

        // Run
        Customer returnedCustomer = quickBooksModule.updateCustomerFromSchool(school);

        // Verify
        assertEquals(expectedCustomer, returnedCustomer);
        verify(dataService).add(returnedCustomer);
    }

    @Test
    public void testUpdateCustomerFromSchool_updateCustomer() throws Exception {
        // Setup
        setupMethodTests();
        Customer initialCustomer = new Customer();
        initialCustomer.setDisplayName("Berkeley");
        initialCustomer.setSyncToken("12345");
        initialCustomer.setId("12345");
        when(dataService.findAll(any(Customer.class))).thenReturn(List.of(initialCustomer));

        Customer expectedCustomer = getExpectedCustomer();
        expectedCustomer.setSyncToken("12345");
        expectedCustomer.setId("12345");
        expectedCustomer.setSparse(true);

        // Run
        Customer returnedCustomer = quickBooksModule.updateCustomerFromSchool(school);

        // Verify
        assertEquals(expectedCustomer, returnedCustomer);
        verify(dataService).update(returnedCustomer);
    }

    @Test(expected = QuickBooksException.class)
    public void testUpdateCustomerFromSchool_dataServiceThrows() throws Exception {
        // Setup
        setupMethodTests();
        // Mocking "add" as the test case will interact with a dataService with
        // no existing customers and will add a new customer
        when(dataService.add(any())).thenThrow((FMSException.class));

        // Run
        quickBooksModule.updateCustomerFromSchool(school);
    }

    @Test
    public void testQueryInvoicesFromRegistration_happyPath() throws Exception {
        // Setup
        // dataservice returns a list of customers, one of which matches the school
        setupMethodTests();
        when(dataService.findAll(any(Customer.class))).thenReturn(List.of(customer));
        when(customer.getDisplayName()).thenReturn(SCHOOL_NAME);
        // construct matching invoices
        Invoice schoolFeeInvoice = new Invoice();
        schoolFeeInvoice.setId("school fee");
        Invoice delFeeInvoice = new Invoice();
        delFeeInvoice.setId("del fee");
        Invoice randomInvoice = new Invoice();
        randomInvoice.setId("random fee");
        when(dataService.findAll(any(Invoice.class)))
                .thenReturn(List.of(schoolFeeInvoice, delFeeInvoice, randomInvoice));
        mockStatic(Util.class);
        when(Util.checkInvoiceMatchesCustomer(any(Invoice.class), eq(customer))).thenReturn(true);
        when(Util.getInvoiceTypeFromInvoice(schoolFeeInvoice)).thenReturn(InvoiceType.BMUN_SCHOOL_FEE);
        when(Util.getInvoiceTypeFromInvoice(delFeeInvoice)).thenReturn(InvoiceType.BMUN_DELEGATE_FEE);
        when(Util.getInvoiceTypeFromInvoice(randomInvoice)).thenReturn(null);

        // Do
        Map<InvoiceType, Invoice> invoiceMap = quickBooksModule.queryInvoicesFromRegistration(registration);

        // Verify
        assertEquals(
                Map.of(InvoiceType.BMUN_DELEGATE_FEE, delFeeInvoice, InvoiceType.BMUN_SCHOOL_FEE, schoolFeeInvoice),
                invoiceMap
        );
    }

    @Test
    public void testQueryInvoicesFromRegistration_nullCustomer() throws Exception {
        // Setup
        // dataservice returns a list of customers, one of which matches the school
        setupMethodTests();
        when(dataService.findAll(any(Customer.class))).thenReturn(List.of(customer));
        when(customer.getContactName()).thenReturn(MEMO);

        // Do
        Map<InvoiceType, Invoice> invoiceMap = quickBooksModule.queryInvoicesFromRegistration(registration);

        // Verify
        assertEquals(Map.of(), invoiceMap);
    }

    @Test
    public void testQueryInvoicesFromRegistration_noInvoicesForCustomer() throws Exception {
        // Setup
        // dataservice returns a list of customers, one of which matches the school
        setupMethodTests();
        when(dataService.findAll(any(Customer.class))).thenReturn(List.of(customer));
        when(customer.getContactName()).thenReturn(SCHOOL_NAME);
        // construct matching invoices
        Invoice schoolFeeInvoice = new Invoice();
        schoolFeeInvoice.setId("school fee");
        Invoice delFeeInvoice = new Invoice();
        delFeeInvoice.setId("del fee");
        Invoice randomInvoice = new Invoice();
        randomInvoice.setId("random fee");
        when(dataService.findAll(any(Invoice.class)))
                .thenReturn(List.of(schoolFeeInvoice, delFeeInvoice, randomInvoice));
        mockStatic(Util.class);
        when(Util.checkInvoiceMatchesCustomer(any(Invoice.class), eq(customer))).thenReturn(false);

        // Do
        Map<InvoiceType, Invoice> invoiceMap = quickBooksModule.queryInvoicesFromRegistration(registration);

        // Verify
        assertEquals(Map.of(), invoiceMap);
    }

    private ReferenceType getExpectedCustomerRef_for_createInvoicesFromRegistration() {
        ReferenceType expectedCustomerRef = new ReferenceType();
        expectedCustomerRef.setValue(CUSTOMER_ID);
        expectedCustomerRef.setName(SCHOOL_NAME);
        return expectedCustomerRef;
    }

    private Date getExpectedTxnDate_for_createInvoicesFromRegistration() {
        return java.sql.Date.valueOf(registrationDate);
    }

    private boolean getAllowOnlineCreditCardPayment_for_createInvoicesFromRegistration() {
        return registration.getPaymentMethod() == PaymentMethod.CARD;
    }

    private ReferenceType getFCSchoolFeeItemRef() {
        ReferenceType itemRef = new ReferenceType();
        itemRef.setValue(FC_SCHOOL_FEE_ITEM_ID);
        itemRef.setName(ItemType.FC_SCHOOL_FEE.toString());
        return itemRef;
    }

    private ReferenceType getFCDelFeeItemRef() {
        ReferenceType itemRef = new ReferenceType();
        itemRef.setValue(FC_DEL_FEE_ITEM_ID);
        itemRef.setName(ItemType.FC_DELEGATE_FEE.toString());
        return itemRef;
    }

    private ReferenceType getCCFeeItemRef() {
        ReferenceType itemRef = new ReferenceType();
        itemRef.setValue(CC_FEE_ITEM_ID);
        itemRef.setName(ItemType.CREDIT_CARD_PROCESSING_FEE.toString());
        return itemRef;
    }

    private BigDecimal computeCCFeeAmount(BigDecimal subtotal) {
        BigDecimal fee = BigDecimal.valueOf(0.029).multiply(subtotal);
        fee = fee.add(BigDecimal.valueOf(0.25));
        fee = fee.divide(BigDecimal.valueOf(0.971), 2, RoundingMode.CEILING);
        return fee;
    }

    private Line getCCFeeLineItem(BigDecimal amount) {
        Line lineItem = new Line();
        lineItem.setDetailType(LineDetailTypeEnum.SALES_ITEM_LINE_DETAIL);
        lineItem.setAmount(amount);

        SalesItemLineDetail salesItemLineDetail = new SalesItemLineDetail();
        salesItemLineDetail.setItemRef(getCCFeeItemRef());
        salesItemLineDetail.setQty(BigDecimal.ONE);
        salesItemLineDetail.setUnitPrice(amount);
        lineItem.setSalesItemLineDetail(salesItemLineDetail);

        return lineItem;
    }

    private Invoice getExpectedSchoolFeeInvoice_for_createInvoicesFromRegistration() {
        Invoice invoice = new Invoice();
        // Fill in invoice details
        ReferenceType expectedCustomerRef = getExpectedCustomerRef_for_createInvoicesFromRegistration();
        invoice.setCustomerRef(expectedCustomerRef);
        invoice.setTxnDate(getExpectedTxnDate_for_createInvoicesFromRegistration());
        invoice.setDueDate(java.sql.Date.valueOf(RegistrationRound.FC_ROUND_1.getSchoolFeeDueDate()));
        invoice.setAllowOnlineCreditCardPayment(getAllowOnlineCreditCardPayment_for_createInvoicesFromRegistration());
        // Create school fee line item
        Line schoolFeeLineItem = new Line();
        schoolFeeLineItem.setDetailType(LineDetailTypeEnum.SALES_ITEM_LINE_DETAIL);
        schoolFeeLineItem.setAmount(InvoiceType.FC_SCHOOL_FEE.getUnitCost());

        SalesItemLineDetail schoolFeeSalesItemLineDetail = new SalesItemLineDetail();
        schoolFeeSalesItemLineDetail.setItemRef(getFCSchoolFeeItemRef());
        schoolFeeSalesItemLineDetail.setQty(BigDecimal.ONE);
        schoolFeeSalesItemLineDetail.setUnitPrice(InvoiceType.FC_SCHOOL_FEE.getUnitCost());
        schoolFeeLineItem.setSalesItemLineDetail(schoolFeeSalesItemLineDetail);
        // Get CC fee line item
        Line ccLineItem = getCCFeeLineItem(computeCCFeeAmount(InvoiceType.FC_SCHOOL_FEE.getUnitCost()));

        // Set lines
        invoice.setLine(List.of(schoolFeeLineItem, ccLineItem));

        return invoice;
    }

    private Invoice getExpectedDelegateFeeInvoice_for_createInvoicesFromRegistration() {
        Invoice invoice = new Invoice();
        // Fill in invoice details
        ReferenceType expectedCustomerRef = getExpectedCustomerRef_for_createInvoicesFromRegistration();
        invoice.setCustomerRef(expectedCustomerRef);
        invoice.setTxnDate(getExpectedTxnDate_for_createInvoicesFromRegistration());
        invoice.setDueDate(java.sql.Date.valueOf(RegistrationRound.FC_ROUND_1.getDelegateFeeDueDate()));
        invoice.setAllowOnlineCreditCardPayment(getAllowOnlineCreditCardPayment_for_createInvoicesFromRegistration());
        // Create del fee line item
        Line delFeeLineItem = new Line();
        delFeeLineItem.setDetailType(LineDetailTypeEnum.SALES_ITEM_LINE_DETAIL);
        BigDecimal subtotal = InvoiceType.FC_DELEGATE_FEE.getUnitCost().multiply(new BigDecimal(NUM_DELEGATES));
        delFeeLineItem.setAmount(subtotal);

        SalesItemLineDetail delFeeSalesItemLineDetail = new SalesItemLineDetail();
        delFeeSalesItemLineDetail.setItemRef(getFCDelFeeItemRef());
        delFeeSalesItemLineDetail.setQty(new BigDecimal(NUM_DELEGATES));
        delFeeSalesItemLineDetail.setUnitPrice(InvoiceType.FC_DELEGATE_FEE.getUnitCost());
        delFeeLineItem.setSalesItemLineDetail(delFeeSalesItemLineDetail);
        // Get CC fee line item
        Line ccLineItem = getCCFeeLineItem(computeCCFeeAmount(subtotal));

        // Set lines
        invoice.setLine(List.of(delFeeLineItem, ccLineItem));

        return invoice;
    }

    @Test
    public void testCreateInvoicesFromRegistration_happyPath() throws Exception {
        // Setup
        setupMethodTests();
        // Mock customer to be used in CustomerRef
        when(dataService.findAll(any(Customer.class))).thenReturn(List.of(customer));
        when(customer.getId()).thenReturn(CUSTOMER_ID);
        when(customer.getDisplayName()).thenReturn(SCHOOL_NAME);
        // Mock items to be added to invoices
        Item fcSchoolFeeItem = new Item();
        fcSchoolFeeItem.setId(FC_SCHOOL_FEE_ITEM_ID);
        fcSchoolFeeItem.setName(ItemType.FC_SCHOOL_FEE.toString());
        Item fcDelFeeItem = new Item();
        fcDelFeeItem.setId(FC_DEL_FEE_ITEM_ID);
        fcDelFeeItem.setName(ItemType.FC_DELEGATE_FEE.toString());
        Item ccFeeItem = new Item();
        ccFeeItem.setId(CC_FEE_ITEM_ID);
        ccFeeItem.setName(ItemType.CREDIT_CARD_PROCESSING_FEE.toString());
        when(dataService.findAll(any(Item.class))).thenReturn(List.of(fcSchoolFeeItem, fcDelFeeItem, ccFeeItem));


        // Run
        Map<InvoiceType, Invoice> invoiceMap = quickBooksModule.createInvoicesFromRegistration(registration);

        // Verify
        // Construct expected invoices
        Invoice expectedDelegateFeeInvoice = getExpectedDelegateFeeInvoice_for_createInvoicesFromRegistration();
        Invoice expectedSchoolFeeInvoice = getExpectedSchoolFeeInvoice_for_createInvoicesFromRegistration();

        assertTrue(invoiceMap.containsKey(InvoiceType.FC_SCHOOL_FEE));
        Invoice actualSchoolFeeInvoice = invoiceMap.get(InvoiceType.FC_SCHOOL_FEE);
        assertTrue(expectedSchoolFeeInvoice.equals(invoiceMap.get(InvoiceType.FC_SCHOOL_FEE)));
        assertEquals(expectedSchoolFeeInvoice.getLine(), actualSchoolFeeInvoice.getLine());

        assertTrue(invoiceMap.containsKey(InvoiceType.FC_DELEGATE_FEE));
        Invoice actualDelegateFeeInvoice = invoiceMap.get(InvoiceType.FC_DELEGATE_FEE);
        boolean delFeeInvoiceMatches = expectedDelegateFeeInvoice.equals(actualDelegateFeeInvoice);
        assertTrue(delFeeInvoiceMatches);
        assertEquals(expectedDelegateFeeInvoice.getLine(), actualDelegateFeeInvoice.getLine());
        Mockito.verify(dataService).add(expectedDelegateFeeInvoice);
        Mockito.verify(dataService).add(expectedSchoolFeeInvoice);
    }
}

