package invoice_automation.module;

import com.intuit.ipp.data.Customer;
import com.intuit.ipp.data.EmailAddress;
import com.intuit.ipp.data.PhysicalAddress;
import com.intuit.ipp.data.TelephoneNumber;
import com.intuit.ipp.util.Config;
import invoice_automation.model.Address;
import invoice_automation.model.School;
import invoice_automation.utils.QuickBooksUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest({QuickBooksUtils.class, Config.class})
public class QuickBooksUtilsTest {

    private final List<String> phoneNumbers = List.of("1234567890", "0987654321");
    private final Address address = new Address("110 Sproul Hall", "", "Berkeley", "CA", "US", "94720");
    private final School school = School.builder()
                .schoolName("Berkeley")
                .quickBooksId("id")
                .address(address)
                .email("oski@berkeley.edu")
                .phoneNumbers(phoneNumbers)
                .build();
    @Test
    public void getCustomerFromSchool_happyPath() {
        // Setup
        Customer expectedCustomer = new Customer();
        expectedCustomer.setCompanyName("Berkeley");
        expectedCustomer.setDisplayName("Berkeley");
        expectedCustomer.setPrimaryEmailAddr(new EmailAddress());
        expectedCustomer.getPrimaryEmailAddr().setAddress("oski@berkeley.edu");
        PhysicalAddress schoolAddress = new PhysicalAddress();
        schoolAddress.setLine1("110 Sproul Hall");
        schoolAddress.setLine2("");
        schoolAddress.setCity("Berkeley");
        schoolAddress.setCountrySubDivisionCode("CA");
        schoolAddress.setCountry("US");
        schoolAddress.setPostalCode("94720");
        expectedCustomer.setBillAddr(schoolAddress);
        expectedCustomer.setShipAddr(schoolAddress);
        expectedCustomer.setPrimaryPhone(new TelephoneNumber());
        expectedCustomer.getPrimaryPhone().setFreeFormNumber(phoneNumbers.get(0));
        expectedCustomer.setAlternatePhone(new TelephoneNumber());
        expectedCustomer.getAlternatePhone().setFreeFormNumber(phoneNumbers.get(1));

        // Run
        Customer customer = QuickBooksUtils.getCustomerFromSchool(school);

        // Verify
        assertEquals(expectedCustomer, customer);
    }

    @Test
    public void getPhysicalAddressFromAddress_happyPath() {
        // Setup
        PhysicalAddress expectedPhysicalAddress = new PhysicalAddress();
        expectedPhysicalAddress.setLine1("110 Sproul Hall");
        expectedPhysicalAddress.setLine2("");
        expectedPhysicalAddress.setCity("Berkeley");
        expectedPhysicalAddress.setCountrySubDivisionCode("CA");
        expectedPhysicalAddress.setCountry("US");
        expectedPhysicalAddress.setPostalCode("94720");

        // Run
        PhysicalAddress physicalAddress = QuickBooksUtils.getPhysicalAddressFromAddress(address);

        // Verify
        assertEquals(expectedPhysicalAddress, physicalAddress);
    }

}
