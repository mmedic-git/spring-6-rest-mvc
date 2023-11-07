package guru.springframework.spring6restmvc.repositories;

import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.entities.BeerOrder;
import guru.springframework.spring6restmvc.entities.BeerOrderShipment;
import guru.springframework.spring6restmvc.entities.Customer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/* za razliku od @DataJpaTest anotacija @SpringBootTest će doista i pokrenuti BootstrapDataTest klasu i zato će dobiti korektan broj customera, 3 a ne 0.
   Odnosno učitati će se SpringData "full context", odnosno izvršit će se BootstrapDataTest klasa */

@SpringBootTest
class BeerOrderRepositoryTest {

    @Autowired
    BeerOrderRepository beerOrderRepository;


    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    BeerRepository beerRepository;


    Customer testCustomer;
    Beer testBeer;

    @BeforeEach
    void setUp() {

        testCustomer = customerRepository.findAll().get(0);
        testBeer = beerRepository.findAll().get(0);

    }

    @Transactional
    @Test
    void testBeerOrders () {
        System.out.println(beerOrderRepository.count());
        System.out.println(customerRepository.count());
        System.out.println(beerRepository.count());
        System.out.println(testCustomer.getName());
        System.out.println(testBeer.getBeerName());

        BeerOrder beerOrder = BeerOrder.builder()

                .customerRef("Test order")
                .customer(testCustomer)
                .beerOrderShipment(BeerOrderShipment.builder()
                        .trackingNumber("1235r")
                        .build())
                .build();

        BeerOrder savedBeerOrder = beerOrderRepository.saveAndFlush(beerOrder);

        System.out.println(savedBeerOrder.getCustomerRef());
    }




}