package invoice_automation.module;

import com.intuit.ipp.core.Context;
import com.intuit.ipp.core.ServiceType;
import com.intuit.ipp.data.*;
import com.intuit.ipp.data.Customer;
import com.intuit.ipp.data.EmailStatusEnum;
import com.intuit.ipp.data.Invoice;
import com.intuit.ipp.data.MemoRef;
import com.intuit.ipp.exception.FMSException;
import com.intuit.ipp.security.OAuth2Authorizer;
import com.intuit.ipp.services.DataService;
import com.intuit.ipp.util.Config;
import invoice_automation.QuickBooksException;
import invoice_automation.model.InvoiceType;
import invoice_automation.model.Registration;
import invoice_automation.model.School;
import invoice_automation.utils.QuickBooksUtils;
import lombok.Builder;
import lombok.NonNull;

import java.util.List;
import java.util.Map;

import static invoice_automation.Consts.SANDBOX_BASE_URL;

/**
 * Abstraction barrier for calling QuickBooks APIs
 *
 * @author skberkeley
 */
public class QuickBooksModule {
    /**
     * The DataService object used to make QuickBooks API calls
     */
    private final DataService dataService;

    /**
     * Creates a new QuickBooksModule object, instantiating a new DataService object in the process.
     * accessToken must be a valid value generated by some OAuth2 process.
     * realmId must be a valid value for some QuickBooks app.
     * @param accessToken The OAuth2 accessToken used to authenticate while instantiating the new DataService object
     * @param realmId The realmId for the QuickBooks app being used
     * @param useSandbox Whether API calls should be made to the sandbox endpoint (for testing)
     */
    @Builder
    public QuickBooksModule(@NonNull String accessToken, @NonNull String realmId, boolean useSandbox) {
        if (useSandbox) {
            Config.setProperty(Config.BASE_URL_QBO, SANDBOX_BASE_URL);
        }

        // Create OAuth2 object
        OAuth2Authorizer oAuth2Authorizer = new OAuth2Authorizer(accessToken);

        // Create QuickBooks context
        Context context;
        try {
            context = new Context(oAuth2Authorizer, ServiceType.QBO, realmId);
        } catch (FMSException e) {
            throw new QuickBooksException("Exception creating context for QuickBooksModule", e);
        }

        // Create QuickBooks DataService object
        this.dataService = new DataService(context);
    }

    // Customer methods

    /**
     * Get a list of all existing customers through the QuickBooks API
     * @return A list of all existing customers
     */
    public List<Customer> getAllCustomers() {
        Customer customer = new Customer();
        try {
            return this.dataService.findAll(customer);
        } catch (FMSException e) {
            throw new QuickBooksException("Exception getting all customers", e);
        }
    }

    /**
     * Updates the corresponding QuickBooks Customer and returns a copy of that object. If no corresponding Customer
     * exists, then a new one is created.
     * @return A copy of the Customer updated or created
     */
    public Customer updateCustomerFromSchool(@NonNull School school) {
        boolean addNew = true;
        // Get customer associated with the school, if it exists
        Customer schoolCustomer = new Customer();
        List<Customer> customers = getAllCustomers();
        for (Customer c: customers) {
            if (c.getDisplayName().equals(school.getSchoolName())) {
                schoolCustomer = c;
                addNew = false;
                break;
            }
        }
        // Convert the school object to a customer object
        Customer newCustomer = QuickBooksUtils.getCustomerFromSchool(school);
        if (!addNew) {
            newCustomer.setId(schoolCustomer.getId());
            newCustomer.setSyncToken(schoolCustomer.getSyncToken());
        }
        // Add newCustomer or update the existing customer
        try {
            if (addNew) {
                dataService.add(newCustomer);
            } else {
                dataService.update(newCustomer);
            }
        } catch (FMSException e) {
            throw new QuickBooksException("Exception updating customer", e);
        }
        return newCustomer;
    }

    // Invoice methods

    /**
     * Get all the invoices corresponding to the passed registration through the QuickBooks API. If any required
     * invoices don't exist yet, create them, also through the QuickBooks API.
     * @param registration The Registration to match invoices against
     * @return A map from InvoiceType to the corresponding Invoice. May be empty if no matching invoices exist
     */
    public Map<InvoiceType, Invoice> getOrCreateInvoicesFromRegistration(@NonNull Registration registration) {
        // TODO: IA-8
        return null;
    }

    /**
     * Sends the passed invoice, using the email of the associated Customer.
     * @param invoice The Invoice to send
     */
    public void sendInvoice(@NonNull Invoice invoice) {
        try {
            this.dataService.sendEmail(invoice, invoice.getBillEmail().getAddress());
        } catch (FMSException e) {
            throw new QuickBooksException("Exception sending invoice", e);
        }
        invoice.setEmailStatus(EmailStatusEnum.EMAIL_SENT);
    }

    /**
     * Finds and returns the first invoice with matching memo
     * @param memoToMatch - A string containing the memo value to match against
     * @return invoice - The first invoice with a matching memo value
     */
    public Invoice getInvoiceWithMatchingMemo(String memoToMatch) {
        List<Invoice> invoices;
        try {
            invoices = dataService.findAll(new Invoice());
        } catch (FMSException e) {
            throw new QuickBooksException("Error fetching all invoices", e);
        }

        for (Invoice inv : invoices) {
            MemoRef memo = inv.getCustomerMemo();
            if (memo != null && memo.getValue().equals(memoToMatch)) {
                return inv;
            }
        }
        return null;
    }
}
