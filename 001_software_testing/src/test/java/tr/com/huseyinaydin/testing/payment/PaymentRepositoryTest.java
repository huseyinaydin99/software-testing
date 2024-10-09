package tr.com.huseyinaydin.testing.payment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(
        properties = {
                "spring.jpa.properties.javax.persistence.validation.mode=none"
        }
)
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository underTest;

    @Test
    void itShouldInsertPayment() {
        // Given - ön hazırlık
        long paymentId = 1L;
        Payment payment = new Payment(
                null,
                UUID.randomUUID(),
                new BigDecimal("10.00"),
                Currency.TL, 
				"card123",
                "Kurs parası yapıştırdım hocam (:");
        // When - ödeme kaydediliyor.
        underTest.save(payment);

        // Then - test edilme kısmı. Veritabanında var mı? Id'ye göre bul.
        Optional<Payment> paymentOptional = underTest.findById(paymentId);
        assertThat(paymentOptional)
                .isPresent() // Optional'da veri var mı?
                .hasValueSatisfying(p -> assertThat(p).isEqualTo(payment)); // Optional nesnesindeki payment'a uyuyor mu?
    }
}