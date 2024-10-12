package tr.com.huseyinaydin.testing.payment.stripe;

import tr.com.huseyinaydin.testing.payment.CardPaymentCharge;
import tr.com.huseyinaydin.testing.payment.CardPaymentCharger;
import tr.com.huseyinaydin.testing.payment.Currency;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.net.RequestOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@ConditionalOnProperty(
        value = "stripe.enabled",
        havingValue = "true"
) //Bu anotasyon, stripe.enabled adlı yapılandırma özelliği "false" ise ilgili sınıfın veya bileşenin oluşturulmasını sağlar.
public class StripeService implements CardPaymentCharger {

    private final StripeApi stripeApi;

    private final static RequestOptions requestOptions = RequestOptions.builder()
            .setApiKey("sk_test_5eV38HqOkmWDzrjHT1znp5er")
            .build();

    @Autowired
    public StripeService(StripeApi stripeApi) {
        this.stripeApi = stripeApi;
    }

    @Override
    public CardPaymentCharge chargeCard(String cardSource,
                                        BigDecimal amount,
                                        Currency currency,
                                        String description) {
        Map<String, Object> params = new HashMap<>();
        params.put("amount", amount);
        params.put("currency", currency);
        params.put("source", cardSource);
        params.put("description", description);

        try {
            Charge charge = stripeApi.create(params, requestOptions);
            return new CardPaymentCharge(charge.getPaid());
        } catch (StripeException e) {
            throw new IllegalStateException("Stripe şarjı yapılamadı.", e);
        }
    }
}