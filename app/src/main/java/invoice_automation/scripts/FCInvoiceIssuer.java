package invoice_automation.scripts;

import com.google.gson.Gson;
import invoice_automation.OAuthKeys;
import invoice_automation.handler.RegistrationHandler;
import invoice_automation.model.Conference;
import invoice_automation.model.Registration;
import invoice_automation.module.GoogleSheetsModule;

import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public class FCInvoiceIssuer {
    private static final String O_AUTH_KEYS_PATH = "app/src/main/resources/intuit_oauth_keys.json";
    private static final String REGISTRATION_SHEET_ID = "1TU1ADMbf0wXmHjcpjnGfKVJqHvgEATlGKS0dHsJfenI";
    private static final String SHEET_NAME = "Registrations (for Treasurer, please don't edit)";
    public static void main(String[] args) throws IOException, GeneralSecurityException {
        Gson gson = new Gson();
        OAuthKeys oAuthKeys = gson.fromJson(new FileReader(O_AUTH_KEYS_PATH), OAuthKeys.class);

        List<Registration> registrations = GoogleSheetsModule.parseRegistrationsFromGoogleSheet(
                REGISTRATION_SHEET_ID,
                SHEET_NAME,
                Conference.FC
        );

        RegistrationHandler handler = new RegistrationHandler(
                oAuthKeys.getAccessToken(),
                oAuthKeys.getRealmId(),
                false
        );

        for (Registration reg: registrations) {
            handler.handleRegistration(reg);
            System.out.println("Issued invoices for " + reg.getSchool().getSchoolName());
        }
    }
}
