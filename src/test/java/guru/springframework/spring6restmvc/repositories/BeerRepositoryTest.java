package guru.springframework.spring6restmvc.repositories;

import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.model.BeerStyle;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
class BeerRepositoryTest {

    @Autowired
    BeerRepository beerRepository;

    @Test
    void testSaveBeerNameTooLong() {


        //ovako smo napisali uspješan test (koji je uspješno fejlao i bacio ConstraintViolationException iznimku

        assertThrows(ConstraintViolationException.class, () -> {

            Beer savedBeer = beerRepository.save(Beer.builder()
                    .id(UUID.randomUUID())
                    .beerName("My Beer 0123456789 0123456789 0123456789 0123456789 0123456789")
                    .beerStyle(BeerStyle.PALE_ALE)      //stavili smo @NotNull i @NotBlank data validaciju na Style, upc i price pa ih moramo setirati
                    .upc("2323232323")
                    .price(new BigDecimal("11.99"))
                    .build());


            beerRepository.flush();

        });




/*
        assertThat(savedBeer).isNotNull();
        assertThat(savedBeer.getId()).isNotNull();

         */
    }

    @Test
    void testSaveBeer() {


        Beer savedBeer = beerRepository.save(Beer.builder()
                        .id(UUID.randomUUID())
                .beerName("My Beer")
                        .beerStyle(BeerStyle.PALE_ALE)      //stavili smo @NotNull i @NotBlank data validaciju na Style, upc i price pa ih moramo setirati
                        .upc("2323232323")
                        .price(new BigDecimal("11.99"))
                .build());


        // moramo dadati flush naredbu, da bi test "uspješno fajlao" nakon dodavanja data validacija na Beer entitet
        // Validacija kaže da osim beerName obavezni su još i upc, price, style...
        // ova naredba kaže Hibernate-u da sve zapiše u database

        beerRepository.flush();


/*
        assertThat(savedBeer).isNotNull();
        assertThat(savedBeer.getId()).isNotNull();

         */
    }
}