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

    @Mock
    private PhoneNumberValidator phoneNumberValidator;

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
        underTest = new CustomerRegistrationService(customerRepository, phoneNumberValidator);
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
	
	@Test
    void itShouldNotSaveNewCustomerWhenPhoneNumberIsInvalid() {
        // Given - belirlenen telefon numarası.
        String phoneNumber = "000099";
        Customer customer = new Customer(UUID.randomUUID(), "Huseyin", phoneNumber);

        // ... Müşteri nesnesini sarmalayan istek nesnesi.
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);


        //... Telefon numarasının geçerli olup olamayacağı. Beklenen değer false.
        given(phoneNumberValidator.test(phoneNumber)).willReturn(false);

        // When - Hata fırlatması bekleniyor çünkü geçersiz bir telefon numarası girildi.
        assertThatThrownBy(() -> underTest.registerNewCustomer(request))
                .isInstanceOf(IllegalStateException.class) //beklenen istisna IllegalStateException'dır.
                .hasMessageContaining("Phone Number " + phoneNumber + " is not valid"); //mesaj bu --<< ifadeyi içeriyor mu?

        // Then
        then(customerRepository).shouldHaveNoInteractions();
		/*
		Mockito ile yazılan bir testte customerRepository nesnesi ile herhangi bir 
		etkileşim (metod çağrısı, argüman geçişi vb.) olmadığını doğrulamak için kullanılır. 
		Bu, belirli bir kod parçasının customerRepository üzerinde hiç bir 
		işlem gerçekleştirmediğini kontrol etmek için yararlıdır, böylece beklenmeyen 
		veya gereksiz etkileşimlerin olup olmadığını tespit edebilirim.
		*/
    }
	
	@Test
    void itShouldThrowWhenPhoneNumberIsTaken() {
        // Given - Beklenen telefon numarası.
        String phoneNumber = "000099";
        Customer customer = new Customer(UUID.randomUUID(), "Huseyin", phoneNumber);
        Customer customerTwo = new Customer(UUID.randomUUID(), "Ceyda", phoneNumber);

        // ... müşteri nesnesini sarmalayan istek nesnesi.
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);

        // ... Telefon numarasına göre beklenen her ikinci müşteri kaydı dönüyor mu?
        given(customerRepository.selectCustomerByPhoneNumber(phoneNumber))
                .willReturn(Optional.of(customerTwo));

        //... Telefon numarasının geçerliliği testi. Beklenen true.
        given(phoneNumberValidator.test(phoneNumber)).willReturn(true);

        // When
        // Then
        assertThatThrownBy(() -> underTest.registerNewCustomer(request))
                .isInstanceOf(IllegalStateException.class) //Geçersiz durum istisnası fırlatılıyor mu?
                .hasMessageContaining(String.format("Telefon numarası [%s] geçersiz.", phoneNumber)); //Hata mesajı beklenen hatayı içeriyor mu?

        // Finally
        then(customerRepository).should(never()).save(any(Customer.class));
		/*
		Bu kod, customerRepository nesnesinin save metodunun hiçbir zaman çağrılmadığını doğrular.
		Eğer save metodu, herhangi bir Customer parametresi ile çağrıldıysa, bu test başarısız olur.
		
		any(Customer.class) ifadesi, customerRepository nesnesinin save metodunun
		Customer türünde bir parametre alıp almadığını kontrol eder.
		*/
    }
	
	@Test
    void itShouldSaveNewCustomerWhenIdIsNull() {
        // Given - telefon numarası.
        String phoneNumber = "000099";
        Customer customer = new Customer(null, "Huseyin", phoneNumber);

        // ... müşteri nesnesini sarmalayan istek nesnesi.
        CustomerRegistrationRequest request = new CustomerRegistrationRequest(customer);

        // ... Böyle bir telefon numarası ile veritabanında kayıt var mı? Beklenen Optional boş.
        given(customerRepository.selectCustomerByPhoneNumber(phoneNumber))
                .willReturn(Optional.empty());

        //... Telefon numarası doğru formatta mı? Beklenen değer true.
        given(phoneNumberValidator.test(phoneNumber)).willReturn(true);

        // When - müşteri kaydediliyor.
        underTest.registerNewCustomer(request);

        // Then - asıl test kısmı.
        then(customerRepository).should().save(customerArgumentCaptor.capture()); //capture ile customerRepository save metoduna girilen değer ile save metodu çağrılmış mı? Çağrılmışsa testi geçer.
        Customer customerArgumentCaptorValue = customerArgumentCaptor.getValue(); //capture ile save metoduna girilen değer yakalanıyor.
        assertThat(customerArgumentCaptorValue)
                .isEqualToIgnoringGivenFields(customer, "id"); //isEqualToIgnoringGivenFields(customer, "id"): Bu ifade, iki nesnenin diğer tüm alanlarının eşit olup olmadığını kontrol ederken, yalnızca id alanını göz ardı eder.
        assertThat(customerArgumentCaptorValue.getId()).isNotNull(); //yakalanan customer nesnesi'nin Id'si null mı? Null'sa testi geçer.
    }
}