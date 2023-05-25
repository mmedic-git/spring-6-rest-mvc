package guru.springframework.spring6restmvc.controller;

import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.repositories.BeerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest             // ovo je integracijski test, pa želimo full Spring context
class BeerControllerIT {

    @Autowired
    BeerController beerController;

    @Autowired
    BeerRepository beerRepository;

    @Test
    void testListBeers() {
        List<BeerDTO>   dtos = beerController.listBeers();

        assertThat(dtos.size()).isEqualTo(3);
        
    }

    @Transactional
    @Rollback                   // rollbackaj deleteAll() metodu, tako da pobrišemo recorde samo za test
    @Test
    void testEmptyList() {
        beerRepository.deleteAll();

        List<BeerDTO>   dtos = beerController.listBeers();

        assertThat(dtos.size()).isEqualTo(0);
    }
}