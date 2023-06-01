package guru.springframework.spring6restmvc.controller;

import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.mappers.BeerMapper;
import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.repositories.BeerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest             // ovo je integracijski test, pa želimo full Spring context
class BeerControllerIT {

    @Autowired
    BeerController beerController;

    @Autowired
    BeerRepository beerRepository;

    @Autowired
    BeerMapper beerMapper;

    @Test
    void testDeleteByIDNotFound () {
        assertThrows(NotFoundException.class, () -> {
            beerController.deleteByID(UUID.randomUUID());  //daj neki random UUID i prazan BeerDTO objekt za NotFound test
        });
    }

    @Rollback
    @Transactional
    @Test
    void deleteByIdFound() {

        Beer beer = beerRepository.findAll().get(0);

        ResponseEntity responseEntity = beerController.deleteByID(beer.getId());

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));


        assertThat(beerRepository.findById(beer.getId()).isEmpty());

        // ovo nam više ne treba

        // Beer foundBeer = beerRepository.findById(beer.getId()).get();

        // assertThat(foundBeer).isNull();

    }

    @Test
    void testUpdateNotFound () {
        assertThrows(NotFoundException.class, () -> {
            beerController.updateById(UUID.randomUUID(), BeerDTO.builder().build());  //daj neki random UUID i prazan BeerDTO objekt za NotFound test
        });
    }

    @Rollback
    @Transactional
    @Test
    void updateExistingBeer() {
        Beer beer = beerRepository.findAll().get(0);
        BeerDTO beerDTO = beerMapper.beerToBeerDTO(beer);
        beerDTO.setId(null);
        beerDTO.setVersion(null);
        final String beerName = "UPDATED";
        beerDTO.setBeerName(beerName);

        ResponseEntity responseEntity = beerController.updateById(beer.getId(), beerDTO);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(204));

        Beer updatedBeer = beerRepository.findById(beer.getId()).get();
        assertThat(updatedBeer.getBeerName()).isEqualTo(beerName);


    }

    @Rollback           //modificiramo stanje baze, pa da nam ne fejlalju drugi testovi koji možda provjeravaju count()
    @Transactional
    @Test
    void saveNewBeerTest() {
        BeerDTO beerDTO = BeerDTO.builder()
                .beerName("New beer")
                .build();

        ResponseEntity responseEntity = beerController.handlePost(beerDTO);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.valueOf(201));
        assertThat(responseEntity.getHeaders().getLocation()).isNotNull();

        String[] locationUUID = responseEntity.getHeaders().getLocation().getPath().split("/");
        UUID savedUUID = UUID.fromString(locationUUID[4]);

        Beer beer = beerRepository.findById(savedUUID).get();

        assertThat(beer).isNotNull();

    }

    @Test
    void testBeerIdNotFound() {


        //pošto znamo da će getBeerById sa random UUID-om baciti exception, ulovit ćemo ga tako da test bude uspješan
        assertThrows(NotFoundException.class, ()-> {beerController.getBeerById(UUID.randomUUID());});

    }

    @Test
    void testGetById() {
        Beer beer=beerRepository.findAll().get(0);

        BeerDTO dto = beerController.getBeerById(beer.getId());

        assertThat(dto).isNotNull();
    }

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