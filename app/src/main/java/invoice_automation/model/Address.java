package invoice_automation.model;

import lombok.NonNull;
import lombok.Value;

/**
 * Class to model addresses inputted by schools during registration
 *
 * @author skberkeley
 */
@Value
public class Address {
    /**
     * First line of address
     */
    @NonNull String line1;
    /**
     * Second line of address, empty string if blank
     */
    @NonNull String line2;
    /**
     * City of address
     */
    @NonNull String city;
    /**
     * Country subdivision code of address
     * In the USA, corresponds to state abbreviation, e.g. CA
     */
    @NonNull String countrySubdivisionCode;
    /**
     * Country of address
     */
    @NonNull String country;
    /**
     * Zip code of address
     */
    @NonNull String zipCode;
}
