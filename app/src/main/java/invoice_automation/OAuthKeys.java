package invoice_automation;

import lombok.Value;

/**
 * A class used to hold OAuthKeys
 *
 * @author skberkeley
 */
@Value
public class OAuthKeys {
    String realmId;
    String accessToken;
}
