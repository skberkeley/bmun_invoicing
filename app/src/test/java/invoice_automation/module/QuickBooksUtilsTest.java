package invoice_automation.module;

import com.intuit.ipp.data.Customer;
import com.intuit.ipp.data.PhysicalAddress;
import com.intuit.ipp.util.Config;
import invoice_automation.model.Address;
import invoice_automation.model.School;
import invoice_automation.utils.QuickBooksUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(PowerMockRunner.class)
@PrepareForTest({QuickBooksUtils.class, Config.class})
public class QuickBooksUtilsTest {

    @Test
    public void getCustomerFromSchool_happyPath() {
        // Setup
        List<String> phoneNumbers = new ArrayList<>();
        Address address = new Address("", "", "", "", "", "");
        phoneNumbers.add("1234567890");
        phoneNumbers.add("0987654321");
        School school = School.builder()
                .schoolName("Berkeley")
                .quickBooksId("id")
                .address(address)
                .email("oski@berkeley.edu")
                .phoneNumbers(phoneNumbers)
                .build();

        // Run
        Customer customer = QuickBooksUtils.getCustomerFromSchool(school);

        // Verify
        assertEquals(school.getSchoolName(), customer.getDisplayName());
        assertEquals(school.getSchoolName(), customer.getCompanyName());
        assertEquals(school.getEmail(), customer.getPrimaryEmailAddr().getAddress());
        assertEquals(school.getPhoneNumbers().get(0), customer.getPrimaryPhone().getFreeFormNumber());
        assertEquals(school.getPhoneNumbers().get(1), customer.getAlternatePhone().getFreeFormNumber());
        assertEquals(QuickBooksUtils.getPhysicalAddressFromAddress(address), customer.getBillAddr());
        assertEquals(QuickBooksUtils.getPhysicalAddressFromAddress(address), customer.getShipAddr());
    }

    @Test
    public void getPhysicalAddressFromAddress_happyPath() {
        // Setup
        Address address = new Address("110 Sproul Hall", "", "Berkeley", "US-CA", "US", "94720");

        // Run
        PhysicalAddress physicalAddress = QuickBooksUtils.getPhysicalAddressFromAddress(address);

        // Verify
        assertEquals(address.getLine1(), physicalAddress.getLine1());
        assertEquals(address.getLine2(), physicalAddress.getLine2());
        assertEquals(address.getCity(), physicalAddress.getCity());
        assertEquals(address.getCountrySubdivisionCode(), physicalAddress.getCountrySubDivisionCode());
        assertEquals(address.getZipCode(), physicalAddress.getPostalCode());
    }

}
