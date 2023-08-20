package invoice_automation.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Class to model schools registering for BMUN
 *
 * @author skberkeley
 */
@Value
@Builder
public class School {
    @NonNull String schoolName;
    /**
     * Email of school's primary contact
     */
    @NonNull String email;
    /**
     * List containing school's primary phone number and optional secondary phone number
     */
    @NonNull List<String> phoneNumbers;
    /**
     * School's mailing address
     */
    @NonNull Address address;
}
