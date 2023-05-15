package guru.springframework.spring6restmvc.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.spring6restmvc.model.Beer;
import guru.springframework.spring6restmvc.services.BeerService;
import guru.springframework.spring6restmvc.services.BeerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get; // ovo sam morao ručno dodati, nije pokupio automatski mockMvc.perform(get(...));

import java.util.UUID;


import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @SpringBootTest
@WebMvcTest(BeerController.class)   //ovo sam promijenio 8.5.2023, umjesto @SpringBootTest ubacio @WebMvcTest ,
                                    // želimo ogrančiti testove samo na BeerController klasu.

class BeerControllerTest {

    // @Autowired
    // BeerController beerController;


    // Spring MockMVC allows you to test the controller interactions in a servlet context without the application running in a application server.

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    BeerService beerService;  //@MockBean provide-a automatski sve potrebne dependcies, inače bi došlo do exceptiona-a

    BeerServiceImpl beerServiceImpl; //  = new BeerServiceImpl();ovo mičem jer sam dolje stavio @BeforeEach pa da ne radi 2 puta novi objekt

    @BeforeEach
    void setUp() {
        beerServiceImpl = new BeerServiceImpl();
    }

    @Test
    void testCreateNewBeer() throws Exception {

        /*

        Obzirom da smo gore stavili

        @Autowired
        ObjectMapper objectMapper;

        onda ne trebamo više "ručno" instancirati novu objectMapper instancu. POdsjećam @Autowired sam pripremi sve da bi se objekt inicijalizirao unutar SpringBoot contexta.

        ObjectMapper objectMapper = new ObjectMapper();  //testiramo dodavanje novog Beer objekta pomoću Jackson-a, JSON -> Java POJO and vice versa

        objectMapper.findAndRegisterModules(); //bez ovoga bi dobili exception, Jackson ne bi znao hendlati LocalDatetime datatype

        */

        Beer    beer = beerServiceImpl.listBeers().get(0);
        beer.setVersion(null);
        beer.setId(null);

        given(beerService.saveNewBeer(any(Beer.class))).willReturn(beerServiceImpl.listBeers().get(1)); //za svaki Beer ovjekt koji se proslijedi metoda


        mockMvc.perform(post("/api/v1/beer")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(beer)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));

        System.out.println(objectMapper.writeValueAsString(beer));

    }

    @Test
    void testListBeers() throws Exception {
        given(beerService.listBeers()).willReturn(beerServiceImpl.listBeers());

        mockMvc.perform(get("/api/v1/beer")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()",is(3)));      // obzirom da su svi UUID-ovi generirani na pokretanju, stavili smo uvjet da su u listi 3 piva, š
                                                                              // to znamo da jesu na inicijalizaciji programa


    }
    @Test
    void getBeerById() throws Exception {

        Beer testBeer = beerServiceImpl.listBeers().get(0);

        // given(beerService.getBeerById(any(UUID.class))).willReturn(testBeer); //reći ćemo Mockito-u da za bilo koji UUID vrati naš odabrani test beer sa indeksom 0 iz liste
        given(beerService.getBeerById(testBeer.getId())).willReturn(testBeer);

        mockMvc.perform(MockMvcRequestBuilders
                // .get("/api/v1/beer/" +UUID.randomUUID())
                .get("/api/v1/beer/" + testBeer.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))           //da postrožim malo uvjete testa, mora vratiti nešto što nije empty response
                .andExpect(jsonPath("$.id",is(testBeer.getId().toString())))
                .andExpect(jsonPath("$.beerName", is(testBeer.getBeerName())));

        // System.out.println(beerController.getBeerById(UUID.randomUUID()));
    }
}