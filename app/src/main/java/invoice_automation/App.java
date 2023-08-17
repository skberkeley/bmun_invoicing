/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package invoice_automation;

import com.google.gson.Gson;
import com.intuit.ipp.data.Invoice;
import invoice_automation.model.Address;
import invoice_automation.model.School;
import invoice_automation.module.QuickBooksModule;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

public class App {
    private static final String O_AUTH_KEYS_PATH = "app/src/main/resources/oauth_keys.json";

    public static void main(String[] args) throws FileNotFoundException {
        Gson gson = new Gson();
        OAuthKeys oAuthKeys = gson.fromJson(new FileReader(O_AUTH_KEYS_PATH), OAuthKeys.class);
        QuickBooksModule quickBooksModule = new QuickBooksModule(
                oAuthKeys.getAccessToken(),
                oAuthKeys.getRealmId(),
                true
        );

        School school = School.builder()
                .schoolName("Berkeley")
                .email("oski@berkeley.edu")
                .quickBooksId("Id")
                .address(new Address("110 Sproul Hall",
                        "",
                        "Berkeley",
                        "CA",
                        "US",
                        "94720"))
                .phoneNumbers(new ArrayList<>())
                .build();
        quickBooksModule.updateCustomerFromSchool(school);
        Invoice invoice = quickBooksModule.getInvoiceWithMatchingMemo("BMUN Test");
        quickBooksModule.sendInvoice(invoice);
    }
}
