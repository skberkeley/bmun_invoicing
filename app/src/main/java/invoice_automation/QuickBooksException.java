package invoice_automation;

/**
 * A wrapper class for exceptions thrown while doing QuickBooks-related things
 *
 * @author skberkeley
 */
public class QuickBooksException extends RuntimeException {
    /**
     * Create a new QuickBooks exception using the passed message and cause
     * @param message A string describing what caused the issue
     * @param cause The exception thrown by some QuickBooks API
     */
    public QuickBooksException(String message, Throwable cause) {
        super(message, cause);
    }
}
