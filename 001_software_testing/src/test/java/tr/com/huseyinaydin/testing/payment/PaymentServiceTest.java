package tr.com.huseyinaydin.testing.payment;

import tr.com.huseyinaydin.testing.customer.Customer;
import tr.com.huseyinaydin.testing.customer.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

class PaymentServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private CardPaymentCharger cardPaymentCharger;

    private PaymentService underTest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        underTest = new PaymentService(customerRepository, paymentRepository, cardPaymentCharger);
    }

    @Test
    void itShouldChargeCardSuccessfully() {
        // Given - bizim beklediğimiz değer veya referans.
        UUID customerId = UUID.randomUUID();

        // ... ilgili müşteri var mı yok mu?
        given(customerRepository.findById(customerId)).willReturn(Optional.of(mock(Customer.class)));

        // ... Ödeme isteği
        PaymentRequest paymentRequest = new PaymentRequest(
                new Payment(
                        null,
                        null,
                        new BigDecimal("101.02"),
                        Currency.TL,
                        "card123xx",
                        "Bağış"
                )
        );

        // ... kart şarjı başarılı mı?
        given(cardPaymentCharger.chargeCard(
                paymentRequest.getPayment().getSource(),
                paymentRequest.getPayment().getAmount(),
                paymentRequest.getPayment().getCurrency(),
                paymentRequest.getPayment().getDescription()
        )).willReturn(new CardPaymentCharge(true));

        // When
        underTest.chargeCard(customerId, paymentRequest);

        // Then - arguman yakalayıcı nesnesi.
        ArgumentCaptor<Payment> paymentArgumentCaptor =
                ArgumentCaptor.forClass(Payment.class);

        //paymentRepository'nin save metodu arguman yakalayıcı tarafından save isimli metoda Payment sınıfı ile parametre olarak girilip çalıştırıldı mı diye bakıyor.
        then(paymentRepository).should().save(paymentArgumentCaptor.capture());

        Payment paymentArgumentCaptorValue = paymentArgumentCaptor.getValue(); //yakalanan değer veya refrerans
        assertThat(paymentArgumentCaptorValue)
                .isEqualToIgnoringGivenFields(
                        paymentRequest.getPayment(),
                        "customerId");

        assertThat(paymentArgumentCaptorValue.getCustomerId()).isEqualTo(customerId);
    }

    @Test
    void itShouldThrowWhenCardIsNotCharged() {
        // Given
        UUID customerId = UUID.randomUUID();

        // ... Optional of sahte müşteri objesi üretiyor mu?
        given(customerRepository.findById(customerId)).willReturn(Optional.of(mock(Customer.class)));

        // ... ödeme nesnesini sarmalayan ödeme isteği nesnesi.
        PaymentRequest paymentRequest = new PaymentRequest(
                new Payment(
                        null,
                        null,
                        new BigDecimal("111.91"),
                        Currency.TL,
                        "card123xx",
                        "Harçlık"
                )
        );

        // ... kart şarjı yapılıyor mu? Beklenen referans CardPaymentCharge nesnesi false değer tutacak.
        given(cardPaymentCharger.chargeCard(
                paymentRequest.getPayment().getSource(),
                paymentRequest.getPayment().getAmount(),
                paymentRequest.getPayment().getCurrency(),
                paymentRequest.getPayment().getDescription()
        )).willReturn(new CardPaymentCharge(false));

        // When
        // Then
        assertThatThrownBy(() -> underTest.chargeCard(customerId, paymentRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Kart şarjı yapılamadı " + customerId);

        // ... bu kodda paymentRepository'nin herhangi bir metodunun çalışmamasını bekliyor. Yani paymentRepository'nin herhangi bir metodu çalışmadıysa testi geçer.
        then(paymentRepository).shouldHaveNoInteractions();
    }

    @Test
    void itShouldNotChargeCardAndThrowWhenCurrencyNotSupported() {
        // Given - oluşturulan benzeriz Id hash'i.
        UUID customerId = UUID.randomUUID();

        // ... sahte Customer objesi dönüyor mu?
        given(customerRepository.findById(customerId)).willReturn(Optional.of(mock(Customer.class)));

        // ... Türk lirası cnm Euro ile ciklet al. (-:
        Currency currency = Currency.TL;

        // ... ödeme isteğini sarmalayan ödeme isteği nesnesi.
        PaymentRequest paymentRequest = new PaymentRequest(
                new Payment(
                        null,
                        null,
                        new BigDecimal("444.77"),
                        currency,
                        "card123xx",
                        "Yemek parası."
                )
        );

        // When - chargeCard metodu IllegalStateException istisnası fırlatıyor mu? Beklenilen hata mesajını içeriyor mu?
        assertThatThrownBy(() -> underTest.chargeCard(customerId, paymentRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("[" + currency + "] para birimi desteklenmiyor.");

        // Then

        //cardPaymentCharger nesnesinin herhangi bir metodu çalıştırılmış mı?
        then(cardPaymentCharger).shouldHaveNoInteractions();

        //paymentRepository nesnesinin herhangi bir metodu çalıştırılmış mı?
        then(paymentRepository).shouldHaveNoInteractions();
    }

    @Test
    void itShouldNotChargeAndThrowWhenCustomerNotFound() {
        // Given - olmayan bir Id farz edelim.
        UUID customerId = UUID.randomUUID();

        // Veritabanında böyle bir müşteri yok.
        given(customerRepository.findById(customerId)).willReturn(Optional.empty());

        // When
        // Then
        assertThatThrownBy(() -> underTest.chargeCard(customerId, new PaymentRequest(new Payment())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("[" + customerId + "] Id'li müşteri bulunamadı.");

        // ... PaymentCharger not PaymentRepository için metot çağrısının olmaması bekleniyor. Eğer çağrı yoksa testi geçer.
        then(cardPaymentCharger).shouldHaveNoInteractions();
        then(paymentRepository).shouldHaveNoInteractions();
    }
}