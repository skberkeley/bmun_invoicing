package invoice_automation.module;

import com.intuit.ipp.core.Context;
import com.intuit.ipp.data.Customer;
import com.intuit.ipp.data.EmailAddress;
import com.intuit.ipp.data.EmailStatusEnum;
import com.intuit.ipp.data.Invoice;
import com.intuit.ipp.data.MemoRef;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.services.DataService;
import com.intuit.ipp.util.Config;
import invoice_automation.QuickBooksException;
import invoice_automation.Util;
import invoice_automation.model.Address;
import invoice_automation.model.Conference;
import invoice_automation.model.InvoiceType;
import invoice_automation.model.PaymentMethod;
import invoice_automation.model.Registration;
import invoice_automation.model.School;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static invoice_automation.Consts.SANDBOX_BASE_URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
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
    private final Registration registration = Registration.builder()
            .school(school)
            .numDelegates(0)
            .registrationDate(LocalDate.now())
            .paymentMethod(PaymentMethod.CARD)
            .conference(Conference.BMUN)
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
}

