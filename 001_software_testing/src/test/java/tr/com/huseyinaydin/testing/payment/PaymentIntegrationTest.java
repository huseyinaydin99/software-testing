package tr.com.huseyinaydin.testing.payment;

import tr.com.huseyinaydin.testing.customer.Customer;
import tr.com.huseyinaydin.testing.customer.CustomerRegistrationRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class PaymentIntegrationTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void itShouldCreatePaymentSuccessfully() throws Exception {
        // Given - hazırlanan müşteri nesnesi.
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer(customerId, "Huseyin", "+90554554454");

        CustomerRegistrationRequest customerRegistrationRequest = new CustomerRegistrationRequest(customer);

        // Register işlemi / kaydolma.
        ResultActions customerRegResultActions = mockMvc.perform(put("/api/v1/customer-registration")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectToJson(customerRegistrationRequest))));

        // ... ödeme nesnesi.
        long paymentId = 1L;

        Payment payment = new Payment(
                paymentId,
                customerId,
                new BigDecimal("321.12"),
                Currency.TL,
                "x1x2x3x4",
                "Zekat/Fitre/Sadaka/Başımızın gözümüzün üstüne."
        );

        // ... ödeme nesnesini sarmalayan ödeme isteği nesnesi.
        PaymentRequest paymentRequest = new PaymentRequest(payment);

        // ... ödeme işlemini kaydet. [POST]
        ResultActions paymentResultActions = mockMvc.perform(post("/api/v1/payment")
                .contentType(MediaType.APPLICATION_JSON)
                .content(Objects.requireNonNull(objectToJson(paymentRequest))));

        // müşteri kaydolması ve ödeme kaydı durum kodu beklendisi 200 ok'dir. hal böyleyken test geçerli olur.
        customerRegResultActions.andExpect(status().isOk());
        paymentResultActions.andExpect(status().isOk());

        // DB'deki ödeme kaydı bizim beklediğimiz kayda eşit mi?
        // TODO: Payment repository kullanılmamalı bunun yerine yeni endpoint oluşturulmalı.
        assertThat(paymentRepository.findById(paymentId))
                .isPresent()
                .hasValueSatisfying(p -> assertThat(p).isEqualToComparingFieldByField(payment));

        // TODO: SMS işlemleri yapılabilir. Hani ödeme için yapılan onay SMS'i gibi.
    }

    private String objectToJson(Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            fail("Obje'den JSON çevrilirken hata oldu.");
            return null;
        }
    }
}