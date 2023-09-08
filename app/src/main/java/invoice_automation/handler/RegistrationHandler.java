package invoice_automation.handler;

import com.intuit.ipp.data.Customer;
import com.intuit.ipp.data.EmailStatusEnum;
import com.intuit.ipp.data.Invoice;
import invoice_automation.model.InvoiceType;
import invoice_automation.module.QuickBooksModule;
import invoice_automation.model.Registration;
import lombok.Builder;
import lombok.NonNull;

import java.util.Map;

/**
 * A class to handle new registrations
 *
 * @author skberkeley
 */
public class RegistrationHandler {
    /**
     * The QuickBooksModule used by this handler to make calls to the QuickBooks API
     */
    private final QuickBooksModule quickBooksModule;

    /**
     * Creates a new RegistrationHandler, instantiating the handler's QuickBooksModule in the process.
     * accessToken and realmId should be valid values as specified by the QuickBooksModule class.
     * @param accessToken The accessToken used to instantiate the QuickBooksModule
     * @param realmId The realmId used to instantiate the QuickBooksModule
     * @param useSandbox Whether the QuickBooksModule should make calls to the sandbox endpoint (for testing)
     */
    @Builder
    public RegistrationHandler(@NonNull String accessToken, @NonNull String realmId, boolean useSandbox) {
        this.quickBooksModule = new QuickBooksModule(accessToken, realmId, useSandbox);
    }

    /**
     * Carry out the actions needed to handle a registration:
     *  - Create or get the associated QuickBooks customer
     *  - Query whether any matching invoices exist
     *  - If invoices don't exist, create them
     *  - Send each invoice if it hasn't been sent
     * @param registration
     */
    public void handleRegistration(@NonNull Registration registration) {
        Customer customer = this.quickBooksModule.updateCustomerFromSchool(registration.getSchool());

        Map<InvoiceType, Invoice> invoices = this.quickBooksModule.queryInvoicesFromRegistration(registration);

        if (invoices.isEmpty()) {
            invoices = this.quickBooksModule.createInvoicesFromRegistration(registration);
        }

        for (Invoice invoice : invoices.values()) {
            if (invoice.getEmailStatus() != EmailStatusEnum.EMAIL_SENT) {
                this.quickBooksModule.sendInvoice(invoice, customer.getPrimaryEmailAddr().getAddress());
            }
        }
    }
}
