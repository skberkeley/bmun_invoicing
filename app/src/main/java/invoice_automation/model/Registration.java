package invoice_automation.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.time.LocalDate;

/**
 * Class to model school's registration for a particular conference
 *
 * @author skberkeley
 */
@Value
@Builder
public class Registration {
    @NonNull School school;
    int numDelegates;
    @NonNull Conference conference;
    /**
     * Date registration was made
     * Used to determine which round this registration falls into
     */
    @NonNull LocalDate registrationDate;
    @NonNull PaymentMethod paymentMethod;
}
