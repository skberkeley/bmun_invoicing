package invoice_automation.module;

import com.intuit.ipp.core.Context;
import com.intuit.ipp.data.Customer;
import com.intuit.ipp.data.EmailAddress;
import com.intuit.ipp.data.EmailStatusEnum;
import com.intuit.ipp.data.Invoice;
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

import java.util.List;

import static invoice_automation.Consts.SANDBOX_BASE_URL;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.powermock.api.mockito.PowerMockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({QuickBooksModule.class, Config.class})
public class QuickBooksModuleTest {
    private static final String ACCESS_TOKEN = "access token";
    private static final String REALM_ID = "realm id";

    @Mock
    private DataService dataService;
    @Mock
    private List<Customer> customerList;
    @Mock
    private EmailAddress billEmail;
    private Invoice invoice;
    private QuickBooksModule quickBooksModule;

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
    public void sendInvoice_happyPath() throws Exception {
        // Setup
        setupMethodTests();
        setupInvoiceTests();

        // Run
        quickBooksModule.sendInvoice(invoice);

        // Verify
        assertEquals(EmailStatusEnum.EMAIL_SENT, invoice.getEmailStatus());
    }

    @Test(expected = QuickBooksException.class)
    public void sendInvoice_dataServiceThrows() throws Exception {
        // Setup
        setupMethodTests();
        setupInvoiceTests();
        when(dataService.sendEmail(any(), any())).thenThrow(FMSException.class);

        // Run
        quickBooksModule.sendInvoice(invoice);
    }
}
