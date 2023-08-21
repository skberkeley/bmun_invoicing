package invoice_automation.utils;

import com.intuit.ipp.data.Customer;
import com.intuit.ipp.data.EmailAddress;
import com.intuit.ipp.data.PhysicalAddress;
import com.intuit.ipp.data.TelephoneNumber;
import invoice_automation.model.Address;
import invoice_automation.model.School;

import java.util.List;

public class QuickBooksUtils {
    /**
     * Basic utility functions with QuickBooks
     * */


    /**
     * Derives a new customer object from a school object
     * @param school the school object whose values are used in the creation of a new customer
     * @return a new customer created from the school
     * */
    public static Customer getCustomerFromSchool(School school) {
        Customer newCustomer = new Customer();
        newCustomer.setCompanyName(school.getSchoolName());
        newCustomer.setDisplayName(school.getSchoolName());
        newCustomer.setPrimaryEmailAddr(new EmailAddress());
        newCustomer.getPrimaryEmailAddr().setAddress(school.getEmail());
        PhysicalAddress schoolAddress = QuickBooksUtils.getPhysicalAddressFromAddress(school.getAddress());
        newCustomer.setBillAddr(schoolAddress);
        newCustomer.setShipAddr(schoolAddress);
        List<String> phoneNumbers = school.getPhoneNumbers();
        if (!phoneNumbers.isEmpty()) {
            if (school.getPhoneNumbers().size() > 0) {
                newCustomer.setPrimaryPhone(new TelephoneNumber());
                newCustomer.getPrimaryPhone().setFreeFormNumber(phoneNumbers.get(0));
            }
            if (school.getPhoneNumbers().size() > 1) {
                newCustomer.setAlternatePhone(new TelephoneNumber());
                newCustomer.getAlternatePhone().setFreeFormNumber(phoneNumbers.get(1));
            }
        }
        return newCustomer;
    }

    /**
     * Creates a physical address object from an address object
     * @param address an address model
     * @return a physicalAddress object which contains the values of the passed in address
     */
    public static PhysicalAddress getPhysicalAddressFromAddress(Address address) {
        PhysicalAddress physicalAddress = new PhysicalAddress();
        physicalAddress.setLine1(address.getLine1());
        physicalAddress.setLine2(address.getLine2());
        physicalAddress.setCity(address.getCity());
        physicalAddress.setCountrySubDivisionCode(address.getCountrySubdivisionCode());
        physicalAddress.setCountry(address.getCountry());
        physicalAddress.setPostalCode(address.getZipCode());
        return physicalAddress;
    }
}
