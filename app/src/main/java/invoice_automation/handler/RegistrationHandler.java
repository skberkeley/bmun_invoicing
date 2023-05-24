package invoice_automation.handler;

import invoice_automation.module.QuickBooksModule;
import invoice_automation.model.Registration;
import lombok.Builder;
import lombok.NonNull;

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

    public void handleRegistration(@NonNull Registration registration) {
        // TODO: IA-3
        return;
    }
}
