package invoice_automation.module;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import invoice_automation.QuickBooksException;
import invoice_automation.model.Address;
import invoice_automation.model.Conference;
import invoice_automation.model.PaymentMethod;
import invoice_automation.model.Registration;
import invoice_automation.model.School;
import lombok.NonNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

// Class used to read Google Sheets from which we get invoicing info
public class GoogleSheetsModule {
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String CREDENTIALS_FILE_PATH = "app/src/main/resources/google_sheets_credentials.json";
    private static final String TOKENS_DIRECTORY_PATH = "app/src/main/resources/tokens";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = List.of(SheetsScopes.SPREADSHEETS);

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets
        File credentials_file = new File(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new FileReader(credentials_file));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow
                .Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    /**
     * Parse a list of registrations to issue invoices for from a Google sheet
     * @param spreadsheetId - The id of the sheet to parse registrations from
     * @param conference - The conference for which registrations are being parsed
     * @return - The parsed registrations
     */
    public static List<Registration> parseRegistrationsFromGoogleSheet(
            @NonNull String spreadsheetId,
            @NonNull String sheetName,
            @NonNull Conference conference
    ) throws GeneralSecurityException, IOException {
        // Build a new authorized API client service.
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final String range = sheetName + "!A2:M";
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName("Invoicing")
                .build();
        ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            throw new QuickBooksException("Failed to find registrations to parse", null);
        }

        return values.stream()
                .map(row -> parseRegistration(row, conference))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Parses a Registration from the passed row, if appropriate. Assumes the row's elements are:
     * schoolName, email, phoneNumber, addressLine1, addressLine2, city, state, country, zipCode, numDelegates, registrationDate, paymentMethod, invoiceSent
     * If invoiceSent is "TRUE", returns null
     * @param row
     * @return
     */
    private static Registration parseRegistration (@NonNull List<Object> row, @NonNull Conference conference) {
        if (row.get(12).equals("TRUE")) {
            return null;
        }

        String schoolName = ((String) row.get(0)).strip();
        String email = ((String) row.get(1)).strip();
        String phoneNumber = ((String) row.get(2)).strip();
        String addressLine1 = ((String) row.get(3)).strip();
        String addressLine2 = ((String) row.get(4)).strip();
        String city = ((String) row.get(5)).strip();
        String state = ((String) row.get(6)).strip();
        String country = ((String) row.get(7)).strip();
        String zipCode = ((String) row.get(8)).strip();
        String numDelegates = ((String) row.get(9)).strip();
        String registrationDate = ((String) row.get(10)).strip();
        String paymentMethod = ((String) row.get(11)).strip();

        Address address = new Address(addressLine1, addressLine2, city, state, country, zipCode);
        School school = School.builder()
                .schoolName(schoolName)
                .email(email)
                .phoneNumbers(List.of(phoneNumber))
                .address(address)
                .build();
        PaymentMethod paymentMethod1 = paymentMethod.equals("Card") ? PaymentMethod.CARD : PaymentMethod.CHECK;

        return Registration.builder()
                .school(school)
                .numDelegates(Integer.parseInt(numDelegates))
                .conference(conference)
                .registrationDate(LocalDate.parse(registrationDate, FORMATTER))
                .paymentMethod(paymentMethod1)
                .build();
    }
}
