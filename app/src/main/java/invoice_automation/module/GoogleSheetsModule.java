package invoice_automation.module;

import invoice_automation.model.Registration;
import lombok.NonNull;

import java.util.List;

// Class used to read Google Sheets from which we get invoicing info
public class GoogleSheetsModule {
    List<Registration> parseRegistrationsFromGoogleSheet(@NonNull String spreadsheetId) {
        // TODO: IA-10
        return null;
    }
}
