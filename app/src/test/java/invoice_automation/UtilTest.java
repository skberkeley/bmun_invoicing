package invoice_automation;

import com.intuit.ipp.data.Customer;
import com.intuit.ipp.data.Invoice;
import com.intuit.ipp.data.Line;
import com.intuit.ipp.data.LineDetailTypeEnum;
import com.intuit.ipp.data.ReferenceType;
import com.intuit.ipp.data.SalesItemLineDetail;
import invoice_automation.model.Address;
import invoice_automation.model.InvoiceType;
import invoice_automation.model.PaymentMethod;
import invoice_automation.model.Registration;
import invoice_automation.model.School;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.time.LocalDate;
import java.util.List;

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
    private final Registration.RegistrationBuilder registrationBuilder = Registration.builder()
            .school(school)
            .numDelegates(0)
            .registrationDate(LocalDate.now())
            .paymentMethod(PaymentMethod.CARD);

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
}
