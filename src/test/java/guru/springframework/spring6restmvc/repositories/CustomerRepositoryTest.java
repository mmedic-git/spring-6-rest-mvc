package guru.springframework.spring6restmvc.repositories;

import guru.springframework.spring6restmvc.entities.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest    // ovo će naloadati minimalni set DataJPA komponenti,iz nekog razloga kod mene ne radi, probao sam milijun kombinacija
                // ali ja konstantno dobijam ovu grešku:
                // Unable to make field private final long java.util.UUID.leastSigBits accessible: module java.base does not "opens java.util" to unnamed module @3c0ecd4b
class CustomerRepositoryTest {
    
    @Autowired
    CustomerRepository customerRepository;

    @Test
    void testSavedCustomer() {

        System.out.println("Hibernate version is: "  + org.hibernate.Version.getVersionString());

        Customer customer = customerRepository.save(Customer.builder().name("New Name").build());

            assertThat(customer.getId()).isNotNull();

    }
}