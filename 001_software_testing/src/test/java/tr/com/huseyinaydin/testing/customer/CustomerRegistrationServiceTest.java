package tr.com.huseyinaydin.testing.customer;

import tr.com.huseyinaydin.testing.utils.PhoneNumberValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

class CustomerRegistrationServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerRegistrationService underTest;
	
	@Captor
    private ArgumentCaptor<Customer> customerArgumentCaptor;
    /*
        ArgumentCaptor, bir Mockito testi sırasında, bir metodun çağrıldığı argümanı yakalamak ve bu argüman 
		üzerinde doğrulama yapmak için kullanılan bir araçtır, böylece testlerde daha ayrıntılı kontrol ve analiz sağlar.
    */

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        underTest = new CustomerRegistrationService(customerRepository);
    }
	
	@Test
    void itShouldSaveNewCustomer() {
        // Given - bizim beklediğimiz değer veya referans.
        String phoneNumber = "000099";
        Customer customer = new Customer(UUID.randomUUID(), "Huseyin", phoneNumber);

        // ... müşteri nesnesini tutan kaydolma isteği.
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);

        // ... DB'de '000099' tel nolu müşteri var mı? Beklenen referans Optional boş.
        given(customerRepository.selectCustomerByPhoneNumber(phoneNumber))
                .willReturn(Optional.empty());

        //... Beklenen değer true mu? Telefon numarası doğrulama tel no doğru formatta mı?
        given(phoneNumberValidator.test(phoneNumber)).willReturn(true);

        // Müşteri kaydı.
        underTest.registerNewCustomer(request);

        // Then
        then(customerRepository).should().save(customerArgumentCaptor.capture());
        /*
         Daha önce yapılan bir çağrıda kullanılan nesneyi yakalar.
		 Bu, test sırasında save metoduna hangi nesnenin geçtiğini öğrenmek
		 ve bu nesne üzerinde test yapmak için kullanılır.
		 Böylece çağrılan nesnenin durumu ya da içeriği üzerinde daha ayrıntılı doğrulamalar yapabilirim.
         */

        //Müşteri argümanı yakalanan değeri.
        Customer customerArgumentCaptorValue = customerArgumentCaptor.getValue();
        assertThat(customerArgumentCaptorValue).isEqualTo(customer); //Yakalanan referans beklenen'e eşit mi?
    }
}