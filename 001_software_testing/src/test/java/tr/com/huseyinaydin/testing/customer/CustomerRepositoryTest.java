package tr.com.huseyinaydin.testing.customer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class CustomerRepositoryTest {

    @Autowired
    private CustomerRepository underTest;
	
	@Test
    void itNotShouldSelectCustomerByPhoneNumberWhenNumberDoesNotExists() {
        // Given - bizim beklediğimiz değer veya referans.
        String phoneNumber = "0000";

        // When - test etmeden önce yapılan işlem.
        Optional<Customer> optionalCustomer = underTest.selectCustomerByPhoneNumber(phoneNumber);

        // Then - //Telefon numarasına göre veritabanında müşteri var mı?
        assertThat(optionalCustomer).isNotPresent();
    }

    @Test
    void itShouldSelectCustomerByPhoneNumber() {
        // Given - bizim beklediğimiz değer veya referans.
        UUID id = UUID.randomUUID();
        String phoneNumber = "0000";
        Customer customer = new Customer(id, "Huseyin", phoneNumber);

        // When - test etmeden önce yapılan işlem.
        underTest.save(customer);

        // Then - yapılan işlemin beklediğimiz gibi olup olmadığının test edildiği kısım.
        Optional<Customer> optionalCustomer = underTest.selectCustomerByPhoneNumber(phoneNumber);
        assertThat(optionalCustomer)
                .isPresent()
                .hasValueSatisfying(c -> {
                    assertThat(c).isEqualToComparingFieldByField(customer);
                });
    }
}