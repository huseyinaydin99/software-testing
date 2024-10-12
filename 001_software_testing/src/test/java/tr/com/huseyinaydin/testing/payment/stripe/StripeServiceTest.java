package tr.com.huseyinaydin.testing.payment.stripe;

import tr.com.huseyinaydin.testing.payment.CardPaymentCharge;
import tr.com.huseyinaydin.testing.payment.Currency;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.net.RequestOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

class StripeServiceTest {

    private StripeService underTest;

    @Mock
    private StripeApi stripeApi;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        underTest = new StripeService(stripeApi);
    }

    @Test
    void itShouldChargeCard() throws StripeException {
        // Given - test için ön hazırlık
        String cardSource = "0x1x2x";
        BigDecimal amount = new BigDecimal("100.00");
        Currency currency = Currency.EUR;
        String description = "Zekat";

        // Şarj işlemi.
        Charge charge = new Charge();
        charge.setPaid(true);
        //Bu satır, stripeApi.create metodunun herhangi bir Map ve herhangi bir nesne ile çağrıldığında, charge nesnesini döndürmesini sağlar.
        //anyMap() herhangi bir map objesi oluşturmaya yarar. Yani sahte ve rastgele.
        given(stripeApi.create(anyMap(), any())).willReturn(charge); //beklenen referans charge'ye şit mi? Eşitse testi geçer.

        // When - kartı şarj et.
        CardPaymentCharge cardPaymentCharge = underTest.chargeCard(cardSource, amount, currency, description);

        // Then
        ArgumentCaptor<Map<String, Object>> mapArgumentCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<RequestOptions> optionsArgumentCaptor = ArgumentCaptor.forClass(RequestOptions.class);

        // yakala map'i ve request options'u.
        then(stripeApi).should().create(mapArgumentCaptor.capture(), optionsArgumentCaptor.capture());

        // yakalanan map referansını al.
        Map<String, Object> requestMap = mapArgumentCaptor.getValue();

        //requestMap'in anahtar seti beklenen boyutu 4 olmak zorunda.
        assertThat(requestMap.keySet()).hasSize(4);

        assertThat(requestMap.get("amount")).isEqualTo(amount);
        assertThat(requestMap.get("currency")).isEqualTo(currency);
        assertThat(requestMap.get("source")).isEqualTo(cardSource);
        assertThat(requestMap.get("description")).isEqualTo(description);

        // yakalanan request options referansını al.
        RequestOptions options = optionsArgumentCaptor.getValue();

        //beklenen RequestOptions nesnesi null olmamalı.
        assertThat(options).isNotNull();

        // cardPaymentCharge null olmamalı.
        assertThat(cardPaymentCharge).isNotNull();
        // cardPaymentCharge kart borçlanması true olmalı.
        assertThat(cardPaymentCharge.isCardDebited()).isTrue();

        //şartlar böyleyse testi geçer.
    }

    @Test
    void itShouldNotChargeWhenApiThrowsException() throws StripeException {
        // Given - ödeme öncesi ön hazırlık
        String cardSource = "1x2x3x";
        BigDecimal amount = new BigDecimal("15.00");
        Currency currency = Currency.USD;
        String description = "Fitre/Sadaka";

        // StripeApi çağrısında bulunulduğunda StripeException istisnası fırlatılsın.
        StripeException stripeException = mock(StripeException.class); // istisna nesnesi hazırlığı.
        doThrow(stripeException).when(stripeApi).create(anyMap(), any()); // stripeApi.create metodu çağrıldığında, herhangi bir Map ve herhangi bir nesne ile birlikte stripeException istisnasını fırlatmasını sağlar.

        // When
        // Then
        assertThatThrownBy(() -> underTest.chargeCard(cardSource, amount, currency, description))
                .isInstanceOf(IllegalStateException.class)
                .hasRootCause(stripeException) //chargeCard metodu çağrıldığında beklenen hata stripeException ise.
                .hasMessageContaining("Stripe şarjı yapılamadı."); //beklnen hata mesajı "Stripe şarjı yapılamadı." ise.
    }
}