package tr.com.huseyinaydin.testing.payment.stripe;

import tr.com.huseyinaydin.testing.payment.CardPaymentCharge;
import tr.com.huseyinaydin.testing.payment.CardPaymentCharger;
import tr.com.huseyinaydin.testing.payment.Currency;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@ConditionalOnProperty(
        value = "stripe.enabled",
        havingValue = "false"
) //Bu anotasyon, stripe.enabled adlı yapılandırma özelliği "false" ise ilgili sınıfın veya bileşenin oluşturulmasını sağlar.
public class MockStripeService implements CardPaymentCharger {

    @Override
    public CardPaymentCharge chargeCard(String cardSource,
                                        BigDecimal amount,
                                        Currency currency,
                                        String description) {
        return new CardPaymentCharge(true);
    }
}