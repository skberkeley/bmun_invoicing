package invoice_automation.module;

import com.intuit.ipp.core.Context;
import com.intuit.ipp.data.*;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.services.DataService;
import com.intuit.ipp.util.Config;
import invoice_automation.QuickBooksException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static invoice_automation.Consts.SANDBOX_BASE_URL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({QuickBooksModule.class, Config.class})
public class QuickBooksModuleTest {
    private static final String ACCESS_TOKEN = "access token";
    private static final String REALM_ID = "realm id";
    private static final String EMAIL_ADDRESS = "oski@berkeley.edu";
    private static final String MEMO = "memo123";

    @Mock
    private DataService dataService;
    @Mock
    private List<Customer> customerList;
    @Mock
    private EmailAddress billEmail;
    private Invoice invoice;
    private QuickBooksModule quickBooksModule;
    private List<Invoice> invoices;

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
        invoices = new ArrayList<>();
        invoices.add(nonMatchingInvoice);
        invoices.add(matchingInvoice);
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
        invoices = new ArrayList<>();
        invoices.add(nonMatchingInvoice);
        invoices.add(nonMatchingInvoice1);
        when(dataService.findAll(any(Invoice.class))).thenReturn(invoices);

        // Run
        Invoice returnedInvoice = quickBooksModule.getInvoiceWithMatchingMemo(MEMO);

        // Verify
        assertNull(returnedInvoice);
    }
}

